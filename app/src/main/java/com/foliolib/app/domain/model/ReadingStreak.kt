package com.foliolib.app.domain.model

data class ReadingStreak(
    val currentStreak: Int, // days
    val longestStreak: Int,
    val lastReadDate: String? = null,
    val totalDaysRead: Int,
    val streakHistory: List<StreakDay> = emptyList()
)

data class StreakDay(
    val date: String,
    val pagesRead: Int,
    val minutesRead: Int
)
