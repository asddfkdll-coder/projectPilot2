package com.projectpilot.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

/**
 * مدير المشاريع الأخيرة
 *
 * يتابع المشاريع التي تم فتحها مؤخراً لتسهيل الوصول السريع إليها
 */
private val Context.recentProjectsDataStore by preferencesDataStore(name = "recent_projects")

class RecentProjectsManager(private val context: Context) {

    companion object {
        private val RECENT_PROJECTS_KEY = stringSetPreferencesKey("recent_projects_set")
        private const val MAX_RECENT_PROJECTS = 5
    }

    /**
     * إضافة مشروع إلى قائمة المشاريع الأخيرة
     */
    suspend fun addRecentProject(projectId: Long, projectName: String, projectPath: String) {
        context.recentProjectsDataStore.edit { preferences ->
            val currentRecent = preferences[RECENT_PROJECTS_KEY] ?: emptySet()

            // إنشاء سجل المشروع الأخير
            val recentEntry = "$projectId|$projectName|$projectPath|${Instant.now().toEpochMilli()}"

            // إضافة المشروع الجديد وإزالة الأقدم إذا تجاوزنا الحد الأقصى
            val updatedRecent = (currentRecent + recentEntry)
                .sortedByDescending { entry ->
                    entry.substringAfterLast("|").toLongOrNull() ?: 0L
                }
                .take(MAX_RECENT_PROJECTS)
                .toSet()

            preferences[RECENT_PROJECTS_KEY] = updatedRecent
        }
    }

    /**
     * الحصول على قائمة المشاريع الأخيرة
     */
    fun getRecentProjects(): Flow<List<RecentProject>> {
        return context.recentProjectsDataStore.data.map { preferences ->
            val recentSet = preferences[RECENT_PROJECTS_KEY] ?: emptySet()

            recentSet.mapNotNull { entry ->
                try {
                    val parts = entry.split("|")
                    if (parts.size >= 4) {
                        RecentProject(
                            projectId = parts[0].toLong(),
                            projectName = parts[1],
                            projectPath = parts[2],
                            lastAccessTime = parts[3].toLong()
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }.sortedByDescending { it.lastAccessTime }
        }
    }

    /**
     * مسح المشاريع الأخيرة
     */
    suspend fun clearRecentProjects() {
        context.recentProjectsDataStore.edit { preferences ->
            preferences[RECENT_PROJECTS_KEY] = emptySet()
        }
    }
}

/**
 * نموذج بيانات المشروع الأخير
 */
data class RecentProject(
    val projectId: Long,
    val projectName: String,
    val projectPath: String,
    val lastAccessTime: Long
)
