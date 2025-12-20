package com.foliolib.app.core.di

import android.content.Context
import androidx.room.Room
import com.foliolib.app.data.local.dao.*
import com.foliolib.app.data.local.database.FolioDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFolioDatabase(@ApplicationContext context: Context): FolioDatabase {
        return Room.databaseBuilder(
            context,
            FolioDatabase::class.java,
            FolioDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideBookDao(database: FolioDatabase): BookDao {
        return database.bookDao()
    }

    @Provides
    @Singleton
    fun provideReadingSessionDao(database: FolioDatabase): ReadingSessionDao {
        return database.readingSessionDao()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: FolioDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    fun provideUserPreferencesDao(database: FolioDatabase): UserPreferencesDao {
        return database.userPreferencesDao()
    }
}
