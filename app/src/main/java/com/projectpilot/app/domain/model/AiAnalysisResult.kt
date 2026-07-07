package com.projectpilot.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_analysis_results")
data class AiAnalysisResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long = 0,
    val projectName: String = "",
    val projectPath: String = "",
    val analysisType: String = "QUICK_SCAN",
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val architectureSummary: String = "",
    val detectedPatterns: String = "",
    val layerStructure: String = "",
    val totalFiles: Int = 0,
    val totalDirectories: Int = 0,
    val linesOfCode: Int = 0,
    val languageBreakdown: String = "",
    val dependencies: String = "",
    val outdatedDependencies: String = "",
    val vulnerableDependencies: String = "",
    val detectedTechnologies: String = "",
    val detectedFrameworks: String = "",
    val buildInstructions: String = "",
    val runInstructions: String = "",
    val securityScore: Int = 0,
    val securityIssues: String = "",
    val performanceScore: Int = 0,
    val performanceBottlenecks: String = "",
    val codeQualityScore: Int = 0,
    val codeSmells: String = "",
    val complexityIssues: String = "",
    val duplicationIssues: String = "",
    val recommendations: String = "",
    val documentationScore: Int = 0,
    val documentationIssues: String = "",
    val rawResponse: String = ""
)
