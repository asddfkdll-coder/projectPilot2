package com.projectpilot.app.data.repository

import com.projectpilot.app.data.ai.AiAnalysisService
import com.projectpilot.app.data.local.AiAnalysisDao
import com.projectpilot.app.domain.model.AiAnalysisResult
import kotlinx.coroutines.flow.Flow

class AiAnalysisRepository(
    private val dao: AiAnalysisDao,
    private val aiService: AiAnalysisService
) {
    fun getAllAnalyses(): Flow<List<AiAnalysisResult>> = dao.getAllAnalyses()

    fun getAnalysesForProject(projectId: Long): Flow<List<AiAnalysisResult>> =
        dao.getAnalysesForProject(projectId)

    fun searchAnalyses(query: String): Flow<List<AiAnalysisResult>> =
        dao.searchAnalyses("%$query%")

    suspend fun getAnalysisById(id: Long): AiAnalysisResult? = dao.getAnalysisById(id)

    suspend fun saveAnalysis(result: AiAnalysisResult): Long = dao.insert(result)

    suspend fun deleteAnalysis(result: AiAnalysisResult) = dao.delete(result)

    suspend fun deleteOldAnalyses(retentionDays: Int) =
        dao.deleteOldAnalyses(System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000))

    suspend fun analyzeProject(
        projectPath: String,
        projectName: String,
        analysisType: String,
        fileTree: String,
        buildFiles: String
    ): Result<AiAnalysisResult> {
        return aiService.analyzeProject(projectPath, projectName, analysisType, fileTree, buildFiles)
    }

    suspend fun testConnection(): Result<String> = aiService.testConnection()
}
