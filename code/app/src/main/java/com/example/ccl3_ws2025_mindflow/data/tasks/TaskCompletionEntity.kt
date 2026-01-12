package com.example.ccl3_ws2025_mindflow.data.tasks

import androidx.room.Entity

@Entity(
    tableName = "task_completions",
    primaryKeys = ["taskId", "dateKey"]
)
data class TaskCompletionEntity(
    val taskId: Long,
    val dateKey: String,     // yyyy-MM-dd
    val isCompleted: Boolean
)
