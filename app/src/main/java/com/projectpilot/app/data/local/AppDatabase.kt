package com.projectpilot.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.projectpilot.app.domain.model.AiAnalysisResult
import com.projectpilot.app.domain.model.Project

@Database(
    entities = [Project::class, AiAnalysisResult::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun aiAnalysisDao(): AiAnalysisDao

    companion object {
        const val NAME = "projectpilot_database"
    }
}
