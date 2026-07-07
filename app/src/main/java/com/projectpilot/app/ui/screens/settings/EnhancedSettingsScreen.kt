package com.projectpilot.app.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.documentfile.provider.DocumentFile
import com.projectpilot.app.data.local.SettingsExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedSettingsScreen(
    onBack: () -> Unit,
    onNavigateToAiProviders: () -> Unit = {},
    vm: EnhancedSettingsViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showResetConfirm by remember { mutableStateOf(false) }
    var showClearCacheConfirm by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Export launcher
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { vm.exportSettings(it) }
    }

    // Import launcher
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { vm.importSettings(it) }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset All Settings") },
            text = { Text("This will reset all settings to their default values. AI provider configurations will be preserved. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { vm.resetAllSettings(); showResetConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showClearCacheConfirm) {
        AlertDialog(
            onDismissRequest = { showClearCacheConfirm = false },
            title = { Text("Clear Analysis Cache") },
            text = { Text("This will delete all ${state.analysisCount} stored AI analysis results. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { vm.clearAnalysisCache(); showClearCacheConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Clear All") }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI Providers Section
            SettingsSectionCard(title = "AI Providers", icon = Icons.Default.Psychology) {
                Text(
                    "Configure AI providers for project analysis",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onNavigateToAiProviders,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Settings, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Manage AI Providers")
                }
            }

            // Analysis Settings
            SettingsSectionCard(title = "Analysis Settings", icon = Icons.Default.Analytics) {
                // Auto-save
                SettingsToggle(
                    title = "Auto-save analyses",
                    description = "Automatically save AI analysis results",
                    checked = state.analysisAutoSave,
                    onCheckedChange = vm::setAnalysisAutoSave
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Retention days
                SettingsSlider(
                    title = "Retention period: ${state.analysisRetentionDays} days",
                    value = state.analysisRetentionDays.toFloat(),
                    onValueChange = { vm.setAnalysisRetentionDays(it.toInt()) },
                    valueRange = 7f..365f,
                    steps = 358
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Default analysis type
                SettingsDropdown(
                    title = "Default analysis type",
                    options = listOf("FULL", "QUICK_SCAN", "SECURITY", "PERFORMANCE", "CODE_QUALITY", "ARCHITECTURE"),
                    selected = state.defaultAnalysisType,
                    onSelect = vm::setDefaultAnalysisType
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Timeout
                SettingsSlider(
                    title = "Timeout: ${state.analysisTimeoutSeconds}s",
                    value = state.analysisTimeoutSeconds.toFloat(),
                    onValueChange = { vm.setAnalysisTimeoutSeconds(it.toInt()) },
                    valueRange = 10f..180f,
                    steps = 170
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // AI suggestions
                SettingsToggle(
                    title = "Show AI suggestions",
                    description = "Display AI-powered suggestions in the app",
                    checked = state.showAiSuggestions,
                    onCheckedChange = vm::setShowAiSuggestions
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Offline mode
                SettingsToggle(
                    title = "Offline mode",
                    description = "Work without internet using cached analyses",
                    checked = state.enableOfflineMode,
                    onCheckedChange = vm::setEnableOfflineMode
                )
            }

            // Cache Management
            SettingsSectionCard(title = "Cache Management", icon = Icons.Default.Storage) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Analysis Results", fontWeight = FontWeight.Medium)
                        Text(
                            "${state.analysisCount} analyses stored",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = vm::clearOldAnalyses,
                            enabled = state.analysisCount > 0
                        ) { Text("Clear Old") }
                        OutlinedButton(
                            onClick = { showClearCacheConfirm = true },
                            enabled = state.analysisCount > 0,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) { Text("Clear All") }
                    }
                }
            }

            // Import/Export
            SettingsSectionCard(title = "Import / Export", icon = Icons.Default.ImportExport) {
                SettingsToggle(
                    title = "Include AI analyses in export",
                    description = "Export analysis results along with settings",
                    checked = state.exportIncludeAiAnalysis,
                    onCheckedChange = vm::setExportIncludeAiAnalysis
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { exportLauncher.launch(SettingsExporter.generateExportFileName()) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Upload, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Export")
                    }
                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Import")
                    }
                }
                Text(
                    "Note: API keys are never exported. You must re-enter them after import.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Appearance
            SettingsSectionCard(title = "Appearance", icon = Icons.Default.Palette) {
                SettingsDropdown(
                    title = "Theme",
                    options = listOf("SYSTEM", "LIGHT", "DARK"),
                    selected = state.theme,
                    onSelect = vm::setTheme
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsDropdown(
                    title = "Language",
                    options = listOf("SYSTEM", "ENGLISH", "ARABIC"),
                    selected = state.language,
                    onSelect = vm::setLanguage
                )
            }

            // Notifications
            SettingsSectionCard(title = "Notifications", icon = Icons.Default.Notifications) {
                SettingsToggle(
                    title = "Enable notifications",
                    description = "Server monitoring and alert notifications",
                    checked = state.enableNotifications,
                    onCheckedChange = vm::setEnableNotifications
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsSlider(
                    title = "Monitoring interval: ${state.monitoringIntervalMs / 1000}s",
                    value = (state.monitoringIntervalMs / 1000).toFloat(),
                    onValueChange = { vm.setMonitoringInterval(it.toLong() * 1000) },
                    valueRange = 5f..60f,
                    steps = 55
                )
            }

            // Privacy
            SettingsSectionCard(title = "Privacy", icon = Icons.Default.Security) {
                SettingsToggle(
                    title = "Confirm before delete",
                    description = "Show confirmation dialog before deleting projects",
                    checked = state.confirmDelete,
                    onCheckedChange = vm::setConfirmDelete
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsToggle(
                    title = "Git tracking",
                    description = "Track Git repository information",
                    checked = state.enableGitTracking,
                    onCheckedChange = vm::setEnableGitTracking
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsToggle(
                    title = "Crash reporting",
                    description = "Send anonymous crash reports",
                    checked = state.enableCrashReporting,
                    onCheckedChange = vm::setEnableCrashReporting
                )
            }

            // Reset
            SettingsSectionCard(title = "Danger Zone", icon = Icons.Default.Warning) {
                Text(
                    "Reset all settings to their default values",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showResetConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.RestartAlt, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Reset All Settings")
                }
            }

            Text(
                "ProjectPilot v2.0",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDropdown(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        Text(title, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.replace("_", " ")) },
                        onClick = { onSelect(option); expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSlider(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int
) {
    Column {
        Text(title, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
