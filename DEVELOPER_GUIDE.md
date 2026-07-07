# دليل المطورين - ProjectPilot v1.5+

## مقدمة

هذا الدليل يوفر معلومات شاملة لمطوري ProjectPilot حول البنية المعمارية، أفضل الممارسات، والميزات الجديدة.

---

## 1. البنية المعمارية

### Clean Architecture + MVVM

```
ProjectPilot/
├── data/
│   ├── local/          # Room Database + DataStore
│   ├── repository/     # Data abstraction layer
│   ├── scanner/        # Project detection logic
│   ├── git/            # Git information extraction
│   └── backup/         # Backup & recovery
├── domain/
│   └── model/          # Business entities
├── ui/
│   ├── screens/        # Composable screens
│   ├── components/     # Reusable UI components
│   └── theme/          # Material 3 theming
├── service/            # Background services
├── security/           # Encryption & security
├── termux/             # Termux integration
└── utils/              # Utilities & helpers
```

### تدفق البيانات

```
UI (Composable)
    ↓
ViewModel (StateFlow)
    ↓
Repository (Abstraction)
    ↓
DAO / DataStore (Persistence)
```

---

## 2. الميزات الرئيسية

### 2.1 اكتشاف المشاريع الذكي (Smart Project Detection)

**الملف:** `data/scanner/`

يدعم اكتشاف 10 أنواع مشاريع:
- Node.js (React, Vue, Nuxt, Svelte)
- Python (Django, Flask, FastAPI, Streamlit)
- PHP (Laravel, Symfony)
- Java/Spring Boot
- Go
- Rust
- Docker
- .NET
- Ruby on Rails
- Static HTML

**كيفية الإضافة:**

```kotlin
// في Detectors.kt
class CustomDetector : ProjectDetector {
    override fun detect(dir: File): DetectionResult? {
        if (dir.resolve("custom.config").exists()) {
            return DetectionResult(
                type = ProjectType.CUSTOM,
                framework = "CustomFramework",
                installCmd = "custom install",
                runCmd = "custom run",
                defaultPort = 9000
            )
        }
        return null
    }
}
```

### 2.2 تكامل Termux (Termux Integration)

**الملف:** `termux/TermuxCommandRunner.kt`

تشغيل الأوامر في Termux بأمان:

```kotlin
val runner = TermuxCommandRunner(context)
runner.runCommand(
    command = "npm start",
    workdir = "/path/to/project",
    inBackground = true,
    onResult = { pid, output ->
        // معالجة النتيجة
    }
)
```

### 2.3 مراقبة الخوادم (Server Monitoring)

**الملف:** `service/ServerMonitorService.kt`

مراقبة مستمرة للعمليات النشطة:

```kotlin
// يتم تشغيل الخدمة تلقائياً عند:
// 1. بدء التطبيق
// 2. تشغيل خادم
// 3. إعادة تشغيل الجهاز
```

### 2.4 النسخ الاحتياطي المشفرة (Encrypted Backups)

**الملف:** `data/backup/BackupWorker.kt`

- تشفير AES-256-GCM
- نسخ احتياطية تلقائية كل 24 ساعة
- استخراج متغيرات `.env`
- مشاركة آمنة عبر FileProvider

---

## 3. الميزات الجديدة (v1.5+)

### 3.1 منتقي المشاريع الذكي (Smart Project Picker)

**الملف:** `ui/components/SmartProjectPicker.kt`

```kotlin
SmartProjectPicker(
    onProjectSelected = { path, name ->
        // معالجة المشروع المختار
    },
    onError = { message ->
        // معالجة الأخطاء
    }
)
```

**المميزات:**
- استخدام Storage Access Framework (SAF)
- اكتشاف تلقائي لنوع المشروع
- تذكر المشاريع الأخيرة

### 3.2 البحث الشامل (Search Everywhere)

**الملف:** `ui/components/SearchEverywhere.kt`

بحث موحد عن:
- المشاريع
- الأوامر والوصفات
- الإعدادات

```kotlin
SearchEverywhere(
    projects = projectList,
    recipes = recipeList,
    onProjectSelected = { project ->
        // التعامل مع اختيار المشروع
    }
)
```

### 3.3 لوحة تحكم الصحة (Health Dashboard)

**الملف:** `ui/components/HealthDashboard.kt`

عرض موحد لحالة الخوادم:
- عدد الخوادم النشطة
- الإحصائيات السريعة
- مدة التشغيل
- التنبيهات

```kotlin
HealthDashboard(
    activeProjects = activeList,
    onProjectClick = { project ->
        // فتح تفاصيل المشروع
    }
)
```

### 3.4 معالجة الأخطاء المحسّنة (Enhanced Error Handling)

**الملف:** `utils/ErrorHandlingEnhanced.kt`

```kotlin
val analyzer = EnhancedErrorAnalyzer()
val errorInfo = analyzer.analyzeError(
    exception = e,
    projectType = ProjectType.NODE
)

// عرض رسالة واضحة + اقتراحات للإصلاح
showErrorDialog(
    message = errorInfo.userMessage,
    suggestions = errorInfo.suggestions
)
```

