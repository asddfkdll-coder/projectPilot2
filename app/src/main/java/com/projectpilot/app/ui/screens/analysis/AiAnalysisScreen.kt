package com.projectpilot.app.ui.screens.analysis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAnalysisScreen(
    navController: NavController,
    projectId: Long,
    projectName: String,
    viewModel: AiAnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedType by remember { mutableStateOf("QUICK_SCAN") }

    LaunchedEffect(Unit) {
        viewModel.loadProject(projectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Analysis: $projectName") },
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
            Text("Analysis type", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            val types = listOf("QUICK_SCAN", "FULL", "ARCHITECTURE", "SECURITY", "PERFORMANCE", "CODE_QUALITY")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                types.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.replace("_", " ")) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.startAnalysis(projectId, projectName, selectedType) },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Start Analysis")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            uiState.result?.let { result ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Architecture: ${result.architectureSummary}")
                        Text("Files: ${result.totalFiles}")
                        Text("LOC: ${result.linesOfCode}")
                        Text("Security Score: ${result.securityScore}/100")
                        Text("Quality Score: ${result.codeQualityScore}/100")
                    }
                }
            }
        }
    }
}
