package com.projectpilot.app.data.local

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.projectpilot.app.data.repository.AiAnalysisRepository
import com.projectpilot.app.data.repository.AiSettingsRepository
import com.projectpilot.app.data.repository.AppSettingsRepository
import com.projectpilot.app.data.repository.ProjectRepository
import com.projectpilot.app.data.security.SecureApiKeyStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles export and import of application settings.
 * Security: API keys are NEVER exported - only provider configurations (without keys).
 */
@Singleton
class SettingsExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appSettingsRepository: AppSettingsRepository,
    private val aiSettingsRepository: AiSettingsRepository,
    private val secureApiKeyStore: SecureApiKeyStore,
    private val projectRepository: ProjectRepository,
    private val aiAnalysisRepository: AiAnalysisRepository
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    
    data class ExportResult(val success: Boolean, val message: String, val fileUri: Uri? = null)
    data class ImportResult(val success: Boolean, val message: String, val importedProviders: Int = 0)
    
    /**
     * Exports all non-sensitive settings to a JSON file.
     * API keys are NEVER included - only provider configs (URLs, models, enabled state).
     */
    suspend fun exportSettings(outputUri: Uri): ExportResult = withContext(Dispatchers.IO) {
        try {
            val exportData = buildExportData()
            val exportJson = json.encodeToString(exportData)
            
            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(exportJson)
                }
            } ?: return@withContext ExportResult(false, "Failed to open output stream")
            
            ExportResult(true, "Settings exported successfully", outputUri)
        } catch (e: Exception) {
            ExportResult(false, "Export failed: ${e.message}")
        }
    }
    
    /**
     * Imports settings from a JSON file.
     * Does NOT import API keys - those must be re-entered manually.
     */
    suspend fun importSettings(inputUri: Uri): ImportResult = withContext(Dispatchers.IO) {
        try {
            val content = context.contentResolver.openInputStream(inputUri)?.use { 
                it.bufferedReader().readText() 
            } ?: return@withContext ImportResult(false, "Failed to read import file")
            
            val exportData = runCatching { json.decodeFromString<ExportedSettings>(content) }.getOrElse {
                return@withContext ImportResult(false, "Invalid export file format: ${it.message}")
            }
            
            var importedCount = 0
            
            // Import AI provider configs (without API keys)
            exportData.aiProviders.forEach { providerExport ->
                aiSettingsRepository.upsertConfig(providerExport.config)
                importedCount++
            }
            
            ImportResult(true, "Imported $importedCount provider configurations. API keys must be re-entered manually.", importedCount)
        } catch (e: Exception) {
            ImportResult(false, "Import failed: ${e.message}")
        }
    }
    
    /**
     * Exports AI analysis results for a project.
     */
    suspend fun exportAnalysisResults(projectId: Long, outputUri: Uri): ExportResult = withContext(Dispatchers.IO) {
        try {
            val analyses = aiAnalysisRepository.observeProjectAnalyses(projectId).first()
            val exportData = AnalysisExport(
                projectId = projectId,
                exportedAt = System.currentTimeMillis(),
                analyses = analyses
            )
            
            val exportJson = json.encodeToString(exportData)
            
            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(exportJson)
                }
            } ?: return@withContext ExportResult(false, "Failed to open output stream")
            
            ExportResult(true, "${analyses.size} analysis results exported", outputUri)
        } catch (e: Exception) {
            ExportResult(false, "Export failed: ${e.message}")
        }
    }
    
    /**
     * Generates a default export filename with timestamp.
     */
    fun generateExportFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "projectpilot_settings_$timestamp.json"
    }
    
    private suspend fun buildExportData(): ExportedSettings {
        val providerConfigs = aiSettingsRepository.providerConfigs.first()
        
        val providerExports = providerConfigs.map { (type, config) ->
            AiProviderExport(
                type = type.name,
                displayName = type.displayName,
                config = config,
                hasApiKey = secureApiKeyStore.hasKey(type),
                apiKeyMasked = secureApiKeyStore.maskedPreview(type) ?: "Not set"
            )
        }
        
        return ExportedSettings(
            version = 1,
            exportedAt = System.currentTimeMillis(),
            aiProviders = providerExports
        )
    }
    
    @kotlinx.serialization.Serializable
    data class ExportedSettings(
        val version: Int,
        val exportedAt: Long,
        val aiProviders: List<AiProviderExport>
    )
    
    @kotlinx.serialization.Serializable
    data class AiProviderExport(
        val type: String,
        val displayName: String,
        val config: com.projectpilot.app.data.ai.AiProviderConfig,
        val hasApiKey: Boolean,
        val apiKeyMasked: String // e.g., "sk-ab••••••••••••1a2b"
    )
    
    @kotlinx.serialization.Serializable
    data class AnalysisExport(
        val projectId: Long,
        val exportedAt: Long,
        val analyses: List<AiAnalysisResult>
    )
}
