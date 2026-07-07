package com.projectpilot.app.domain.model

/**
 * نموذج المشروع المحسّن
 * 
 * إضافة حقول جديدة لتحسين المراقبة والتتبع:
 * - lastStartedAt: وقت آخر تشغيل للخادم
 * - serverStatus: حالة الخادم (نشط/متوقف)
 * - errorCount: عدد الأخطاء المسجلة
 * - lastError: آخر رسالة خطأ
 * - autoRestartEnabled: تفعيل إعادة التشغيل التلقائي
 */

enum class ServerStatus {
    RUNNING,      // الخادم يعمل
    STOPPED,      // الخادم متوقف
    FAILED,       // فشل الخادم
    RESTARTING,   // الخادم يعيد التشغيل
    UNKNOWN       // حالة غير معروفة
}

/**
 * امتداد لنموذج Project مع ميزات المراقبة
 */
data class ProjectMonitoring(
    val projectId: Long,
    val lastStartedAt: Long? = null,        // وقت آخر تشغيل (milliseconds)
    val serverStatus: ServerStatus = ServerStatus.STOPPED,
    val errorCount: Int = 0,                // عدد الأخطاء المسجلة
    val lastError: String? = null,          // آخر رسالة خطأ
    val autoRestartEnabled: Boolean = false, // تفعيل إعادة التشغيل التلقائي
    val restartAttempts: Int = 0,           // عدد محاولات إعادة التشغيل
    val maxRestartAttempts: Int = 3,        // الحد الأقصى لمحاولات إعادة التشغيل
    val lastHealthCheck: Long? = null       // آخر فحص صحة
)

/**
 * معلومات الأداء للمشروع
 */
data class ProjectPerformance(
    val projectId: Long,
    val cpuUsage: Float = 0f,               // استهلاك المعالج (%)
    val memoryUsage: Float = 0f,            // استهلاك الذاكرة (%)
    val uptime: Long = 0L,                  // مدة التشغيل (milliseconds)
    val requestsPerSecond: Float = 0f,      // عدد الطلبات في الثانية
    val averageResponseTime: Long = 0L,     // متوسط وقت الاستجابة (ms)
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * تنبيهات المشروع
 */
data class ProjectAlert(
    val id: String,
    val projectId: Long,
    val type: AlertType,
    val severity: AlertSeverity,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false
)

enum class AlertType {
    SERVER_DOWN,           // الخادم توقف
    HIGH_CPU,              // استهلاك معالج مرتفع
    HIGH_MEMORY,           // استهلاك ذاكرة مرتفع
    REPEATED_FAILURES,     // فشل متكرر
    SLOW_RESPONSE,         // استجابة بطيئة
    DISK_SPACE_LOW,        // مساحة القرص منخفضة
    CUSTOM                 // تنبيه مخصص
}

enum class AlertSeverity {
    INFO,
    WARNING,
    ERROR,
    CRITICAL
}

/**
 * سجل الأحداث للمشروع
 */
data class ProjectEventLog(
    val id: String,
    val projectId: Long,
    val eventType: EventType,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val details: Map<String, String> = emptyMap()
)

enum class EventType {
    SERVER_STARTED,
    SERVER_STOPPED,
    SERVER_CRASHED,
    COMMAND_EXECUTED,
    ERROR_OCCURRED,
    CONFIG_CHANGED,
    BACKUP_CREATED,
    CUSTOM
}
