package com.projectpilot.app.data.ai

data class AiProviderConfig(
    val providerType: AiProviderType = AiProviderType.OPENAI,
    val apiKey: String = "",
    val apiUrl: String = "",
    val modelName: String = "",
    val isEnabled: Boolean = false,
    val timeoutSeconds: Int = 60,
    val maxRetries: Int = 3
)
