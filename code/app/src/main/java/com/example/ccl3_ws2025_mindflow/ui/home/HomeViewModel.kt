package com.example.ccl3_ws2025_mindflow.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material.icons.outlined.SentimentNeutral
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.SentimentVeryDissatisfied
import androidx.compose.material.icons.outlined.SentimentVerySatisfied
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ccl3_ws2025_mindflow.data.mood.MoodRepository
import com.example.ccl3_ws2025_mindflow.data.mood.MoodType
import com.example.ccl3_ws2025_mindflow.data.notes.NoteEntity
import com.example.ccl3_ws2025_mindflow.data.notes.NoteRepository
import com.example.ccl3_ws2025_mindflow.data.tasks.TaskRepository
import com.example.ccl3_ws2025_mindflow.data.tasks.TodayTaskRow
import com.example.ccl3_ws2025_mindflow.ui.breathing.breathingExercises
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ---------- Navigation events ----------
sealed class HomeNavEvent {
    data class Navigate(val route: String) : HomeNavEvent()
    data object PopBack : HomeNavEvent()
    data object ScrollToBottom : HomeNavEvent()
}

// ---------- Breathing UI models (pure UI data) ----------
data class BreathingExerciseUi(
    val id: String,
    val name: String
)

// ---------- State ----------
data class HomeUiState(
    // Mood
    val todayMood: MoodType? = null,
    val showMoodOverlay: Boolean = false,
    val moodIcon: ImageVector? = null,
    val showMoodPlaceholder: Boolean = true,

    // Notes
    val yesterdayNoteText: String = "(No note from yesterday)",
    val tomorrowNoteDraft: String = "",
    val tomorrowNoteSavedPreview: String? = null,

    // Tasks
    val tasksTitle: String = "Today’s tasks",
    val shownTaskRows: List<TodayTaskRow> = emptyList(),
    val tasksExpanded: Boolean = false,
    val tasksCanExpand: Boolean = false,
    val tasksExpandLabel: String = "Expand",
    val progress: Float = 0f,
    val progressLabel: String = "Progress 0%",
    val streakLabel: String = "0 🔥",

    // Breathing
    val breathingTitle: String = "Take a moment to relax",
    val breathingExercises: List<BreathingExerciseUi> = emptyList(),
    val breathingExpanded: Boolean = false,
    val breathingExpandIconUp: Boolean = false,
    val breathingSelectedLabel: String = "Choose breathing exercise",
    val breathingCanStart: Boolean = false
)

private data class HomePartial(
    val mood: MoodType?,
    val yNote: NoteEntity?,
    val tNote: NoteEntity?,
    val todayRows: List<TodayTaskRow>,
    val draft: String,
    val draftTouched: Boolean,
    val tasksExpanded: Boolean,
    val breathingExpanded: Boolean,
    val breathingSelectedId: String?
)

