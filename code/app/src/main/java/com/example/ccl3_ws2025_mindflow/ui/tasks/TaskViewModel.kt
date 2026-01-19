// FILE: app/src/main/java/com/example/ccl3_ws2025_mindflow/ui/tasks/TaskViewModel.kt
package com.example.ccl3_ws2025_mindflow.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ccl3_ws2025_mindflow.data.tasks.TaskEntity
import com.example.ccl3_ws2025_mindflow.data.tasks.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale

data class TaskEditorUiState(
    val title: String = "",
    val selectedDays: List<Int> = emptyList(),

    val repeatOnlyOnce: Boolean = false,

    val dayText: String = "",
    val monthText: String = "",
    val yearText: String = "",

    val dayInteracted: Boolean = false,
    val monthInteracted: Boolean = false,
    val yearInteracted: Boolean = false,

    val dayOptions: List<String> = (1..31).map { it.toString() },
    val monthOptions: List<String> = (1..12).map { it.toString() },
    val yearOptions: List<String> = (2020..2035).map { it.toString() },

    val dateError: String? = null,
    val oneTimeDateKey: String? = null,

    val saveEnabled: Boolean = false
)

class TaskViewModel(private val repo: TaskRepository) : ViewModel() {

    fun observeAllTasks(): Flow<List<TaskEntity>> = repo.observeAllTasks()

    suspend fun getTaskById(id: Long): TaskEntity? = repo.getTaskById(id)

    // --- Editor state ---
    private val _editorUiState = MutableStateFlow(TaskEditorUiState())
    val editorUiState: StateFlow<TaskEditorUiState> = _editorUiState.asStateFlow()

    fun startEditing(taskId: Long) {
        if (taskId == -1L) {
            _editorUiState.value = recalc(TaskEditorUiState())
            return
        }

        viewModelScope.launch {
            val t = repo.getTaskById(taskId)

            val base = TaskEditorUiState(
                title = t?.title ?: "",
                repeatOnlyOnce = t?.isOneTime ?: false
            )

            val filled = if (base.repeatOnlyOnce) {
                val key = t?.oneTimeDateKey
                if (!key.isNullOrBlank() && key.length == 10) {
                    base.copy(
                        yearText = key.substring(0, 4),
                        monthText = key.substring(5, 7).toIntOrNull()?.toString() ?: "",
                        dayText = key.substring(8, 10).toIntOrNull()?.toString() ?: ""
                    )
                } else base
            } else {
                val days = t?.daysCsv
                    ?.split(",")
                    ?.mapNotNull { it.trim().toIntOrNull() }
                    ?: emptyList()
                base.copy(selectedDays = days)
            }

            _editorUiState.value = recalc(filled)
        }
    }

    fun setTitle(value: String) {
        _editorUiState.update { recalc(it.copy(title = value)) }
    }

    fun toggleSelectedDay(dayValue: Int) {
        _editorUiState.update { cur ->
            val set = cur.selectedDays.toMutableSet()
            if (set.contains(dayValue)) set.remove(dayValue) else set.add(dayValue)
            recalc(cur.copy(selectedDays = set.toList()))
        }
    }

    fun setRepeatOnlyOnce(checked: Boolean) {
        _editorUiState.update { cur ->
            if (checked) {
                // switching to one-time: clear weekly selection
                recalc(
                    cur.copy(
                        repeatOnlyOnce = true,
                        selectedDays = emptyList()
                    )
                )
            } else {
                // switching back to weekly: clear one-time fields + interacted flags
                recalc(
                    cur.copy(
                        repeatOnlyOnce = false,
                        dayText = "",
                        monthText = "",
                        yearText = "",
                        dayInteracted = false,
                        monthInteracted = false,
                        yearInteracted = false
                    )
                )
            }
        }
    }

    fun setDayText(value: String) {
        _editorUiState.update { recalc(it.copy(dayText = value.filter { c -> c.isDigit() })) }
    }

    fun setMonthText(value: String) {
        _editorUiState.update { recalc(it.copy(monthText = value.filter { c -> c.isDigit() })) }
    }

    fun setYearText(value: String) {
        _editorUiState.update { recalc(it.copy(yearText = value.filter { c -> c.isDigit() })) }
    }

    fun setDayInteracted(value: Boolean) {
        _editorUiState.update { it.copy(dayInteracted = value) }
    }

