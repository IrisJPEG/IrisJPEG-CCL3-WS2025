package com.example.ccl3_ws2025_mindflow.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import kotlinx.coroutines.android.awaitFrame
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.ccl3_ws2025_mindflow.R
import com.example.ccl3_ws2025_mindflow.data.tasks.TodayTaskRow
import com.example.ccl3_ws2025_mindflow.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    // UI-only state that belongs to UI system (scroll position)
    val scrollState = rememberScrollState()

    // Collect state
    val state by viewModel.uiState.collectAsState()

    // Collect one-shot navigation events (no navigation logic in composables)
    LaunchedEffect(Unit) {
        viewModel.navEvents.collectLatest { event ->
            when (event) {
                is HomeNavEvent.Navigate -> navController.navigate(event.route)
                is HomeNavEvent.PopBack -> navController.popBackStack()
                is HomeNavEvent.ScrollToBottom -> scrollState.animateScrollTo(scrollState.maxValue)
            }
        }
    }

    // Tell VM when screen is visible again (no lifecycle observer in UI)
    LaunchedEffect(Unit) {
        viewModel.onScreenShown()
    }
    LaunchedEffect(state.tasksExpanded) {
        if (state.tasksExpanded) {
            awaitFrame()
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }
    MindFlowBackground {
        Box(modifier = Modifier.padding(Dimens.ScreenPadding)) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 30.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(Dimens.CardGap)
            ) {
                MoodJourneyHeader(
                    moodIcon = state.moodIcon,
                    showMoodPlaceholder = state.showMoodPlaceholder,
                    onOpenJourney = viewModel::onOpenMoodJourney,
                    onEditMood = viewModel::openMoodPicker
                )

                DailyNoteToSelfCard(
                    yesterdayNote = state.yesterdayNoteText,
                    onLeaveMessageForTomorrow = viewModel::onOpenNoteToSelf,
                    onHistory = viewModel::onOpenNoteHistory
                )

                BreathingCard(
                    title = state.breathingTitle,
                    selectedLabel = state.breathingSelectedLabel,
                    expanded = state.breathingExpanded,
                    canStart = state.breathingCanStart,
                    expandIconUp = state.breathingExpandIconUp,
                    exercises = state.breathingExercises,
                    onToggleExpand = viewModel::toggleBreathingExpanded,
                    onSelectExercise = viewModel::selectBreathingExercise,
                    onStart = viewModel::startBreathing
                )

                TodayTasksCard(
                    title = state.tasksTitle,
                    progressLabel = state.progressLabel,
                    streakLabel = state.streakLabel,
                    progress = state.progress,
                    rows = state.shownTaskRows,
                    expanded = state.tasksExpanded,
                    canExpand = state.tasksCanExpand,
                    expandLabel = state.tasksExpandLabel,
                    onToggleDone = viewModel::toggleTaskDone,
                    onToggleExpand = viewModel::toggleTasksExpanded,
                    onManage = viewModel::onOpenTasks,
                    onHistory = viewModel::onOpenTaskHistory
                )




                Spacer(modifier = Modifier.height(6.dp))
            }

            if (state.showMoodOverlay) {
                MoodOverlay(
                    onSelect = viewModel::selectMood,
                    onDismissOnce = viewModel::dismissOverlayOnce
                )
            }
        }
    }
}

@Composable
private fun MoodJourneyHeader(
    moodIcon: ImageVector?,
    showMoodPlaceholder: Boolean,
    onOpenJourney: () -> Unit,
    onEditMood: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {

        MindFlowCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    "Mood journey",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(72.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(top = 5.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(2.dp, MindFlowColors.Surface),
                    color = MindFlowColors.Surface
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.mood_journey_flip),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.FillWidth
                    )
                }
            }
            PillOutlineButton(
                text = "View your journey",
                onClick = onOpenJourney,
                modifier = Modifier.fillMaxWidth(),
                borderColor = MindFlowColors.Stroke,
                textColor = MindFlowColors.Primary,
                containerColor = MindFlowColors.Surface
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(72.dp)
                .zIndex(999f)
                .clip(CircleShape)
                .background(MindFlowColors.Surface.copy(alpha = 0.001f))
                .clickable { onEditMood() },
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 6.dp)
            ) {
                if (showMoodPlaceholder) {
                    Image(
                        painter = painterResource(id = R.drawable.circle_dashed),
                        contentDescription = "Set mood",
                        modifier = Modifier
                            .size(32.dp)
                            .offset(y = -2.dp)
                    )
                } else {
                    Icon(
                        imageVector = moodIcon!!,
                        contentDescription = "Edit mood",
                        tint = MindFlowColors.TextPrimary,
                        modifier = Modifier
                            .size(32.dp)
                            .offset(y = -2.dp)
                    )
                }

                Text(
                    text = "Edit",
                    style = MaterialTheme.typography.labelSmall,
                    color = MindFlowColors.TextPrimary,
                    modifier = Modifier.offset(y = (-6).dp)
                )
            }
        }
    }
}


