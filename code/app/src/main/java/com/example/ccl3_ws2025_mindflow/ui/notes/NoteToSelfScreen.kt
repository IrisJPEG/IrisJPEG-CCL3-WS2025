package com.example.ccl3_ws2025_mindflow.ui.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ccl3_ws2025_mindflow.ui.home.HomeViewModel
import com.example.ccl3_ws2025_mindflow.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteToSelfScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val hasExisting = !state.tomorrowNoteSavedPreview.isNullOrBlank()

    MindFlowBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Note to self for\ntomorrow ♡",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MindFlowColors.TextPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        titleContentColor = MindFlowColors.TextPrimary
                    )
                )
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = Dimens.ScreenPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Dimens.CardGap)
            ) {

                Spacer(Modifier.height(2.dp))

                // Big writing panel (white)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    color = MindFlowColors.Surface,
                    shape = RoundedCornerShape(Dimens.CardRadius),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    border = BorderStroke(1.dp, MindFlowColors.Stroke)
                ) {
                    OutlinedTextField(
                        value = state.tomorrowNoteDraft,
                        onValueChange = { viewModel.onTomorrowDraftChanged(it) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        placeholder = {
                            Text(
                                text = "Write something kind for tomorrow you…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MindFlowColors.TextMuted
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MindFlowColors.TextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                            cursorColor = MindFlowColors.Primary,
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.SmallGap),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PillOutlineButton(
                        text = if (hasExisting) "Update message" else "Save message",
                        onClick = {
                            viewModel.saveTomorrowNote()
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = MindFlowColors.Stroke,
                        textColor = MindFlowColors.Primary,
                        containerColor = MindFlowColors.Surface
                    )

                    Text(
                        text = "Tomorrow’s note will appear on Home the next day as “Yesterday’s note”.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MindFlowColors.TextPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(Dimens.BottomActionsPadding))
                }
            }
        }
    }
}
