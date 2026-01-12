package com.example.ccl3_ws2025_mindflow.data.tasks

import androidx.room.Embedded

data class TodayTaskRow(
    @Embedded val task: TaskEntity,
    val isCompleted: Boolean
)
