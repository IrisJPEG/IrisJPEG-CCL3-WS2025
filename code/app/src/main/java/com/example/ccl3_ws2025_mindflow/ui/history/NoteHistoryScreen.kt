package com.example.ccl3_ws2025_mindflow.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ccl3_ws2025_mindflow.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteHistoryScreen(
    navController: NavController,
    viewModel: NoteHistoryViewModel
) {
    val weekNotes by viewModel.weekNotes.collectAsState()
    val headerLabel by viewModel.headerLabel.collectAsState()
    val isAtCurrentWeek by viewModel.isAtCurrentWeek.collectAsState()

    MindFlowBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Notes", style = MaterialTheme.typography.titleLarge) },
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

                // Header card: arrows + week label
                MindFlowCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.previousWeek() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Previous", tint = MindFlowColors.TextPrimary)
                        }

                        Text(headerLabel, style = MaterialTheme.typography.titleLarge)

                        IconButton(
                            onClick = { viewModel.nextWeek() },
                            enabled = !isAtCurrentWeek
                        ) {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "Next",
                                tint = if (isAtCurrentWeek)
                                    MindFlowColors.TextMuted
                                else
                                    MindFlowColors.TextPrimary
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(weekNotes, key = { it.isoKey }) { day ->
                        MindFlowCard(modifier = Modifier.fillMaxWidth()) {
                            Text(day.dateLabel, style = MaterialTheme.typography.titleMedium)

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = day.text ?: "No note on this day.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (day.text == null) MindFlowColors.TextMuted else MindFlowColors.TextPrimary,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}