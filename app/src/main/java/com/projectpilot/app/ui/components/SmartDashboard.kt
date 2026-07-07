package com.projectpilot.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projectpilot.app.R
import com.projectpilot.app.domain.model.Project

/**
 * لوحة التحكم الذكية
 * 
 * تعرض إحصائيات المشاريع والمشاريع الأخيرة والمفضلة
 */
@Composable
fun SmartDashboard(
    projects: List<Project>,
    recentProjects: List<Project> = emptyList(),
    favoriteProjects: List<Project> = emptyList(),
    onProjectClick: (Long) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // قسم الإحصائيات
        StatisticsSection(projects)
        
        // قسم المشاريع المفضلة
        if (favoriteProjects.isNotEmpty()) {
            FavoritesSection(favoriteProjects, onProjectClick)
        }
        
        // قسم المشاريع الأخيرة
        if (recentProjects.isNotEmpty()) {
            RecentProjectsSection(recentProjects, onProjectClick)
        }
    }
}

/**
 * قسم الإحصائيات
 */
@Composable
private fun StatisticsSection(projects: List<Project>) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.all_projects),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(
                    icon = Icons.Default.FolderOpen,
                    label = "إجمالي",
                    value = projects.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                
                StatisticItem(
                    icon = Icons.Default.Star,
                    label = "مفضلة",
                    value = projects.count { it.isFavorite }.toString(),
                    modifier = Modifier.weight(1f)
                )
                
                StatisticItem(
                    icon = Icons.Default.Code,
                    label = "أنواع",
                    value = projects.map { it.type }.distinct().size.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * عنصر إحصائي
 */
@Composable
private fun StatisticItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * قسم المشاريع المفضلة
 */
@Composable
private fun FavoritesSection(
    favoriteProjects: List<Project>,
    onProjectClick: (Long) -> Unit
) {
    Column {
        Text(
            stringResource(R.string.favorites),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(favoriteProjects) { project ->
                ProjectChip(
                    project = project,
                    onClick = { onProjectClick(project.id) }
                )
            }
        }
    }
}

/**
 * قسم المشاريع الأخيرة
 */
@Composable
private fun RecentProjectsSection(
    recentProjects: List<Project>,
    onProjectClick: (Long) -> Unit
) {
    Column {
        Text(
            stringResource(R.string.recent_projects),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(recentProjects) { project ->
                ProjectChip(
                    project = project,
                    onClick = { onProjectClick(project.id) }
                )
            }
        }
    }
}

/**
 * شريط المشروع
 */
@Composable
private fun ProjectChip(
    project: Project,
    onClick: () -> Unit
) {
    ElevatedFilterChip(
        selected = false,
        onClick = onClick,
        label = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                if (project.isFavorite) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                Text(project.name)
            }
        },
        shape = RoundedCornerShape(8.dp)
    )
}
