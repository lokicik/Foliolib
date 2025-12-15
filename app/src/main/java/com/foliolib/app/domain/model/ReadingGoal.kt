package com.foliolib.app.domain.model

data class ReadingGoal(
    val id: String,
    val type: GoalType,
    val target: Int,
    val current: Int = 0,
    val startDate: Long,
    val endDate: Long,
    val isCompleted: Boolean = false
)
