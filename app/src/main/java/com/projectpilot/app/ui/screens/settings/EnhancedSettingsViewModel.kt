package com.projectpilot.app.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectpilot.app.data.local.SettingsExporter
import com.projectpilot.app.data.repository.AiAnalysisRepository
import com.projectpilot.app.data.repository.AppSettingsRepository
import com.projectpilot.app.domain.model.AnalysisType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EnhancedSettingsUiState(
    // Theme
    val theme: String = "SYSTEM",
    val language: String = "SYSTEM",
    
    // Analysis
    val analysisAutoSave: Boolean = true,
    val analysisRetentionDays: Int = 90,
    val defaultAnalysisType: String = "FULL",
    val analysisTimeoutSeconds: Int = 60,
    val showAiSuggestions: Boolean = true,
    val enableOfflineMode: Boolean = false,
    
    // Privacy
    val enableAnalytics: Boolean = false,
    val enableCrashReporting: Boolean = true,
    val confirmDelete: Boolean = true,
    
    // General
    val enableNotifications: Boolean = true,
    val monitoringIntervalMs: Long = 5000,
    val maxRecentProjects: Int = 10,
    val enableGitTracking: Boolean = true,
    
    // Export/Import
    val exportIncludeAiAnalysis: Boolean = true,
    val exportIncludeEnv: Boolean = false,
    
    // Cache
    val analysisCount: Int = 0,
    val isLoading: Boolean = true,
    val message: String? = null
)

