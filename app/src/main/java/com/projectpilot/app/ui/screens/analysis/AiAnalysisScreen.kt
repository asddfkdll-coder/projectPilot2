package com.projectpilot.app.ui.screens.analysis

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.projectpilot.app.domain.model.AiAnalysisResult
import com.projectpilot.app.domain.model.AnalysisType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAnalysisScreen(
    projectId: Long,
    projectName: String,
    onBack: () -> Unit,
    vm: AiAnalysisViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var selectedType by remember { mutableStateOf(AnalysisType.FULL) }
    var showTypeSelector by remember { mutableStateOf(false) }

    LaunchedEffect(projectId) {
        vm.loadProjectAnalyses(projectId)
        vm.checkProviders()
    }

    LaunchedEffect(state.message, state.error) {
        state.message?.let { snackbar.showSnackbar(it); vm.clearMessages() }
        state.error?.let { snackbar.showSnackbar(it); vm.clearMessages() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("AI Analysis: $projectName") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    if (!state.hasConfiguredProvider) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "No provider configured",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Analysis Type Selector
            AnalysisTypeSelector(
                selected = selectedType,
                onSelect = { selectedType = it }
            )

            // Run Analysis Button
            Button(
                onClick = { vm.runAnalysis(projectId, selectedType) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isAnalyzing && state.hasConfiguredProvider
            ) {
                if (state.isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(state.analysisProgress.ifEmpty { "Analyzing..." })
                } else {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Run ${selectedType.name.replace("_", " ")} Analysis")
                }
            }

            if (!state.hasConfiguredProvider) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "No AI Provider Configured",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                "Go to Settings > AI Providers to set up an AI provider.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Selected Analysis Detail
            AnimatedVisibility(visible = state.selectedAnalysis != null) {
                state.selectedAnalysis?.let { analysis ->
                    AnalysisDetailCard(analysis = analysis)
                }
            }

            // Analysis History
            Text(
                "Analysis History (${state.analyses.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (state.analyses.isEmpty()) {
                EmptyAnalysisState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.analyses, key = { it.id }) { analysis ->
                        AnalysisHistoryItem(
                            analysis = analysis,
                            isSelected = state.selectedAnalysis?.id == analysis.id,
                            onClick = { vm.selectAnalysis(analysis.id) },
                            onDelete = { vm.deleteAnalysis(analysis.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisTypeSelector(
    selected: AnalysisType,
    onSelect: (AnalysisType) -> Unit
) {
    val types = AnalysisType.values()
    Column {
        Text("Analysis Type", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            types.forEach { type ->
                FilterChip(
                    selected = selected == type,
                    onClick = { onSelect(type) },
                    label = { Text(type.name.replace("_", " "), style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
    }
}

@Composable
private fun AnalysisHistoryItem(
    analysis: AiAnalysisResult,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type icon
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        when (analysis.analysisType) {
                            AnalysisType.FULL -> Icons.Default.Assessment
                            AnalysisType.SECURITY -> Icons.Default.Security
                            AnalysisType.PERFORMANCE -> Icons.Default.Speed
                            AnalysisType.CODE_QUALITY -> Icons.Default.Code
                            AnalysisType.ARCHITECTURE -> Icons.Default.AccountTree
                            AnalysisType.QUICK_SCAN -> Icons.Default.FlashOn
                        },
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    analysis.analysisType.name.replace("_", " "),
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    dateFormat.format(Date(analysis.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (analysis.modelUsed != null) {
                    Text(
                        "${analysis.modelUsed} · ${analysis.generationTimeMs}ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            // Score badges
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                analysis.securityScore?.let { ScoreBadge("S", it, 0xFFF44336) }
                analysis.codeQualityScore?.let { ScoreBadge("Q", it, 0xFF4CAF50) }
                analysis.performanceScore?.let { ScoreBadge("P", it, 0xFF2196F3) }
            }
            
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun ScoreBadge(label: String, score: Int, color: Long) {
    val backgroundColor = if (score >= 80) 0xFFE8F5E9
    else if (score >= 60) 0xFFFFF3E0
    else 0xFFFFEBEE
    
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = androidx.compose.ui.graphics.Color(backgroundColor)
    ) {
        Text(
            "$label$score",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.ui.graphics.Color(color)
        )
    }
}

@Composable
private fun AnalysisDetailCard(analysis: AiAnalysisResult) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text("Analysis Results", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            
            // Score Overview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ScoreCircle("Security", analysis.securityScore ?: 0, MaterialTheme.colorScheme.error)
                ScoreCircle("Quality", analysis.codeQualityScore ?: 0, MaterialTheme.colorScheme.primary)
                ScoreCircle("Performance", analysis.performanceScore ?: 0, MaterialTheme.colorScheme.tertiary)
                ScoreCircle("Docs", analysis.documentationScore ?: 0, MaterialTheme.colorScheme.secondary)
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Architecture Summary
            analysis.architectureSummary?.let { summary ->
                Text("Architecture", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                Text(summary, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }
            
            // Technologies
            val techs = analysis.getDetectedTechnologiesList()
            if (techs.isNotEmpty()) {
                Text("Technologies", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    techs.take(8).forEach { tech ->
                        AssistChip(onClick = {}, label = { Text(tech, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            
            // Build Instructions
            analysis.buildInstructions?.let { build ->
                Text("Build", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        build,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
            
            // Security Issues
            val securityIssues = analysis.getSecurityIssuesList()
            if (securityIssues.isNotEmpty()) {
                Text(
                    "Security Issues (${analysis.criticalVulnerabilities} critical, ${analysis.warnings} warnings)",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error
                )
                securityIssues.take(3).forEach { issue ->
                    Text(
                        "${issue.severity}: ${issue.description}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
            
            // Recommendations
            val recommendations = analysis.getImprovementRecommendationsList()
            if (recommendations.isNotEmpty()) {
                Text("Top Recommendations", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                recommendations.take(3).forEach { rec ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            "${rec.priority}.",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Column {
                            Text(rec.title, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
                            Text(
                                rec.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun ScoreCircle(label: String, score: Int, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = androidx.compose.foundation.shape.CircleShape,
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    score.toString(),
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun EmptyAnalysisState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Analytics,
                null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(16.dp))
            Text("No analyses yet", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Run an AI analysis to see results here",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
