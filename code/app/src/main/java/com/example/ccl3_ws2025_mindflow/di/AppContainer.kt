package com.example.ccl3_ws2025_mindflow.di

import android.content.Context
import androidx.room.Room
import com.example.ccl3_ws2025_mindflow.data.AppDatabase
import com.example.ccl3_ws2025_mindflow.data.mood.MoodRepository
import com.example.ccl3_ws2025_mindflow.data.notes.NoteRepository
import com.example.ccl3_ws2025_mindflow.data.tasks.TaskRepository

class AppContainer(context: Context) {

    private val db: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "mindflow_db"
    )
        .fallbackToDestructiveMigration()
        .build()

    val moodRepository = MoodRepository(db.moodDao())
    val noteRepository = NoteRepository(db.noteDao())

    // NEW
    val taskRepository = TaskRepository(
        taskDao = db.taskDao(),
        completionDao = db.taskCompletionDao()
    )
}
