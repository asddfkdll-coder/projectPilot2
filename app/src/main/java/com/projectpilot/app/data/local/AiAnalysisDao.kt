package com.projectpilot.app.data.local

import androidx.room.*
import com.projectpilot.app.domain.model.AiAnalysisResult
import kotlinx.coroutines.flow.Flow

@Dao
interface AiAnalysisDao {
    @Query("SELECT * FROM ai_analysis_results ORDER BY createdAt DESC")
    fun getAllAnalyses(): Flow<List<AiAnalysisResult>>

    @Query("SELECT * FROM ai_analysis_results WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getAnalysesForProject(projectId: Long): Flow<List<AiAnalysisResult>>

    @Query("SELECT * FROM ai_analysis_results WHERE projectName LIKE :query OR projectPath LIKE :query OR architectureSummary LIKE :query ORDER BY createdAt DESC")
    fun searchAnalyses(query: String): Flow<List<AiAnalysisResult>>

    @Query("SELECT * FROM ai_analysis_results WHERE id = :id")
    suspend fun getAnalysisById(id: Long): AiAnalysisResult?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: AiAnalysisResult): Long

    @Delete
    suspend fun delete(result: AiAnalysisResult)

    @Query("DELETE FROM ai_analysis_results WHERE createdAt < :timestamp")
    suspend fun deleteOldAnalyses(timestamp: Long)

    @Query("SELECT COUNT(*) FROM ai_analysis_results")
    suspend fun getAnalysisCount(): Int
}
