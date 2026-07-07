package com.projectpilot.app.domain.model

/**
 * Every AI provider family the app can talk to. Adding a new provider
 * requires adding one entry here plus one [com.projectpilot.app.data.ai.AiProviderSpec]
 * entry in AiProviderRegistry — no other file needs to change to support it
 * (settings UI, secure storage, and the HTTP client all read from the registry).
 */
enum class AiProviderType(val displayName: String) {
    OPENAI("OpenAI"),
    ANTHROPIC("Claude (Anthropic)"),
    GEMINI("Google Gemini"),
    DEEPSEEK("DeepSeek"),
    QWEN("Qwen"),
    GROK("Grok (xAI)"),
    CUSTOM_OPENAI_COMPATIBLE("Custom (OpenAI-compatible)")
}
