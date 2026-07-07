package com.projectpilot.app.ui.screens.knowledge

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.text.SimpleDateFormat
import java.util.*

/**
 * Offline Knowledge Screen - Searchable history of all AI analysis results.
 * No internet required to view reports.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineKnowledgeScreen(
    onBack: () -> Unit,
    onOpenAnalysis: (Long) -> Unit,
    onOpenProjectAnalyses: (Long) -> Unit,
    vm: OfflineKnowledgeViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadAllKnowledge() }
    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); vm.clearMessage() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Knowledge Base (${state.totalAnalyses})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    vm.search(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search analyses, technologies, architectures...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = ""; vm.loadAllKnowledge() }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                singleLine = true
            )

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatChip("${state.totalAnalyses} analyses", Icons.Default.Analytics)
                StatChip("${state.totalProjects} projects", Icons.Default.Folder)
                StatChip("${state.avgSecurityScore.toInt()} avg security", Icons.Default.Security)
                StatChip("${state.avgQualityScore.toInt()} avg quality", Icons.Default.Code)
            }

            // Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.filter == KnowledgeFilter.ALL,
                    onClick = { vm.setFilter(KnowledgeFilter.ALL) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = state.filter == KnowledgeFilter.RECENT,
                    onClick = { vm.setFilter(KnowledgeFilter.RECENT) },
                    label = { Text("Recent") }
                )
                FilterChip(
                    selected = state.filter == KnowledgeFilter.SECURITY_ISSUES,
                    onClick = { vm.setFilter(KnowledgeFilter.SECURITY_ISSUES) },
                    label = { Text("Security") }
                )
                FilterChip(
                    selected = state.filter == KnowledgeFilter.LOW_QUALITY,
                    onClick = { vm.setFilter(KnowledgeFilter.LOW_QUALITY) },
                    label = { Text("Low Quality") }
                )
            }

            // Results
            if (state.analyses.isEmpty()) {
                EmptyKnowledgeState(searchQuery.isNotEmpty())
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.analyses, key = { it.id }) { analysis ->
                        KnowledgeItem(
                            analysis = analysis,
                            onClick = { onOpenAnalysis(analysis.id) },
                            onViewProject = { onOpenProjectAnalyses(analysis.projectId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(4.dp))
            Text(text, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun KnowledgeItem(
    analysis: AiAnalysisResult,
    onClick: () -> Unit,
    onViewProject: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Analysis type indicator
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        analysis.analysisType.name.replace("_", " "),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    dateFormat.format(Date(analysis.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                analysis.projectName,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleSmall
            )
            
            if (!analysis.architectureSummary.isNullOrBlank()) {
                Text(
                    analysis.architectureSummary.take(100) + if (analysis.architectureSummary.length > 100) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Scores
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    analysis.securityScore?.let { ScorePill("S", it) }
                    analysis.codeQualityScore?.let { ScorePill("Q", it) }
                    analysis.performanceScore?.let { ScorePill("P", it) }
                }
                
                // Technologies
                val techs = analysis.getDetectedTechnologiesList()
                if (techs.isNotEmpty()) {
                    Text(
                        techs.take(3).joinToString(", "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                
                TextButton(onClick = onViewProject) {
                    Text("Project", style = MaterialTheme.typography.labelSmall)
                }
            }
            
            if (analysis.modelUsed != null) {
                Text(
                    "Generated by ${analysis.modelUsed} in ${analysis.generationTimeMs}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun ScorePill(label: String, score: Int) {
    val color = when {
        score >= 80 -> MaterialTheme.colorScheme.primary
        score >= 60 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            "$label$score",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun EmptyKnowledgeState(isSearching: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                if (isSearching) Icons.Default.SearchOff else Icons.Default.MenuBook,
                null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(16.dp))
            Text(
                if (isSearching) "No matching analyses found" else "No analysis history yet",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (isSearching) "Try a different search term" 
                else "Run AI analyses on your projects to build your knowledge base",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
