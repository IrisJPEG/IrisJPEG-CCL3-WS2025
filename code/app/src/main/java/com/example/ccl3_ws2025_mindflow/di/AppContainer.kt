package com.example.ccl3_ws2025_mindflow.di

import android.content.Context
import androidx.room.Room
import com.example.ccl3_ws2025_mindflow.data.AppDatabase
import com.example.ccl3_ws2025_mindflow.data.mood.MoodRepository
import com.example.ccl3_ws2025_mindflow.data.notes.NoteRepository
import com.example.ccl3_ws2025_mindflow.data.tasks.TaskRepository

class AppContainer(context: Context) {

    // Single Room database instance for the whole app
    private val db: AppDatabase = Room.databaseBuilder(
        context.applicationContext, // important: avoid leaking an Activity
        AppDatabase::class.java,
        "mindflow_db"
    )
        //.fallbackToDestructiveMigration() FOR DEBUGGING
        .build()

    // Repositories (single source of truth)
    val moodRepository: MoodRepository =
        MoodRepository(db.moodDao())

    val noteRepository: NoteRepository =
        NoteRepository(db.noteDao())

    val taskRepository: TaskRepository =
        TaskRepository(
            taskDao = db.taskDao(),
            completionDao = db.taskCompletionDao()
        )
}
