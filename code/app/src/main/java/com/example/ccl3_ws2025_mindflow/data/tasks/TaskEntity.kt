package com.example.ccl3_ws2025_mindflow.data.tasks

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val daysCsv: String,        // "1,2,3"  (Mon=1 ... Sun=7)
    val createdDateKey: String  // yyyy-MM-dd
)

fun TaskEntity.isActiveOn(weekday: Int): Boolean =
    daysCsv.split(",").mapNotNull { it.toIntOrNull() }.contains(weekday)
