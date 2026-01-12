package com.example.ccl3_ws2025_mindflow.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ccl3_ws2025_mindflow.data.tasks.TaskEntity
import com.example.ccl3_ws2025_mindflow.data.tasks.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HistoryTaskItem(
    val task: TaskEntity,
    val isCompleted: Boolean
)

enum class DayStatus {
    COMPLETED,
    INCOMPLETE,
    SCHEDULED,
    NONE
}

data class HistoryDayState(
    val isoKey: String,        // yyyy-MM-dd
    val dateLabel: String,     // e.g., Mon, Jan 12
    val tasks: List<HistoryTaskItem>,
    val status: DayStatus,
    val isFutureDay: Boolean
)

class HistoryViewModel(
    private val repo: TaskRepository
) : ViewModel() {

    private val labelFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM")

    private val _weekStart = MutableStateFlow(currentWeekStart())
    private val _weekHistoryState = MutableStateFlow<List<HistoryDayState>>(emptyList())
    private val _currentMonth = MutableStateFlow(_weekStart.value.format(monthFormatter))

    val weekHistoryState: StateFlow<List<HistoryDayState>> = _weekHistoryState
    val currentMonth: StateFlow<String> = _currentMonth

    init {
        // Live refresh whenever tasks OR completions change
        viewModelScope.launch {
            combine(
                repo.observeAllTasks().distinctUntilChanged(),
                repo.observeAllCompletions().distinctUntilChanged()
            ) { _, _ -> Unit }
                .collect {
                    loadWeek(_weekStart.value)
                }
        }

        loadWeek(_weekStart.value)
    }

    fun previousWeek() {
        _weekStart.value = _weekStart.value.minusWeeks(1)
        loadWeek(_weekStart.value)
    }

    fun nextWeek() {
        _weekStart.value = _weekStart.value.plusWeeks(1)
        loadWeek(_weekStart.value)
    }

    fun refresh() {
        loadWeek(_weekStart.value)
    }

    private fun loadWeek(startOfWeek: LocalDate) {
        _currentMonth.value = startOfWeek.format(monthFormatter)

        viewModelScope.launch {
            val allTasks = repo.getAllTasksSnapshot()
            val today = LocalDate.now()
            val days = (0..6).map { startOfWeek.plusDays(it.toLong()) }

            _weekHistoryState.value = days.map { date ->
                val dateKey = date.toString()
                val weekday = date.dayOfWeek.value // Mon=1..Sun=7
                val isFuture = date.isAfter(today)

                // Tasks that exist for that date (createdDateKey gate prevents showing in weeks before creation/update)
                val tasksForDay = allTasks.filter { task ->
                    val active = repo.taskIsActiveOnWeekday(task.daysCsv, weekday)
                    val existed = task.createdDateKey <= dateKey
                    active && existed
                }

                val completions = repo.getCompletionsForDate(dateKey)
                val completionMap = completions.associateBy { it.taskId }

                val items = tasksForDay.map { task ->
                    HistoryTaskItem(
                        task = task,
                        isCompleted = completionMap[task.id]?.isCompleted == true
                    )
                }

                val status = when {
                    isFuture && items.isNotEmpty() -> DayStatus.SCHEDULED
                    isFuture && items.isEmpty() -> DayStatus.NONE
                    items.isEmpty() -> DayStatus.NONE
                    items.all { it.isCompleted } -> DayStatus.COMPLETED
                    else -> DayStatus.INCOMPLETE
                }

                HistoryDayState(
                    isoKey = dateKey,
                    dateLabel = date.format(labelFormatter),
                    tasks = items,
                    status = status,
                    isFutureDay = isFuture
                )
            }
        }
    }

    private fun currentWeekStart(): LocalDate {
        val today = LocalDate.now()
        val diff = (today.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong()
        return today.minusDays(diff)
    }
}
