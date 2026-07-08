package com.projectpilot.app.di

import android.content.Context
import androidx.room.Room
import com.projectpilot.app.data.local.AppDatabase
import com.projectpilot.app.data.local.AiAnalysisDao
import com.projectpilot.app.data.local.ProjectDao
import com.projectpilot.app.data.local.SettingsExporter
import com.projectpilot.app.data.ai.AiAnalysisService
import com.projectpilot.app.data.ai.AiProviderConfig
import com.projectpilot.app.data.repository.AiAnalysisRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.NAME
        ).build()
    }

    @Provides
    fun provideProjectDao(database: AppDatabase): ProjectDao {
        return database.projectDao()
    }

    @Provides
    fun provideAiAnalysisDao(database: AppDatabase): AiAnalysisDao {
        return database.aiAnalysisDao()
    }

    @Provides
    fun provideAiAnalysisService(config: AiProviderConfig): AiAnalysisService {
        return AiAnalysisService(config)
    }

    @Provides
    fun provideAiAnalysisRepository(
        dao: AiAnalysisDao,
        service: AiAnalysisService
    ): AiAnalysisRepository {
        return AiAnalysisRepository(dao, service)
    }

    @Provides
    fun provideSettingsExporter(@ApplicationContext context: Context): SettingsExporter {
        return SettingsExporter(context)
    }

    @Provides
    fun provideAiProviderConfig(): AiProviderConfig {
        return AiProviderConfig()
    }
}
