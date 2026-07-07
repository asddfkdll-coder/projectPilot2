package com.projectpilot.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.projectpilot.app.data.ai.AiProviderConfig
import com.projectpilot.app.data.ai.AiProviderRegistry
import com.projectpilot.app.data.security.SecureApiKeyStore
import com.projectpilot.app.domain.model.AiProviderType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.aiSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ai_settings"
)

/**
 * Single source of truth for AI provider configuration. Secrets live
 * in [SecureApiKeyStore]; everything else (enabled state, selected
 * model, base URL, default provider) lives in Preferences DataStore
 * as non-sensitive JSON. New code that needs provider config or that
 * will call an AI provider should depend on this repository only —
 * never read the two backing stores directly.
 */
@Singleton
class AiSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secureApiKeyStore: SecureApiKeyStore
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val configsKey = stringPreferencesKey("provider_configs_json")
    private val defaultProviderKey = stringPreferencesKey("default_provider")

    val providerConfigs: Flow<Map<AiProviderType, AiProviderConfig>> =
        context.aiSettingsDataStore.data.map { prefs -> decodeConfigs(prefs[configsKey]) }

    val defaultProvider: Flow<AiProviderType?> =
        context.aiSettingsDataStore.data.map { prefs ->
            prefs[defaultProviderKey]?.let { name ->
                runCatching { AiProviderType.valueOf(name) }.getOrNull()
            }
        }

    suspend fun upsertConfig(config: AiProviderConfig) {
        context.aiSettingsDataStore.edit { prefs ->
            val updated = decodeConfigs(prefs[configsKey]) + (config.type to config)
            prefs[configsKey] = json.encodeToString(updated.mapKeys { it.key.name })
        }
    }

    suspend fun setEnabled(provider: AiProviderType, enabled: Boolean) {
        context.aiSettingsDataStore.edit { prefs ->
            val current = decodeConfigs(prefs[configsKey])
            val existing = current[provider] ?: defaultConfigFor(provider)
            val updated = current + (provider to existing.copy(enabled = enabled))
            prefs[configsKey] = json.encodeToString(updated.mapKeys { it.key.name })
        }
    }

    suspend fun setDefaultProvider(provider: AiProviderType) {
        context.aiSettingsDataStore.edit { prefs ->
            prefs[defaultProviderKey] = provider.name
        }
    }

    suspend fun setApiKey(provider: AiProviderType, apiKey: String) {
        secureApiKeyStore.saveKey(provider, apiKey)
    }

    suspend fun clearApiKey(provider: AiProviderType) {
        secureApiKeyStore.deleteKey(provider)
    }

    fun hasApiKey(provider: AiProviderType): Boolean =
        secureApiKeyStore.hasKey(provider)

    fun maskedKeyPreview(provider: AiProviderType): String? =
        secureApiKeyStore.maskedPreview(provider)

    private fun decodeConfigs(raw: String?): Map<AiProviderType, AiProviderConfig> {
        if (raw == null) return defaultConfigs()
        return runCatching {
            json.decodeFromString<Map<String, AiProviderConfig>>(raw)
                .mapKeys { AiProviderType.valueOf(it.key) }
        }.getOrElse { defaultConfigs() }
    }

    private fun defaultConfigs(): Map<AiProviderType, AiProviderConfig> =
        AiProviderType.values().associateWith { defaultConfigFor(it) }

    private fun defaultConfigFor(type: AiProviderType): AiProviderConfig {
        val spec = AiProviderRegistry.specFor(type)
        return AiProviderConfig(
            type = type,
            enabled = false,
            baseUrl = spec.defaultBaseUrl,
            selectedModel = spec.defaultModels.firstOrNull().orEmpty()
        )
    }
}
