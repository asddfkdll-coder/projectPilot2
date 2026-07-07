package com.projectpilot.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

/**
 * Application settings repository.
 * Manages: cache settings, DB preferences, import/export, and application preferences.
 */
@Singleton
class AppSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val LANGUAGE = stringPreferencesKey("language")
        val ANALYSIS_AUTO_SAVE = booleanPreferencesKey("analysis_auto_save")
        val ANALYSIS_RETENTION_DAYS = intPreferencesKey("analysis_retention_days")
        val ENABLE_ANALYTICS = booleanPreferencesKey("enable_analytics")
        val ENABLE_CRASH_REPORTING = booleanPreferencesKey("enable_crash_reporting")
        val CONFIRM_DELETE = booleanPreferencesKey("confirm_delete")
        val DEFAULT_ANALYSIS_TYPE = stringPreferencesKey("default_analysis_type")
        val SHOW_AI_SUGGESTIONS = booleanPreferencesKey("show_ai_suggestions")
        val ENABLE_NOTIFICATIONS = booleanPreferencesKey("enable_notifications")
        val MONITORING_INTERVAL_MS = longPreferencesKey("monitoring_interval_ms")
        val MAX_RECENT_PROJECTS = intPreferencesKey("max_recent_projects")
        val ENABLE_GIT_TRACKING = booleanPreferencesKey("enable_git_tracking")
        val EXPORT_INCLUDE_AI_ANALYSIS = booleanPreferencesKey("export_include_ai_analysis")
        val EXPORT_INCLUDE_ENV = booleanPreferencesKey("export_include_env")
        val LAST_EXPORT_PATH = stringPreferencesKey("last_export_path")
        val LAST_IMPORT_PATH = stringPreferencesKey("last_import_path")
        val ANALYSIS_TIMEOUT_SECONDS = intPreferencesKey("analysis_timeout_seconds")
        val ENABLE_OFFLINE_MODE = booleanPreferencesKey("enable_offline_mode")
    }
    
    // ---- Theme Settings ----
    
    val theme: Flow<AppTheme> = context.appSettingsDataStore.data.map { prefs ->
        prefs[Keys.THEME]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() } ?: AppTheme.SYSTEM
    }
    
    suspend fun setTheme(theme: AppTheme) {
        context.appSettingsDataStore.edit { it[Keys.THEME] = theme.name }
    }
    
    // ---- Language Settings ----
    
    val language: Flow<AppLanguage> = context.appSettingsDataStore.data.map { prefs ->
        prefs[Keys.LANGUAGE]?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() } ?: AppLanguage.SYSTEM
    }
    
    suspend fun setLanguage(language: AppLanguage) {
        context.appSettingsDataStore.edit { it[Keys.LANGUAGE] = language.name }
    }
    
    // ---- Analysis Settings ----
    
    val analysisAutoSave: Flow<Boolean> = context.appSettingsDataStore.data.map { 
        it[Keys.ANALYSIS_AUTO_SAVE] != false 
    }
    
    suspend fun setAnalysisAutoSave(enabled: Boolean) {
        context.appSettingsDataStore.edit { it[Keys.ANALYSIS_AUTO_SAVE] = enabled }
    }
    
    val analysisRetentionDays: Flow<Int> = context.appSettingsDataStore.data.map { 
        it[Keys.ANALYSIS_RETENTION_DAYS] ?: 90 
    }
    
    suspend fun setAnalysisRetentionDays(days: Int) {
        context.appSettingsDataStore.edit { it[Keys.ANALYSIS_RETENTION_DAYS] = days }
    }
    
    val defaultAnalysisType: Flow<String> = context.appSettingsDataStore.data.map { 
        it[Keys.DEFAULT_ANALYSIS_TYPE] ?: "FULL" 
    }
    
    suspend fun setDefaultAnalysisType(type: String) {
        context.appSettingsDataStore.edit { it[Keys.DEFAULT_ANALYSIS_TYPE] = type }
    }
    
    val analysisTimeoutSeconds: Flow<Int> = context.appSettingsDataStore.data.map {
        it[Keys.ANALYSIS_TIMEOUT_SECONDS] ?: 60
    }
    
    suspend fun setAnalysisTimeoutSeconds(seconds: Int) {
        context.appSettingsDataStore.edit { it[Keys.ANALYSIS_TIMEOUT_SECONDS] = seconds }
    }
    
    // ---- Privacy Settings ----
    
    val enableAnalytics: Flow<Boolean> = context.appSettingsDataStore.data.map { 
        it[Keys.ENABLE_ANALYTICS] == true 
    }
    
    suspend fun setEnableAnalytics(enabled: Boolean) {
        context.appSettingsDataStore.edit { it[Keys.ENABLE_ANALYTICS] = enabled }
    }
    
    val enableCrashReporting: Flow<Boolean> = context.appSettingsDataStore.data.map { 
        it[Keys.ENABLE_CRASH_REPORTING] != false 
    }
    
    suspend fun setEnableCrashReporting(enabled: Boolean) {
        context.appSettingsDataStore.edit { it[Keys.ENABLE_CRASH_REPORTING] = enabled }
    }
    
    // ---- General Settings ----
    
    val confirmDelete: Flow<Boolean> = context.appSettingsDataStore.data.map { 
        it[Keys.CONFIRM_DELETE] != false 
    }
    
    suspend fun setConfirmDelete(enabled: Boolean) {
        context.appSettingsDataStore.edit { it[Keys.CONFIRM_DELETE] = enabled }
    }
    
    val showAiSuggestions: Flow<Boolean> = context.appSettingsDataStore.data.map { 
        it[Keys.SHOW_AI_SUGGESTIONS] != false 
    }
    
    suspend fun setShowAiSuggestions(enabled: Boolean) {
        context.appSettingsDataStore.edit { it[Keys.SHOW_AI_SUGGESTIONS] = enabled }
    }
    
    val enableNotifications: Flow<Boolean> = context.appSettingsDataStore.data.map { 
        it[Keys.ENABLE_NOTIFICATIONS] != false 
    }
    
    suspend fun setEnableNotifications(enabled: Boolean) {
        context.appSettingsDataStore.edit { it[Keys.ENABLE_NOTIFICATIONS] = enabled }
    }
    
    val monitoringIntervalMs: Flow<Long> = context.appSettingsDataStore.data.map { 
        it[Keys.MONITORING_INTERVAL_MS] ?: 5000L 
    }
    
    suspend fun setMonitoringIntervalMs(interval: Long) {
        context.appSettingsDataStore.edit { it[Keys.MONITORING_INTERVAL_MS] = interval }
    }
    
    val maxRecentProjects: Flow<Int> = context.appSettingsDataStore.data.map { 
        it[Keys.MAX_RECENT_PROJECTS] ?: 10 
    }
    
    suspend fun setMaxRecentProjects(max: Int) {
        context.appSettingsDataStore.edit { it[Keys.MAX_RECENT_PROJECTS] = max }
    }
    
    val enableGitTracking: Flow<Boolean> = context.appSettingsDataStore.data.map { 
        it[Keys.ENABLE_GIT_TRACKING] != false 
    }
    
    suspend fun setEnableGitTracking(enabled: Boolean) {
        context.appSettingsDataStore.edit { it[Keys.ENABLE_GIT_TRACKING] = enabled }
    }
    
    val enableOfflineMode: Flow<Boolean> = context.appSettingsDataStore.data.map {
        it[Keys.ENABLE_OFFLINE_MODE] == true
    }
    
    suspend fun setEnableOfflineMode(enabled: Boolean) {
        context.appSettingsDataStore.edit { it[Keys.ENABLE_OFFLINE_MODE] = enabled }
    }
    
    // ---- Export/Import Settings ----
    
    val exportIncludeAiAnalysis: Flow<Boolean> = context.appSettingsDataStore.data.map { 
        it[Keys.EXPORT_INCLUDE_AI_ANALYSIS] != false 
    }
    
    suspend fun setExportIncludeAiAnalysis(include: Boolean) {
        context.appSettingsDataStore.edit { it[Keys.EXPORT_INCLUDE_AI_ANALYSIS] = include }
    }
    
    val exportIncludeEnv: Flow<Boolean> = context.appSettingsDataStore.data.map { 
        it[Keys.EXPORT_INCLUDE_ENV] == true 
    }
    
    suspend fun setExportIncludeEnv(include: Boolean) {
        context.appSettingsDataStore.edit { it[Keys.EXPORT_INCLUDE_ENV] = include }
    }
    
    val lastExportPath: Flow<String?> = context.appSettingsDataStore.data.map { 
        it[Keys.LAST_EXPORT_PATH] 
    }
    
    suspend fun setLastExportPath(path: String) {
        context.appSettingsDataStore.edit { it[Keys.LAST_EXPORT_PATH] = path }
    }
    
    val lastImportPath: Flow<String?> = context.appSettingsDataStore.data.map { 
        it[Keys.LAST_IMPORT_PATH] 
    }
    
    suspend fun setLastImportPath(path: String) {
        context.appSettingsDataStore.edit { it[Keys.LAST_IMPORT_PATH] = path }
    }
    
    // ---- Reset All ----
    
    suspend fun resetToDefaults() {
        context.appSettingsDataStore.edit { it.clear() }
    }
}

enum class AppTheme { LIGHT, DARK, SYSTEM }
enum class AppLanguage { SYSTEM, ENGLISH, ARABIC }
