package com.projectpilot.app.data.scanner

import com.projectpilot.app.domain.model.ProjectType
import java.io.File

/**
 * كاشف نوع المشروع الذكي
 * 
 * يستخدم عدة استراتيجيات لاكتشاف نوع المشروع بدقة أعلى.
 * يستخدم caching داخلي لتجنب القراءات المتكررة للملفات.
 */
class ProjectAutoDetector {
    
    private val fileContentCache = mutableMapOf<String, String>()
    private val fileExistsCache = mutableMapOf<String, Boolean>()
    
    private fun File.hasFileCached(pattern: String): Boolean {
        val cacheKey = this.absolutePath + "/" + pattern
        return fileExistsCache.getOrPut(cacheKey) { this.hasFile(pattern) }
    }
    
    private fun File.readTextCached(filename: String): String? {
        val cacheKey = this.absolutePath + "/" + filename
        return fileContentCache.getOrPut(cacheKey) {
            this.resolve(filename).takeIf { it.exists() }?.readText() ?: ""
        }.takeIf { it.isNotEmpty() }
    }
    
    /**
     * اكتشاف نوع المشروع من خلال البحث عن ملفات التكوين المميزة
     */
    fun detectProjectType(projectDir: File): ProjectType {
        // البحث عن ملفات التكوين المشهورة
        return when {
            // Node.js / JavaScript
            projectDir.hasFile("package.json") -> ProjectType.NODE
            projectDir.hasFile("yarn.lock") -> ProjectType.NODE
            projectDir.hasFile("pnpm-lock.yaml") -> ProjectType.NODE
            
            // Python
            projectDir.hasFile("requirements.txt") -> ProjectType.PYTHON
            projectDir.hasFile("setup.py") -> ProjectType.PYTHON
            projectDir.hasFile("pyproject.toml") -> ProjectType.PYTHON
            projectDir.hasFile("Pipfile") -> ProjectType.PYTHON
            projectDir.hasFile("poetry.lock") -> ProjectType.PYTHON
            
            // PHP
            projectDir.hasFile("composer.json") -> ProjectType.PHP
            projectDir.hasFile("composer.lock") -> ProjectType.PHP
            projectDir.hasFile("wp-config.php") -> ProjectType.PHP
            
            // Java
            projectDir.hasFile("pom.xml") -> ProjectType.JAVA
            projectDir.hasFile("build.gradle") -> ProjectType.JAVA
            projectDir.hasFile("build.gradle.kts") -> ProjectType.JAVA
            projectDir.hasFile("settings.gradle") -> ProjectType.JAVA
            
            // Go
            projectDir.hasFile("go.mod") -> ProjectType.GO
            projectDir.hasFile("go.sum") -> ProjectType.GO
            
            // Rust
            projectDir.hasFile("Cargo.toml") -> ProjectType.RUST
            projectDir.hasFile("Cargo.lock") -> ProjectType.RUST
            
            // Docker
            projectDir.hasFile("Dockerfile") -> ProjectType.DOCKER
            projectDir.hasFile("docker-compose.yml") -> ProjectType.DOCKER
            projectDir.hasFile("docker-compose.yaml") -> ProjectType.DOCKER
            
            // .NET
            projectDir.hasFile("*.csproj") -> ProjectType.DOTNET
            projectDir.hasFile("*.fsproj") -> ProjectType.DOTNET
            projectDir.hasFile("*.vbproj") -> ProjectType.DOTNET
            projectDir.hasFile("*.sln") -> ProjectType.DOTNET
            
            // Ruby
            projectDir.hasFile("Gemfile") -> ProjectType.RUBY
            projectDir.hasFile("Gemfile.lock") -> ProjectType.RUBY
            
            // HTML/Static
            projectDir.hasFile("index.html") -> ProjectType.STATIC_HTML
            projectDir.hasFile("index.htm") -> ProjectType.STATIC_HTML
            
            else -> ProjectType.UNKNOWN
        }
    }
    
    /**
     * اكتشاف إطار العمل (Framework) المستخدم
     */
    fun detectFramework(projectDir: File, projectType: ProjectType): String? {
        return when (projectType) {
            ProjectType.NODE -> detectNodeFramework(projectDir)
            ProjectType.PYTHON -> detectPythonFramework(projectDir)
            ProjectType.PHP -> detectPhpFramework(projectDir)
            ProjectType.JAVA -> detectJavaFramework(projectDir)
            ProjectType.GO -> detectGoFramework(projectDir)
            ProjectType.RUST -> detectRustFramework(projectDir)
            else -> null
        }
    }
    