@Composable
private fun TodayTasksCard(
    title: String,
    progressLabel: String,
    streakLabel: String,
    progress: Float,
    rows: List<TodayTaskRow>,
    expanded: Boolean,
    canExpand: Boolean,
    expandLabel: String,
    onToggleDone: (taskId: Long) -> Unit,
    onToggleExpand: () -> Unit,
    onManage: () -> Unit,
    onHistory: () -> Unit
) {
    MindFlowCard(modifier = Modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onHistory) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = "View in calendar",
                    tint = MindFlowColors.TextPrimary,
                            modifier = Modifier.size(25.dp)
                )
            }

        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                progressLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MindFlowColors.TextSecondary
            )
            Spacer(Modifier.weight(1f))
            Text(streakLabel, style = MaterialTheme.typography.titleMedium)
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
            color = MindFlowColors.Success,
            trackColor = MindFlowColors.Stroke
        )

        if (rows.isEmpty()) {
            Text(
                "(No tasks scheduled for today)",
                style = MaterialTheme.typography.labelMedium,
                color = MindFlowColors.TextMuted
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rows.forEach { row ->
                    PillRowSurface(
                        modifier = Modifier.height(50.dp)
                    ) {
                        Checkbox(
                            checked = row.isCompleted,
                            onCheckedChange = { onToggleDone(row.task.id) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MindFlowColors.Primary,
                                uncheckedColor = MindFlowColors.TextMuted,
                                checkmarkColor = MindFlowColors.Surface
                            )
                        )
                        Text(
                            text = row.task.title,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textDecoration = if (row.isCompleted) TextDecoration.LineThrough else null
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (canExpand) {
                TextButton(onClick = onToggleExpand) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = expandLabel,
                            color = MindFlowColors.TextMuted,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MindFlowColors.TextMuted
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            TextButton(onClick = onManage) {
                Text("Manage",
                    color = MindFlowColors.TextPrimary,
                    style = MaterialTheme.typography.labelMedium,)
            }
        }
    }
}

@Composable
private fun DailyNoteToSelfCard(
    yesterdayNote: String,
    onLeaveMessageForTomorrow: () -> Unit,
    onHistory: () -> Unit
) {
    MindFlowCard(modifier = Modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Daily note to self ♡",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onHistory) {
                Icon(
                    Icons.Default.History,
                    contentDescription = "Notes history",
                    tint = MindFlowColors.TextPrimary,
                    modifier = Modifier.size(29.dp)
                )
            }
        }

        Text("Yesterday’s note:", style = MaterialTheme.typography.labelLarge)
        Text(
            text = yesterdayNote,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            color = MindFlowColors.TextSecondary
        )

        PillOutlineButton(
            text = "Leave a message for tomorrow",
            onClick = onLeaveMessageForTomorrow,
            modifier = Modifier.fillMaxWidth(),
            borderColor = MindFlowColors.Stroke,
            textColor = MindFlowColors.Primary,
            containerColor = MindFlowColors.Surface
        )
    }
}

@Composable
private fun BreathingCard(
    title: String,
    selectedLabel: String,
    expanded: Boolean,
    canStart: Boolean,
    expandIconUp: Boolean,
    exercises: List<BreathingExerciseUi>,
    onToggleExpand: () -> Unit,
    onSelectExercise: (String) -> Unit,
    onStart: () -> Unit
) {
    MindFlowCard(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleLarge)

        Column {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PillRowSurface(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clickable { onToggleExpand() }
                ) {
                    Text(
                        text = selectedLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MindFlowColors.TextMuted,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = if (expandIconUp)
                            Icons.Default.KeyboardArrowUp
                        else
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MindFlowColors.TextMuted
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, MindFlowColors.Stroke),
                    color = if(canStart)
                        MindFlowColors.HillMid
                    else
                        MindFlowColors.Surface
                ) {
                    IconButton(
                        onClick = onStart,
                        enabled = canStart
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            tint = if (canStart)
                                Color.White
                            else
                                Color.LightGray
                        )
                    }
                }
            }


            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    exercises.forEach { ex ->
                        Text(
                            text = ex.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MindFlowColors.TextSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectExercise(ex.id) }
                                .padding(vertical = 10.dp, horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    }
}
