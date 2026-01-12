package com.example.ccl3_ws2025_mindflow.data.notes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val dateKey: String, // yyyy-MM-dd (the day this note should be shown)
    val text: String
)
