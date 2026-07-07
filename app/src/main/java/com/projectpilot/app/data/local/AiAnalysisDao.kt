package com.projectpilot.app.data.local

import androidx.room.*
import com.projectpilot.app.domain.model.AiAnalysisResult
import com.projectpilot.app.domain.model.AnalysisType
import kotlinx.coroutines.flow.Flow

/**
 * DAO for AI analysis results - supports the Offline Knowledge System.
 * Enables searching history, versioning, and fast loading of previous analyses.
 */
@Dao
interface AiAnalysisDao {
    
    @Query("SELECT * FROM ai_analysis_results ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<AiAnalysisResult>>
    
    @Query("SELECT * FROM ai_analysis_results WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun observeByProject(projectId: Long): Flow<List<AiAnalysisResult>>
    
    @Query("SELECT * FROM ai_analysis_results WHERE projectId = :projectId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestForProject(projectId: Long): AiAnalysisResult?
    
    @Query("SELECT * FROM ai_analysis_results WHERE id = :id")
    suspend fun getById(id: Long): AiAnalysisResult?
    
    @Query("SELECT * FROM ai_analysis_results WHERE analysisType = :type ORDER BY createdAt DESC")
    fun observeByType(type: AnalysisType): Flow<List<AiAnalysisResult>>
    
    @Query("""
        SELECT * FROM ai_analysis_results 
        WHERE projectName LIKE '%' || :query || '%' 
        OR projectPath LIKE '%' || :query || '%'
        OR architectureSummary LIKE '%' || :query || '%'
        OR detectedTechnologies LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun search(query: String): Flow<List<AiAnalysisResult>>
    
    @Query("""
        SELECT * FROM ai_analysis_results 
        WHERE projectName LIKE '%' || :query || '%' 
        OR projectPath LIKE '%' || :query || '%'
        OR architectureSummary LIKE '%' || :query || '%'
        OR detectedTechnologies LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    suspend fun searchSync(query: String): List<AiAnalysisResult>
    
    @Query("SELECT COUNT(*) FROM ai_analysis_results")
    suspend fun getCount(): Int
    
    @Query("SELECT COUNT(*) FROM ai_analysis_results WHERE projectId = :projectId")
    suspend fun getCountForProject(projectId: Long): Int
    
    @Query("SELECT * FROM ai_analysis_results WHERE projectId = :projectId ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getPageForProject(projectId: Long, limit: Int, offset: Int): List<AiAnalysisResult>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: AiAnalysisResult): Long
    
    @Delete
    suspend fun delete(result: AiAnalysisResult)
    
    @Query("DELETE FROM ai_analysis_results WHERE projectId = :projectId")
    suspend fun deleteByProject(projectId: Long)
    
    @Query("DELETE FROM ai_analysis_results WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM ai_analysis_results WHERE createdAt < :olderThan")
    suspend fun deleteOlderThan(olderThan: Long)
    
    @Query("DELETE FROM ai_analysis_results")
    suspend fun deleteAll()
    
    // Get analysis history with pagination
    @Query("SELECT * FROM ai_analysis_results ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getPage(limit: Int, offset: Int): List<AiAnalysisResult>
    
    // Get version count for a project
    @Query("SELECT COUNT(*) FROM ai_analysis_results WHERE projectId = :projectId")
    suspend fun getVersionCount(projectId: Long): Int
    
    // Get average scores across all analyses
    @Query("""
        SELECT 
            AVG(securityScore) as avgSecurity,
            AVG(performanceScore) as avgPerformance,
            AVG(codeQualityScore) as avgQuality,
            AVG(documentationScore) as avgDocumentation
        FROM ai_analysis_results
    """)
    suspend fun getAverageScores(): AverageScores
    
    // Get analysis count by type
    @Query("SELECT analysisType, COUNT(*) as count FROM ai_analysis_results GROUP BY analysisType")
    suspend fun getAnalysisTypeDistribution(): List<AnalysisTypeCount>
}

data class AverageScores(
    val avgSecurity: Float?,
    val avgPerformance: Float?,
    val avgQuality: Float?,
    val avgDocumentation: Float?
)

data class AnalysisTypeCount(
    val analysisType: AnalysisType,
    val count: Int
)
