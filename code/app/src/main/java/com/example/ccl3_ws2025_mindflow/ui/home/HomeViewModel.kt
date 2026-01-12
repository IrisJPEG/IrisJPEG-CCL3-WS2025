package com.example.ccl3_ws2025_mindflow.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ccl3_ws2025_mindflow.data.mood.MoodRepository
import com.example.ccl3_ws2025_mindflow.data.mood.MoodType
import com.example.ccl3_ws2025_mindflow.data.notes.NoteEntity
import com.example.ccl3_ws2025_mindflow.data.notes.NoteRepository
import com.example.ccl3_ws2025_mindflow.data.tasks.TaskRepository
import com.example.ccl3_ws2025_mindflow.data.tasks.TodayTaskRow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HomeUiState(
    val todayMood: MoodType? = null,
    val showMoodOverlay: Boolean = false,
    val yesterdayNote: String? = null,

    // Used by NoteToSelf screen
    val tomorrowNoteDraft: String = "",
    val tomorrowNoteSavedPreview: String? = null,

    // Today tasks
    val todayTasks: List<TodayTaskRow> = emptyList(),

    val progress: Float = 0f, // 0..1
    val doneCount: Int = 0,
    val totalCount: Int = 0,

    val streakDays: Int = 0
)

private data class HomePartial(
    val mood: MoodType?,
    val yNote: NoteEntity?,
    val tNote: NoteEntity?,
    val todayRows: List<TodayTaskRow>,
    val draft: String,
    val draftTouched: Boolean
)

class HomeViewModel(
    private val moodRepo: MoodRepository,
    private val noteRepo: NoteRepository,
    private val taskRepo: TaskRepository
) : ViewModel() {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    private fun todayKey(): String = LocalDate.now().format(formatter)
    private fun yesterdayKey(): String = LocalDate.now().minusDays(1).format(formatter)
    private fun tomorrowKey(): String = LocalDate.now().plusDays(1).format(formatter)
    private fun todayWeekday(): Int = LocalDate.now().dayOfWeek.value // Mon=1..Sun=7

    private val _tomorrowDraft = MutableStateFlow("")
    private val _tomorrowDraftTouched = MutableStateFlow(false)
    private val _overlayDismissedManually = MutableStateFlow(false)

    // Emits every time today's completion state changes (because query joins task_completions)
    private val todayRowsFlow = taskRepo.observeTodayTasks(
        dateKey = todayKey(),
        weekday = todayWeekday()
    )

    // Recompute streak whenever today's rows change
    private val streakFlow = todayRowsFlow.flatMapLatest {
        flow { emit(taskRepo.getDailyAllTaskStreak(todayKey())) }
    }

    // --- Build partial state using ONLY 2-flow combine() to avoid overload issues ---

    private val moodFlow = moodRepo.observeMoodForDate(todayKey())
    private val yNoteFlow = noteRepo.observeNoteForDate(yesterdayKey())
    private val tNoteFlow = noteRepo.observeNoteForDate(tomorrowKey())

    private val notesFlow = combine(yNoteFlow, tNoteFlow) { y, t ->
        y to t
    }

    private val draftFlow = combine(_tomorrowDraft, _tomorrowDraftTouched) { draft, touched ->
        draft to touched
    }

    private val moodNotesFlow = combine(moodFlow, notesFlow) { mood, notes ->
        mood to notes // Pair<MoodType?, Pair<NoteEntity?, NoteEntity?>>
    }

    private val moodNotesRowsFlow = combine(moodNotesFlow, todayRowsFlow) { mn, rows ->
        Triple(mn.first, mn.second, rows)
        // Triple<MoodType?, Pair<NoteEntity?, NoteEntity?>, List<TodayTaskRow>>
    }

    private val partial: StateFlow<HomePartial> = combine(moodNotesRowsFlow, draftFlow) { triple, draftPair ->
        val mood = triple.first
        val yNote = triple.second.first
        val tNote = triple.second.second
        val rows = triple.third

        HomePartial(
            mood = mood,
            yNote = yNote,
            tNote = tNote,
            todayRows = rows,
            draft = draftPair.first,
            draftTouched = draftPair.second
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomePartial(null, null, null, emptyList(), "", false))

    // --- Final UI state, again using 2-flow combine chaining ---

    private val partialOverlayFlow = combine(partial, _overlayDismissedManually) { p, overlayDismissed ->
        p to overlayDismissed
    }

    val uiState: StateFlow<HomeUiState> = combine(partialOverlayFlow, streakFlow) { po, streakDays ->
        val p = po.first
        val overlayDismissed = po.second

        val total = p.todayRows.size
        val done = p.todayRows.count { it.isCompleted }
        val progress = if (total == 0) 0f else done.toFloat() / total.toFloat()

        val shouldShowOverlay = (p.mood == null) && !overlayDismissed

        // Auto-prefill draft from saved tomorrow note ONLY if user hasn't typed yet
        val savedTomorrow = p.tNote?.text
        val effectiveDraft =
            if (!p.draftTouched && p.draft.isBlank() && !savedTomorrow.isNullOrBlank()) savedTomorrow
            else p.draft

        HomeUiState(
            todayMood = p.mood,
            showMoodOverlay = shouldShowOverlay,
            yesterdayNote = p.yNote?.text,

            tomorrowNoteDraft = effectiveDraft,
            tomorrowNoteSavedPreview = savedTomorrow,

            todayTasks = p.todayRows,
            progress = progress,
            doneCount = done,
            totalCount = total,
            streakDays = streakDays
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun onTomorrowDraftChanged(text: String) {
        _tomorrowDraftTouched.value = true
        _tomorrowDraft.value = text
    }

    fun saveTomorrowNote() {
        val dateKey = tomorrowKey()
        val text = _tomorrowDraft.value
        viewModelScope.launch {
            noteRepo.saveNote(dateKey, text)
            _tomorrowDraftTouched.value = false
        }
    }

    fun selectMood(mood: MoodType) {
        val key = todayKey()
        viewModelScope.launch { moodRepo.saveMood(key, mood) }
    }

    fun dismissOverlayOnce() {
        _overlayDismissedManually.value = true
    }

    fun toggleTaskDone(taskId: Long) {
        val dateKey = todayKey()
        viewModelScope.launch {
            taskRepo.toggleCompletion(taskId = taskId, dateKey = dateKey)
        }
    }
}
