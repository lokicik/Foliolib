package com.foliolib.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.foliolib.app.data.local.dao.*
import com.foliolib.app.data.local.entity.*

@Database(
    entities = [
        BookEntity::class,
        ReadingSessionEntity::class,
        NoteEntity::class,
        UserPreferencesEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FolioDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun readingSessionDao(): ReadingSessionDao
    abstract fun noteDao(): NoteDao
    abstract fun userPreferencesDao(): UserPreferencesDao

    companion object {
        const val DATABASE_NAME = "foliolib_database"
    }
}
