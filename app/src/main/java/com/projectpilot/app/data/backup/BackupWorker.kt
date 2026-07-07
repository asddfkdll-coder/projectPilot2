package com.projectpilot.app.data.backup

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.projectpilot.app.data.repository.ProjectRepository
import com.projectpilot.app.security.EncryptionManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey

/**
 * Improved periodic backup worker with encryption.
 * 
 * Exports the project list + each project's encrypted .env blob into the app's 
 * external files dir under /backups, with the entire backup file encrypted.
 *
 * Improvements:
 * - Entire backup file is encrypted on disk
 * - Option to exclude sensitive data
 * - Better error handling
 * - Secure key management
 */
@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val repo: ProjectRepository,
    private val crypto: EncryptionManager
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result = try {
        val projects = repo.observeAll().first()
        val dir = File(applicationContext.getExternalFilesDir(null), "backups").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(dir, "projectpilot_backup_$stamp.json.enc")
        val json = Json { prettyPrint = true; encodeDefaults = true }
        
        // Project is a Room entity — serialize manually to avoid leaking @PrimaryKey internals.
        val snapshot = projects.map {
            BackupRecord(
                name = it.name, 
                path = it.path, 
                type = it.type.name,
                framework = it.framework, 
                installCommand = it.installCommand,
                runCommand = it.runCommand, 
                defaultPort = it.defaultPort,
                notes = it.notes, 
                customCommands = it.customCommands,
                // Option to exclude sensitive data: check if preference is set
                envBlob = if (shouldIncludeSensitiveData()) crypto.getEnv(it.id) else null
            )
        }
        
        val backupContent = json.encodeToString(BackupFile(stamp, snapshot))
        
        // Encrypt the entire backup file
        val encryptedContent = encryptBackupContent(backupContent)
        file.writeBytes(encryptedContent)
        
        // Rotate: keep at most 10 backups
        dir.listFiles()?.sortedByDescending { it.lastModified() }?.drop(10)
            ?.forEach { it.delete() }
        
        Result.success()
    } catch (t: Throwable) {
        Result.retry()
    }

    /**
     * Encrypts backup content using AES-256-GCM via Android Keystore.
     * Uses EncryptedFile from AndroidX Security for robust key management,
     * avoiding direct keystore access and manual cipher configuration.
     */
    private fun encryptBackupContent(content: String): ByteArray {
        try {
            // Build MasterKey for consistent key management
            val masterKey = MasterKey.Builder(applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            // Use EncryptedFile for secure encryption with proper key handling
            val tempFile = File.createTempFile("backup_tmp", ".bin", applicationContext.cacheDir)
            val encryptedFile = EncryptedFile.Builder(
                applicationContext,
                tempFile,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            // Write content securely via EncryptedFile
            encryptedFile.openFileOutput().use { output ->
                output.write(content.toByteArray(Charsets.UTF_8))
            }

            // Read the encrypted result and clean up
            val encryptedBytes = tempFile.readBytes()
            tempFile.delete()
            return encryptedBytes
        } catch (e: Exception) {
            throw RuntimeException("Failed to encrypt backup: ${e.message}", e)
        }
    }

    /**
     * Checks if sensitive data should be included in backups.
     * This can be configured through SharedPreferences or WorkManager inputData.
     */
    private fun shouldIncludeSensitiveData(): Boolean {
        // Check if there's a preference to exclude sensitive data
        // Default: include (for backward compatibility)
        return inputData.getBoolean("include_sensitive_data", true)
    }

    @kotlinx.serialization.Serializable
    data class BackupRecord(
        val name: String, 
        val path: String, 
        val type: String,
        val framework: String?, 
        val installCommand: String?,
        val runCommand: String?, 
        val defaultPort: Int?,
        val notes: String, 
        val customCommands: String,
        val envBlob: String? = null
    )
    
    @kotlinx.serialization.Serializable
    data class BackupFile(val createdAt: String, val projects: List<BackupRecord>)

    companion object {
        const val UNIQUE_NAME = "pp_periodic_backup"

        fun schedule(
            ctx: Context, 
            intervalHours: Long = 24,
            includeSensitiveData: Boolean = true
        ) {
            val inputData = Data.Builder()
                .putBoolean("include_sensitive_data", includeSensitiveData)
                .build()
            
            val req = PeriodicWorkRequestBuilder<BackupWorker>(intervalHours, TimeUnit.HOURS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .setInputData(inputData)
                .build()
            
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                UNIQUE_NAME, ExistingPeriodicWorkPolicy.UPDATE, req
            )
        }

        fun cancel(ctx: Context) {
            WorkManager.getInstance(ctx).cancelUniqueWork(UNIQUE_NAME)
        }
    }
}
