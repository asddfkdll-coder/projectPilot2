package com.projectpilot.app.data.repository

import com.projectpilot.app.data.ai.AiAnalysisService
import com.projectpilot.app.data.local.AiAnalysisDao
import com.projectpilot.app.data.local.AverageScores
import com.projectpilot.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for AI analysis results - part of the Offline Knowledge System.
 * Provides a clean API for storing, retrieving, and searching analysis history.
 */
@Singleton
class AiAnalysisRepository @Inject constructor(
    private val aiAnalysisDao: AiAnalysisDao,
    private val aiAnalysisService: AiAnalysisService
) {
    // ---- Local Storage (Offline Knowledge) ----
    
    fun observeAllAnalyses(): Flow<List<AiAnalysisResult>> = aiAnalysisDao.observeAll()
    
    fun observeProjectAnalyses(projectId: Long): Flow<List<AiAnalysisResult>> = 
        aiAnalysisDao.observeByProject(projectId)
    
    fun searchAnalyses(query: String): Flow<List<AiAnalysisResult>> = 
        aiAnalysisDao.search(query)
    
    suspend fun getLatestAnalysis(projectId: Long): AiAnalysisResult? = 
        aiAnalysisDao.getLatestForProject(projectId)
    
    suspend fun getAnalysisById(id: Long): AiAnalysisResult? = 
        aiAnalysisDao.getById(id)
    
    suspend fun saveAnalysis(result: AiAnalysisResult): Long = 
        aiAnalysisDao.insert(result)
    
    suspend fun deleteAnalysis(result: AiAnalysisResult) = 
        aiAnalysisDao.delete(result)
    
    suspend fun deleteAnalysisById(id: Long) = 
        aiAnalysisDao.deleteById(id)
    
    suspend fun deleteProjectAnalyses(projectId: Long) = 
        aiAnalysisDao.deleteByProject(projectId)
    
    suspend fun deleteOlderThan(timestamp: Long) = 
        aiAnalysisDao.deleteOlderThan(timestamp)
    
    suspend fun deleteAllAnalyses() = 
        aiAnalysisDao.deleteAll()
    
    suspend fun getAnalysisCount(): Int = 
        aiAnalysisDao.getCount()
    
    suspend fun getAnalysisCountForProject(projectId: Long): Int = 
        aiAnalysisDao.getCountForProject(projectId)
    
    suspend fun getAverageScores(): AverageScores = 
        aiAnalysisDao.getAverageScores()
    
    // ---- AI Service Integration ----
    
    /**
     * Runs a new AI analysis and stores the result locally.
     */
    suspend fun analyzeAndSave(
        project: Project,
        analysisType: AnalysisType = AnalysisType.FULL,
        fileTreeSummary: String? = null,
        dependencyContent: String? = null
    ): AiAnalysisService.AnalysisResult {
        val result = aiAnalysisService.analyzeProject(project, analysisType, fileTreeSummary, dependencyContent)
        
        if (result is AiAnalysisService.AnalysisResult.Success) {
            // Calculate version number
            val existingCount = aiAnalysisDao.getCountForProject(project.id)
            val toSave = result.analysis.copy(version = existingCount + 1)
            aiAnalysisDao.insert(toSave)
            return AiAnalysisService.AnalysisResult.Success(toSave)
        }
        
        return result
    }
    
    /**
     * Tests connection to the configured AI provider.
     */
    suspend fun testConnection(config: AiProviderConfig, apiKey: String): 
        AiAnalysisService.ConnectionTestResult = 
        aiAnalysisService.testConnection(config, apiKey)
    
    // ---- Project Comparison ----
    
    /**
     * Compares two projects based on their latest analyses.
     */
    suspend fun compareProjects(project1Id: Long, project2Id: Long): ProjectComparisonResult? {
        val analysis1 = aiAnalysisDao.getLatestForProject(project1Id) ?: return null
        val analysis2 = aiAnalysisDao.getLatestForProject(project2Id) ?: return null
        
        return ProjectComparisonResult(
            baseProjectId = project1Id,
            compareProjectId = project2Id,
            baseProjectName = analysis1.projectName,
            compareProjectName = analysis2.projectName,
            securityScoreDiff = (analysis2.securityScore ?: 0) - (analysis1.securityScore ?: 0),
            qualityScoreDiff = (analysis2.codeQualityScore ?: 0) - (analysis1.codeQualityScore ?: 0),
            performanceScoreDiff = (analysis2.performanceScore ?: 0) - (analysis1.performanceScore ?: 0),
            overallAssessment = generateOverallComparison(analysis1, analysis2)
        )
    }
    
    private fun generateOverallComparison(a1: AiAnalysisResult, a2: AiAnalysisResult): String {
        val parts = mutableListOf<String>()
        
        val s1 = a1.securityScore ?: 0
        val s2 = a2.securityScore ?: 0
        if (s2 > s1) parts.add("${a2.projectName} has better security (+${s2 - s1})")
        else if (s1 > s2) parts.add("${a1.projectName} has better security (+${s1 - s2})")
        
        val q1 = a1.codeQualityScore ?: 0
        val q2 = a2.codeQualityScore ?: 0
        if (q2 > q1) parts.add("${a2.projectName} has better code quality (+${q2 - q1})")
        else if (q1 > q2) parts.add("${a1.projectName} has better code quality (+${q1 - q2})")
        
        return if (parts.isEmpty()) "Both projects are comparable." else parts.joinToString(". ") + "."
    }
}
