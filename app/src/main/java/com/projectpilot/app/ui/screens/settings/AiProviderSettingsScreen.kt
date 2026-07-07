package com.projectpilot.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AiProviderSettingsScreen(
    viewModel: AiProviderSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = "AI Providers", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Keys are encrypted with Android Keystore (AES-256-GCM) and never leave this device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
        }
        items(state.providers, key = { it.type }) { provider ->
            AiProviderCard(
                provider = provider,
                onEnabledChange = { viewModel.setEnabled(provider.type, it) },
                onSaveKey = { viewModel.saveApiKey(provider.type, it) },
                onClearKey = { viewModel.clearApiKey(provider.type) },
                onSetDefault = { viewModel.setDefaultProvider(provider.type) },
                onModelChange = { viewModel.updateModel(provider.type, it, provider.baseUrl) },
                onBaseUrlChange = { viewModel.updateBaseUrl(provider.type, it, provider.selectedModel) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiProviderCard(
    provider: ProviderUiState,
    onEnabledChange: (Boolean) -> Unit,
    onSaveKey: (String) -> Unit,
    onClearKey: () -> Unit,
    onSetDefault: () -> Unit,
    onModelChange: (String) -> Unit,
    onBaseUrlChange: (String) -> Unit
) {
    var keyInput by remember(provider.type) { mutableStateOf("") }
    var keyVisible by remember { mutableStateOf(false) }
    var modelMenuExpanded by remember { mutableStateOf(false) }
    var baseUrlInput by remember(provider.type) { mutableStateOf(provider.baseUrl) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(provider.displayName, style = MaterialTheme.typography.titleMedium)
                    if (provider.isDefault) {
                        AssistChip(onClick = {}, label = { Text("Default") })
                    }
                }
                Switch(checked = provider.enabled, onCheckedChange = onEnabledChange)
            }

            Spacer(Modifier.height(12.dp))

            if (provider.requiresCustomBaseUrl) {
                OutlinedTextField(
                    value = baseUrlInput,
                    onValueChange = {
                        baseUrlInput = it
                        onBaseUrlChange(it)
                    },
                    label = { Text("Base URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = keyInput,
                onValueChange = { keyInput = it },
                label = {
                    Text(
                        if (provider.hasApiKey) "New API key (leave blank to keep current)"
                        else "API key"
                    )
                },
                placeholder = { provider.maskedKeyPreview?.let { Text(it) } },
                singleLine = true,
                visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { keyVisible = !keyVisible }) {
                        Icon(
                            imageVector = if (keyVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (keyVisible) "Hide key" else "Show key"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (provider.availableModels.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = modelMenuExpanded,
                    onExpandedChange = { modelMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = provider.selectedModel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Model") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    androidx.compose.material3.ExposedDropdownMenu(
                        expanded = modelMenuExpanded,
                        onDismissRequest = { modelMenuExpanded = false }
                    ) {
                        provider.availableModels.forEach { model ->
                            DropdownMenuItem(
                                text = { Text(model) },
                                onClick = {
                                    onModelChange(model)
                                    modelMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (keyInput.isNotBlank()) {
                            onSaveKey(keyInput)
                            keyInput = ""
                        }
                    },
                    enabled = keyInput.isNotBlank()
                ) {
                    Text("Save key")
                }
                OutlinedButton(onClick = onClearKey, enabled = provider.hasApiKey) {
                    Text("Remove key")
                }
                if (!provider.isDefault && provider.hasApiKey) {
                    TextButton(onClick = onSetDefault) {
                        Text("Set default")
                    }
                }
            }
        }
    }
}
