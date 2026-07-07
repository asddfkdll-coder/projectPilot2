package com.projectpilot.app.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.projectpilot.app.domain.model.AiProviderType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores AI provider API keys with AES-256-GCM via Android Keystore —
 * the same encryption approach already used for `.env` secrets
 * elsewhere in the app (see EncryptionManager).
 *
 * Integration note: add "pp_ai_secure_prefs" to the same backup
 * exclusion list that already protects "pp_secure_prefs" in this
 * project's backup rules XML, so keys are never included in Auto
 * Backup or cloud restore.
 */
@Singleton
class SecureApiKeyStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveKey(provider: AiProviderType, apiKey: String) {
        require(apiKey.isNotBlank()) { "API key cannot be blank" }
        prefs.edit().putString(keyFor(provider), apiKey).apply()
    }

    fun getKey(provider: AiProviderType): String? =
        prefs.getString(keyFor(provider), null)

    fun hasKey(provider: AiProviderType): Boolean =
        !getKey(provider).isNullOrBlank()

    fun deleteKey(provider: AiProviderType) {
        prefs.edit().remove(keyFor(provider)).apply()
    }

    /**
     * Masked preview for display only, e.g. "sk-a•••••••••••1a2b".
     * The full key is never returned to the UI layer or logged.
     */
    fun maskedPreview(provider: AiProviderType): String? {
        val key = getKey(provider) ?: return null
        if (key.length <= 8) return "•".repeat(8)
        return "${key.take(4)}${"•".repeat(8)}${key.takeLast(4)}"
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    private fun keyFor(provider: AiProviderType) = "api_key_${provider.name}"

    private companion object {
        const val PREFS_FILE_NAME = "pp_ai_secure_prefs"
    }
}
