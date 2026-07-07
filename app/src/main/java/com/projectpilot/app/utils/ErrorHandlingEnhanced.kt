package com.projectpilot.app.utils

import android.content.Context
import com.projectpilot.app.domain.model.ProjectType

/**
 * نظام معالجة الأخطاء المحسّن
 * 
 * يوفر:
 * - تصنيف الأخطاء حسب النوع والخطورة
 * - اقتراحات ذكية للإصلاح
 * - رسائل خطأ واضحة وموجهة للمستخدم
 */

enum class ErrorCategory {
    PERMISSION_ERROR,
    FILE_NOT_FOUND,
    INVALID_PATH,
    COMMAND_FAILED,
    TERMUX_NOT_INSTALLED,
    NETWORK_ERROR,
    STORAGE_ERROR,
    CONFIGURATION_ERROR,
    UNKNOWN
}

enum class ErrorSeverity {
    INFO,
    WARNING,
    ERROR,
    CRITICAL
}

data class ErrorInfo(
    val category: ErrorCategory,
    val severity: ErrorSeverity,
    val message: String,
    val userMessage: String,
    val suggestions: List<String> = emptyList(),
    val actionable: Boolean = false
)

/**
 * محلل الأخطاء المحسّن
 */
class EnhancedErrorAnalyzer {
    
    fun analyzeError(
        exception: Exception,
        context: String = "",
        projectType: ProjectType? = null
    ): ErrorInfo {
        val message = exception.message ?: exception.toString()
        
        return when {
            message.contains("Permission denied", ignoreCase = true) -> {
                ErrorInfo(
                    category = ErrorCategory.PERMISSION_ERROR,
                    severity = ErrorSeverity.ERROR,
                    message = message,
                    userMessage = "تم رفض الصلاحية المطلوبة",
                    suggestions = listOf(
                        "تحقق من صلاحيات التطبيق في إعدادات النظام",
                        "جرب منح الصلاحيات يدوياً من خلال الإعدادات",
                        "أعد تشغيل التطبيق بعد منح الصلاحيات"
                    ),
                    actionable = true
                )
            }
            
            message.contains("No such file", ignoreCase = true) ||
            message.contains("File not found", ignoreCase = true) -> {
                ErrorInfo(
                    category = ErrorCategory.FILE_NOT_FOUND,
                    severity = ErrorSeverity.ERROR,
                    message = message,
                    userMessage = "الملف أو المجلد غير موجود",
                    suggestions = listOf(
                        "تحقق من صحة المسار المدخل",
                        "تأكد من وجود المجلد على الجهاز",
                        "جرب اختيار المجلد من خلال منتقي الملفات"
                    ),
                    actionable = true
                )
            }
            
            message.contains("Invalid path", ignoreCase = true) ||
            message.contains("Invalid argument", ignoreCase = true) -> {
                ErrorInfo(
                    category = ErrorCategory.INVALID_PATH,
                    severity = ErrorSeverity.WARNING,
                    message = message,
                    userMessage = "المسار المدخل غير صحيح",
                    suggestions = listOf(
                        "تجنب استخدام أحرف خاصة في المسار",
                        "استخدم المسارات المطلقة بدلاً من النسبية",
                        "تأكد من عدم وجود مسافات غير ضرورية"
                    ),
                    actionable = true
                )
            }
            
            message.contains("Termux", ignoreCase = true) ||
            message.contains("RUN_COMMAND", ignoreCase = true) -> {
                ErrorInfo(
                    category = ErrorCategory.TERMUX_NOT_INSTALLED,
                    severity = ErrorSeverity.CRITICAL,
                    message = message,
                    userMessage = "Termux غير مثبت أو غير مفعل",
                    suggestions = listOf(
                        "ثبت Termux من F-Droid (وليس Google Play)",
                        "فعّل الأوامر الخارجية في Termux: echo 'allow-external-apps = true' >> ~/.termux/termux.properties",
                        "أعد تشغيل Termux بعد التفعيل"
                    ),
                    actionable = true
                )
            }
            
            message.contains("Connection", ignoreCase = true) ||
            message.contains("Network", ignoreCase = true) -> {
                ErrorInfo(
                    category = ErrorCategory.NETWORK_ERROR,
                    severity = ErrorSeverity.WARNING,
                    message = message,
                    userMessage = "خطأ في الاتصال بالشبكة",
                    suggestions = listOf(
                        "تحقق من اتصالك بالإنترنت",
                        "جرب إعادة تشغيل الجهاز",
                        "تأكد من عدم وجود جدار ناري يحجب الاتصال"
                    ),
                    actionable = true
                )
            }
            
            message.contains("Storage", ignoreCase = true) ||
            message.contains("Disk", ignoreCase = true) -> {
                ErrorInfo(
                    category = ErrorCategory.STORAGE_ERROR,
                    severity = ErrorSeverity.ERROR,
                    message = message,
                    userMessage = "خطأ في الوصول إلى التخزين",
                    suggestions = listOf(
                        "تحقق من مساحة التخزين المتاحة",
                        "حاول حذف الملفات غير الضرورية",
                        "تأكد من صلاحيات الوصول إلى مجلد التخزين"
                    ),
                    actionable = true
                )
            }
            
            else -> {
                ErrorInfo(
                    category = ErrorCategory.UNKNOWN,
                    severity = ErrorSeverity.ERROR,
                    message = message,
                    userMessage = "حدث خطأ غير متوقع",
                    suggestions = listOf(
                        "تحقق من سجل الأخطاء للمزيد من التفاصيل",
                        "جرب إعادة تشغيل التطبيق",
                        "تأكد من تحديث التطبيق إلى أحدث إصدار"
                    ),
                    actionable = false
                )
            }
        }
    }
}