class HomeViewModel(
    private val moodRepo: MoodRepository,
    private val noteRepo: NoteRepository,
    private val taskRepo: TaskRepository
) : ViewModel() {

    // ----- date keys -----
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    private fun key(date: LocalDate) = date.format(formatter)
    private fun todayKey(date: LocalDate) = key(date)
    private fun yesterdayKey(date: LocalDate) = key(date.minusDays(1))
    private fun weekday(date: LocalDate) = date.dayOfWeek.value

    private val _currentDate = MutableStateFlow(LocalDate.now())

    // ----- UI-only flags owned by VM (so UI has no logic) -----
    private val _tasksExpanded = MutableStateFlow(false)

    private val _breathingExpanded = MutableStateFlow(false)
    private val _breathingSelectedId = MutableStateFlow<String?>(null)

    // ----- note draft -----
    private val _tomorrowDraft = MutableStateFlow("")
    private val _tomorrowDraftTouched = MutableStateFlow(false)

    // ----- overlay -----
    private val _overlayDismissedManually = MutableStateFlow(false)
    private val _forceShowMoodOverlay = MutableStateFlow(false)

    // ----- navigation events -----
    private val _navEvents = Channel<HomeNavEvent>(Channel.BUFFERED)
    val navEvents: Flow<HomeNavEvent> = _navEvents.receiveAsFlow()

    fun onScreenShown() {
        // Called by UI once; ensures date refresh logic is centralized
        refreshDate()
    }

    fun refreshDate() {
        val now = LocalDate.now()
        if (_currentDate.value != now) {
            _currentDate.value = now

            _overlayDismissedManually.value = false
            _forceShowMoodOverlay.value = false

            _tomorrowDraftTouched.value = false
            _tomorrowDraft.value = ""

            _tasksExpanded.value = false
            _breathingExpanded.value = false
            _breathingSelectedId.value = null
        }
    }

    // ----- data flows -----
    private val todayRowsFlow = _currentDate.flatMapLatest { date ->
        taskRepo.observeTodayTasks(
            dateKey = todayKey(date),
            weekday = weekday(date)
        )
    }

    private val streakFlow = _currentDate.flatMapLatest { date ->
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

        noteRepo.observeNoteForDate(todayKey(date))
    }

    private val notesFlow = combine(yNoteFlow, tNoteFlow) { y, t -> y to t }

    private val draftFlow = combine(_tomorrowDraft, _tomorrowDraftTouched) { draft, touched ->
        draft to touched
    }

    private val uiFlagsFlow =
        combine(_tasksExpanded, _breathingExpanded, _breathingSelectedId) { tExp, bExp, bSel ->
            Triple(tExp, bExp, bSel)
        }

    private val partial: StateFlow<HomePartial> =
        combine(moodFlow, notesFlow, todayRowsFlow, draftFlow, uiFlagsFlow) { mood, notes, rows, draftPair, flags ->
            HomePartial(
                mood = mood,
                yNote = notes.first,
                tNote = notes.second,
                todayRows = rows,
                draft = draftPair.first,
                draftTouched = draftPair.second,
                tasksExpanded = flags.first,
                breathingExpanded = flags.second,
                breathingSelectedId = flags.third
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            HomePartial(null, null, null, emptyList(), "", false, false, false, null)
        )

    private val overlayInputsFlow =
        combine(partial, _overlayDismissedManually, _forceShowMoodOverlay) { p, dismissed, forced ->
            Triple(p, dismissed, forced)
        }

    val uiState: StateFlow<HomeUiState> =
        combine(overlayInputsFlow, streakFlow) { triple, streakDays ->
            val p = triple.first
            val dismissed = triple.second
            val forced = triple.third

            // ----- overlay -----
            val shouldShowOverlay = (((p.mood == null) && !dismissed) || forced)

            // ----- mood icon -----
            val showPlaceholder = (p.mood == null)
            val icon = p.mood?.let { moodToIcon(it) }

            // ----- tasks derived -----
            val collapsedCount = 2
            val canExpand = p.todayRows.size > collapsedCount
            val shownRows =
                if (p.tasksExpanded) p.todayRows else p.todayRows.take(collapsedCount)

            val total = p.todayRows.size
            val done = p.todayRows.count { it.isCompleted }
            val progress = if (total == 0) 0f else (done.toFloat() / total.toFloat()).coerceIn(0f, 1f)
            val progressLabel = "Progress ${(progress * 100).toInt()}%"
            val streakLabel = "${streakDays.coerceAtLeast(0)} 🔥"
            val expandLabel = if (p.tasksExpanded) "Collapse" else "Expand"

            // ----- notes derived -----
            val savedTomorrow = p.tNote?.text
            val effectiveDraft =
                if (!p.draftTouched && p.draft.isBlank() && !savedTomorrow.isNullOrBlank()) savedTomorrow
                else p.draft

            val yesterdayText = p.yNote?.text ?: "(No note from yesterday)"

            // ----- breathing derived -----
            val breathingList = breathingExercises.map { BreathingExerciseUi(it.id, it.name) }
            val selectedName = breathingExercises.firstOrNull { it.id == p.breathingSelectedId }?.name
            val breathingSelectedLabel = selectedName ?: "Choose breathing exercise"
            val canStart = (p.breathingSelectedId != null)
            val expandIconUp = p.breathingExpanded

            HomeUiState(
                todayMood = p.mood,
                showMoodOverlay = shouldShowOverlay,
                moodIcon = icon,
                showMoodPlaceholder = showPlaceholder,

                yesterdayNoteText = yesterdayText,
                tomorrowNoteDraft = effectiveDraft,
                tomorrowNoteSavedPreview = savedTomorrow,

                shownTaskRows = shownRows,
                tasksExpanded = p.tasksExpanded,
                tasksCanExpand = canExpand,
                tasksExpandLabel = expandLabel,
                progress = progress,
                progressLabel = progressLabel,
                streakLabel = streakLabel,

                breathingExercises = breathingList,
                breathingExpanded = p.breathingExpanded,
                breathingExpandIconUp = expandIconUp,
                breathingSelectedLabel = breathingSelectedLabel,
                breathingCanStart = canStart
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    // ---------- EVENTS from UI (no logic in UI) ----------

    fun onOpenMoodJourney() {
        viewModelScope.launch { _navEvents.send(HomeNavEvent.Navigate("moodJourney")) }
    }

    fun onOpenTasks() {
        viewModelScope.launch { _navEvents.send(HomeNavEvent.Navigate("tasks")) }
    }

    fun onOpenTaskHistory() {
        viewModelScope.launch { _navEvents.send(HomeNavEvent.Navigate("history")) }
    }

    fun onOpenNoteToSelf() {
        viewModelScope.launch { _navEvents.send(HomeNavEvent.Navigate("noteToSelf")) }
    }

    fun onOpenNoteHistory() {
        viewModelScope.launch { _navEvents.send(HomeNavEvent.Navigate("noteHistory")) }
    }

    fun toggleTasksExpanded() {
        _tasksExpanded.value = !_tasksExpanded.value
    }

    fun toggleBreathingExpanded() {
        val now = !_breathingExpanded.value
        _breathingExpanded.value = now
        if (now) {
            // match your old behavior: when expanded, scroll to bottom
            viewModelScope.launch { _navEvents.send(HomeNavEvent.ScrollToBottom) }
        }
    }

    fun selectBreathingExercise(exerciseId: String) {
        _breathingSelectedId.value = exerciseId
        _breathingExpanded.value = false
    }

    fun startBreathing() {
        val id = _breathingSelectedId.value ?: return
        viewModelScope.launch { _navEvents.send(HomeNavEvent.Navigate("breathing/$id")) }
    }

    // Notes
    fun onTomorrowDraftChanged(text: String) {
        _tomorrowDraftTouched.value = true
        _tomorrowDraft.value = text
    }

    fun saveTomorrowNote() {
        val text = _tomorrowDraft.value
        viewModelScope.launch {
            val date = _currentDate.value
            noteRepo.saveNote(todayKey(date), text)
            _tomorrowDraftTouched.value = false
        }
    }

    // Mood overlay
    fun openMoodPicker() {
        _forceShowMoodOverlay.value = true
        _overlayDismissedManually.value = false
    }

    fun selectMood(mood: MoodType) {
        viewModelScope.launch {
            val key = todayKey(_currentDate.value)
            moodRepo.saveMood(key, mood)
            _forceShowMoodOverlay.value = false
            _overlayDismissedManually.value = false
        }
    }

    fun dismissOverlayOnce() {
        _overlayDismissedManually.value = true
        _forceShowMoodOverlay.value = false
    }

    // Tasks
    fun toggleTaskDone(taskId: Long) {
        viewModelScope.launch {
            val dateKey = todayKey(_currentDate.value)
            taskRepo.toggleCompletion(taskId = taskId, dateKey = dateKey)
        }
    }

    // ---------- helper ----------
    private fun moodToIcon(mood: MoodType): ImageVector {
        return when (mood) {
            MoodType.VERY_SAD -> Icons.Outlined.SentimentVeryDissatisfied
            MoodType.SAD -> Icons.Outlined.SentimentDissatisfied
            MoodType.NEUTRAL -> Icons.Outlined.SentimentNeutral
            MoodType.HAPPY -> Icons.Outlined.SentimentSatisfied
            MoodType.VERY_HAPPY -> Icons.Outlined.SentimentVerySatisfied
        }
    }
}
