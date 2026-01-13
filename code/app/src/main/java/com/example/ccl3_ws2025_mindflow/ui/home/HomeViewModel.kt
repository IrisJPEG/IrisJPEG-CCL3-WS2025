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

    // IMPORTANT: date changes must be modeled as state, not as "LocalDate.now()" calls at init time.
    private val _currentDate = MutableStateFlow(LocalDate.now())

    // Draft state (screen-local, not persisted)
    private val _tomorrowDraft = MutableStateFlow("")
    private val _tomorrowDraftTouched = MutableStateFlow(false)

    // Mood overlay "dismissed" state
    private val _overlayDismissedManually = MutableStateFlow(false)

    /**
     * Call this whenever the Home screen becomes visible (or on resume).
     * If the date changed while the app was in memory, this will rebuild all flows.
     */
    fun refreshDate() {
        val now = LocalDate.now()
        if (_currentDate.value != now) {
            _currentDate.value = now

            // Optional: reset overlay dismissal so mood prompt can show again on a new day
            _overlayDismissedManually.value = false

            // Optional: clear draft-touch flag so we can prefill tomorrow draft from DB for the new date
            _tomorrowDraftTouched.value = false
            _tomorrowDraft.value = ""
        }
    }

    private fun dateKey(date: LocalDate): String = date.format(formatter)

    private fun todayKey(date: LocalDate): String = dateKey(date)
    private fun yesterdayKey(date: LocalDate): String = dateKey(date.minusDays(1))
    private fun tomorrowKey(date: LocalDate): String = dateKey(date.plusDays(1))
    private fun weekday(date: LocalDate): Int = date.dayOfWeek.value // Mon=1..Sun=7

    // --- Date-dependent flows (REBUILT when _currentDate changes) ---

    private val todayRowsFlow = _currentDate.flatMapLatest { date ->
        taskRepo.observeTodayTasks(
            dateKey = todayKey(date),
            weekday = weekday(date)
        )
    }

    private val streakFlow = _currentDate.flatMapLatest { date ->
        // Recompute streak (and also re-run whenever today's rows change)
        todayRowsFlow.flatMapLatest {
            flow { emit(taskRepo.getDailyAllTaskStreak(todayKey(date))) }
        }
    }

    private val moodFlow = _currentDate.flatMapLatest { date ->
        moodRepo.observeMoodForDate(todayKey(date))
    }

    private val yNoteFlow = _currentDate.flatMapLatest { date ->
        noteRepo.observeNoteForDate(yesterdayKey(date))
    }

    private val tNoteFlow = _currentDate.flatMapLatest { date ->
        noteRepo.observeNoteForDate(tomorrowKey(date))
    }

    // --- Combine into partial state (keep your 2-flow combine chaining) ---

    private val notesFlow = combine(yNoteFlow, tNoteFlow) { y, t -> y to t }

    private val draftFlow = combine(_tomorrowDraft, _tomorrowDraftTouched) { draft, touched ->
        draft to touched
    }

    private val moodNotesFlow = combine(moodFlow, notesFlow) { mood, notes ->
        mood to notes // Pair<MoodType?, Pair<NoteEntity?, NoteEntity?>>
    }

    private val moodNotesRowsFlow = combine(moodNotesFlow, todayRowsFlow) { mn, rows ->
        Triple(mn.first, mn.second, rows)
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
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomePartial(null, null, null, emptyList(), "", false)
    )

    // --- Final UI state ---

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
        val text = _tomorrowDraft.value

        viewModelScope.launch {
            // Use currentDate at save-time (not captured at init)
            val date = _currentDate.value
            val key = tomorrowKey(date)

            noteRepo.saveNote(key, text)

            // After saving, allow future auto-prefill to come from DB again
            _tomorrowDraftTouched.value = false
        }
    }

    fun selectMood(mood: MoodType) {
        viewModelScope.launch {
            val key = todayKey(_currentDate.value)
            moodRepo.saveMood(key, mood)
        }
    }

    fun dismissOverlayOnce() {
        _overlayDismissedManually.value = true
    }

    fun toggleTaskDone(taskId: Long) {
        viewModelScope.launch {
            val dateKey = todayKey(_currentDate.value)
            taskRepo.toggleCompletion(taskId = taskId, dateKey = dateKey)
        }
    }
}
