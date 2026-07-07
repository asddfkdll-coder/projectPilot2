package com.projectpilot.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.projectpilot.app.data.local.AppDatabase
import com.projectpilot.app.data.local.AiAnalysisDao
import com.projectpilot.app.data.local.ProjectDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS ai_analysis_results (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    projectId INTEGER NOT NULL,
                    projectName TEXT NOT NULL,
                    projectPath TEXT NOT NULL,
                    analysisType TEXT NOT NULL,
                    createdAt INTEGER NOT NULL DEFAULT 0,
                    version INTEGER NOT NULL DEFAULT 1,
                    architectureSummary TEXT,
                    detectedPatterns TEXT NOT NULL DEFAULT '[]',
                    layerStructure TEXT,
                    totalFiles INTEGER NOT NULL DEFAULT 0,
                    totalDirectories INTEGER NOT NULL DEFAULT 0,
                    totalLinesOfCode INTEGER NOT NULL DEFAULT 0,
                    languageBreakdown TEXT NOT NULL DEFAULT '{}',
                    fileTreeSummary TEXT,
                    dependencies TEXT NOT NULL DEFAULT '[]',
                    outdatedDependencies TEXT NOT NULL DEFAULT '[]',
                    dependencyVulnerabilities TEXT NOT NULL DEFAULT '[]',
                    dependencyHealthScore REAL,
                    detectedTechnologies TEXT NOT NULL DEFAULT '[]',
                    frameworks TEXT NOT NULL DEFAULT '[]',
                    buildTools TEXT NOT NULL DEFAULT '[]',
                    buildInstructions TEXT,
                    runInstructions TEXT,
                    prerequisites TEXT NOT NULL DEFAULT '[]',
                    securityScore INTEGER,
                    securityIssues TEXT NOT NULL DEFAULT '[]',
                    criticalVulnerabilities INTEGER NOT NULL DEFAULT 0,
                    warnings INTEGER NOT NULL DEFAULT 0,
                    securityRecommendations TEXT NOT NULL DEFAULT '[]',
                    performanceScore INTEGER,
                    performanceIssues TEXT NOT NULL DEFAULT '[]',
                    performanceRecommendations TEXT NOT NULL DEFAULT '[]',
                    detectedBottlenecks TEXT NOT NULL DEFAULT '[]',
                    codeQualityScore INTEGER,
                    codeSmells TEXT NOT NULL DEFAULT '[]',
                    complexityIssues TEXT NOT NULL DEFAULT '[]',
                    duplicationIssues TEXT NOT NULL DEFAULT '[]',
                    qualityRecommendations TEXT NOT NULL DEFAULT '[]',
                    improvementRecommendations TEXT NOT NULL DEFAULT '[]',
                    prioritizedActions TEXT NOT NULL DEFAULT '[]',
                    hasReadme INTEGER NOT NULL DEFAULT 0,
                    hasApiDocs INTEGER NOT NULL DEFAULT 0,
                    hasContributingGuide INTEGER NOT NULL DEFAULT 0,
                    documentationScore INTEGER,
                    documentationRecommendations TEXT NOT NULL DEFAULT '[]',
                    rawResponse TEXT,
                    modelUsed TEXT,
                    providerUsed TEXT,
                    generationTimeMs INTEGER NOT NULL DEFAULT 0
                )
            """)
            db.execSQL("CREATE INDEX IF NOT EXISTS index_ai_analysis_results_projectId ON ai_analysis_results(projectId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_ai_analysis_results_createdAt ON ai_analysis_results(createdAt)")
        }
    }

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, AppDatabase.NAME)
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideProjectDao(db: AppDatabase): ProjectDao = db.projectDao()

    @Provides
    fun provideAiAnalysisDao(db: AppDatabase): AiAnalysisDao = db.aiAnalysisDao()
}
