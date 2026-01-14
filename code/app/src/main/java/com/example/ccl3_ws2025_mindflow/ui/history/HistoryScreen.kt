package com.example.ccl3_ws2025_mindflow.ui.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.ccl3_ws2025_mindflow.ui.theme.*
import androidx.compose.ui.text.font.FontWeight
import java.time.LocalDate

private enum class HistoryViewMode { WEEK, MONTH }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel
) {
    val weekData by viewModel.weekHistoryState.collectAsState()
    val monthCells by viewModel.monthCells.collectAsState()
    val headerLabel by viewModel.headerLabel.collectAsState()

    var mode by rememberSaveable { mutableStateOf(HistoryViewMode.WEEK) }

    // Weekly expanded states
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    // Monthly selected day
    var selectedMonthDayKey by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }

    // Refresh whenever this screen is resumed
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    MindFlowBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Schedule", style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(Dimens.ScreenPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Dimens.CardGap)
            ) {

                // Header card: arrows + month label + view toggle
                MindFlowCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (mode == HistoryViewMode.WEEK) viewModel.previousWeek()
                                    else viewModel.previousMonth()
                                }
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Previous",
                                    tint = MindFlowColors.TextPrimary
                                )
                            }

                            Text(headerLabel, style = MaterialTheme.typography.titleLarge)

                            IconButton(
                                onClick = {
                                    if (mode == HistoryViewMode.WEEK) viewModel.nextWeek()
                                    else viewModel.nextMonth()
                                }
                            ) {
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = "Next",
                                    tint = MindFlowColors.TextPrimary
                                )
                            }
                        }

                        // Toggle Week / Month
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = mode == HistoryViewMode.WEEK,
                                onClick = { mode = HistoryViewMode.WEEK },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) { Text("Week") }

                            SegmentedButton(
                                selected = mode == HistoryViewMode.MONTH,
                                onClick = { mode = HistoryViewMode.MONTH },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) { Text("Month") }
                        }
                    }
                }

                if (mode == HistoryViewMode.WEEK) {
                    // ---------------- WEEK VIEW (your existing list) ----------------
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        weekData.forEach { day ->
                            val hasTasks = day.tasks.isNotEmpty()
                            val expanded = expandedStates[day.isoKey] ?: false

                            item(day.isoKey) {
                                MindFlowCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(
                                            if (hasTasks) {
                                                Modifier.clickable {
                                                    expandedStates[day.isoKey] = !expanded
                                                }
                                            } else Modifier
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(day.dateLabel, style = MaterialTheme.typography.titleMedium)

                                            val statusText = when (day.status) {
                                                DayStatus.COMPLETED -> "Completed"
                                                DayStatus.INCOMPLETE -> "Incomplete"
                                                DayStatus.SCHEDULED -> "Scheduled"
                                                DayStatus.NONE -> "No tasks"
                                            }

                                            val statusStyle = if (day.status == DayStatus.COMPLETED) {
                                                MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                            } else {
                                                MaterialTheme.typography.bodyMedium
                                            }

                                            val statusColor = if (day.status == DayStatus.COMPLETED) {
                                                MindFlowColors.TextPrimary
                                            } else {
                                                MindFlowColors.TextMuted
                                            }

                                            Text(
                                                text = statusText,
                                                style = statusStyle,
                                                color = statusColor
                                            )
                                        }

                                        if (hasTasks) {
                                            Icon(
                                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                tint = MindFlowColors.TextMuted
                                            )
                                        }
                                    }

                                    if (expanded && hasTasks) {
                                        Spacer(Modifier.height(6.dp))

                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            day.tasks.forEach { row ->
                                                // If you want NO STROKE in history tasks:
                                                // PillRowSurfaceNoStroke { ... }
                                                PillRowSurface {
                                                    Text(
                                                        text = row.task.title,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        modifier = Modifier.weight(1f),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )

                                                    // Only show ✓ when completed (no ✕)
                                                    if (!day.isFutureDay && row.isCompleted) {
                                                        Text(
                                                            text = "✓",
                                                            style = MaterialTheme.typography.labelLarge
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // ---------------- MONTH VIEW ----------------
                    // Calendar + tasks under it, inside a scrolling column
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item("calendar") {
                            MonthCalendar(
                                cells = monthCells,
                                selectedIsoKey = selectedMonthDayKey,
                                onSelectDay = { key -> selectedMonthDayKey = key }
                            )
                        }

                        val selectedCell = monthCells.firstOrNull { it.isoKey == selectedMonthDayKey }
                        val selectedTasks = selectedCell?.tasks.orEmpty()
                        val isFuture = selectedCell?.isFutureDay == true

                        item("selected_header") {
                            MindFlowCard(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = selectedCell?.date?.let { it.toString() } ?: "Select a day",
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    val statusText = when (selectedCell?.status) {
                                        DayStatus.COMPLETED -> "Completed"
                                        DayStatus.INCOMPLETE -> "Incomplete"
                                        DayStatus.SCHEDULED -> "Scheduled"
                                        DayStatus.NONE, null -> "No tasks"
                                    }

                                    val statusStyle = if (selectedCell?.status == DayStatus.COMPLETED) {
                                        MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    } else {
                                        MaterialTheme.typography.bodyMedium
                                    }

                                    val statusColor = if (selectedCell?.status == DayStatus.COMPLETED) {
                                        MindFlowColors.TextPrimary
                                    } else {
                                        MindFlowColors.TextMuted
                                    }

                                    Text(
                                        text = statusText,
                                        style = statusStyle,
                                        color = statusColor
                                    )
                                }
                            }
                        }

                        if (selectedTasks.isEmpty()) {
                            item("no_tasks") {
                                MindFlowCard(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "No tasks for this day.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MindFlowColors.TextMuted
                                    )
                                }
                            }
                        } else {
                            items(selectedTasks.size) { index ->
                                val row = selectedTasks[index]
                                // If you want NO STROKE in history tasks:
                                // PillRowSurfaceNoStroke { ... }
                                PillRowSurface {
                                    Text(
                                        text = row.task.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    // Only show ✓ when completed (no ✕)
                                    if (!isFuture && row.isCompleted) {
                                        Text(
                                            text = "✓",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }

                        item("bottom_spacer") { Spacer(Modifier.height(12.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthCalendar(
    cells: List<MonthDayState>,
    selectedIsoKey: String,
    onSelectDay: (String) -> Unit
) {
    MindFlowCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // Weekday headers (Mon start)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun").forEach {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MindFlowColors.TextMuted,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                }
            }

            // 6 rows * 7 columns = 42 cells
            cells.chunked(7).forEach { weekRow ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    weekRow.forEach { cell ->
                        val isSelected = cell.isoKey == selectedIsoKey

                        val bg = when (cell.status) {
                            DayStatus.COMPLETED -> MindFlowColors.SurfaceStrong
                            DayStatus.INCOMPLETE -> MindFlowColors.Surface
                            DayStatus.SCHEDULED -> MindFlowColors.Surface
                            DayStatus.NONE -> MindFlowColors.Surface
                        }

                        // Make out-of-month days look quieter
                        val alpha = if (cell.isInDisplayedMonth) 1f else 0.4f

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clickable { onSelectDay(cell.isoKey) },
                            color = bg.copy(alpha = alpha),
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) BorderStroke(2.dp, MindFlowColors.TextPrimary) else null,
                            tonalElevation = 0.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                contentAlignment = Alignment.TopStart
                            ) {
                                Text(
                                    text = cell.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (cell.isInDisplayedMonth) MindFlowColors.TextPrimary else MindFlowColors.TextMuted
                                )

                                // Tiny dot indicator if there are tasks
                                if (cell.tasks.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(8.dp)
                                            .padding(2.dp)
                                            .then(Modifier)
                                    ) {
                                        Surface(
                                            modifier = Modifier.fillMaxSize(),
                                            shape = RoundedCornerShape(50),
                                            color = MindFlowColors.TextPrimary.copy(alpha = alpha),
                                            tonalElevation = 0.dp
                                        ) {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
