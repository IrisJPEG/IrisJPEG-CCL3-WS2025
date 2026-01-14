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

    fun upsertTask(
        id: Long,
        title: String,
        selectedDays: List<Int>,
        repeatOnlyOnce: Boolean,
        oneTimeDateKey: String?
    ) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return

        val valid =
            if (repeatOnlyOnce) !oneTimeDateKey.isNullOrBlank()
            else selectedDays.isNotEmpty()

        if (!valid) return

        val entity = TaskEntity(
            id = id,
            title = trimmed,
            daysCsv = if (repeatOnlyOnce) "" else selectedDays.sorted().joinToString(","),
            isOneTime = repeatOnlyOnce,
            oneTimeDateKey = if (repeatOnlyOnce) oneTimeDateKey else null,
            createdDateKey = LocalDate.now().toString()
        )

        viewModelScope.launch { repo.upsert(entity) }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch { repo.deleteById(id) }
    }
}
