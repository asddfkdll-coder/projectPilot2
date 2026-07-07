package com.projectpilot.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.projectpilot.app.domain.model.AiAnalysisResult
import com.projectpilot.app.domain.model.AnalysisType
import com.projectpilot.app.domain.model.Project
import com.projectpilot.app.domain.model.ProjectType

class Converters {
    @TypeConverter fun fromType(t: ProjectType): String = t.name
    @TypeConverter fun toType(s: String): ProjectType =
        runCatching { ProjectType.valueOf(s) }.getOrDefault(ProjectType.UNKNOWN)
    
    @TypeConverter fun fromAnalysisType(t: AnalysisType): String = t.name
    @TypeConverter fun toAnalysisType(s: String): AnalysisType =
        runCatching { AnalysisType.valueOf(s) }.getOrDefault(AnalysisType.FULL)
}

@Database(
    entities = [Project::class, AiAnalysisResult::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun aiAnalysisDao(): AiAnalysisDao

    companion object { const val NAME = "projectpilot.db" }
}
