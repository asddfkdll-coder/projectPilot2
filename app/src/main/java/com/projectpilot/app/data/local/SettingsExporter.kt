package com.projectpilot.app.data.local

import android.content.Context
import android.net.Uri
import org.json.JSONObject
import java.io.OutputStreamWriter

class SettingsExporter(private val context: Context) {

    fun exportSettings(settings: Map<String, Any>, uri: Uri): Result<Unit> {
        return try {
            val json = JSONObject()
            settings.forEach { (key, value) ->
                json.put(key, value)
            }

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(json.toString(2))
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun importSettings(uri: Uri): Result<Map<String, Any>> {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonString)
                val map = mutableMapOf<String, Any>()
                json.keys().forEach { key ->
                    map[key] = json.get(key)
                }
                Result.success(map)
            } ?: Result.failure(IllegalStateException("Could not open file"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun generateExportFileName(): String {
        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
            .format(java.util.Date())
        return "projectpilot_settings_$timestamp.json"
    }
}
