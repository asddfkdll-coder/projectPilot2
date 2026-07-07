package com.projectpilot.app.data.ai

import com.projectpilot.app.domain.model.AiAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class AiAnalysisService(
    private val config: AiProviderConfig
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val client = OkHttpClient.Builder()
        .connectTimeout(config.timeoutSeconds.toLong(), TimeUnit.SECONDS)
        .readTimeout(config.timeoutSeconds.toLong(), TimeUnit.SECONDS)
        .writeTimeout(config.timeoutSeconds.toLong(), TimeUnit.SECONDS)
        .build()

    suspend fun analyzeProject(
        projectPath: String,
        projectName: String,
        analysisType: String,
        fileTree: String,
        buildFiles: String
    ): Result<AiAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildPrompt(projectName, analysisType, fileTree, buildFiles)
            val response = sendRequest(prompt)
            response.fold(
                onSuccess = { responseText ->
                    Result.success(AiAnalysisResult(
                        projectPath = projectPath,
                        projectName = projectName,
                        analysisType = analysisType,
                        architectureSummary = "Analysis completed",
                        rawResponse = responseText
                    ))
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: SocketTimeoutException) {
            Result.failure(IOException("Request timed out after ${config.timeoutSeconds}s"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val testPrompt = "Hello, respond with 'OK' only."
            sendRequest(testPrompt).map { "Connection successful" }
        } catch (e: Exception) {
            Result.failure(IOException("Connection failed: ${e.message}"))
        }
    }

    private fun sendRequest(prompt: String): Result<String> {
        return try {
            val requestBody = when (config.providerType) {
                AiProviderType.GEMINI -> buildGeminiRequest(prompt)
                else -> buildOpenAiCompatibleRequest(prompt)
            }

            val request = Request.Builder()
                .url(config.apiUrl)
                .addHeader("Authorization", "Bearer ${config.apiKey}")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                when {
                    response.code == 429 -> Result.failure(IOException("Rate limit exceeded (HTTP 429)"))
                    !response.isSuccessful -> Result.failure(IOException("HTTP ${response.code}: ${response.message}"))
                    else -> {
                        val body = response.body?.string() ?: ""
                        Result.success(body)
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildOpenAiCompatibleRequest(prompt: String): String {
        return "{\"model\":\"${config.modelName}\",\"messages\":[{\"role\":\"user\",\"content\":\"$prompt\"}],\"temperature\":0.3}"
    }

    private fun buildGeminiRequest(prompt: String): String {
        return "{\"contents\":[{\"parts\":[{\"text\":\"$prompt\"}]}]}"
    }

    private fun buildPrompt(
        projectName: String,
        analysisType: String,
        fileTree: String,
        buildFiles: String
    ): String {
        return "Analyze the project \"$projectName\" ($analysisType).\\n\\nFile structure:\\n$fileTree\\n\\nBuild config:\\n$buildFiles"
    }
}
