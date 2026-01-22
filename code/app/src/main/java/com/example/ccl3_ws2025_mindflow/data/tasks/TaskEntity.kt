package com.example.ccl3_ws2025_mindflow.data.tasks

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,

    // Repeating tasks:
    val daysCsv: String,

    // One-time tasks:
    val isOneTime: Boolean = false,
    val oneTimeDateKey: String? = null,   // yyyy-MM-dd when isOneTime=true

    // Existing:
    val createdDateKey: String  // yyyy-MM-dd
)

fun TaskEntity.isActiveOn(weekday: Int): Boolean =
    if (isOneTime) false
    else daysCsv.split(",").mapNotNull { it.trim().toIntOrNull() }.contains(weekday)
