package com.example.ccl3_ws2025_mindflow.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ccl3_ws2025_mindflow.data.mood.MoodEntity
import com.example.ccl3_ws2025_mindflow.data.mood.MoodDao
import com.example.ccl3_ws2025_mindflow.data.notes.NoteDao
import com.example.ccl3_ws2025_mindflow.data.notes.NoteEntity
import com.example.ccl3_ws2025_mindflow.data.tasks.TaskCompletionDao
import com.example.ccl3_ws2025_mindflow.data.tasks.TaskCompletionEntity
import com.example.ccl3_ws2025_mindflow.data.tasks.TaskDao
import com.example.ccl3_ws2025_mindflow.data.tasks.TaskEntity

@Database(
    entities = [
        MoodEntity::class,
        NoteEntity::class,
        TaskEntity::class,
        TaskCompletionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun moodDao(): MoodDao
    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
    abstract fun taskCompletionDao(): TaskCompletionDao
}
