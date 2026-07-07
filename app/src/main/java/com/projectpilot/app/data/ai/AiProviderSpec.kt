package com.projectpilot.app.data.ai

import com.projectpilot.app.domain.model.AiProviderType

/**
 * Static, compile-time metadata describing how to talk to one provider
 * family: default endpoint, default models, and auth header shape.
 * This is the single place to edit when adding a brand-new provider —
 * settings UI, secure key storage, and the HTTP client are all driven
 * from [AiProviderRegistry] rather than hardcoding provider names.
 */
data class AiProviderSpec(
    val type: AiProviderType,
    val defaultBaseUrl: String,
    val defaultModels: List<String>,
    val authHeaderName: String = "Authorization",
    val authHeaderPrefix: String = "Bearer ",
    val chatCompletionsPath: String = "/v1/chat/completions",
    /** True for providers where the base URL is user-supplied (e.g. self-hosted). */
    val requiresCustomBaseUrl: Boolean = false
)

object AiProviderRegistry {

    val specs: Map<AiProviderType, AiProviderSpec> = listOf(
        AiProviderSpec(
            type = AiProviderType.OPENAI,
            defaultBaseUrl = "https://api.openai.com",
            defaultModels = listOf("gpt-4.1", "gpt-4.1-mini", "o4-mini")
        ),
        AiProviderSpec(
            type = AiProviderType.ANTHROPIC,
            defaultBaseUrl = "https://api.anthropic.com",
            defaultModels = listOf("claude-sonnet-4-6", "claude-haiku-4-5"),
            authHeaderName = "x-api-key",
            authHeaderPrefix = "",
            chatCompletionsPath = "/v1/messages"
        ),
        AiProviderSpec(
            type = AiProviderType.GEMINI,
            defaultBaseUrl = "https://generativelanguage.googleapis.com",
            defaultModels = listOf("gemini-2.5-pro", "gemini-2.5-flash"),
            authHeaderName = "x-goog-api-key",
            authHeaderPrefix = "",
            chatCompletionsPath = "/v1beta/models"
        ),
        AiProviderSpec(
            type = AiProviderType.DEEPSEEK,
            defaultBaseUrl = "https://api.deepseek.com",
            defaultModels = listOf("deepseek-chat", "deepseek-reasoner")
        ),
        AiProviderSpec(
            type = AiProviderType.QWEN,
            defaultBaseUrl = "https://dashscope.aliyuncs.com/compatible-mode",
            defaultModels = listOf("qwen-max", "qwen-plus")
        ),
        AiProviderSpec(
            type = AiProviderType.GROK,
            defaultBaseUrl = "https://api.x.ai",
            defaultModels = listOf("grok-4", "grok-4-mini")
        ),
        AiProviderSpec(
            type = AiProviderType.CUSTOM_OPENAI_COMPATIBLE,
            defaultBaseUrl = "",
            defaultModels = emptyList(),
            requiresCustomBaseUrl = true
        )
    ).associateBy { it.type }

    fun specFor(type: AiProviderType): AiProviderSpec =
        specs[type] ?: error("No AiProviderSpec registered for $type")
}
