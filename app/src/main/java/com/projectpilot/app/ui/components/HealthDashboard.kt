package com.projectpilot.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projectpilot.app.R
import com.projectpilot.app.domain.model.Project
import java.text.SimpleDateFormat
import java.util.*

/**
 * لوحة تحكم صحة الخوادم (Health Dashboard)
 * 
 * توفر عرضاً موحداً لحالة جميع الخوادم النشطة مع:
 * - عرض المشاريع النشطة
 * - الإحصائيات السريعة
 * - التنبيهات عند توقف الخوادم
 */
@Composable
fun HealthDashboard(
    activeProjects: List<Project> = emptyList(),
    onProjectClick: (Project) -> Unit = {},
    onStopProject: (Project) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // رأس لوحة التحكم
        HealthDashboardHeader(activeProjects.size)

        if (activeProjects.isEmpty()) {
            EmptyHealthState()
        } else {
            // الإحصائيات السريعة
            QuickStatistics(activeProjects)

            // قائمة الخوادم النشطة
            ActiveServersList(
                projects = activeProjects,
                onProjectClick = onProjectClick,
                onStopProject = onStopProject
            )
        }
    }
}

@Composable
private fun HealthDashboardHeader(activeCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                "Health Dashboard",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Active servers: $activeCount",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // مؤشر الحالة
        StatusIndicator(activeCount > 0)
    }
}

@Composable
private fun StatusIndicator(isActive: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(8.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = if (isActive) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
            modifier = Modifier.size(12.dp)
        ) {}
        
        Text(
            if (isActive) "Active" else "Idle",
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
        )
    }
}

@Composable
private fun QuickStatistics(projects: List<Project>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatisticCard(
            label = "Active",
            value = projects.size.toString(),
            modifier = Modifier.weight(1f)
        )
        
        StatisticCard(
            label = "Types",
            value = projects.map { it.type }.distinct().size.toString(),
            modifier = Modifier.weight(1f)
        )
        
        val avgUptime = if (projects.isNotEmpty()) {
            val validStartTimes = projects.mapNotNull { it.lastStartedAt }
            if (validStartTimes.isNotEmpty()) {
                validStartTimes.average().toLong() / 60000
            } else 0L
        } else {
            0L
        }
        
        StatisticCard(
            label = "Avg Uptime",
            value = "${avgUptime}m",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatisticCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActiveServersList(
    projects: List<Project>,
    onProjectClick: (Project) -> Unit,
    onStopProject: (Project) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Active Servers",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            items(projects) { project ->
                ActiveServerCard(
                    project = project,
                    onProjectClick = { onProjectClick(project) },
                    onStopProject = { onStopProject(project) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveServerCard(
    project: Project,
    onProjectClick: () -> Unit,
    onStopProject: () -> Unit
) {
    Card(
        onClick = onProjectClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // أيقونة الحالة
            Surface(
                shape = MaterialTheme.shapes.small,
                color = Color(0xFF4CAF50),
                modifier = Modifier.size(12.dp)
            ) {}

            // معلومات المشروع
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    project.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${project.type} · PID: ${project.lastPid ?: "N/A"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                project.lastStartedAt?.let { startTime ->
                    val uptime = (System.currentTimeMillis() - startTime) / 1000 / 60
                    Text(
                        "Uptime: ${uptime}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // زر الإيقاف
            IconButton(
                onClick = onStopProject,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Stop,
                    contentDescription = "Stop",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EmptyHealthState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "All Servers Stopped",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "No active servers currently",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
