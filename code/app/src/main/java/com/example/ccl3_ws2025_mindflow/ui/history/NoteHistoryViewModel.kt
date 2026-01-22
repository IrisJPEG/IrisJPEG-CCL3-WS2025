package com.example.ccl3_ws2025_mindflow.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ccl3_ws2025_mindflow.data.notes.NoteRepository
import kotlinx.coroutines.flow.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class NoteDayState(
    val isoKey: String,
    val dateLabel: String,
    val text: String?
)

class NoteHistoryViewModel(
    private val noteRepo: NoteRepository
) : ViewModel() {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val today = LocalDate.now()

    private fun currentWeekStart(): LocalDate {
        val diff = (today.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong()
        return today.minusDays(diff)
    }

    private fun dateKey(d: LocalDate) = d.format(formatter)

    private fun label(d: LocalDate): String {
        val dow = d.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        val month = d.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        return "$dow, $month ${d.dayOfMonth}"
    }

    private val _weekStart = MutableStateFlow(currentWeekStart())

    // Used by UI to disable → arrow
    val isAtCurrentWeek: StateFlow<Boolean> =
        _weekStart
            .map { start ->
                val weekEnd = start.plusDays(6)
                !weekEnd.isBefore(today)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                true
            )

    val headerLabel: StateFlow<String> =
        _weekStart
            .map { makeHeader(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                makeHeader(_weekStart.value)
            )

    val weekNotes: StateFlow<List<NoteDayState>> =
        _weekStart
            .flatMapLatest { start ->
                val end = start.plusDays(6)

                val effectiveEnd =
                    if (end.isAfter(today)) today else end

                noteRepo.observeNotesBetween(
                    dateKey(start),
                    dateKey(effectiveEnd)
                ).map { notes ->
                    val map = notes.associateBy { it.dateKey }

                    generateSequence(start) { it.plusDays(1) }
                        .takeWhile { !it.isAfter(effectiveEnd) }
                        .map { d ->
                            val key = dateKey(d)
                            NoteDayState(
                                isoKey = key,
                                dateLabel = label(d),
                                text = map[key]?.text
                            )
                        }
                        .toList()
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    fun previousWeek() {
        _weekStart.value = _weekStart.value.minusWeeks(1)
    }

    fun nextWeek() {
        val nextStart = _weekStart.value.plusWeeks(1)

        // ❌ Block navigating fully into the future
        if (nextStart.isAfter(today)) return

        _weekStart.value = nextStart
    }

    fun refresh() {
        _weekStart.value = _weekStart.value
    }

    private fun makeHeader(start: LocalDate): String {
        val end = start.plusDays(6)
        val monthStart = start.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        val monthEnd = end.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())

        return if (start.month == end.month) {
            "$monthStart ${start.dayOfMonth}–${end.dayOfMonth}"
        } else {
            "$monthStart ${start.dayOfMonth} – $monthEnd ${end.dayOfMonth}"
        }
    }
}
