package com.example.ccl3_ws2025_mindflow.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ccl3_ws2025_mindflow.data.tasks.TaskEntity
import com.example.ccl3_ws2025_mindflow.data.tasks.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate

class TaskViewModel(private val repo: TaskRepository) : ViewModel() {

    fun observeAllTasks(): Flow<List<TaskEntity>> = repo.observeAllTasks()

    suspend fun getTaskById(id: Long): TaskEntity? = repo.getTaskById(id)

    fun upsertTask(id: Long, title: String, selectedDays: List<Int>) {
        val trimmed = title.trim()
        if (trimmed.isEmpty() || selectedDays.isEmpty()) return

        val entity = TaskEntity(
            id = id,
            title = trimmed,
            daysCsv = selectedDays.sorted().joinToString(","),
            createdDateKey = LocalDate.now().toString()
        )

        viewModelScope.launch { repo.upsert(entity) }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch { repo.deleteById(id) }
    }
}
