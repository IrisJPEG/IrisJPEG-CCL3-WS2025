package com.example.ccl3_ws2025_mindflow.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateMapOf
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel
) {
    val weekData by viewModel.weekHistoryState.collectAsState()
    val month by viewModel.currentMonth.collectAsState()

    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    // Refresh whenever this screen is resumed (e.g., coming back after toggling tasks)
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
                    title = { Text("History", style = MaterialTheme.typography.titleLarge) },
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

                MindFlowCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.previousWeek() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Previous week",
                                tint = MindFlowColors.TextPrimary
                            )
                        }

                        Text(month, style = MaterialTheme.typography.titleLarge)

                        IconButton(onClick = { viewModel.nextWeek() }) {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "Next week",
                                tint = MindFlowColors.TextPrimary
                            )
                        }
                    }
                }

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
                                            MindFlowColors.TextPrimary   // darker + stronger
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
                                            PillRowSurface {
                                                Text(
                                                    text = row.task.title,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    modifier = Modifier.weight(1f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                if (!day.isFutureDay) {
                                                    Text(
                                                        text = if (row.isCompleted) "✓" else "✕",
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
            }
        }
    }
}
