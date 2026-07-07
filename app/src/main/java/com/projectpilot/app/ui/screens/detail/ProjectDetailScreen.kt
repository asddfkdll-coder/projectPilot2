package com.projectpilot.app.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import com.projectpilot.app.R
import com.projectpilot.app.domain.model.Project

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: Long,
    onBack: () -> Unit,
    onGit: (Long) -> Unit,
    onRecipes: (Long) -> Unit,
    onAiAnalysis: ((Long) -> Unit)? = null,
    vm: ProjectDetailViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showEnvEditor by remember { mutableStateOf(false) }

    LaunchedEffect(projectId) { vm.load(projectId) }

    if (showDeleteConfirmation) {
        DeleteConfirmationDialog(
            projectName = state.project?.name ?: "Project",
            onConfirm = {
                vm.deleteProject()
                showDeleteConfirmation = false
                onBack()
            },
            onDismiss = { showDeleteConfirmation = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.project?.name ?: "Project") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    onAiAnalysis?.let { onAnalyze ->
                        IconButton(onClick = { onAnalyze(projectId) }) {
                            Icon(Icons.Default.Psychology, contentDescription = "AI Analysis")
                        }
                    }
                    IconButton(onClick = { onGit(projectId) }) {
                        Icon(Icons.Default.Info, contentDescription = "Git Info")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            state.project?.let { project ->
                // Project Information Card
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.project_details), fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        
                        InfoRow("Type", project.type.name)
                        InfoRow("Framework", project.framework ?: "-")
                        InfoRow("Path", project.path)
                        InfoRow("Port", project.defaultPort?.toString() ?: "-")
                    }
                }

                // Commands Card
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.run_command), fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { vm.runInstall() },
                                modifier = Modifier.weight(1f),
                                enabled = project.installCommand != null
                            ) {
                                Icon(Icons.Default.Download, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.install_command))
                            }
                            
                            Button(
                                onClick = { vm.runServer(background = false) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PlayArrow, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.run_command))
                            }
                            
                            Button(
                                onClick = { vm.runServer(background = true) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Cloud, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("خلفية")
                            }
                        }
                        
                        if (project.lastPid != null) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { vm.stopServer() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Stop, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.delete_project) + " (PID: ${project.lastPid})")
                            }
                        }
                    }
                }

                // Environment Variables Card
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(".env (" + stringResource(R.string.backup_enabled) + ")", fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { showEnvEditor = !showEnvEditor }) {
                                Icon(
                                    if (showEnvEditor) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    null
                                )
                            }
                        }
                        
                        if (showEnvEditor) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = state.env,
                                onValueChange = { vm.saveEnv(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                placeholder = { Text("KEY=value") },
                                readOnly = false
                            )
                            Spacer(Modifier.height(8.dp))
                            // .env is now directly editable in the field above
                        }
                    }
                }

                // Custom Commands Card
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.run_command), fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        
                        var customCmd by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = customCmd,
                            onValueChange = { customCmd = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("npm run build") },
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { vm.runCustom(customCmd) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = customCmd.isNotBlank()
                        ) {
                            Text(stringResource(R.string.open))
                        }
                    }
                }

                // Recipes Button
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Button(
                            onClick = { onRecipes(projectId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.MenuBook, null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.recipes))
                        }
                    }
                }

                // Status Message
                state.message?.let {
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        action = {
                            TextButton(onClick = { vm.clearMessage() }) {
                                Text(stringResource(R.string.close))
                            }
                        }
                    ) {
                        Text(it)
                    }
                }

                // Delete Button
                OutlinedButton(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.delete_project))
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DeleteConfirmationDialog(
    projectName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("${stringResource(R.string.delete_confirm)}: $projectName")
        },
        text = {
            Text("${stringResource(R.string.delete_project)} \"$projectName\"? ${stringResource(R.string.delete_confirm)}")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
