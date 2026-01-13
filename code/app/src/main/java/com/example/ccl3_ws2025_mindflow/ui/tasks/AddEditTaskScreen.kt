package com.example.ccl3_ws2025_mindflow.ui.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    navController: NavController,
    viewModel: TaskViewModel,
    taskId: Long
) {
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var title by remember { mutableStateOf("") }
    val selectedDays = remember { mutableStateListOf<Int>() }


    LaunchedEffect(taskId) {
        if (taskId != -1L) {
            val t = viewModel.getTaskById(taskId)
            taskToEdit = t
            title = t?.title ?: ""
            selectedDays.clear()
            t?.daysCsv
                ?.split(",")
                ?.mapNotNull { it.trim().toIntOrNull() }
                ?.let { selectedDays.addAll(it) }
        }
    }

    MindFlowBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            if (taskId == -1L) "Create task" else "Edit task",
                            style = MaterialTheme.typography.headlineLarge
                        )
                    },
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
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = Dimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Task name") },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MindFlowColors.StrokeStrong,
                        unfocusedBorderColor = MindFlowColors.StrokeStrong,
                        focusedLabelColor = MindFlowColors.TextPrimary,
                        unfocusedLabelColor = MindFlowColors.TextPrimary,
                        cursorColor = MindFlowColors.TextPrimary
                    )
                )

                // Weekday container (fits on all screens: equal-width items)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimens.PillRadius),
                    color = MindFlowColors.Surface,
                    border = BorderStroke(1.dp, MindFlowColors.Stroke)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        daysOfWeek.forEachIndexed { index, label ->
                            val dayValue = index + 1
                            val selected = selectedDays.contains(dayValue)

                            TextButton(
                                onClick = {
                                    if (selected) selectedDays.remove(dayValue)
                                    else selectedDays.add(dayValue)
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = label,
                                    maxLines = 1,
                                    softWrap = false,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (selected)
                                        MindFlowColors.TextPrimary
                                    else
                                        MindFlowColors.TextMuted
                                )

                            }
                        }
                    }
                }


                Spacer(Modifier.weight(1f))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Dimens.BottomActionsPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PillOutlineButton(
                        text = "Save",
                        onClick = {
                            viewModel.upsertTask(
                                id = if (taskId == -1L) 0L else taskId,
                                title = title,
                                selectedDays = selectedDays
                            )
                            navController.popBackStack()
                        },
                        enabled = title.isNotBlank() && selectedDays.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = MindFlowColors.Surface,
                        textColor = MindFlowColors.OnPrimary,
                        containerColor = MindFlowColors.Primary
                    )

                    if (taskId != -1L) {
                        Button(
                            onClick = {
                                viewModel.deleteTask(taskId)
                                navController.popBackStack()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.PillHeight),
                            shape = RoundedCornerShape(Dimens.PillRadius),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MindFlowColors.Surface,
                                contentColor = MindFlowColors.Danger
                            ),
                            border = BorderStroke(2.dp, MindFlowColors.Danger)
                        ) {
                            Text(
                                "Delete",
                                style = MaterialTheme.typography.labelLarge,
                                color = MindFlowColors.Danger
                            )
                        }
                    }
                }
            }
        }
    }
}
