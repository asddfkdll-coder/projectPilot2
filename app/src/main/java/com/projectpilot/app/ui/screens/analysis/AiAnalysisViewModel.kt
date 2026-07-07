package com.projectpilot.app.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectpilot.app.data.repository.AiAnalysisRepository
import com.projectpilot.app.domain.model.AiAnalysisResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiAnalysisViewModel @Inject constructor(
    private val repository: AiAnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiAnalysisUiState())
    val uiState: StateFlow<AiAnalysisUiState> = _uiState

    fun loadProject(projectId: Long) {
        viewModelScope.launch {
            // Load project details if needed
        }
    }

    fun startAnalysis(
        projectId: Long,
        projectName: String,
        analysisType: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val fileTree = "src/\\n  main/\\n    java/\\n    res/\\nbuild.gradle"
            val buildFiles = "plugins { id 'com.android.application' }"

            val result = repository.analyzeProject(
                projectPath = "/path/to/$projectName",
                projectName = projectName,
                analysisType = analysisType,
                fileTree = fileTree,
                buildFiles = buildFiles
            )

            result.fold(
                onSuccess = { analysis ->
                    repository.saveAnalysis(analysis)
                    _uiState.update { it.copy(isLoading = false, result = analysis) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }
}

data class AiAnalysisUiState(
    val isLoading: Boolean = false,
    val result: AiAnalysisResult? = null,
    val error: String? = null
)
