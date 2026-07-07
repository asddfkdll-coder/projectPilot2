package com.projectpilot.app.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.projectpilot.app.domain.model.ProjectType
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDashboardScreen(
    onBack: () -> Unit,
    onNavigateToProject: (Long) -> Unit,
    onNavigateToAnalysis: (Long, String) -> Unit,
    vm: ProjectDashboardViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Control Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { vm.refresh() }) {
                        Icon(Icons.Default.Refresh, null)
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overview Cards
            item { OverviewCards(state) }
            
            // Score Gauges
            item { ScoreOverview(state) }
            
            // Technology Breakdown
            if (state.technologyBreakdown.isNotEmpty()) {
                item { TechnologyBreakdown(state.technologyBreakdown) }
            }
            
            // Security Distribution
            if (state.securityDistribution.isNotEmpty()) {
                item { ScoreDistribution("Security Distribution", state.securityDistribution) }
            }
            
            // Quality Distribution
            if (state.qualityDistribution.isNotEmpty()) {
                item { ScoreDistribution("Code Quality Distribution", state.qualityDistribution) }
            }
            
            // Project List Header
            item {
                Text(
                    "Projects (${state.totalProjects})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Project Status Cards
            items(state.projects, key = { it.project.id }) { projectStat ->
                ProjectStatusCard(
                    projectStat = projectStat,
                    onClick = { onNavigateToProject(projectStat.project.id) },
                    onAnalyze = { onNavigateToAnalysis(projectStat.project.id, projectStat.project.name) }
                )
            }
        }
    }
}

@Composable
private fun OverviewCards(state: ProjectDashboardUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OverviewCard(
            title = "Projects",
            value = state.totalProjects.toString(),
            icon = Icons.Default.Folder,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        OverviewCard(
            title = "Active",
            value = state.activeProjects.toString(),
            icon = Icons.Default.PlayArrow,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
        OverviewCard(
            title = "Analyses",
            value = state.totalAnalyses.toString(),
            icon = Icons.Default.Analytics,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun OverviewCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ScoreOverview(state: ProjectDashboardUiState) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Average Scores", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ScoreGauge(
                    label = "Security",
                    score = state.averageScores.avgSecurity?.toInt() ?: 0,
                    color = MaterialTheme.colorScheme.error
                )
                ScoreGauge(
                    label = "Quality",
                    score = state.averageScores.avgQuality?.toInt() ?: 0,
                    color = MaterialTheme.colorScheme.primary
                )
                ScoreGauge(
                    label = "Performance",
                    score = state.averageScores.avgPerformance?.toInt() ?: 0,
                    color = MaterialTheme.colorScheme.tertiary
                )
                ScoreGauge(
                    label = "Docs",
                    score = state.averageScores.avgDocumentation?.toInt() ?: 0,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun ScoreGauge(label: String, score: Int, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier.size(64.dp),
                color = color,
                trackColor = color.copy(alpha = 0.2f),
                strokeWidth = 6.dp
            )
            Text(
                score.toString(),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun TechnologyBreakdown(techBreakdown: Map<String, Int>) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Technologies", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(techBreakdown.toList().sortedByDescending { it.second }) { (tech, count) ->
                    AssistChip(
                        onClick = {},
                        label = { Text("$tech ($count)") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Code,
                                null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreDistribution(title: String, categories: List<ScoreCategory>) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { category ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(category.color),
                            modifier = Modifier.size(12.dp)
                        ) {}
                        Spacer(Modifier.width(8.dp))
                        Text(category.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        Text(
                            category.count.toString(),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectStatusCard(
    projectStat: DashboardProjectStats,
    onClick: () -> Unit,
    onAnalyze: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status indicator
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = if (projectStat.serverStatus == "running") Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                    modifier = Modifier.size(12.dp)
                ) {}
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        projectStat.project.name,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "${projectStat.project.type} ${projectStat.project.framework ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Mini score badges
                projectStat.latestAnalysis?.let { analysis ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        analysis.securityScore?.let { MiniBadge("S", it) }
                        analysis.codeQualityScore?.let { MiniBadge("Q", it) }
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (projectStat.serverStatus == "running") "Uptime: ${projectStat.uptimeMinutes}m" else "Not running",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                TextButton(onClick = onAnalyze) {
                    Icon(Icons.Default.Psychology, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Analyze")
                }
            }
            
            // Analysis summary if available
            projectStat.latestAnalysis?.let { analysis ->
                analysis.architectureSummary?.let { summary ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        summary.take(120) + if (summary.length > 120) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                val techs = analysis.getDetectedTechnologiesList()
                if (techs.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        techs.joinToString(", ").take(100),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniBadge(label: String, score: Int) {
    val bgColor = when {
        score >= 80 -> 0xFFE8F5E9
        score >= 60 -> 0xFFFFF3E0
        else -> 0xFFFFEBEE
    }
    val textColor = when {
        score >= 80 -> 0xFF2E7D32
        score >= 60 -> 0xFFEF6C00
        else -> 0xFFC62828
    }
    
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color(bgColor)
    ) {
        Text(
            "$label$score",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(textColor)
        )
    }
}
