package com.projectpilot.app.data.ai

import com.projectpilot.app.data.repository.AiSettingsRepository
import com.projectpilot.app.data.security.SecureApiKeyStore
import com.projectpilot.app.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlinx.serialization.builtins.ListSerializer
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI-powered project analysis engine.
 * Communicates with configured AI providers to generate comprehensive project analysis.
 * Supports: architecture analysis, dependency analysis, technology detection,
 * security analysis, performance analysis, code quality insights, and improvement recommendations.
 */
@Singleton
class AiAnalysisService @Inject constructor(
    private val aiSettingsRepository: AiSettingsRepository,
    private val secureApiKeyStore: SecureApiKeyStore
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    
    sealed class AnalysisResult {
        data class Success(val analysis: AiAnalysisResult) : AnalysisResult()
        data class Error(val message: String, val isRetryable: Boolean = true) : AnalysisResult()
        data object NoProviderConfigured : AnalysisResult()
        data object NoApiKey : AnalysisResult()
    }
    
    /**
     * Performs a full AI analysis of a project.
     */
    suspend fun analyzeProject(
        project: Project,
        analysisType: AnalysisType = AnalysisType.FULL,
        fileTreeSummary: String? = null,
        dependencyContent: String? = null
    ): AnalysisResult = withContext(Dispatchers.IO) {
        val provider = getDefaultProvider() ?: return@withContext AnalysisResult.NoProviderConfigured
        val apiKey = secureApiKeyStore.getKey(provider.type) ?: return@withContext AnalysisResult.NoApiKey
        
        val spec = AiProviderRegistry.specFor(provider.type)
        val config = provider
        
        val prompt = buildAnalysisPrompt(project, analysisType, fileTreeSummary, dependencyContent)
        
        val startTime = System.currentTimeMillis()
        
        try {
            val response = sendRequest(spec, config, apiKey, prompt)
            val parsed = parseAnalysisResponse(response, project, analysisType, config)
            
            val result = parsed.copy(
                generationTimeMs = System.currentTimeMillis() - startTime,
                modelUsed = config.selectedModel,
                providerUsed = provider.type.displayName
            )
            
            AnalysisResult.Success(result)
        } catch (e: java.net.SocketTimeoutException) {
            AnalysisResult.Error("Request timed out after ${config.timeoutSeconds}s. Try increasing timeout in settings.", isRetryable = true)
        } catch (e: java.io.IOException) {
            AnalysisResult.Error("Network error: ${e.message}. Check your connection and base URL.", isRetryable = true)
        } catch (e: Exception) {
            AnalysisResult.Error("Analysis failed: ${e.message}", isRetryable = false)
        }
    }
    
    /**
     * Tests connectivity to a provider with the given configuration.
     */
    suspend fun testConnection(config: AiProviderConfig, apiKey: String): ConnectionTestResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val spec = AiProviderRegistry.specFor(config.type)
            val testPrompt = "Respond with only: OK"
            
            val connection = createConnection(spec, config, apiKey, testPrompt)
            val responseCode = connection.responseCode
            val responseTime = System.currentTimeMillis() - startTime
            
            when (responseCode) {
                200 -> ConnectionTestResult.Success(responseTime)
                401 -> ConnectionTestResult.Error("Invalid API key (HTTP 401)")
                403 -> ConnectionTestResult.Error("Access forbidden (HTTP 403) - check permissions")
                429 -> ConnectionTestResult.Error("Rate limited (HTTP 429) - too many requests")
                in 500..599 -> ConnectionTestResult.Error("Server error (HTTP $responseCode) - provider may be down")
                else -> ConnectionTestResult.Error("Unexpected response (HTTP $responseCode)")
            }
        } catch (e: java.net.SocketTimeoutException) {
            ConnectionTestResult.Error("Connection timed out after ${config.timeoutSeconds}s")
        } catch (e: java.net.UnknownHostException) {
            ConnectionTestResult.Error("Unknown host - check base URL: ${config.baseUrl}")
        } catch (e: Exception) {
            ConnectionTestResult.Error("Connection failed: ${e.message}")
        }
    }
    
    /**
     * Get the currently configured default provider.
     */
    suspend fun getDefaultProvider(): AiProviderConfig? {
        val configs = aiSettingsRepository.providerConfigs
        val default = aiSettingsRepository.defaultProvider
        
        // Collect latest values
        var result: AiProviderConfig? = null
        configs.collect { configMap ->
            val defaultType = default
            result = configMap.values.firstOrNull { it.enabled }?.let { enabled ->
                // Try to use the default provider if it's enabled
                defaultType?.let { dt -> configMap[dt]?.takeIf { it.enabled } } ?: enabled
            }
        }
        return result
    }
    
    private fun buildAnalysisPrompt(
        project: Project,
        analysisType: AnalysisType,
        fileTreeSummary: String?,
        dependencyContent: String?
    ): String {
        val basePrompt = """
            You are an expert software architect and code reviewer. Analyze this project and respond ONLY with valid JSON.
            
            Project: ${project.name}
            Type: ${project.type}
            Framework: ${project.framework ?: "Unknown"}
            Path: ${project.path}
            ${fileTreeSummary?.let { "\nFile Structure Summary:\n$it" } ?: ""}
            ${dependencyContent?.let { "\nDependencies:\n$it" } ?: ""}
            ${project.dependenciesJson.takeIf { it != "[]" }?.let { "\nKnown Dependencies: $it" } ?: ""}
            
            Respond with a JSON object containing these fields:
        """.trimIndent()
        
        return when (analysisType) {
            AnalysisType.FULL -> basePrompt + FULL_ANALYSIS_FIELDS
            AnalysisType.ARCHITECTURE -> basePrompt + ARCHITECTURE_FIELDS
            AnalysisType.SECURITY -> basePrompt + SECURITY_FIELDS
            AnalysisType.PERFORMANCE -> basePrompt + PERFORMANCE_FIELDS
            AnalysisType.CODE_QUALITY -> basePrompt + QUALITY_FIELDS
            AnalysisType.QUICK_SCAN -> basePrompt + QUICK_SCAN_FIELDS
        }
    }
    
    private fun sendRequest(
        spec: AiProviderSpec,
        config: AiProviderConfig,
        apiKey: String,
        prompt: String
    ): String {
        val url = URL("${config.baseUrl}${spec.chatCompletionsPath}")
        val connection = url.openConnection() as HttpURLConnection
        
        connection.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty(spec.authHeaderName, "${spec.authHeaderPrefix}${if (spec.authHeaderPrefix.isNotEmpty()) " " else ""}$apiKey".trim())
            connectTimeout = config.timeoutSeconds * 1000
            readTimeout = config.timeoutSeconds * 1000
            doOutput = true
        }
        
        val requestBody = buildRequestBody(spec, config, prompt)
        connection.outputStream.use { it.write(requestBody.toByteArray()) }
        
        val responseCode = connection.responseCode
        if (responseCode != 200) {
            val error = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
            throw RuntimeException("HTTP $responseCode: $error")
        }
        
        return connection.inputStream.bufferedReader().use { it.readText() }
    }
    
    private fun buildRequestBody(spec: AiProviderSpec, config: AiProviderConfig, prompt: String): String {
        return when (spec.type) {
            AiProviderType.GEMINI -> {
                // Gemini uses a different API structure
                """
                {
                    "contents": [{"parts": [{"text": ${json.encodeToString(prompt)}}]}],
                    "generationConfig": {"temperature": 0.1, "maxOutputTokens": 8000}
                }
                """.trimIndent()
            }
            else -> {
                // OpenAI-compatible format
                """
                {
                    "model": "${config.selectedModel}",
                    "messages": [
                        {"role": "system", "content": "You are an expert software analysis engine. Respond only with valid JSON."},
                        {"role": "user", "content": ${json.encodeToString(prompt)}}
                    ],
                    "temperature": 0.1,
                    "max_tokens": 8000
                }
                """.trimIndent()
            }
        }
    }
    
    private fun parseAnalysisResponse(
        response: String,
        project: Project,
        analysisType: AnalysisType,
        config: AiProviderConfig
    ): AiAnalysisResult {
        // Extract JSON from response (handle markdown code blocks)
        val jsonStr = extractJsonFromResponse(response)
        
        val parsed = runCatching { json.parseToJsonElement(jsonStr).jsonObject }.getOrElse { 
            // Fallback: create a basic analysis from raw text
            return createFallbackAnalysis(project, analysisType, response, config)
        }
        
        return AiAnalysisResult(
            projectId = project.id,
            projectName = project.name,
            projectPath = project.path,
            analysisType = analysisType,
            architectureSummary = parsed["architectureSummary"]?.jsonPrimitive?.content,
            detectedPatterns = parsed["detectedPatterns"]?.toString() ?: "[]",
            layerStructure = parsed["layerStructure"]?.jsonPrimitive?.content,
            totalFiles = parsed["totalFiles"]?.jsonPrimitive?.intOrNull ?: 0,
            totalDirectories = parsed["totalDirectories"]?.jsonPrimitive?.intOrNull ?: 0,
            totalLinesOfCode = parsed["totalLinesOfCode"]?.jsonPrimitive?.intOrNull ?: 0,
            languageBreakdown = parsed["languageBreakdown"]?.toString() ?: "{}",
            fileTreeSummary = parsed["fileTreeSummary"]?.jsonPrimitive?.content,
            dependencies = parsed["dependencies"]?.toString() ?: "[]",
            outdatedDependencies = parsed["outdatedDependencies"]?.toString() ?: "[]",
            dependencyVulnerabilities = parsed["dependencyVulnerabilities"]?.toString() ?: "[]",
            dependencyHealthScore = parsed["dependencyHealthScore"]?.jsonPrimitive?.floatOrNull,
            detectedTechnologies = parsed["detectedTechnologies"]?.toString() ?: "[]",
            frameworks = parsed["frameworks"]?.toString() ?: "[]",
            buildTools = parsed["buildTools"]?.toString() ?: "[]",
            buildInstructions = parsed["buildInstructions"]?.jsonPrimitive?.content,
            runInstructions = parsed["runInstructions"]?.jsonPrimitive?.content,
            prerequisites = parsed["prerequisites"]?.toString() ?: "[]",
            securityScore = parsed["securityScore"]?.jsonPrimitive?.intOrNull,
            securityIssues = parsed["securityIssues"]?.toString() ?: "[]",
            criticalVulnerabilities = parsed["criticalVulnerabilities"]?.jsonPrimitive?.intOrNull ?: 0,
            warnings = parsed["warnings"]?.jsonPrimitive?.intOrNull ?: 0,
            securityRecommendations = parsed["securityRecommendations"]?.toString() ?: "[]",
            performanceScore = parsed["performanceScore"]?.jsonPrimitive?.intOrNull,
            performanceIssues = parsed["performanceIssues"]?.toString() ?: "[]",
            performanceRecommendations = parsed["performanceRecommendations"]?.toString() ?: "[]",
            detectedBottlenecks = parsed["detectedBottlenecks"]?.toString() ?: "[]",
            codeQualityScore = parsed["codeQualityScore"]?.jsonPrimitive?.intOrNull,
            codeSmells = parsed["codeSmells"]?.toString() ?: "[]",
            complexityIssues = parsed["complexityIssues"]?.toString() ?: "[]",
            duplicationIssues = parsed["duplicationIssues"]?.toString() ?: "[]",
            qualityRecommendations = parsed["qualityRecommendations"]?.toString() ?: "[]",
            improvementRecommendations = parsed["improvementRecommendations"]?.toString() ?: "[]",
            prioritizedActions = parsed["prioritizedActions"]?.toString() ?: "[]",
            hasReadme = parsed["hasReadme"]?.jsonPrimitive?.booleanOrNull ?: false,
            hasApiDocs = parsed["hasApiDocs"]?.jsonPrimitive?.booleanOrNull ?: false,
            hasContributingGuide = parsed["hasContributingGuide"]?.jsonPrimitive?.booleanOrNull ?: false,
            documentationScore = parsed["documentationScore"]?.jsonPrimitive?.intOrNull,
            documentationRecommendations = parsed["documentationRecommendations"]?.toString() ?: "[]",
            rawResponse = response.take(10000), // Store truncated raw response
            modelUsed = config.selectedModel,
            providerUsed = config.type.displayName
        )
    }
    
    private fun createFallbackAnalysis(
        project: Project,
        analysisType: AnalysisType,
        rawResponse: String,
        config: AiProviderConfig
    ): AiAnalysisResult {
        return AiAnalysisResult(
            projectId = project.id,
            projectName = project.name,
            projectPath = project.path,
            analysisType = analysisType,
            architectureSummary = "Analysis completed but response parsing failed. Raw response saved.",
            rawResponse = rawResponse.take(10000),
            modelUsed = config.selectedModel,
            providerUsed = config.type.displayName
        )
    }
    
    private fun extractJsonFromResponse(response: String): String {
        // Try to extract JSON from markdown code blocks
        val codeBlockRegex = "```json\\s*\\n?(.*?)\\n?```".toRegex(RegexOption.DOT_MATCHES_ALL)
        val match = codeBlockRegex.find(response)
        if (match != null) {
            return match.groupValues[1].trim()
        }
        
        // Try to find JSON object directly
        val jsonStart = response.indexOf('{')
        val jsonEnd = response.lastIndexOf('}')
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return response.substring(jsonStart, jsonEnd + 1)
        }
        
        return response
    }
    
    sealed class ConnectionTestResult {
        data class Success(val responseTimeMs: Long) : ConnectionTestResult()
        data class Error(val message: String) : ConnectionTestResult()
    }
    
    companion object {
        private const val FULL_ANALYSIS_FIELDS = """
            
            - "architectureSummary": "Brief architecture description"
            - "detectedPatterns": ["pattern1", "pattern2"]
            - "layerStructure": "Description of layer structure"
            - "totalFiles": 150
            - "totalDirectories": 20
            - "totalLinesOfCode": 5000
            - "languageBreakdown": {"Kotlin": 3000, "XML": 1000, "Gradle": 500}
            - "fileTreeSummary": "Brief file structure summary"
            - "dependencies": [{"name": "dep1", "version": "1.0", "isOutdated": false, "hasVulnerability": false}]
            - "outdatedDependencies": [{"name": "old1", "version": "1.0", "latestVersion": "2.0"}]
            - "dependencyVulnerabilities": [{"name": "vuln1", "severity": "HIGH"}]
            - "dependencyHealthScore": 85
            - "detectedTechnologies": ["Spring Boot", "React"]
            - "frameworks": ["Spring Boot"]
            - "buildTools": ["Gradle"]
            - "buildInstructions": "How to build"
            - "runInstructions": "How to run"
            - "prerequisites": ["Java 17", "Node 18"]
            - "securityScore": 75
            - "securityIssues": [{"severity": "HIGH", "category": "Authentication", "description": "Issue", "recommendation": "Fix"}]
            - "criticalVulnerabilities": 1
            - "warnings": 3
            - "securityRecommendations": ["Use HTTPS", "Validate inputs"]
            - "performanceScore": 80
            - "performanceIssues": [{"severity": "MEDIUM", "category": "Database", "description": "N+1 query", "recommendation": "Use join"}]
            - "performanceRecommendations": ["Add caching", "Optimize queries"]
            - "detectedBottlenecks": ["Database queries", "Image loading"]
            - "codeQualityScore": 82
            - "codeSmells": [{"severity": "LOW", "type": "Long Method", "description": "Method too long", "recommendation": "Extract method"}]
            - "complexityIssues": [{"severity": "MEDIUM", "description": "High cyclomatic complexity", "filePath": "path"}]
            - "duplicationIssues": [{"severity": "LOW", "description": "Code duplication", "filePath": "path"}]
            - "qualityRecommendations": ["Add unit tests", "Reduce complexity"]
            - "improvementRecommendations": [{"priority": 1, "category": "Testing", "title": "Add tests", "description": "Add unit tests", "estimatedEffort": "Medium", "impact": "High"}]
            - "prioritizedActions": [{"order": 1, "action": "Add authentication", "reason": "Security critical", "category": "Security"}]
            - "hasReadme": true
            - "hasApiDocs": false
            - "hasContributingGuide": false
            - "documentationScore": 60
            - "documentationRecommendations": ["Add README", "Add API docs"]
        """
        
        private const val ARCHITECTURE_FIELDS = """
            
            - "architectureSummary": "Architecture description"
            - "detectedPatterns": ["pattern1"]
            - "layerStructure": "Layer structure"
            - "detectedTechnologies": ["tech1"]
            - "frameworks": ["framework1"]
            - "buildTools": ["tool1"]
        """
        
        private const val SECURITY_FIELDS = """
            
            - "securityScore": 75
            - "securityIssues": [{"severity": "HIGH", "category": "Auth", "description": "Issue", "recommendation": "Fix"}]
            - "criticalVulnerabilities": 1
            - "warnings": 3
            - "securityRecommendations": ["Fix 1", "Fix 2"]
        """
        
        private const val PERFORMANCE_FIELDS = """
            
            - "performanceScore": 80
            - "performanceIssues": [{"severity": "MEDIUM", "category": "DB", "description": "Issue", "recommendation": "Fix"}]
            - "performanceRecommendations": ["Fix 1"]
            - "detectedBottlenecks": ["bottleneck1"]
        """
        
        private const val QUALITY_FIELDS = """
            
            - "codeQualityScore": 82
            - "codeSmells": [{"severity": "LOW", "type": "Type", "description": "Desc", "recommendation": "Fix"}]
            - "complexityIssues": [{"severity": "MEDIUM", "description": "Desc", "filePath": "path"}]
            - "duplicationIssues": [{"severity": "LOW", "description": "Desc", "filePath": "path"}]
            - "qualityRecommendations": ["Fix 1"]
        """
        
        private const val QUICK_SCAN_FIELDS = """
            
            - "architectureSummary": "Brief summary"
            - "totalFiles": 100
            - "totalLinesOfCode": 3000
            - "detectedTechnologies": ["tech1"]
            - "securityScore": 70
            - "performanceScore": 75
            - "codeQualityScore": 80
            - "hasReadme": true
        """
    }
}
