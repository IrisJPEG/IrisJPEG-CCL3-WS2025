package com.example.ccl3_ws2025_mindflow.data.tasks

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface TaskCompletionDao {

    @Query("SELECT * FROM task_completions WHERE dateKey = :dateKey")
    suspend fun getCompletionsForDate(dateKey: String): List<TaskCompletionEntity>

    @Query("SELECT * FROM task_completions WHERE taskId = :taskId AND dateKey = :dateKey LIMIT 1")
    suspend fun getCompletion(taskId: Long, dateKey: String): TaskCompletionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TaskCompletionEntity)

    @Query("SELECT * FROM task_completions")
    fun observeAllCompletions(): Flow<List<TaskCompletionEntity>>
}
