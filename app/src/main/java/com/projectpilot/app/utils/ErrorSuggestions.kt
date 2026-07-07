package com.projectpilot.app.utils

/**
 * نظام الاقتراحات الذكية للأخطاء
 *
 * يحلل رسائل الخطأ ويقدم حلول مقترحة للمستخدم
 */
object ErrorSuggestions {

    /**
     * الحصول على اقتراح حل لرسالة خطأ
     */
    fun getSuggestion(errorMessage: String): String? {
        return when {
            // أخطاء Termux
            errorMessage.contains("Termux not installed", ignoreCase = true) ->
                "تأكد من تثبيت Termux من F-Droid وليس من Play Store"

            errorMessage.contains("permission denied", ignoreCase = true) ->
                "تحقق من صلاحيات الوصول للملفات في إعدادات التطبيق"

            // أخطاء npm/yarn
            errorMessage.contains("npm ERR!", ignoreCase = true) ->
                "جرب تنظيف ذاكرة التخزين المؤقت: npm cache clean --force"

            errorMessage.contains("EACCES", ignoreCase = true) ->
                "قد تحتاج إلى صلاحيات إدارية. جرب: sudo npm install"

            // أخطاء Python
            errorMessage.contains("ModuleNotFoundError", ignoreCase = true) ->
                "قد تكون المكتبة غير مثبتة. جرب: pip install -r requirements.txt"

            errorMessage.contains("No module named", ignoreCase = true) ->
                "تأكد من تثبيت جميع المتطلبات: pip install -r requirements.txt"

            // أخطاء Git
            errorMessage.contains("fatal: not a git repository", ignoreCase = true) ->
                "هذا المجلد ليس مستودع Git. جرب: git init"

            errorMessage.contains("fatal: pathspec", ignoreCase = true) ->
                "تأكد من وجود الملف المطلوب في المستودع"

            // أخطاء الشبكة
            errorMessage.contains("Connection refused", ignoreCase = true) ->
                "تحقق من أن الخادم يعمل على المنفذ المحدد"

            errorMessage.contains("Network is unreachable", ignoreCase = true) ->
                "تحقق من اتصالك بالإنترنت"

            // أخطاء الملفات
            errorMessage.contains("No such file or directory", ignoreCase = true) ->
                "تأكد من أن المسار صحيح وأن الملف موجود"

            errorMessage.contains("Permission denied", ignoreCase = true) ->
                "قد لا تملك صلاحيات كافية لتنفيذ هذا الإجراء"

            // أخطاء الذاكرة
            errorMessage.contains("out of memory", ignoreCase = true) ->
                "المشروع يستهلك ذاكرة كبيرة. جرب إغلاق التطبيقات الأخرى"

            errorMessage.contains("ENOMEM", ignoreCase = true) ->
                "لا توجد ذاكرة كافية. حرر بعض المساحة"

            // أخطاء الحزم
            errorMessage.contains("package.json", ignoreCase = true) ->
                "تأكد من وجود ملف package.json في مجلد المشروع"

            errorMessage.contains("Unexpected token", ignoreCase = true) ->
                "قد يكون هناك خطأ في صيغة الملف. تحقق من الأقواس والفواصل"

            else -> null
        }
    }

    /**
     * الحصول على قائمة خطوات استكشاف الأخطاء
     */
    fun getTroubleshootingSteps(errorType: String): List<String> {
        return when {
            errorType.contains("npm", ignoreCase = true) -> listOf(
                "1. تحقق من تثبيت Node.js و npm",
                "2. نظف ذاكرة التخزين المؤقت: npm cache clean --force",
                "3. احذف node_modules و package-lock.json",
                "4. أعد التثبيت: npm install"
            )

            errorType.contains("python", ignoreCase = true) -> listOf(
                "1. تحقق من تثبيت Python",
                "2. تحقق من الإصدار: python --version",
                "3. أعد تثبيت المتطلبات: pip install -r requirements.txt",
                "4. جرب بيئة افتراضية: python -m venv venv"
            )

            errorType.contains("git", ignoreCase = true) -> listOf(
                "1. تحقق من أن المجلد مستودع Git: git status",
                "2. تحقق من الاتصال بالخادم البعيد: git remote -v",
                "3. جرب: git fetch origin",
                "4. تحقق من الفروع: git branch -a"
            )

            else -> listOf(
                "1. تحقق من رسالة الخطأ بعناية",
                "2. ابحث عن الخطأ على الإنترنت",
                "3. تحقق من سجلات النظام",
                "4. جرب إعادة تشغيل التطبيق"
            )
        }
    }

    /**
     * تصنيف شدة الخطأ
     */
    fun getErrorSeverity(errorMessage: String): ErrorSeverity {
        return when {
            errorMessage.contains("fatal", ignoreCase = true) -> ErrorSeverity.CRITICAL
            errorMessage.contains("error", ignoreCase = true) -> ErrorSeverity.ERROR
            errorMessage.contains("warning", ignoreCase = true) -> ErrorSeverity.WARNING
            errorMessage.contains("info", ignoreCase = true) -> ErrorSeverity.INFO
            else -> ErrorSeverity.ERROR
        }
    }
}
