package com.example.ccl3_ws2025_mindflow.ui.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ccl3_ws2025_mindflow.data.tasks.TaskEntity
import com.example.ccl3_ws2025_mindflow.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    navController: NavController,
    viewModel: TaskViewModel
) {
    val tasks by viewModel.observeAllTasks().collectAsState(initial = emptyList())

    MindFlowBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Task editor", style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("addEditTask/-1") },
                    containerColor = MindFlowColors.Primary,
                    contentColor = MindFlowColors.OnPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        ) { padding ->

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No tasks yet.\nTap + to add your first task.",
                        color = MindFlowColors.TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(Dimens.ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        MindFlowCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("addEditTask/${task.id}") }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(task.title, style = MaterialTheme.typography.titleMedium)

                                if (task.isOneTime) {
                                    OneTimeScheduleLine(dateKey = task.oneTimeDateKey)
                                } else {
                                    // Your original “nice row” of days
                                    TaskWeekdaysRow(daysCsv = task.daysCsv)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OneTimeScheduleLine(dateKey: String?) {
    val formattedDate = dateKey?.let {
        // Parse the dateKey as a LocalDate object and format it as "day/month/year"
        try {
            val date = LocalDate.parse(it) // Assumes date is in "yyyy-MM-dd" format
            date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (e: Exception) {
            // Return the original string if parsing fails (fallback)
            it
        }
    } ?: "No date" // Fallback in case dateKey is null

    AssistChip(
        onClick = { /* no-op */ },
        label = {
            Text(
                text = "Scheduled for: $formattedDate",
                style = MaterialTheme.typography.labelLarge
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MindFlowColors.Surface,
            labelColor = MindFlowColors.TextPrimary
        ),
        border = BorderStroke(1.dp, MindFlowColors.Transparent)
    )
}

@Composable
private fun TaskWeekdaysRow(daysCsv: String) {
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val selected = daysCsv
        .split(",")
        .mapNotNull { it.trim().toIntOrNull() }
        .toSet() // 1..7

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEachIndexed { index, label ->
            val isSelected = selected.contains(index + 1)

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isSelected) MindFlowColors.TextPrimary else MindFlowColors.TextMuted
            )
        }
    }
}