@HiltViewModel
class EnhancedSettingsViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val aiAnalysisRepository: AiAnalysisRepository,
    private val settingsExporter: SettingsExporter
) : ViewModel() {

    private val _state = MutableStateFlow(EnhancedSettingsUiState())
    val state: StateFlow<EnhancedSettingsUiState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                appSettingsRepository.theme,
                appSettingsRepository.language,
                appSettingsRepository.analysisAutoSave,
                appSettingsRepository.analysisRetentionDays,
                appSettingsRepository.defaultAnalysisType,
                appSettingsRepository.analysisTimeoutSeconds,
                appSettingsRepository.showAiSuggestions,
                appSettingsRepository.enableOfflineMode,
                appSettingsRepository.enableAnalytics,
                appSettingsRepository.enableCrashReporting,
                appSettingsRepository.confirmDelete,
                appSettingsRepository.enableNotifications,
                appSettingsRepository.monitoringIntervalMs,
                appSettingsRepository.maxRecentProjects,
                appSettingsRepository.enableGitTracking,
                appSettingsRepository.exportIncludeAiAnalysis,
                appSettingsRepository.exportIncludeEnv,
                flow { emit(aiAnalysisRepository.getAnalysisCount()) }
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                EnhancedSettingsUiState(
                    theme = (values[0] as com.projectpilot.app.data.repository.AppTheme).name,
                    language = (values[1] as com.projectpilot.app.data.repository.AppLanguage).name,
                    analysisAutoSave = values[2] as Boolean,
                    analysisRetentionDays = values[3] as Int,
                    defaultAnalysisType = values[4] as String,
                    analysisTimeoutSeconds = values[5] as Int,
                    showAiSuggestions = values[6] as Boolean,
                    enableOfflineMode = values[7] as Boolean,
                    enableAnalytics = values[8] as Boolean,
                    enableCrashReporting = values[9] as Boolean,
                    confirmDelete = values[10] as Boolean,
                    enableNotifications = values[11] as Boolean,
                    monitoringIntervalMs = values[12] as Long,
                    maxRecentProjects = values[13] as Int,
                    enableGitTracking = values[14] as Boolean,
                    exportIncludeAiAnalysis = values[15] as Boolean,
                    exportIncludeEnv = values[16] as Boolean,
                    analysisCount = values[17] as Int,
                    isLoading = false
                )
            }.collect { _state.value = it }
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            runCatching { 
                appSettingsRepository.setTheme(com.projectpilot.app.data.repository.AppTheme.valueOf(theme)) 
            }
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            runCatching { 
                appSettingsRepository.setLanguage(com.projectpilot.app.data.repository.AppLanguage.valueOf(language)) 
            }
        }
    }

    fun setAnalysisAutoSave(enabled: Boolean) {
        viewModelScope.launch { appSettingsRepository.setAnalysisAutoSave(enabled) }
    }

    fun setAnalysisRetentionDays(days: Int) {
        viewModelScope.launch { appSettingsRepository.setAnalysisRetentionDays(days) }
    }

    fun setDefaultAnalysisType(type: String) {
        viewModelScope.launch { appSettingsRepository.setDefaultAnalysisType(type) }
    }

    fun setAnalysisTimeoutSeconds(seconds: Int) {
        viewModelScope.launch { appSettingsRepository.setAnalysisTimeoutSeconds(seconds) }
    }

    fun setShowAiSuggestions(enabled: Boolean) {
        viewModelScope.launch { appSettingsRepository.setShowAiSuggestions(enabled) }
    }

    fun setEnableOfflineMode(enabled: Boolean) {
        viewModelScope.launch { appSettingsRepository.setEnableOfflineMode(enabled) }
    }

    fun setEnableAnalytics(enabled: Boolean) {
        viewModelScope.launch { appSettingsRepository.setEnableAnalytics(enabled) }
    }

    fun setEnableCrashReporting(enabled: Boolean) {
        viewModelScope.launch { appSettingsRepository.setEnableCrashReporting(enabled) }
    }

    fun setConfirmDelete(enabled: Boolean) {
        viewModelScope.launch { appSettingsRepository.setConfirmDelete(enabled) }
    }

    fun setEnableNotifications(enabled: Boolean) {
        viewModelScope.launch { appSettingsRepository.setEnableNotifications(enabled) }
    }

    fun setMonitoringInterval(intervalMs: Long) {
        viewModelScope.launch { appSettingsRepository.setMonitoringIntervalMs(intervalMs) }
    }

    fun setMaxRecentProjects(max: Int) {
        viewModelScope.launch { appSettingsRepository.setMaxRecentProjects(max) }
    }

    fun setEnableGitTracking(enabled: Boolean) {
        viewModelScope.launch { appSettingsRepository.setEnableGitTracking(enabled) }
    }

    fun setExportIncludeAiAnalysis(include: Boolean) {
        viewModelScope.launch { appSettingsRepository.setExportIncludeAiAnalysis(include) }
    }

    fun setExportIncludeEnv(include: Boolean) {
        viewModelScope.launch { appSettingsRepository.setExportIncludeEnv(include) }
    }

    // ---- Cache Management ----

    fun clearAnalysisCache() {
        viewModelScope.launch {
            aiAnalysisRepository.deleteAllAnalyses()
            _state.value = _state.value.copy(
                analysisCount = 0,
                message = "All analysis results cleared"
            )
        }
    }

    fun clearOldAnalyses() {
        viewModelScope.launch {
            val retentionDays = state.value.analysisRetentionDays
            val cutoffTime = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L)
            aiAnalysisRepository.deleteOlderThan(cutoffTime)
            val remaining = aiAnalysisRepository.getAnalysisCount()
            _state.value = _state.value.copy(
                analysisCount = remaining,
                message = "Analyses older than $retentionDays days removed"
            )
        }
    }

    // ---- Import/Export ----

    fun exportSettings(uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = settingsExporter.exportSettings(uri)
            _state.value = _state.value.copy(
                isLoading = false,
                message = if (result.success) "Settings exported successfully" else "Export failed: ${result.message}"
            )
        }
    }

    fun importSettings(uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = settingsExporter.importSettings(uri)
            _state.value = _state.value.copy(
                isLoading = false,
                message = if (result.success) "Settings imported: ${result.message}" else "Import failed: ${result.message}"
            )
        }
    }

    fun resetAllSettings() {
        viewModelScope.launch {
            appSettingsRepository.resetToDefaults()
            _state.value = _state.value.copy(message = "All settings reset to defaults")
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
