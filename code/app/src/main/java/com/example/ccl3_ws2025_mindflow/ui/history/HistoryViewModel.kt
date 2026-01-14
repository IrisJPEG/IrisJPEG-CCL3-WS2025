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
import java.time.YearMonth
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

data class MonthDayState(
    val date: LocalDate,
    val isoKey: String,                  // yyyy-MM-dd
    val dayOfMonth: Int,
    val isInDisplayedMonth: Boolean,
    val tasks: List<HistoryTaskItem>,
    val status: DayStatus,
    val isFutureDay: Boolean
)

class HistoryViewModel(
    private val repo: TaskRepository
) : ViewModel() {

    private val labelFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
    private val monthHeaderFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    // Weekly state
    private val _weekStart = MutableStateFlow(currentWeekStart())
    private val _weekHistoryState = MutableStateFlow<List<HistoryDayState>>(emptyList())

    // Monthly state
    private val _displayedMonth = MutableStateFlow(YearMonth.now())
    private val _monthCells = MutableStateFlow<List<MonthDayState>>(emptyList())

    // Shared header label (week or month can set it)
    private val _headerLabel = MutableStateFlow(LocalDate.now().format(monthHeaderFormatter))

    val weekHistoryState: StateFlow<List<HistoryDayState>> = _weekHistoryState
    val monthCells: StateFlow<List<MonthDayState>> = _monthCells
    val headerLabel: StateFlow<String> = _headerLabel

    init {
        // Live refresh whenever tasks OR completions change
        viewModelScope.launch {
            combine(
                repo.observeAllTasks().distinctUntilChanged(),
                repo.observeAllCompletions().distinctUntilChanged()
            ) { _, _ -> Unit }
                .collect {
                    // Refresh both datasets so switching views is instant
                    loadWeek(_weekStart.value)
                    loadMonth(_displayedMonth.value)
                }
        }

        loadWeek(_weekStart.value)
        loadMonth(_displayedMonth.value)
    }

    // ---------- WEEK NAV ----------
    fun previousWeek() {
        _weekStart.value = _weekStart.value.minusWeeks(1)
        loadWeek(_weekStart.value)
    }

    fun nextWeek() {
        _weekStart.value = _weekStart.value.plusWeeks(1)
        loadWeek(_weekStart.value)
    }

    // ---------- MONTH NAV ----------
    fun previousMonth() {
        _displayedMonth.value = _displayedMonth.value.minusMonths(1)
        loadMonth(_displayedMonth.value)
    }

    fun nextMonth() {
        _displayedMonth.value = _displayedMonth.value.plusMonths(1)
        loadMonth(_displayedMonth.value)
    }

    fun refresh() {
        loadWeek(_weekStart.value)
        loadMonth(_displayedMonth.value)
    }

    // ---------- LOADERS ----------
    private fun loadWeek(startOfWeek: LocalDate) {
        // Header label for weekly view: show month/year of that week
        _headerLabel.value = startOfWeek.format(monthHeaderFormatter)

        viewModelScope.launch {
            val allTasks = repo.getAllTasksSnapshot()
            val today = LocalDate.now()
            val days = (0..6).map { startOfWeek.plusDays(it.toLong()) }

            _weekHistoryState.value = days.map { date ->
                buildDayState(
                    date = date,
                    allTasks = allTasks,
                    today = today
                ).let { built ->
                    HistoryDayState(
                        isoKey = built.isoKey,
                        dateLabel = date.format(labelFormatter),
                        tasks = built.tasks,
                        status = built.status,
                        isFutureDay = built.isFutureDay
                    )
                }
            }
        }
    }

    private fun loadMonth(yearMonth: YearMonth) {
        // Header label for monthly view: month + year
        _headerLabel.value = yearMonth.atDay(1).format(monthHeaderFormatter)

        viewModelScope.launch {
            val allTasks = repo.getAllTasksSnapshot()
            val today = LocalDate.now()

            val firstDay = yearMonth.atDay(1)
            val daysInMonth = yearMonth.lengthOfMonth()

            // We want a Monday-start calendar grid
            // Java: Monday=1..Sunday=7
            val firstDow = firstDay.dayOfWeek.value
            val leadingBlanks = (firstDow - DayOfWeek.MONDAY.value).let { if (it < 0) it + 7 else it }

            // We will render a 6-week grid (42 cells)
            val totalCells = 42
            val startCellDate = firstDay.minusDays(leadingBlanks.toLong())

            _monthCells.value = (0 until totalCells).map { offset ->
                val date = startCellDate.plusDays(offset.toLong())
                val inMonth = (date.month == yearMonth.month && date.year == yearMonth.year)

                val built = buildDayState(
                    date = date,
                    allTasks = allTasks,
                    today = today
                )

                MonthDayState(
                    date = date,
                    isoKey = built.isoKey,
                    dayOfMonth = date.dayOfMonth,
                    isInDisplayedMonth = inMonth,
                    tasks = built.tasks,
                    status = built.status,
                    isFutureDay = built.isFutureDay
                )
            }
        }
    }

    private data class BuiltDay(
        val isoKey: String,
        val tasks: List<HistoryTaskItem>,
        val status: DayStatus,
        val isFutureDay: Boolean
    )

    private suspend fun buildDayState(
        date: LocalDate,
        allTasks: List<TaskEntity>,
        today: LocalDate
    ): BuiltDay {
        val dateKey = date.toString()
        val weekday = date.dayOfWeek.value // Mon=1..Sun=7
        val isFuture = date.isAfter(today)

        // Tasks that exist for that date (createdDateKey gate prevents showing in weeks/months before creation/update)
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

        return BuiltDay(
            isoKey = dateKey,
            tasks = items,
            status = status,
            isFutureDay = isFuture
        )
    }

    private fun currentWeekStart(): LocalDate {
        val today = LocalDate.now()
        val diff = (today.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong()
        return today.minusDays(diff)
    }
}