    private fun detectNodeFramework(projectDir: File): String? {
        val content = projectDir.readTextCached("package.json")
        if (content != null) {
            return when {
                content.contains("\"react\"") -> "React"
                content.contains("\"vue\"") -> "Vue"
                content.contains("\"@angular/core\"") -> "Angular"
                content.contains("\"next\"") -> "Next.js"
                content.contains("\"nuxt\"") -> "Nuxt"
                content.contains("\"express\"") -> "Express"
                content.contains("\"fastify\"") -> "Fastify"
                content.contains("\"nestjs\"") -> "NestJS"
                else -> "Node.js"
            }
        }
        return null
    }
    
    private fun detectPythonFramework(projectDir: File): String? {
        val content = projectDir.readTextCached("requirements.txt")
        if (content != null) {
            return when {
                content.contains("django") -> "Django"
                content.contains("flask") -> "Flask"
                content.contains("fastapi") -> "FastAPI"
                content.contains("pyramid") -> "Pyramid"
                content.contains("tornado") -> "Tornado"
                else -> "Python"
            }
        }
        return null
    }
    
    private fun detectPhpFramework(projectDir: File): String? {
        val content = projectDir.readTextCached("composer.json")
        if (content != null) {
            return when {
                content.contains("laravel") -> "Laravel"
                content.contains("symfony") -> "Symfony"
                content.contains("wordpress") -> "WordPress"
                content.contains("drupal") -> "Drupal"
                content.contains("yii") -> "Yii"
                else -> "PHP"
            }
        }
        return null
    }
    
    private fun detectJavaFramework(projectDir: File): String? {
        val pomContent = projectDir.readTextCached("pom.xml")
        val gradleContent = projectDir.readTextCached("build.gradle")
        val gradleKtsContent = projectDir.readTextCached("build.gradle.kts")
        
        return when {
            pomContent?.contains("spring-boot") == true -> "Spring Boot"
            pomContent?.contains("spring") == true -> "Spring"
            gradleContent?.contains("spring-boot") == true -> "Spring Boot"
            gradleContent?.contains("spring") == true -> "Spring"
            gradleKtsContent?.contains("spring-boot") == true -> "Spring Boot"
            gradleKtsContent?.contains("spring") == true -> "Spring"
            else -> "Java"
        }
    }
    
    private fun detectGoFramework(projectDir: File): String? {
        val goModFile = projectDir.resolve("go.mod")
        if (goModFile.exists()) {
            val content = goModFile.readText()
            return when {
                content.contains("gin") -> "Gin"
                content.contains("echo") -> "Echo"
                content.contains("fiber") -> "Fiber"
                content.contains("beego") -> "Beego"
                else -> "Go"
            }
        }
        return null
    }
    
    private fun detectRustFramework(projectDir: File): String? {
        val cargoFile = projectDir.resolve("Cargo.toml")
        if (cargoFile.exists()) {
            val content = cargoFile.readText()
            return when {
                content.contains("actix") -> "Actix"
                content.contains("tokio") -> "Tokio"
                content.contains("rocket") -> "Rocket"
                content.contains("warp") -> "Warp"
                else -> "Rust"
            }
        }
        return null
    }
    
    /**
     * اكتشاف أوامر التثبيت والتشغيل الافتراضية
     */
    fun detectCommands(projectDir: File, projectType: ProjectType): Pair<String?, String?> {
        return when (projectType) {
            ProjectType.NODE -> {
                val installCmd = if (projectDir.hasFile("yarn.lock")) "yarn install" else "npm install"
                val runCmd = if (projectDir.hasFile("package.json")) {
                    val content = projectDir.resolve("package.json").readText()
                    if (content.contains("\"dev\"")) "npm run dev" else "npm start"
                } else "npm start"
                installCmd to runCmd
            }
            ProjectType.PYTHON -> {
                "pip install -r requirements.txt" to "python main.py"
            }
            ProjectType.PHP -> {
                "composer install" to "php -S localhost:8000"
            }
            ProjectType.JAVA -> {
                if (projectDir.hasFile("pom.xml")) {
                    "mvn clean install" to "mvn spring-boot:run"
                } else {
                    "gradle build" to "gradle run"
                }
            }
            ProjectType.GO -> {
                "go mod download" to "go run main.go"
            }
            ProjectType.RUST -> {
                "cargo build" to "cargo run"
            }
            else -> null to null
        }
    }
    
    /**
     * دالة مساعدة للتحقق من وجود ملف
     */
    private fun File.hasFile(pattern: String): Boolean {
        return if (pattern.contains("*")) {
            val regex = pattern.replace(".", "\\.").replace("*", ".*").toRegex()
            listFiles()?.any { regex.matches(it.name) } ?: false
        } else {
            resolve(pattern).exists()
        }
    }
}
