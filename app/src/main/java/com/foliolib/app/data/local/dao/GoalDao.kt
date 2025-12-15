package com.foliolib.app.data.local.dao

import androidx.room.*
import com.foliolib.app.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Query("SELECT * FROM goals ORDER BY start_date DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE is_completed = 0 ORDER BY end_date ASC")
    fun getActiveGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE is_completed = 1 ORDER BY end_date DESC")
    fun getCompletedGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :goalId")
    fun getGoalById(goalId: String): Flow<GoalEntity?>

    @Query("SELECT * FROM goals WHERE type = :type AND is_completed = 0 LIMIT 1")
    fun getActiveGoalByType(type: String): Flow<GoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoalById(goalId: String)
}
