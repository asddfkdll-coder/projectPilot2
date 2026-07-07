package com.projectpilot.app.ui.screens.knowledge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectpilot.app.data.repository.AiAnalysisRepository
import com.projectpilot.app.domain.model.AiAnalysisResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class KnowledgeFilter { ALL, RECENT, SECURITY_ISSUES, LOW_QUALITY }

data class OfflineKnowledgeUiState(
    val analyses: List<AiAnalysisResult> = emptyList(),
    val totalAnalyses: Int = 0,
    val totalProjects: Int = 0,
    val avgSecurityScore: Float = 0f,
    val avgQualityScore: Float = 0f,
    val filter: KnowledgeFilter = KnowledgeFilter.ALL,
    val isLoading: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class OfflineKnowledgeViewModel @Inject constructor(
    private val aiAnalysisRepository: AiAnalysisRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OfflineKnowledgeUiState())
    val state: StateFlow<OfflineKnowledgeUiState> = _state.asStateFlow()

    private var allAnalyses: List<AiAnalysisResult> = emptyList()

    fun loadAllKnowledge() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                allAnalyses = aiAnalysisRepository.observeAllAnalyses().first()
                applyFilter(_state.value.filter)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    message = "Failed to load knowledge: ${e.message}"
                )
            }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val results = if (query.isBlank()) {
                    allAnalyses
                } else {
                    aiAnalysisRepository.searchAnalyses(query).first()
                }
                _state.value = _state.value.copy(
                    analyses = results,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun setFilter(filter: KnowledgeFilter) {
        applyFilter(filter)
    }

    private fun applyFilter(filter: KnowledgeFilter) {
        val filtered = when (filter) {
            KnowledgeFilter.ALL -> allAnalyses
            KnowledgeFilter.RECENT -> allAnalyses.filter {
                it.createdAt > System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            }
            KnowledgeFilter.SECURITY_ISSUES -> allAnalyses.filter {
                (it.securityScore ?: 100) < 70 || it.criticalVulnerabilities > 0
            }
            KnowledgeFilter.LOW_QUALITY -> allAnalyses.filter {
                (it.codeQualityScore ?: 100) < 60
            }
        }

        val uniqueProjects = allAnalyses.map { it.projectId }.distinct().size
        val avgSecurity = allAnalyses.mapNotNull { it.securityScore }.average().toFloat()
        val avgQuality = allAnalyses.mapNotNull { it.codeQualityScore }.average().toFloat()

        _state.value = _state.value.copy(
            analyses = filtered,
            totalAnalyses = allAnalyses.size,
            totalProjects = uniqueProjects,
            avgSecurityScore = avgSecurity,
            avgQualityScore = avgQuality,
            filter = filter,
            isLoading = false
        )
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
