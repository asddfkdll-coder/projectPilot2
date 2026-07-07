package com.projectpilot.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnhancedSettingsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    fun setAutoSaveAnalysis(value: Boolean) {
        _uiState.value = _uiState.value.copy(autoSaveAnalysis = value)
    }

    fun setRetentionDays(days: Int) {
        _uiState.value = _uiState.value.copy(retentionDays = days)
    }

    fun setAnalysisTimeout(seconds: Int) {
        _uiState.value = _uiState.value.copy(analysisTimeout = seconds)
    }

    fun setTheme(theme: String) {
        _uiState.value = _uiState.value.copy(theme = theme)
    }

    fun setLanguage(language: String) {
        _uiState.value = _uiState.value.copy(language = language)
    }

    fun setMonitoringInterval(minutes: Int) {
        _uiState.value = _uiState.value.copy(monitoringInterval = minutes)
    }

    fun setConfirmDelete(value: Boolean) {
        _uiState.value = _uiState.value.copy(confirmDelete = value)
    }

    fun setGitTracking(value: Boolean) {
        _uiState.value = _uiState.value.copy(gitTracking = value)
    }

    fun setCrashReporting(value: Boolean) {
        _uiState.value = _uiState.value.copy(crashReporting = value)
    }

    fun clearOldAnalyses() {
        viewModelScope.launch {
            // Clear old analyses
        }
    }

    fun clearAllCache() {
        viewModelScope.launch {
            // Clear all cache
        }
    }

    fun exportSettings() {
        // Export settings
    }

    fun importSettings() {
        // Import settings
    }

    fun resetAllSettings() {
        _uiState.value = SettingsUiState()
    }
}

data class SettingsUiState(
    val autoSaveAnalysis: Boolean = true,
    val retentionDays: Int = 30,
    val analysisTimeout: Int = 60,
    val theme: String = "System",
    val language: String = "System",
    val monitoringInterval: Int = 5,
    val confirmDelete: Boolean = true,
    val gitTracking: Boolean = true,
    val crashReporting: Boolean = true
)
