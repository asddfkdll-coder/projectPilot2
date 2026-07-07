package com.projectpilot.app.ui.screens.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.projectpilot.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectScreen(
    onBack: () -> Unit,
    vm: AddProjectViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    var path by remember { mutableStateOf("") }

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
        Column(Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            Text(stringResource(R.string.folder_path), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = path,
                onValueChange = { path = it },
                placeholder = { Text(stringResource(R.string.choose_folder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.addSingle(path, onBack) }, enabled = path.isNotBlank()) {
                    Text(stringResource(R.string.add_this_folder))
                }
                OutlinedButton(onClick = { vm.scanTree(path) }, enabled = path.isNotBlank()) {
                    Text(stringResource(R.string.scan_tree))
                }
            }
            Spacer(Modifier.height(16.dp))

            if (state.scanning) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Text(stringResource(R.string.scanning), style = MaterialTheme.typography.bodySmall)
            }
            state.message?.let {
                Text(it, color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium)
            }

            if (state.items.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Button(onClick = { vm.importAll(); onBack() }) {
                    Text(stringResource(R.string.import_all) + " ${state.items.size}")
                }
                Spacer(Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.items) { item ->
                        ListItem(
                            headlineContent = { Text(item.dir.name, fontWeight = FontWeight.SemiBold) },
                            supportingContent = {
                                Column {
                                    Text("${item.result.type} · ${item.result.framework ?: "-"}")
                                    Text(item.dir.absolutePath,
                                        style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