    fun setMonthInteracted(value: Boolean) {
        _editorUiState.update { it.copy(monthInteracted = value) }
    }

    fun setYearInteracted(value: Boolean) {
        _editorUiState.update { it.copy(yearInteracted = value) }
    }

    fun saveEditing(taskId: Long) {
        val cur = _editorUiState.value

        upsertTask(
            id = if (taskId == -1L) 0L else taskId,
            title = cur.title,
            selectedDays = cur.selectedDays,
            repeatOnlyOnce = cur.repeatOnlyOnce,
            oneTimeDateKey = cur.oneTimeDateKey
        )
    }

    // --- Existing API (kept) ---
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

    // --- Logic moved from screen into VM ---
    private fun isLeapYear(y: Int): Boolean =
        (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)

    private fun daysInMonth(y: Int, m: Int): Int = when (m) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(y)) 29 else 28
        else -> 31
    }

    private fun recalc(state: TaskEditorUiState): TaskEditorUiState {
        val yearInt = state.yearText.trim().toIntOrNull()
        val monthInt = state.monthText.trim().toIntOrNull()
        val dayInt = state.dayText.trim().toIntOrNull()

        val maxDay: Int? =
            if (yearInt != null && monthInt != null && monthInt in 1..12) daysInMonth(yearInt, monthInt)
            else null

        // clamp day if numeric and too large
        val clampedDayText = run {
            val max = maxDay
            val d = dayInt
            if (max != null && d != null && d > max) max.toString() else state.dayText
        }

        val dateError: String? = if (!state.repeatOnlyOnce) {
            null
        } else {
            val y = yearInt
            val m = monthInt
            val d = clampedDayText.trim().toIntOrNull()

            when {
                y == null || y !in 1900..2100 -> "Enter a valid year"
                m == null || m !in 1..12 -> "Enter a valid month (1–12)"
                d == null -> "Enter a valid day"
                maxDay != null && (d < 1 || d > maxDay) -> "That month only has $maxDay days"
                else -> null
            }
        }

        val oneTimeDateKey: String? =
            if (!state.repeatOnlyOnce) null
            else {
                if (dateError != null) null
                else {
                    val y = yearInt ?: return state.copy(
                        dayText = clampedDayText,
                        dayOptions = buildDayOptions(maxDay),
                        dateError = dateError,
                        oneTimeDateKey = null,
                        saveEnabled = computeSaveEnabled(state.title, state.selectedDays, state.repeatOnlyOnce, null)
                    )
                    val m = monthInt ?: return state.copy(
                        dayText = clampedDayText,
                        dayOptions = buildDayOptions(maxDay),
                        dateError = dateError,
                        oneTimeDateKey = null,
                        saveEnabled = computeSaveEnabled(state.title, state.selectedDays, state.repeatOnlyOnce, null)
                    )
                    val d = clampedDayText.trim().toIntOrNull() ?: return state.copy(
                        dayText = clampedDayText,
                        dayOptions = buildDayOptions(maxDay),
                        dateError = dateError,
                        oneTimeDateKey = null,
                        saveEnabled = computeSaveEnabled(state.title, state.selectedDays, state.repeatOnlyOnce, null)
                    )

                    String.format(Locale.US, "%04d-%02d-%02d", y, m, d)
                }
            }

        val saveEnabled = computeSaveEnabled(
            title = state.title,
            selectedDays = state.selectedDays,
            repeatOnlyOnce = state.repeatOnlyOnce,
            oneTimeDateKey = oneTimeDateKey
        )

        return state.copy(
            dayText = clampedDayText,
            dayOptions = buildDayOptions(maxDay),
            dateError = dateError,
            oneTimeDateKey = oneTimeDateKey,
            saveEnabled = saveEnabled
        )
    }

    private fun buildDayOptions(maxDay: Int?): List<String> {
        val max = maxDay ?: 31
        return (1..max).map { it.toString() }
    }

    private fun computeSaveEnabled(
        title: String,
        selectedDays: List<Int>,
        repeatOnlyOnce: Boolean,
        oneTimeDateKey: String?
    ): Boolean {
        val t = title.trim()
        return t.isNotBlank() && (
                if (repeatOnlyOnce) oneTimeDateKey != null
                else selectedDays.isNotEmpty()
                )
    }
}
