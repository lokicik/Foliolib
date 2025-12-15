package com.foliolib.app.domain.model

data class Shelf(
    val id: String,
    val name: String,
    val description: String? = null,
    val color: String, // Hex color for UI
    val icon: String? = null, // Material icon name
    val isDefault: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val bookCount: Int = 0
)
