package com.foliolib.app.domain.model

data class Note(
    val id: String,
    val bookId: String,
    val content: String,
    val page: Int? = null,
    val chapter: String? = null,
    val color: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Highlight(
    val id: String,
    val bookId: String,
    val text: String,
    val page: Int? = null,
    val color: String,
    val createdAt: Long = System.currentTimeMillis()
)
