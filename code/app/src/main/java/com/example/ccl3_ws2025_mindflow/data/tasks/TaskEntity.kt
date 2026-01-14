package com.example.ccl3_ws2025_mindflow.data.tasks

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,

    // Repeating tasks:
    val daysCsv: String,        // "1,2,3" (Mon=1 ... Sun=7). Can be "" for one-time tasks.

    // One-time tasks:
    val isOneTime: Boolean = false,
    val oneTimeDateKey: String? = null,   // yyyy-MM-dd when isOneTime=true

    // Existing:
    val createdDateKey: String  // yyyy-MM-dd
)

fun TaskEntity.isActiveOn(weekday: Int): Boolean =
    if (isOneTime) false
    else daysCsv.split(",").mapNotNull { it.trim().toIntOrNull() }.contains(weekday)