/**
 * مقترح الأوامر الذكي
 */
class SmartCommandSuggester {
    
    fun suggestCommands(
        projectType: ProjectType,
        context: String = ""
    ): List<String> {
        return when (projectType) {
            ProjectType.NODE -> listOf(
                "npm install",
                "npm start",
                "npm run dev",
                "npm test",
                "npm run build"
            )
            
            ProjectType.PYTHON -> listOf(
                "pip install -r requirements.txt",
                "python manage.py runserver",
                "python app.py",
                "pytest",
                "python -m venv venv"
            )
            
            ProjectType.PHP -> listOf(
                "composer install",
                "php artisan serve",
                "php -S localhost:8000",
                "composer update",
                "php artisan migrate"
            )
            
            ProjectType.JAVA -> listOf(
                "mvn clean install",
                "mvn spring-boot:run",
                "gradle build",
                "gradle run",
                "mvn test"
            )
            
            ProjectType.GO -> listOf(
                "go mod download",
                "go run main.go",
                "go build",
                "go test ./...",
                "go mod tidy"
            )
            
            ProjectType.RUST -> listOf(
                "cargo build",
                "cargo run",
                "cargo test",
                "cargo build --release",
                "cargo fmt"
            )
            
            ProjectType.DOCKER -> listOf(
                "docker build -t myapp .",
                "docker run -p 8000:8000 myapp",
                "docker-compose up",
                "docker-compose down",
                "docker ps"
            )
            
            ProjectType.DOTNET -> listOf(
                "dotnet restore",
                "dotnet build",
                "dotnet run",
                "dotnet test",
                "dotnet publish"
            )
            
            ProjectType.RUBY -> listOf(
                "bundle install",
                "rails server",
                "rails db:migrate",
                "bundle exec rspec",
                "rails console"
            )
            
            ProjectType.STATIC_HTML -> listOf(
                "python -m http.server 8000",
                "php -S localhost:8000",
                "npx http-server",
                "npx serve"
            )
            
            ProjectType.UNKNOWN -> listOf(
                "ls -la",
                "pwd",
                "find . -name '*.json'",
                "find . -name '*.xml'"
            )
        }
    }
    
    fun suggestCommandForError(
        projectType: ProjectType,
        errorMessage: String
    ): List<String> {
        return when {
            errorMessage.contains("module not found", ignoreCase = true) ||
            errorMessage.contains("dependency", ignoreCase = true) -> {
                when (projectType) {
                    ProjectType.NODE -> listOf("npm install", "npm ci")
                    ProjectType.PYTHON -> listOf("pip install -r requirements.txt")
                    ProjectType.PHP -> listOf("composer install")
                    ProjectType.JAVA -> listOf("mvn clean install", "gradle build")
                    ProjectType.RUST -> listOf("cargo build")
                    else -> listOf()
                }
            }
            
            errorMessage.contains("port", ignoreCase = true) ||
            errorMessage.contains("already in use", ignoreCase = true) -> {
                listOf(
                    "lsof -i :8000",
                    "kill -9 <PID>",
                    "netstat -tulpn | grep LISTEN"
                )
            }
            
            errorMessage.contains("permission", ignoreCase = true) -> {
                listOf(
                    "chmod +x ./script.sh",
                    "sudo chown -R user:user .",
                    "chmod 755 ."
                )
            }
            
            else -> listOf()
        }
    }
}

/**
 * نظام التنبيهات الذكي
 */
class SmartAlertSystem {
    
    fun generateAlert(
        projectName: String,
        eventType: String,
        details: Map<String, Any> = emptyMap()
    ): String {
        return when (eventType) {
            "SERVER_DOWN" -> "⚠️ الخادم '$projectName' توقف بشكل غير متوقع"
            "HIGH_CPU" -> "🔥 استهلاك المعالج مرتفع في '$projectName': ${details["cpu"]}%"
            "HIGH_MEMORY" -> "💾 استهلاك الذاكرة مرتفع في '$projectName': ${details["memory"]}%"
            "REPEATED_FAILURES" -> "❌ فشل متكرر في '$projectName': ${details["attempts"]} محاولات"
            "SLOW_RESPONSE" -> "🐢 استجابة بطيئة من '$projectName': ${details["time"]}ms"
            else -> "📢 حدث حدث في '$projectName': $eventType"
        }
    }
}
