package com.projectpilot.app.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectpilot.app.data.ai.AiAnalysisService
import com.projectpilot.app.data.repository.AiAnalysisRepository
import com.projectpilot.app.data.repository.AiSettingsRepository
import com.projectpilot.app.data.repository.ProjectRepository
import com.projectpilot.app.data.security.SecureApiKeyStore
import com.projectpilot.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class AiAnalysisUiState(
    val analyses: List<AiAnalysisResult> = emptyList(),
    val selectedAnalysis: AiAnalysisResult? = null,
    val isLoading: Boolean = false,
    val isAnalyzing: Boolean = false,
    val analysisProgress: String = "",
    val availableProviders: List<String> = emptyList(),
    val hasConfiguredProvider: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class AiAnalysisViewModel @Inject constructor(
    private val aiAnalysisRepository: AiAnalysisRepository,
    private val projectRepository: ProjectRepository,
    private val aiSettingsRepository: AiSettingsRepository,
    private val secureApiKeyStore: SecureApiKeyStore
) : ViewModel() {

    private val _state = MutableStateFlow(AiAnalysisUiState())
    val state: StateFlow<AiAnalysisUiState> = _state.asStateFlow()

    init {
        checkProviders()
    }

    private fun checkProviders() {
        viewModelScope.launch {
            val configs = aiSettingsRepository.providerConfigs.first()
            val enabledProviders = configs.filter { it.value.enabled }.keys.map { it.displayName }
            val hasKey = configs.filter { it.value.enabled }.keys.any { secureApiKeyStore.hasKey(it) }
            _state.value = _state.value.copy(
                availableProviders = enabledProviders,
                hasConfiguredProvider = hasKey
            )
        }
    }

    fun loadProjectAnalyses(projectId: Long) {
        viewModelScope.launch {
            aiAnalysisRepository.observeProjectAnalyses(projectId)
                .collect { analyses ->
                    _state.value = _state.value.copy(analyses = analyses)
                }
        }
    }

    fun loadAllAnalyses() {
        viewModelScope.launch {
            aiAnalysisRepository.observeAllAnalyses()
                .collect { analyses ->
                    _state.value = _state.value.copy(analyses = analyses)
                }
        }
    }

    fun selectAnalysis(analysisId: Long) {
        viewModelScope.launch {
            val analysis = aiAnalysisRepository.getAnalysisById(analysisId)
            _state.value = _state.value.copy(selectedAnalysis = analysis)
        }
    }

    fun runAnalysis(projectId: Long, analysisType: AnalysisType) {
        viewModelScope.launch {
            val project = projectRepository.getById(projectId) ?: run {
                _state.value = _state.value.copy(error = "Project not found")
                return@launch
            }

            _state.value = _state.value.copy(isAnalyzing = true, analysisProgress = "Starting analysis...")

            // Gather project data
            _state.value = _state.value.copy(analysisProgress = "Scanning project files...")
            val fileTreeSummary = generateFileTreeSummary(project.path)
            
            _state.value = _state.value.copy(analysisProgress = "Reading dependencies...")
            val dependencyContent = readDependencyFiles(project.path)

            _state.value = _state.value.copy(analysisProgress = "Sending to AI provider...")
            
            val result = aiAnalysisRepository.analyzeAndSave(
                project = project,
                analysisType = analysisType,
                fileTreeSummary = fileTreeSummary,
                dependencyContent = dependencyContent
            )

            when (result) {
                is AiAnalysisService.AnalysisResult.Success -> {
                    _state.value = _state.value.copy(
                        isAnalyzing = false,
                        selectedAnalysis = result.analysis,
                        message = "Analysis complete! ${result.analysis.modelUsed} analyzed in ${result.analysis.generationTimeMs}ms"
                    )
                }
                is AiAnalysisService.AnalysisResult.Error -> {
                    _state.value = _state.value.copy(
                        isAnalyzing = false,
                        error = result.message
                    )
                }
                AiAnalysisService.AnalysisResult.NoProviderConfigured -> {
                    _state.value = _state.value.copy(
                        isAnalyzing = false,
                        error = "No AI provider configured. Go to Settings > AI Providers to configure one."
                    )
                }
                AiAnalysisService.AnalysisResult.NoApiKey -> {
                    _state.value = _state.value.copy(
                        isAnalyzing = false,
                        error = "No API key set for the default provider. Go to Settings > AI Providers to add your key."
                    )
                }
            }
        }
    }

    fun deleteAnalysis(analysisId: Long) {
        viewModelScope.launch {
            aiAnalysisRepository.deleteAnalysisById(analysisId)
            _state.value = _state.value.copy(
                selectedAnalysis = if (_state.value.selectedAnalysis?.id == analysisId) null else _state.value.selectedAnalysis,
                message = "Analysis deleted"
            )
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(message = null, error = null)
    }

    private fun generateFileTreeSummary(path: String): String {
        return try {
            val dir = File(path)
            if (!dir.exists() || !dir.isDirectory) return ""
            
            val sb = StringBuilder()
            val maxFiles = 50
            var count = 0
            
            dir.walkTopDown()
                .maxDepth(3)
                .filter { it.isFile }
                .take(maxFiles)
                .forEach { file ->
                    val relativePath = file.relativeTo(dir).path
                    val extension = file.extension.lowercase()
                    sb.appendLine("$relativePath ($extension)")
                    count++
                }
            
            if (count >= maxFiles) sb.appendLine("... (truncated)")
            sb.toString()
        } catch (e: Exception) {
            ""
        }
    }

    private fun readDependencyFiles(path: String): String {
        return try {
            val dir = File(path)
            if (!dir.exists() || !dir.isDirectory) return ""
            
            val sb = StringBuilder()
            
            // Try various dependency files
            val dependencyFiles = listOf(
                "package.json", "requirements.txt", "pyproject.toml",
                "Cargo.toml", "go.mod", "composer.json", 
                "pom.xml", "build.gradle.kts", "build.gradle",
                "Gemfile", "Dockerfile", "docker-compose.yml"
            )
            
            dependencyFiles.forEach { filename ->
                val file = File(dir, filename)
                if (file.exists() && file.length() < 50000) {
                    sb.appendLine("--- $filename ---")
                    sb.appendLine(file.readText().take(5000))
                    sb.appendLine()
                }
            }
            
            sb.toString()
        } catch (e: Exception) {
            ""
        }
    }
}
