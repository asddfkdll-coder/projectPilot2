package com.projectpilot.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedSettingsScreen(
    navController: NavController,
    viewModel: EnhancedSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var showResetDialog by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            SettingsSection(title = "AI Providers") {
                SettingsItem(
                    icon = Icons.Default.Cloud,
                    title = "Manage AI Providers",
                    subtitle = "Configure OpenAI, Claude, Gemini, etc.",
                    onClick = { navController.navigate("ai_providers") }
                )
            }

            SettingsSection(title = "Analysis") {
                SettingsSwitch(
                    title = "Auto-save analysis",
                    subtitle = "Automatically save AI analysis results",
                    checked = uiState.autoSaveAnalysis,
                    onCheckedChange = { viewModel.setAutoSaveAnalysis(it) }
                )

                SettingsSlider(
                    title = "Retention period",
                    subtitle = "${uiState.retentionDays} days",
                    value = uiState.retentionDays.toFloat(),
                    valueRange = 7f..90f,
                    onValueChange = { viewModel.setRetentionDays(it.toInt()) }
                )

                SettingsSlider(
                    title = "Analysis timeout",
                    subtitle = "${uiState.analysisTimeout}s",
                    value = uiState.analysisTimeout.toFloat(),
                    valueRange = 10f..180f,
                    onValueChange = { viewModel.setAnalysisTimeout(it.toInt()) }
                )
            }

            SettingsSection(title = "Appearance") {
                SettingsDropdown(
                    title = "Theme",
                    selected = uiState.theme,
                    options = listOf("Light", "Dark", "System"),
                    onSelect = { viewModel.setTheme(it) }
                )

                SettingsDropdown(
                    title = "Language",
                    selected = uiState.language,
                    options = listOf("System", "English", "Arabic"),
                    onSelect = { viewModel.setLanguage(it) }
                )
            }

            SettingsSection(title = "Notifications") {
                SettingsSlider(
                    title = "Monitoring interval",
                    subtitle = "${uiState.monitoringInterval} minutes",
                    value = uiState.monitoringInterval.toFloat(),
                    valueRange = 1f..60f,
                    onValueChange = { viewModel.setMonitoringInterval(it.toInt()) }
                )
            }

            SettingsSection(title = "Privacy") {
                SettingsSwitch(
                    title = "Confirm delete",
                    subtitle = "Show confirmation before deleting projects",
                    checked = uiState.confirmDelete,
                    onCheckedChange = { viewModel.setConfirmDelete(it) }
                )

                SettingsSwitch(
                    title = "Git tracking",
                    subtitle = "Track Git activity in projects",
                    checked = uiState.gitTracking,
                    onCheckedChange = { viewModel.setGitTracking(it) }
                )

                SettingsSwitch(
                    title = "Crash reporting",
                    subtitle = "Send anonymous crash reports",
                    checked = uiState.crashReporting,
                    onCheckedChange = { viewModel.setCrashReporting(it) }
                )
            }

            SettingsSection(title = "Cache") {
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "Clear old analyses",
                    subtitle = "Remove analyses older than retention period",
                    onClick = { viewModel.clearOldAnalyses() }
                )

                SettingsItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Clear all cache",
                    subtitle = "Remove all cached data",
                    onClick = { showClearAllDialog = true }
                )
            }

            SettingsSection(title = "Backup") {
                SettingsItem(
                    icon = Icons.Default.FileUpload,
                    title = "Export settings",
                    subtitle = "Save settings to JSON file (API keys excluded)",
                    onClick = { viewModel.exportSettings() }
                )

                SettingsItem(
                    icon = Icons.Default.FileDownload,
                    title = "Import settings",
                    subtitle = "Restore settings from JSON file",
                    onClick = { viewModel.importSettings() }
                )
            }

            SettingsSection(title = "Danger Zone") {
                Button(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.RestartAlt, "Reset")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset all settings")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "ProjectPilot v2.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset all settings?") },
            text = { Text("This will restore all settings to default values. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAllSettings()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear all cache?") },
            text = { Text("This will remove all cached analyses and temporary files.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllCache()
                        showClearAllDialog = false
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, null) },
        trailingContent = { Icon(Icons.Default.ChevronRight, null) },
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
private fun SettingsSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    )
}

@Composable
private fun SettingsSlider(
    title: String,
    subtitle: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Text(subtitle, style = MaterialTheme.typography.bodySmall)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

@Composable
private fun SettingsDropdown(
    title: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(title) },
        trailingContent = {
            Box {
                TextButton(onClick = { expanded = true }) {
                    Text(selected)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onSelect(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    )
}
