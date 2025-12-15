package com.foliolib.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "book_shelf_cross_ref",
    primaryKeys = ["book_id", "shelf_id"]
)
data class BookShelfCrossRef(
    @ColumnInfo(name = "book_id") val bookId: String,
    @ColumnInfo(name = "shelf_id") val shelfId: String,
    @ColumnInfo(name = "added_at") val addedAt: Long = System.currentTimeMillis()
)
