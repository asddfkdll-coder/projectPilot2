package com.projectpilot.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectpilot.app.data.local.AverageScores
import com.projectpilot.app.data.repository.AiAnalysisRepository
import com.projectpilot.app.data.repository.ProjectRepository
import com.projectpilot.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardProjectStats(
    val project: Project,
    val latestAnalysis: AiAnalysisResult? = null,
    val serverStatus: String = "stopped",
    val uptimeMinutes: Long = 0
)

data class ProjectDashboardUiState(
    val projects: List<DashboardProjectStats> = emptyList(),
    val totalProjects: Int = 0,
    val activeProjects: Int = 0,
    val totalAnalyses: Int = 0,
    val averageScores: AverageScores = AverageScores(null, null, null, null),
    val securityDistribution: List<ScoreCategory> = emptyList(),
    val qualityDistribution: List<ScoreCategory> = emptyList(),
    val technologyBreakdown: Map<String, Int> = emptyMap(),
    val recentAlerts: List<ProjectAlert> = emptyList(),
    val isLoading: Boolean = true,
    val selectedProjectId: Long? = null,
    val message: String? = null
)

data class ScoreCategory(
    val name: String,
    val count: Int,
    val color: Long // ARGB color
)

@HiltViewModel
class ProjectDashboardViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val aiAnalysisRepository: AiAnalysisRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectDashboardUiState())
    val state: StateFlow<ProjectDashboardUiState> = _state.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            projectRepository.observeAll().collect { projects ->
                val projectStats = projects.map { project ->
                    val latestAnalysis = aiAnalysisRepository.getLatestAnalysis(project.id)
                    val uptime = if (project.lastRunAt != null) {
                        (System.currentTimeMillis() - project.lastRunAt) / 1000 / 60
                    } else 0
                    
                    DashboardProjectStats(
                        project = project,
                        latestAnalysis = latestAnalysis,
                        serverStatus = if (project.lastPid != null) "running" else "stopped",
                        uptimeMinutes = uptime
                    )
                }
                
                val totalAnalyses = aiAnalysisRepository.getAnalysisCount()
                val avgScores = aiAnalysisRepository.getAverageScores()
                
                val securityDist = calculateScoreDistribution(projectStats.mapNotNull { it.latestAnalysis?.securityScore })
                val qualityDist = calculateScoreDistribution(projectStats.mapNotNull { it.latestAnalysis?.codeQualityScore })
                
                val techBreakdown = mutableMapOf<String, Int>()
                projectStats.forEach { stat ->
                    stat.latestAnalysis?.getDetectedTechnologiesList()?.forEach { tech ->
                        techBreakdown[tech] = (techBreakdown[tech] ?: 0) + 1
                    } ?: run {
                        val typeName = stat.project.type.name
                        techBreakdown[typeName] = (techBreakdown[typeName] ?: 0) + 1
                    }
                }
                
                _state.value = _state.value.copy(
                    projects = projectStats,
                    totalProjects = projects.size,
                    activeProjects = projects.count { it.lastPid != null },
                    totalAnalyses = totalAnalyses,
                    averageScores = avgScores,
                    securityDistribution = securityDist,
                    qualityDistribution = qualityDist,
                    technologyBreakdown = techBreakdown,
                    isLoading = false
                )
            }
        }
    }

    fun selectProject(projectId: Long) {
        _state.value = _state.value.copy(selectedProjectId = projectId)
    }

    fun refresh() {
        loadDashboard()
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    private fun calculateScoreDistribution(scores: List<Int>): List<ScoreCategory> {
        if (scores.isEmpty()) return emptyList()
        
        val excellent = scores.count { it >= 90 }
        val good = scores.count { it in 75..89 }
        val fair = scores.count { it in 60..74 }
        val poor = scores.count { it in 40..59 }
        val critical = scores.count { it < 40 }
        
        return listOf(
            ScoreCategory("Excellent (90+)", excellent, 0xFF4CAF50),
            ScoreCategory("Good (75-89)", good, 0xFF8BC34A),
            ScoreCategory("Fair (60-74)", fair, 0xFFFFC107),
            ScoreCategory("Poor (40-59)", poor, 0xFFFF9800),
            ScoreCategory("Critical (<40)", critical, 0xFFF44336)
        ).filter { it.count > 0 }
    }
}
