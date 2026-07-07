package com.projectpilot.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.projectpilot.app.domain.model.AiAnalysisResult

@Database(entities = [AiAnalysisResult::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun aiAnalysisDao(): AiAnalysisDao
}
