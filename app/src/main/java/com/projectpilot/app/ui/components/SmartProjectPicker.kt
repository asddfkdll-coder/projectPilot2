package com.projectpilot.app.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.projectpilot.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Smart Project Picker Component
 * 
 * يوفر واجهة ذكية لاختيار المشاريع باستخدام Storage Access Framework
 * مع دعم الاكتشاف التلقائي لنوع المشروع
 */
@Composable
fun SmartProjectPicker(
    onProjectSelected: (projectPath: String, projectName: String) -> Unit,
    onError: (message: String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            isProcessing = true
            
            scope.launch {
                try {
                    val projectPath = getPathFromUri(context, uri)
                    val projectName = getProjectNameFromUri(context, uri)
                    
                    withContext(Dispatchers.Main) {
                        isProcessing = false
                        if (projectPath != null && projectName != null) {
                            onProjectSelected(projectPath, projectName)
                        } else {
                            onError("فشل في الحصول على مسار المشروع")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        isProcessing = false
                        onError("خطأ: ${e.message}")
                    }
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { folderPickerLauncher.launch(null) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isProcessing
        ) {
            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                if (isProcessing) stringResource(R.string.loading) 
                else stringResource(R.string.choose_folder)
            )
        }
        
        if (isProcessing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        
        selectedUri?.let { uri ->
            Text(
                "المسار المختار: $uri",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * استخراج المسار الفعلي من URI
 * يحاول تحويل DocumentTree URI إلى مسار حقيقي إن أمكن.
 * يستخدم getExternalFilesDir() بدلاً من getExternalStorageDirectory() المهجور.
 */
private suspend fun getPathFromUri(context: Context, uri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            val docId = android.provider.DocumentsContract.getTreeDocumentId(uri)
            
            // إذا كان من الذاكرة الخارجية، نستخدم المسار الحديث الآمن
            if (docId.startsWith("primary:")) {
                val path = docId.substring("primary:".length)
                val externalDir = context.getExternalFilesDir(null)?.parentFile?.parentFile?.parentFile?.parentFile
                return@withContext externalDir?.let { "${it.absolutePath}/$path" }
            }
            
            null
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * استخراج اسم المشروع من URI
 */
private suspend fun getProjectNameFromUri(context: Context, uri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(android.provider.DocumentsContract.Document.COLUMN_DISPLAY_NAME),
                null,
                null,
                null
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    return@withContext it.getString(0)
                }
            }
            
            null
        } catch (e: Exception) {
            null
        }
    }
}
