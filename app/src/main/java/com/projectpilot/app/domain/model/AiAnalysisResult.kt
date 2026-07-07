package com.projectpilot.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Comprehensive AI analysis result for a project.
 * Stored locally for offline access and history tracking.
 */
@Serializable
@Entity(tableName = "ai_analysis_results")
data class AiAnalysisResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val projectName: String,
    val projectPath: String,
    val analysisType: AnalysisType,
    val createdAt: Long = System.currentTimeMillis(),
    val version: Int = 1,
    
    // Architecture Analysis
    val architectureSummary: String? = null,
    val detectedPatterns: String = "[]", // JSON list
    val layerStructure: String? = null,
    
    // File Structure
    val totalFiles: Int = 0,
    val totalDirectories: Int = 0,
    val totalLinesOfCode: Int = 0,
    val languageBreakdown: String = "{}", // JSON map
    val fileTreeSummary: String? = null,
    
    // Dependencies
    val dependencies: String = "[]", // JSON list
    val outdatedDependencies: String = "[]", // JSON list
    val dependencyVulnerabilities: String = "[]", // JSON list
    val dependencyHealthScore: Float? = null,
    
    // Technology Detection
    val detectedTechnologies: String = "[]", // JSON list
    val frameworks: String = "[]", // JSON list
    val buildTools: String = "[]", // JSON list
    
    // Build & Run Instructions
    val buildInstructions: String? = null,
    val runInstructions: String? = null,
    val prerequisites: String = "[]", // JSON list
    
    // Security Analysis
    val securityScore: Int? = null, // 0-100
    val securityIssues: String = "[]", // JSON list
    val criticalVulnerabilities: Int = 0,
    val warnings: Int = 0,
    val securityRecommendations: String = "[]", // JSON list
    
    // Performance Analysis
    val performanceScore: Int? = null, // 0-100
    val performanceIssues: String = "[]", // JSON list
    val performanceRecommendations: String = "[]", // JSON list
    val detectedBottlenecks: String = "[]", // JSON list
    
    // Code Quality
    val codeQualityScore: Int? = null, // 0-100
    val codeSmells: String = "[]", // JSON list
    val complexityIssues: String = "[]", // JSON list
    val duplicationIssues: String = "[]", // JSON list
    val qualityRecommendations: String = "[]", // JSON list
    
    // Improvement Recommendations
    val improvementRecommendations: String = "[]", // JSON list
    val prioritizedActions: String = "[]", // JSON list
    
    // Documentation
    val hasReadme: Boolean = false,
    val hasApiDocs: Boolean = false,
    val hasContributingGuide: Boolean = false,
    val documentationScore: Int? = null, // 0-100
    val documentationRecommendations: String = "[]", // JSON list
    
    // Raw AI response for debugging
    val rawResponse: String? = null,
    val modelUsed: String? = null,
    val providerUsed: String? = null,
    val generationTimeMs: Long = 0
) {
    fun getDetectedPatternsList(): List<String> = 
        runCatching { Json.decodeFromString(ListSerializer(String.serializer()), detectedPatterns) }.getOrDefault(emptyList())
    
    fun getLanguageBreakdownMap(): Map<String, Int> =
        runCatching { Json.decodeFromString<Map<String, Int>>(languageBreakdown) }.getOrDefault(emptyMap())
    
    fun getDependenciesList(): List<DependencyInfo> =
        runCatching { Json.decodeFromString(ListSerializer(DependencyInfo.serializer()), dependencies) }.getOrDefault(emptyList())
    
    fun getDetectedTechnologiesList(): List<String> =
        runCatching { Json.decodeFromString(ListSerializer(String.serializer()), detectedTechnologies) }.getOrDefault(emptyList())
    
    fun getSecurityIssuesList(): List<SecurityIssue> =
        runCatching { Json.decodeFromString(ListSerializer(SecurityIssue.serializer()), securityIssues) }.getOrDefault(emptyList())
    
    fun getPerformanceIssuesList(): List<PerformanceIssue> =
        runCatching { Json.decodeFromString(ListSerializer(PerformanceIssue.serializer()), performanceIssues) }.getOrDefault(emptyList())
    
    fun getCodeSmellsList(): List<CodeSmell> =
        runCatching { Json.decodeFromString(ListSerializer(CodeSmell.serializer()), codeSmells) }.getOrDefault(emptyList())
    
    fun getImprovementRecommendationsList(): List<ImprovementRecommendation> =
        runCatching { Json.decodeFromString(ListSerializer(ImprovementRecommendation.serializer()), improvementRecommendations) }.getOrDefault(emptyList())
    
    fun getPrioritizedActionsList(): List<PrioritizedAction> =
        runCatching { Json.decodeFromString(ListSerializer(PrioritizedAction.serializer()), prioritizedActions) }.getOrDefault(emptyList())
    
    fun getSecurityRecommendationsList(): List<String> =
        runCatching { Json.decodeFromString(ListSerializer(String.serializer()), securityRecommendations) }.getOrDefault(emptyList())
    
    fun getQualityRecommendationsList(): List<String> =
        runCatching { Json.decodeFromString(ListSerializer(String.serializer()), qualityRecommendations) }.getOrDefault(emptyList())
}

enum class AnalysisType {
    FULL,           // Complete analysis
    ARCHITECTURE,   // Architecture only
    SECURITY,       // Security only
    PERFORMANCE,    // Performance only
    CODE_QUALITY,   // Code quality only
    QUICK_SCAN      // Quick overview
}

@Serializable
data class DependencyInfo(
    val name: String,
    val version: String? = null,
    val latestVersion: String? = null,
    val isOutdated: Boolean = false,
    val hasVulnerability: Boolean = false,
    val severity: String? = null
)

@Serializable
data class SecurityIssue(
    val severity: String, // CRITICAL, HIGH, MEDIUM, LOW
    val category: String,
    val description: String,
    val filePath: String? = null,
    val lineNumber: Int? = null,
    val recommendation: String
)

@Serializable
data class PerformanceIssue(
    val severity: String,
    val category: String,
    val description: String,
    val filePath: String? = null,
    val recommendation: String
)

@Serializable
data class CodeSmell(
    val severity: String,
    val type: String,
    val description: String,
    val filePath: String? = null,
    val lineNumber: Int? = null,
    val recommendation: String
)

@Serializable
data class ImprovementRecommendation(
    val priority: Int, // 1-5, 1 being highest
    val category: String,
    val title: String,
    val description: String,
    val estimatedEffort: String? = null,
    val impact: String? = null
)

@Serializable
data class PrioritizedAction(
    val order: Int,
    val action: String,
    val reason: String,
    val category: String
)

@Serializable
data class ProjectComparisonResult(
    val baseProjectId: Long,
    val compareProjectId: Long,
    val baseProjectName: String,
    val compareProjectName: String,
    val comparedAt: Long = System.currentTimeMillis(),
    val architectureDiff: String? = null,
    val technologyDiff: String = "[]",
    val dependencyDiff: String = "[]",
    val securityScoreDiff: Int = 0,
    val qualityScoreDiff: Int = 0,
    val performanceScoreDiff: Int = 0,
    val overallAssessment: String? = null
)
