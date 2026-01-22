package com.example.ccl3_ws2025_mindflow.data.tasks

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class TaskRepository(
    private val taskDao: TaskDao,
    private val completionDao: TaskCompletionDao
) {


    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private fun parseIsoToCalendar(dateKey: String): Calendar {
        val date = isoFormatter.parse(dateKey)
            ?: throw IllegalArgumentException("Invalid dateKey: $dateKey")
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun weekdayMon1Sun7(dateKey: String): Int {
        val cal = parseIsoToCalendar(dateKey)
        val dow = cal.get(Calendar.DAY_OF_WEEK) // Sun=1..Sat=7
        return if (dow == Calendar.SUNDAY) 7 else dow - 1 // Mon=1..Sun=7
    }

    private fun addDays(dateKey: String, deltaDays: Int): String {
        val cal = parseIsoToCalendar(dateKey)
        cal.add(Calendar.DAY_OF_YEAR, deltaDays)
        return isoFormatter.format(cal.time)
    }

    // Manage screen
    fun observeAllTasks(): Flow<List<TaskEntity>> = taskDao.observeAllTasks()
    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)
    suspend fun upsert(task: TaskEntity) = taskDao.upsert(task)
    suspend fun deleteById(id: Long) = taskDao.deleteById(id)

    fun observeAllCompletions() = completionDao.observeAllCompletions()

    // Home screen (today)
    fun observeTodayTasks(dateKey: String, weekday: Int): Flow<List<TodayTaskRow>> =
        taskDao.observeTodayTasks(dateKey, weekday)

    suspend fun toggleCompletion(taskId: Long, dateKey: String) {
        val existing = completionDao.getCompletion(taskId, dateKey)
        val newValue = !(existing?.isCompleted ?: false)
        completionDao.upsert(
            TaskCompletionEntity(
                taskId = taskId,
                dateKey = dateKey,
                isCompleted = newValue
            )
        )
    }

    // ---- History needs these snapshots ----
    suspend fun getAllTasksSnapshot(): List<TaskEntity> =
        taskDao.observeAllTasks().first()

    suspend fun getCompletionsForDate(dateKey: String): List<TaskCompletionEntity> =
        completionDao.getCompletionsForDate(dateKey)
    // --------------------------------------

    fun taskIsActiveOnWeekday(daysCsv: String, weekday: Int): Boolean {
        return daysCsv
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .contains(weekday)
    }


    private suspend fun tasksScheduledForDateKey(dateKey: String): List<TaskEntity> {
        val weekday = weekdayMon1Sun7(dateKey)
        val all = taskDao.observeAllTasks().first()

        return all.filter { task ->
            val existed = task.createdDateKey <= dateKey

            val active =
                if (task.isOneTime) {
                    task.oneTimeDateKey == dateKey
                } else {
                    val activeByWeekday = taskIsActiveOnWeekday(task.daysCsv, weekday)
                    activeByWeekday
                }

            active && existed
        }
    }

    suspend fun getDailyAllTaskStreak(todayKey: String): Int {
        var streak = 0

        // count past consecutive fully-completed days
        var cursorKey = addDays(todayKey, -1)
        while (true) {
            val dayTasks = tasksScheduledForDateKey(cursorKey)
            if (dayTasks.isEmpty()) break

            val completions = completionDao.getCompletionsForDate(cursorKey)
            val completedCount =
                completions.count { it.isCompleted && dayTasks.any { t -> t.id == it.taskId } }

            if (completedCount == dayTasks.size) {
                streak++
                cursorKey = addDays(cursorKey, -1)
            } else {
                break
            }
        }

        // include today if fully completed
        val todayTasks = tasksScheduledForDateKey(todayKey)
        if (todayTasks.isNotEmpty()) {
            val todayCompletions = completionDao.getCompletionsForDate(todayKey)
            val todayCompleted =
                todayTasks.all { t -> todayCompletions.any { it.taskId == t.id && it.isCompleted } }

            if (todayCompleted) streak++
        }

        return streak
    }


}
