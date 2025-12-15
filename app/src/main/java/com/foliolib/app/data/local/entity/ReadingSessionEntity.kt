package com.foliolib.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "reading_sessions",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("book_id"),
        Index("date"),
        Index("start_time")
    ]
)
data class ReadingSessionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "book_id") val bookId: String,
    @ColumnInfo(name = "start_page") val startPage: Int,
    @ColumnInfo(name = "end_page") val endPage: Int,
    @ColumnInfo(name = "start_time") val startTime: Long,
    @ColumnInfo(name = "end_time") val endTime: Long,
    val duration: Long, // milliseconds
    @ColumnInfo(name = "pages_read") val pagesRead: Int,
    val date: String // YYYY-MM-DD format
)
