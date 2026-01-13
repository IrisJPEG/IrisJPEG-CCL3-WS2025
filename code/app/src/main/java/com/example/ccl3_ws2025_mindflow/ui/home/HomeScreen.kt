package com.example.ccl3_ws2025_mindflow.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.ccl3_ws2025_mindflow.R
import com.example.ccl3_ws2025_mindflow.data.tasks.TodayTaskRow
import com.example.ccl3_ws2025_mindflow.ui.breathing.BreathingExercise
import com.example.ccl3_ws2025_mindflow.ui.breathing.breathingExercises
import com.example.ccl3_ws2025_mindflow.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    LaunchedEffect(Unit) { viewModel.refreshDate() }

    val state by viewModel.uiState.collectAsState()
    var tasksExpanded by rememberSaveable { mutableStateOf(false) }

    MindFlowBackground {
        Box(modifier = Modifier.padding(Dimens.ScreenPadding)) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Dimens.CardGap)
            ) {

                MoodJourneyHeader(
                    moodEmoji = state.todayMood?.emoji,
                    onOpenJourney = { navController.navigate("moodJourney") },
                    onEditMood = { viewModel.openMoodPicker() }
                )

                TodayTasksCard(
                    progress = state.progress,
                    streakDays = state.streakDays,
                    rows = state.todayTasks,
                    expanded = tasksExpanded,
                    onToggleDone = { taskId -> viewModel.toggleTaskDone(taskId) },
                    onToggleExpand = { tasksExpanded = !tasksExpanded },
                    onManage = { navController.navigate("tasks") },
                    onHistory = { navController.navigate("history") }
                )

                DailyNoteToSelfCard(
                    yesterdayNote = state.yesterdayNote,
                    onLeaveMessageForTomorrow = { navController.navigate("noteToSelf") }
                )

                // NEW breathing dropdown + route with exerciseId
                BreathingCard(navController = navController)

                Spacer(modifier = Modifier.height(6.dp))
            }

            if (state.showMoodOverlay) {
                MoodOverlay(
                    onSelect = { mood -> viewModel.selectMood(mood) },
                    onDismissOnce = { viewModel.dismissOverlayOnce() }
                )
            }
        }
    }
}

@Composable
private fun MoodJourneyHeader(
    moodEmoji: String?,
    onOpenJourney: () -> Unit,
    onEditMood: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {

        // Whole card opens journey
        MindFlowCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenJourney() }
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
                // Reserve space so emoji overlay does not overlap text
                Spacer(modifier = Modifier.width(56.dp))
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
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

        // Emoji hitbox (easy to tap)
        if (moodEmoji != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .size(56.dp)
                    .zIndex(999f)
                    .clip(CircleShape)
                    .background(MindFlowColors.Surface.copy(alpha = 0.01f))
                    .clickable { onEditMood() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = moodEmoji, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun TodayTasksCard(
    progress: Float,
    streakDays: Int,
    rows: List<TodayTaskRow>,
    expanded: Boolean,
    onToggleDone: (taskId: Long) -> Unit,
    onToggleExpand: () -> Unit,
    onManage: () -> Unit,
    onHistory: () -> Unit
) {
    val collapsedCount = 2
    val canExpand = rows.size > collapsedCount

    MindFlowCard(modifier = Modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today’s tasks",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onHistory) {
                Icon(Icons.Default.History, contentDescription = "History", tint = MindFlowColors.TextMuted)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Progress ${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MindFlowColors.TextSecondary
            )
            Spacer(Modifier.weight(1f))
            if (streakDays > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("$streakDays", style = MaterialTheme.typography.titleMedium)
                    Text("🔥", style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
            color = MindFlowColors.Success,
            trackColor = MindFlowColors.Stroke
        )

        if (rows.isEmpty()) {
            Text(
                "(No tasks scheduled for today)",
                style = MaterialTheme.typography.bodyMedium,
                color = MindFlowColors.TextMuted
            )
        } else {
            val shown = if (expanded) rows else rows.take(collapsedCount)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                shown.forEach { row ->
                    PillRowSurface {
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
            TextButton(
                onClick = onToggleExpand,
                enabled = canExpand
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (expanded) "Collapse" else "Expand",
                        color = MindFlowColors.TextMuted
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MindFlowColors.TextMuted
                    )
                }
            }
            TextButton(onClick = onManage) {
                Text("Manage", color = MindFlowColors.TextMuted)
            }
        }
    }
}

@Composable
private fun DailyNoteToSelfCard(
    yesterdayNote: String?,
    onLeaveMessageForTomorrow: () -> Unit
) {
    MindFlowCard(modifier = Modifier.fillMaxWidth()) {
        Text("Daily note to self ♡", style = MaterialTheme.typography.titleLarge)

        Text("Yesterday’s note:", style = MaterialTheme.typography.labelLarge)
        Text(
            text = yesterdayNote ?: "(No note from yesterday)",
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
private fun BreathingCard(navController: NavController) {
    var expanded by remember { mutableStateOf(false) }
    var selectedExercise by remember { mutableStateOf<BreathingExercise?>(null) }

    MindFlowCard(modifier = Modifier.fillMaxWidth()) {
        Text("Take a moment to relax", style = MaterialTheme.typography.titleLarge)

        Column {
            // Tapping the pill toggles dropdown
            PillRowSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
            ) {
                Text(
                    text = selectedExercise?.name ?: "Choose breathing exercise",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MindFlowColors.TextMuted,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MindFlowColors.TextMuted
                )

                Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        selectedExercise?.let { ex ->
                            navController.navigate("breathing/${ex.id}")
                        }
                    },
                    enabled = selectedExercise != null
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        tint = if (selectedExercise != null) MindFlowColors.Primary else MindFlowColors.TextMuted
                    )
                }
            }

            // Dropdown list
            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    breathingExercises.forEach { exercise ->
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MindFlowColors.TextSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedExercise = exercise
                                    expanded = false
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    }
}
