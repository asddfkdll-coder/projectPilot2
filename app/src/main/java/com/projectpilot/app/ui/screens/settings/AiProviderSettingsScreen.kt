package com.projectpilot.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.projectpilot.app.data.ai.AiProviderConfig
import com.projectpilot.app.data.ai.AiProviderType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiProviderSettingsScreen(
    navController: NavController,
    viewModel: AiProviderSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedProvider by remember { mutableStateOf(AiProviderType.OPENAI) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Providers") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Select Provider", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            val providers = AiProviderType.entries
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                providers.forEach { provider ->
                    FilterChip(
                        selected = selectedProvider == provider,
                        onClick = { selectedProvider = provider },
                        label = { Text(provider.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProviderConfigForm(
                providerType = selectedProvider,
                onSave = { config -> viewModel.saveProviderConfig(config) },
                onTest = { config -> viewModel.testConnection(config) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            uiState.testResult?.let { result ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.success)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = result.message,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderConfigForm(
    providerType: AiProviderType,
    onSave: (AiProviderConfig) -> Unit,
    onTest: (AiProviderConfig) -> Unit
) {
    var apiKey by remember { mutableStateOf("") }
    var apiUrl by remember { mutableStateOf(getDefaultUrl(providerType)) }
    var modelName by remember { mutableStateOf(getDefaultModel(providerType)) }
    var isEnabled by remember { mutableStateOf(false) }
    var apiKeyVisible by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = apiUrl,
                onValueChange = { apiUrl = it },
                label = { Text("API URL") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key") },
                visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                        Icon(
                            if (apiKeyVisible) Icons.Default.Clear else Icons.Default.Check,
                            if (apiKeyVisible) "Hide" else "Show"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = modelName,
                onValueChange = { modelName = it },
                label = { Text("Model Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Enabled")
                Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onTest(AiProviderConfig(
                            providerType = providerType,
                            apiKey = apiKey,
                            apiUrl = apiUrl,
                            modelName = modelName,
                            isEnabled = isEnabled
                        ))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Test")
                }
                Button(
                    onClick = {
                        onSave(AiProviderConfig(
                            providerType = providerType,
                            apiKey = apiKey,
                            apiUrl = apiUrl,
                            modelName = modelName,
                            isEnabled = isEnabled
                        ))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }
        }
    }
}

private fun getDefaultUrl(type: AiProviderType): String {
    return when (type) {
        AiProviderType.OPENAI -> "https://api.openai.com/v1/chat/completions"
        AiProviderType.ANTHROPIC -> "https://api.anthropic.com/v1/messages"
        AiProviderType.GEMINI -> "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent"
        AiProviderType.DEEPSEEK -> "https://api.deepseek.com/v1/chat/completions"
        AiProviderType.QWEN -> "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation"
        AiProviderType.GROK -> "https://api.x.ai/v1/chat/completions"
        AiProviderType.CUSTOM -> "https://api.example.com/v1/chat/completions"
    }
}

private fun getDefaultModel(type: AiProviderType): String {
    return when (type) {
        AiProviderType.OPENAI -> "gpt-4o-mini"
        AiProviderType.ANTHROPIC -> "claude-3-haiku-20240307"
        AiProviderType.GEMINI -> "gemini-pro"
        AiProviderType.DEEPSEEK -> "deepseek-chat"
        AiProviderType.QWEN -> "qwen-turbo"
        AiProviderType.GROK -> "grok-beta"
        AiProviderType.CUSTOM -> "model-name"
    }
}
