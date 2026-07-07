package com.projectpilot.app.ui.screens.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.projectpilot.app.R
import com.projectpilot.app.data.local.RecentProject
import com.projectpilot.app.ui.components.SmartProjectPicker

/**
 * تحسين شاشة إضافة المشاريع مع دعم:
 * - اختيار ذكي للمجلدات عبر Storage Access Framework
 * - عرض المشاريع الأخيرة
 * - البحث والمسح المتقدم
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectScreenEnhanced(
    onBack: () -> Unit,
    vm: AddProjectViewModel = hiltViewModel(),
    recentProjects: List<RecentProject> = emptyList()
) {
    val state by vm.state.collectAsState()
    var path by remember { mutableStateOf("") }
    var showSmartPicker by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_scan_project)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // تبويبات الإضافة
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.add_this_folder)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.recent_projects)) }
                )
            }

            when (selectedTab) {
                0 -> AddManualTab(
                    path = path,
                    onPathChange = { path = it },
                    state = state,
                    vm = vm,
                    onBack = onBack,
                    onShowPicker = { showSmartPicker = true }
                )
                1 -> RecentProjectsTab(
                    recentProjects = recentProjects,
                    onProjectSelected = { projectPath ->
                        path = projectPath
                        selectedTab = 0
                    }
                )
            }
        }
    }

    // عرض منتقي المشاريع الذكي
    if (showSmartPicker) {
        SmartProjectPickerDialog(
            onProjectSelected = { projectPath, projectName ->
                path = projectPath
                showSmartPicker = false
            },
            onDismiss = { showSmartPicker = false }
        )
    }
}

@Composable
private fun AddManualTab(
    path: String,
    onPathChange: (String) -> Unit,
    state: AddUiState,
    vm: AddProjectViewModel,
    onBack: () -> Unit,
    onShowPicker: () -> Unit
) {
    Column(
        Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text(stringResource(R.string.folder_path), fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = path,
            onValueChange = onPathChange,
            placeholder = { Text(stringResource(R.string.choose_folder)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { vm.addSingle(path, onBack) },
                enabled = path.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.add_this_folder))
            }
            OutlinedButton(
                onClick = { vm.scanTree(path) },
                enabled = path.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.scan_tree))
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onShowPicker,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors()
        ) {
            Text(stringResource(R.string.choose_folder))
        }

        Spacer(Modifier.height(16.dp))

        if (state.scanning) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text(
                stringResource(R.string.scanning),
                style = MaterialTheme.typography.bodySmall
            )
        }

        state.message?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                it,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (state.items.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { vm.importAll(); onBack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.import_all) + " ${state.items.size}")
            }
            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.items) { item ->
                    ListItem(
                        headlineContent = {
                            Text(item.dir.name, fontWeight = FontWeight.SemiBold)
                        },
                        supportingContent = {
                            Column {
                                Text("${item.result.type} · ${item.result.framework ?: "-"}")
                                Text(
                                    item.dir.absolutePath,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun RecentProjectsTab(
    recentProjects: List<RecentProject>,
    onProjectSelected: (String) -> Unit
) {
    if (recentProjects.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.no_projects),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(recentProjects) { recent ->
                RecentProjectCard(
                    recent = recent,
                    onClick = { onProjectSelected(recent.projectPath) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentProjectCard(
    recent: RecentProject,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                recent.projectName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                recent.projectPath,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SmartProjectPickerDialog(
    onProjectSelected: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.choose_folder)) },
        text = {
            SmartProjectPicker(
                onProjectSelected = onProjectSelected,
                onError = { error ->
                    // معالجة الخطأ
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
