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
import com.example.ccl3_ws2025_mindflow.data.tasks.TaskEntity
import com.example.ccl3_ws2025_mindflow.ui.theme.*
import java.util.Locale
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

    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var title by remember { mutableStateOf("") }
    val selectedDays = remember { mutableStateListOf<Int>() }

    var repeatOnlyOnce by remember { mutableStateOf(false) }

    // date fields (allow typing + dropdown)
    var dayText by remember { mutableStateOf("") }
    var monthText by remember { mutableStateOf("") }
    var yearText by remember { mutableStateOf("") }

    // Track if user has interacted with the fields
    var dayInteracted by remember { mutableStateOf(false) }
    var monthInteracted by remember { mutableStateOf(false) }
    var yearInteracted by remember { mutableStateOf(false) }

    LaunchedEffect(taskId) {
        if (taskId != -1L) {
            val t = viewModel.getTaskById(taskId)
            taskToEdit = t
            title = t?.title ?: ""

            repeatOnlyOnce = t?.isOneTime ?: false

            selectedDays.clear()
            if (repeatOnlyOnce) {
                val key = t?.oneTimeDateKey
                if (!key.isNullOrBlank() && key.length == 10) {
                    yearText = key.substring(0, 4)
                    monthText = key.substring(5, 7).toIntOrNull()?.toString() ?: ""
                    dayText = key.substring(8, 10).toIntOrNull()?.toString() ?: ""
                }
            } else {
                t?.daysCsv
                    ?.split(",")
                    ?.mapNotNull { it.trim().toIntOrNull() }
                    ?.let { selectedDays.addAll(it) }
            }
        }
    }

    fun isLeapYear(y: Int): Boolean =
        (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)

    fun daysInMonth(y: Int, m: Int): Int = when (m) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(y)) 29 else 28
        else -> 31
    }

    val yearInt = yearText.trim().toIntOrNull()
    val monthInt = monthText.trim().toIntOrNull()
    val dayInt = dayText.trim().toIntOrNull()

    val maxDay: Int? =
        if (yearInt != null && monthInt != null && monthInt in 1..12) daysInMonth(yearInt, monthInt)
        else null

    // gentle clamp only if day is numeric and too large
    LaunchedEffect(yearInt, monthInt) {
        val max = maxDay ?: return@LaunchedEffect
        val d = dayText.trim().toIntOrNull() ?: return@LaunchedEffect
        if (d > max) dayText = max.toString()
    }

    val dateError: String? = if (!repeatOnlyOnce) {
        null
    } else {
        when {
            yearInt == null || yearInt !in 1900..2100 -> "Enter a valid year"
            monthInt == null || monthInt !in 1..12 -> "Enter a valid month (1–12)"
            dayInt == null -> "Enter a valid day"
            maxDay != null && (dayInt < 1 || dayInt > maxDay) -> "That month only has $maxDay days"
            else -> null
        }
    }

    fun buildOneTimeDateKeyOrNull(): String? {
        if (dateError != null) return null
        val y = yearInt ?: return null
        val m = monthInt ?: return null
        val d = dayInt ?: return null
        return String.format(Locale.US, "%04d-%02d-%02d", y, m, d)
    }

    val oneTimeDateKey = if (repeatOnlyOnce) buildOneTimeDateKeyOrNull() else null

    val saveEnabled =
        title.isNotBlank() && (
                if (repeatOnlyOnce) oneTimeDateKey != null
                else selectedDays.isNotEmpty()
                )

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
                        checked = repeatOnlyOnce,
                        onCheckedChange = { checked ->
                            repeatOnlyOnce = checked

                            if (checked) {
                                // switching to one-time: clear weekly selection
                                selectedDays.clear()
                            } else {
                                // switching back to weekly: clear one-time fields
                                dayText = ""
                                monthText = ""
                                yearText = ""

                                dayInteracted = false
                                monthInteracted = false
                                yearInteracted = false
                            }
                        }
                    )
                }

                // --- One-time date input (only when checked) ---
                if (repeatOnlyOnce) {
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
                                value = dayText,
                                onValueChange = {
                                    dayText = it
                                    dayInteracted = true
                                },
                                options = buildList {
                                    val max = maxDay ?: 31
                                    for (d in 1..max) add(d.toString())
                                },
                                modifier = Modifier.weight(1f)
                            )

                            NumberDropdownField(
                                label = "Month",
                                value = monthText,
                                onValueChange = {
                                    monthText = it
                                    monthInteracted = true
                                },
                                options = (1..12).map { it.toString() },
                                modifier = Modifier.weight(1f)
                            )

                            NumberDropdownField(
                                label = "Year",
                                value = yearText,
                                onValueChange = {
                                    yearText = it
                                    yearInteracted = true
                                },
                                options = (2020..2035).map { it.toString() },
                                modifier = Modifier.weight(1.3f)
                            )
                        }

                        if ((dayInteracted || monthInteracted || yearInteracted) && dateError != null) {
                            Text(
                                text = dateError,
                                color = MindFlowColors.Danger,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // --- WEEKLY (only when NOT one-time) ---
                if (!repeatOnlyOnce) {
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
                                val selected = selectedDays.contains(dayValue)

                                TextButton(
                                    onClick = {
                                        if (selected) selectedDays.remove(dayValue)
                                        else selectedDays.add(dayValue)
                                    },
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
                            viewModel.upsertTask(
                                id = if (taskId == -1L) 0L else taskId,
                                title = title,
                                selectedDays = selectedDays,
                                repeatOnlyOnce = repeatOnlyOnce,
                                oneTimeDateKey = oneTimeDateKey
                            )
                            navController.popBackStack()
                        },
                        enabled = saveEnabled,
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
