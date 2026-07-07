package com.projectpilot.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectpilot.app.data.ai.AiProviderConfig
import com.projectpilot.app.data.ai.AiProviderRegistry
import com.projectpilot.app.data.repository.AiSettingsRepository
import com.projectpilot.app.domain.model.AiProviderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProviderUiState(
    val type: AiProviderType,
    val displayName: String,
    val enabled: Boolean,
    val baseUrl: String,
    val availableModels: List<String>,
    val selectedModel: String,
    val hasApiKey: Boolean,
    val maskedKeyPreview: String?,
    val isDefault: Boolean,
    val requiresCustomBaseUrl: Boolean
)

data class AiSettingsUiState(
    val providers: List<ProviderUiState> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AiProviderSettingsViewModel @Inject constructor(
    private val repository: AiSettingsRepository
) : ViewModel() {

    val uiState: StateFlow<AiSettingsUiState> = combine(
        repository.providerConfigs,
        repository.defaultProvider
    ) { configs, defaultProvider ->
        val providers = AiProviderType.values().map { type ->
            val spec = AiProviderRegistry.specFor(type)
            val config = configs[type]
            ProviderUiState(
                type = type,
                displayName = spec.type.displayName,
                enabled = config?.enabled ?: false,
                baseUrl = config?.baseUrl ?: spec.defaultBaseUrl,
                availableModels = spec.defaultModels,
                selectedModel = config?.selectedModel ?: spec.defaultModels.firstOrNull().orEmpty(),
                hasApiKey = repository.hasApiKey(type),
                maskedKeyPreview = repository.maskedKeyPreview(type),
                isDefault = defaultProvider == type,
                requiresCustomBaseUrl = spec.requiresCustomBaseUrl
            )
        }
        AiSettingsUiState(providers = providers, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AiSettingsUiState()
    )

    fun setEnabled(type: AiProviderType, enabled: Boolean) {
        viewModelScope.launch { repository.setEnabled(type, enabled) }
    }

    fun saveApiKey(type: AiProviderType, apiKey: String) {
        viewModelScope.launch { repository.setApiKey(type, apiKey) }
    }

    fun clearApiKey(type: AiProviderType) {
        viewModelScope.launch { repository.clearApiKey(type) }
    }

    fun setDefaultProvider(type: AiProviderType) {
        viewModelScope.launch { repository.setDefaultProvider(type) }
    }

    fun updateModel(type: AiProviderType, model: String, currentBaseUrl: String) {
        viewModelScope.launch {
            val current = uiState.value.providers.firstOrNull { it.type == type }
            repository.upsertConfig(
                AiProviderConfig(
                    type = type,
                    enabled = current?.enabled ?: false,
                    baseUrl = currentBaseUrl,
                    selectedModel = model
                )
            )
        }
    }

    fun updateBaseUrl(type: AiProviderType, baseUrl: String, currentModel: String) {
        viewModelScope.launch {
            val current = uiState.value.providers.firstOrNull { it.type == type }
            repository.upsertConfig(
                AiProviderConfig(
                    type = type,
                    enabled = current?.enabled ?: false,
                    baseUrl = baseUrl,
                    selectedModel = currentModel
                )
            )
        }
    }
}
