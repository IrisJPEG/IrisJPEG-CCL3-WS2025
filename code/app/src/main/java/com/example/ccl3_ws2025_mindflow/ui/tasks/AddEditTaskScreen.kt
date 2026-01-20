package com.example.ccl3_ws2025_mindflow.ui.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ccl3_ws2025_mindflow.ui.theme.*
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    navController: NavController,
    viewModel: TaskViewModel,
    taskId: Long
) {
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    // Editor state now lives in the ViewModel
    val ui by viewModel.editorUiState.collectAsState()

    LaunchedEffect(taskId) {
        viewModel.startEditing(taskId)
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

            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = Dimens.ScreenPadding)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

            OutlinedTextField(
                    value = ui.title,
                    onValueChange = { viewModel.setTitle(it) },
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


                // --- Repeat only once row ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Repeat only once",
                            style = MaterialTheme.typography.titleSmall,
                            color = MindFlowColors.TextPrimary
                        )
                        Text(
                            "Choose one specific date",
                            style = MaterialTheme.typography.bodySmall,
                            color = MindFlowColors.TextMuted
                        )
                    }

                    Checkbox(
                        checked = ui.repeatOnlyOnce,
                        onCheckedChange = { checked ->
                            viewModel.setRepeatOnlyOnce(checked)
                        }
                    )
                }

                // --- One-time date input (only when checked) ---
                if (ui.repeatOnlyOnce) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Date (day / month / year)",
                            style = MaterialTheme.typography.titleSmall,
                            color = MindFlowColors.TextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            NumberDropdownField(
                                label = "Day",
                                value = ui.dayText,
                                onValueChange = {
                                    viewModel.setDayText(it)
                                    viewModel.setDayInteracted(true)
                                },
                                options = ui.dayOptions,
                                modifier = Modifier.weight(1f)
                            )

                            NumberDropdownField(
                                label = "Month",
                                value = ui.monthText,
                                onValueChange = {
                                    viewModel.setMonthText(it)
                                    viewModel.setMonthInteracted(true)
                                },
                                options = ui.monthOptions,
                                modifier = Modifier.weight(1f)
                            )

                            NumberDropdownField(
                                label = "Year",
                                value = ui.yearText,
                                onValueChange = {
                                    viewModel.setYearText(it)
                                    viewModel.setYearInteracted(true)
                                },
                                options = ui.yearOptions,
                                modifier = Modifier.weight(1.3f)
                            )
                        }

                        if ((ui.dayInteracted || ui.monthInteracted || ui.yearInteracted) && ui.dateError != null) {
                            Text(
                                text = ui.dateError!!,
                                color = MindFlowColors.Danger,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // --- WEEKLY (only when NOT one-time) ---
                if (!ui.repeatOnlyOnce) {
                    Text(
                        text = "Repeat weekly",
                        style = MaterialTheme.typography.titleSmall,
                        color = MindFlowColors.TextPrimary,
                        modifier = Modifier.padding(4.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Dimens.PillRadius),
                        color = MindFlowColors.Surface,
                        border = BorderStroke(1.dp, MindFlowColors.Stroke)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            daysOfWeek.forEachIndexed { index, label ->
                                val dayValue = index + 1
                                val selected = ui.selectedDays.contains(dayValue)

                                TextButton(
                                    onClick = { viewModel.toggleSelectedDay(dayValue) },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = label,
                                        maxLines = 1,
                                        softWrap = false,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (selected) MindFlowColors.TextPrimary else MindFlowColors.TextMuted
                                    )
                                }
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
                            viewModel.saveEditing(taskId)
                            navController.popBackStack()
                        },
                        enabled = ui.saveEnabled,
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = MindFlowColors.Surface,
                        textColor = MindFlowColors.OnPrimary,
                        containerColor = MindFlowColors.Primary
                    )
                    if (taskId != -1L) { // Only show if editing an existing task
                        var showDeleteDialog by remember { mutableStateOf(false) }

                        Button(
                            onClick = { showDeleteDialog = true },
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

                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Confirm deletion") },
                                text = { Text("Are you sure you want to delete this task?") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            showDeleteDialog = false
                                            viewModel.deleteTask(taskId)
                                            navController.popBackStack()
                                        }
                                    ) {
                                        Text("Delete", color = MindFlowColors.Danger)
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = { showDeleteDialog = false }
                                    ) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }

                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NumberDropdownField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    options: List<String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = if (value.isBlank()) " " else value,
            onValueChange = { new -> onValueChange(new.filter { it.isDigit() }) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .defaultMinSize(minWidth = 0.dp),
            label = {
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MindFlowColors.StrokeStrong,
                unfocusedBorderColor = MindFlowColors.StrokeStrong,
                focusedLabelColor = MindFlowColors.TextPrimary,
                unfocusedLabelColor = MindFlowColors.TextPrimary,
                cursorColor = MindFlowColors.TextPrimary
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 260.dp)
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                        onValueChange(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}