### 3.5 مقترح الأوامر الذكي (Smart Command Suggester)

**الملف:** `utils/ErrorHandlingEnhanced.kt`

```kotlin
val suggester = SmartCommandSuggester()
val commands = suggester.suggestCommands(ProjectType.NODE)
// النتيجة: ["npm install", "npm start", "npm run dev", ...]
```

---

## 4. أفضل الممارسات

### 4.1 إضافة ميزة جديدة

**الخطوات:**

1. **أنشئ Model** في `domain/model/`
2. **أنشئ DAO** في `data/local/` (إذا كنت تحتاج قاعدة بيانات)
3. **أنشئ Repository** في `data/repository/`
4. **أنشئ ViewModel** في `ui/screens/`
5. **أنشئ Composable** في `ui/screens/`

### 4.2 معايير الكود

- **SOLID Principles**: Single Responsibility, Open/Closed, Liskov, Interface Segregation, Dependency Inversion
- **DRY**: لا تكرر الكود
- **KISS**: اجعل الحل بسيطاً
- **Naming**: استخدم أسماء واضحة ومعبرة

### 4.3 التعريب

جميع النصوص يجب أن تكون في `res/values/strings.xml`:

```xml
<string name="my_feature">اسم الميزة</string>
```

ثم استخدمها في الكود:

```kotlin
Text(stringResource(R.string.my_feature))
```

### 4.4 معالجة الأخطاء

```kotlin
try {
    // العملية
} catch (e: Exception) {
    val errorInfo = EnhancedErrorAnalyzer().analyzeError(e)
    // عرض الخطأ بشكل واضح
}
```

---

## 5. الاختبار

### 5.1 اختبار Unit Tests

```kotlin
@Test
fun testProjectDetection() {
    val detector = NodeDetector()
    val result = detector.detect(File("/path/to/node/project"))
    
    assertNotNull(result)
    assertEquals(ProjectType.NODE, result?.type)
}
```

### 5.2 اختبار Integration Tests

```kotlin
@Test
fun testAddProjectFlow() {
    val project = Project(
        name = "Test Project",
        path = "/test/path",
        type = ProjectType.NODE
    )
    
    repository.upsert(project)
    val retrieved = repository.getById(project.id)
    
    assertEquals(project, retrieved)
}
```

---

## 6. الأداء

### 6.1 تحسينات الذاكرة

- استخدام `LazyColumn` بدلاً من `Column` للقوائم الطويلة
- تجنب الحسابات الثقيلة في `Composable`
- استخدام `remember` و `mutableStateOf` بحذر

### 6.2 تحسينات الشبكة

- استخدام `Coroutines` للعمليات غير المتزامنة
- تجنب الطلبات المتكررة
- استخدام `Flow` للبيانات المتغيرة

### 6.3 تحسينات البطارية

- تقليل تكرار الفحوصات في الخدمات
- استخدام `WorkManager` للمهام المجدولة
- تجنب الاستيقاظ المتكرر للشاشة

---

## 7. الأمان

### 7.1 تشفير البيانات

```kotlin
val encryptionManager = EncryptionManager(context)
val encrypted = encryptionManager.encrypt("sensitive_data")
val decrypted = encryptionManager.decrypt(encrypted)
```

### 7.2 تطهير المسارات (Path Sanitization)

```kotlin
val sanitized = sanitizeWorkdir(userInput)
// يزيل الأحرف الخطرة التي قد تؤدي إلى Shell Injection
```

### 7.3 إدارة الصلاحيات

```kotlin
if (ContextCompat.checkSelfPermission(
    context,
    Manifest.permission.READ_EXTERNAL_STORAGE
) != PackageManager.PERMISSION_GRANTED) {
    // طلب الصلاحية
}
```

---

## 8. الخارطة الطريق

- [ ] دعم iOS عبر Kotlin Multiplatform
- [ ] نسخة Desktop (Compose for Desktop)
- [ ] مزامنة سحابية مشفرة
- [ ] اقتراحات أوامر بالذكاء الاصطناعي
- [ ] لوحة تحكم متقدمة للموارد
- [ ] محرر أكواد مدمج

---

## 9. الدعم والمساهمة

للمساهمة في المشروع:

1. Fork المستودع
2. أنشئ فرع جديد (`git checkout -b feature/my-feature`)
3. Commit التغييرات (`git commit -m 'Add my feature'`)
4. Push إلى الفرع (`git push origin feature/my-feature`)
5. فتح Pull Request

---

## 10. الأسئلة الشائعة

**س: كيف أضيف نوع مشروع جديد؟**
ج: أنشئ `Detector` جديد في `data/scanner/Detectors.kt` وأضفه إلى `ProjectScanner`.

**س: كيف أختبر الميزات الجديدة؟**
ج: استخدم Emulator أو جهاز حقيقي مع Termux مثبت.

**س: هل يمكن استخدام المشروع بدون Termux؟**
ج: نعم، لكن لن تتمكن من تشغيل الخوادم. يمكنك استخدام التطبيق لإدارة المشاريع فقط.

---

**آخر تحديث:** يوليو 2026
**الإصدار:** 1.5.0+
