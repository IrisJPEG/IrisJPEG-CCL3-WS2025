package com.example.ccl3_ws2025_mindflow.data.tasks

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun observeAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Today tasks: tasks active on weekday joined with completion state for dateKey.
     * weekday is 1..7 (Mon..Sun).
     *
     * NOTE: REPLACE(...) removes accidental spaces in daysCsv so both "1,2,3"
     * and "1, 2, 3" work.
     *
     * IMPORTANT: createdDateKey <= dateKey prevents tasks from appearing in past days
     * before they were created.
     */
    @Query(
        """
SELECT 
  t.*,
  IFNULL(c.isCompleted, 0) AS isCompleted
FROM tasks t
LEFT JOIN task_completions c
  ON c.taskId = t.id AND c.dateKey = :dateKey
WHERE t.createdDateKey <= :dateKey
  AND (
      (t.isOneTime = 1 AND t.oneTimeDateKey = :dateKey)
      OR
      (t.isOneTime = 0 AND instr(',' || t.daysCsv || ',', ',' || :weekday || ',') > 0)
  )
ORDER BY t.id DESC
"""
    )
    fun observeTodayTasks(dateKey: String, weekday: Int): Flow<List<TodayTaskRow>>
}
