package com.foliolib.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "books",
    indices = [
        Index(value = ["reading_status"]),
        Index(value = ["title"]),
        Index(value = ["date_added"]),
        Index(value = ["isbn"], unique = false),
        Index(value = ["isbn13"], unique = false)
    ]
)
data class BookEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    @ColumnInfo(name = "authors") val authorsJson: String, // List<String> as JSON
    val isbn: String? = null,
    val isbn13: String? = null,
    val publisher: String? = null,
    @ColumnInfo(name = "published_date") val publishedDate: String? = null,
    val description: String? = null,
    @ColumnInfo(name = "page_count") val pageCount: Int? = null,
    @ColumnInfo(name = "categories") val categoriesJson: String? = null, // List<String> as JSON
    @ColumnInfo(name = "thumbnail_url") val thumbnailUrl: String? = null,
    @ColumnInfo(name = "large_image_url") val largeImageUrl: String? = null,
    val language: String? = null,
    val condition: String? = null, // BookCondition enum as String
    @ColumnInfo(name = "current_page") val currentPage: Int = 0,
    @ColumnInfo(name = "reading_status") val readingStatus: String = "NONE", // ReadingStatus enum as String
    val rating: Float? = null,
    val notes: String? = null,
    @ColumnInfo(name = "date_added") val dateAdded: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "date_started") val dateStarted: Long? = null,
    @ColumnInfo(name = "date_finished") val dateFinished: Long? = null,
    @ColumnInfo(name = "is_manual_entry") val isManualEntry: Boolean = false
)
