package com.example.ccl3_ws2025_mindflow.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ccl3_ws2025_mindflow.data.notes.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    private fun currentWeekStart(): LocalDate {
        val today = LocalDate.now()
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

    val headerLabel: StateFlow<String> =
        _weekStart
            .map { start -> makeHeader(start) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), makeHeader(_weekStart.value))

    val weekNotes: StateFlow<List<NoteDayState>> =
        _weekStart
            .flatMapLatest { start ->
                val startKey = dateKey(start)
                val endKey = dateKey(start.plusDays(6))
                noteRepo.observeNotesBetween(startKey, endKey)
                    .map { notes ->
                        val map = notes.associateBy { it.dateKey }
                        (0..6).map { i ->
                            val d = start.plusDays(i.toLong())
                            val key = dateKey(d)
                            NoteDayState(
                                isoKey = key,
                                dateLabel = label(d),
                                text = map[key]?.text
                            )
                        }
                    }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun previousWeek() {
        _weekStart.value = _weekStart.value.minusWeeks(1)
    }

    fun nextWeek() {
        _weekStart.value = _weekStart.value.plusWeeks(1)
    }

    fun refresh() {
        // optional: forces recompute if needed
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
