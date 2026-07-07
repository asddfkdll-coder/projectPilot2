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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projectpilot.app.R
import com.projectpilot.app.domain.model.Project
import com.projectpilot.app.domain.model.ProjectType

/**
 * مكون البحث الشامل (Search Everywhere)
 * 
 * يوفر واجهة بحث موحدة للوصول السريع إلى:
 * - المشاريع
 * - الأوامر والوصفات
 * - الإعدادات
 */
data class SearchResult(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val type: SearchResultType,
    val data: Any? = null
)

enum class SearchResultType {
    PROJECT, RECIPE, COMMAND, SETTING
}

@Composable
fun SearchEverywhere(
    projects: List<Project> = emptyList(),
    recipes: List<String> = emptyList(),
    onProjectSelected: (Project) -> Unit = {},
    onRecipeSelected: (String) -> Unit = {},
    onCommandSelected: (String) -> Unit = {},
    onSettingSelected: (String) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<SearchResult>>(emptyList()) }

    // تحديث النتائج عند تغيير الاستعلام
    LaunchedEffect(query, projects, recipes) {
        results = performSearch(query, projects, recipes)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // شريط البحث
        SearchBar(
            query = query,
            onQueryChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        // عرض النتائج
        if (query.isNotBlank()) {
            if (results.isEmpty()) {
                EmptySearchResults()
            } else {
                SearchResultsList(
                    results = results,
                    onProjectSelected = onProjectSelected,
                    onRecipeSelected = onRecipeSelected,
                    onCommandSelected = onCommandSelected,
                    onSettingSelected = onSettingSelected
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text(stringResource(R.string.search)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
private fun SearchResultsList(
    results: List<SearchResult>,
    onProjectSelected: (Project) -> Unit,
    onRecipeSelected: (String) -> Unit,
    onCommandSelected: (String) -> Unit,
    onSettingSelected: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(results) { result ->
            SearchResultItem(
                result = result,
                onProjectSelected = onProjectSelected,
                onRecipeSelected = onRecipeSelected,
                onCommandSelected = onCommandSelected,
                onSettingSelected = onSettingSelected
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchResultItem(
    result: SearchResult,
    onProjectSelected: (Project) -> Unit,
    onRecipeSelected: (String) -> Unit,
    onCommandSelected: (String) -> Unit,
    onSettingSelected: (String) -> Unit
) {
    val onClick = {
        when (result.type) {
            SearchResultType.PROJECT -> {
                (result.data as? Project)?.let { onProjectSelected(it) }
            }
            SearchResultType.RECIPE -> {
                onRecipeSelected(result.id)
            }
            SearchResultType.COMMAND -> {
                onCommandSelected(result.id)
            }
            SearchResultType.SETTING -> {
                onSettingSelected(result.id)
            }
        }
    }

    ListItem(
        modifier = Modifier.fillMaxWidth(),
        headlineContent = { Text(result.title, fontWeight = FontWeight.Medium) },
        supportingContent = result.subtitle?.let { { Text(it) } },
        leadingContent = { Icon(result.icon, contentDescription = null) },
        trailingContent = {
            Text(
                result.type.name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    )
}

@Composable
private fun EmptySearchResults() {
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
                Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.no_projects_found),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * تنفيذ خوارزمية البحث
 */
private fun performSearch(
    query: String,
    projects: List<Project>,
    recipes: List<String>
): List<SearchResult> {
    val results = mutableListOf<SearchResult>()

    if (query.isBlank()) return results

    val lowerQuery = query.lowercase()

    // البحث في المشاريع
    projects.forEach { project ->
        if (project.name.lowercase().contains(lowerQuery) ||
            project.path.lowercase().contains(lowerQuery) ||
            project.framework?.lowercase()?.contains(lowerQuery) == true
        ) {
            results.add(
                SearchResult(
                    id = project.id.toString(),
                    title = project.name,
                    subtitle = "${project.type} · ${project.framework ?: "-"}",
                    icon = getProjectTypeIcon(project.type),
                    type = SearchResultType.PROJECT,
                    data = project
                )
            )
        }
    }

    // البحث في الوصفات
    recipes.forEach { recipe ->
        if (recipe.lowercase().contains(lowerQuery)) {
            results.add(
                SearchResult(
                    id = recipe,
                    title = recipe,
                    icon = Icons.Default.Terminal,
                    type = SearchResultType.RECIPE
                )
            )
        }
    }

    return results.sortedBy { it.title }
}

/**
 * الحصول على أيقونة نوع المشروع
 */
private fun getProjectTypeIcon(type: ProjectType): ImageVector {
    return when (type) {
        ProjectType.NODE -> Icons.Default.Code
        ProjectType.PYTHON -> Icons.Default.Code
        ProjectType.PHP -> Icons.Default.Code
        ProjectType.JAVA -> Icons.Default.Code
        ProjectType.GO -> Icons.Default.Code
        ProjectType.RUST -> Icons.Default.Code
        ProjectType.DOCKER -> Icons.Default.Storage
        ProjectType.DOTNET -> Icons.Default.Code
        ProjectType.RUBY -> Icons.Default.Code
        ProjectType.STATIC_HTML -> Icons.Default.Code
        ProjectType.UNKNOWN -> Icons.Default.HelpOutline
    }
}
