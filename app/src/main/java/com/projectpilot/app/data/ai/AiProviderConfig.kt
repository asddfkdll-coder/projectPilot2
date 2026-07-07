package com.projectpilot.app.data.ai

import com.projectpilot.app.domain.model.AiProviderType
import kotlinx.serialization.Serializable

/**
 * Non-secret configuration for one provider. The API key itself is
 * never stored here — it lives only in [com.projectpilot.app.data.security.SecureApiKeyStore].
 * Keeping secrets and metadata in separate stores means a bug in
 * settings export/import or logging can never accidentally leak a key.
 */
@Serializable
data class AiProviderConfig(
    val type: AiProviderType,
    val enabled: Boolean = false,
    val baseUrl: String,
    val selectedModel: String,
    val timeoutSeconds: Int = 30,
    val maxRetries: Int = 2
)
