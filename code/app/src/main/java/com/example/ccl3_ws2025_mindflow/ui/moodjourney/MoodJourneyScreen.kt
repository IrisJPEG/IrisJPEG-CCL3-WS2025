package com.example.ccl3_ws2025_mindflow.ui.moodjourney

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.ccl3_ws2025_mindflow.R
import com.example.ccl3_ws2025_mindflow.data.mood.MoodType
import com.example.ccl3_ws2025_mindflow.ui.theme.Dimens
import com.example.ccl3_ws2025_mindflow.ui.theme.MindFlowColors

@Composable
fun MoodJourneyScreen(
    navController: NavController,
    viewModel: MoodJourneyViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {

        // PNG background
        Image(
            painter = painterResource(id = R.drawable.mood_journey_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp)
                .padding(horizontal = Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {

            if (state.days.isEmpty()) {
                item {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "No moods yet.\nPick a mood on Home to start your journey.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MindFlowColors.TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                itemsIndexed(state.days) { index, day ->
                    val offsetX = if (index % 2 == 0) (-55).dp else 55.dp

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(modifier = Modifier.offset(x = offsetX)) {
                            EmojiNode(
                                emoji = day.mood.emoji,
                                mood = day.mood
                            )
                        }
                    }
                }
            }
        }

        // Back button
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 8.dp, top = 8.dp)
                .size(44.dp)
                .zIndex(3f)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MindFlowColors.TextPrimary
            )
        }
    }
}

@Composable
private fun EmojiNode(
    emoji: String,
    mood: MoodType
) {
    // Circle size (subtle scaling)
    val circleSize = when (mood) {
        MoodType.VERY_SAD -> 52.dp
        MoodType.SAD -> 56.dp
        MoodType.NEUTRAL -> 60.dp
        MoodType.HAPPY -> 66.dp
        MoodType.VERY_HAPPY -> 70.dp
    }

    // Emoji font size (kept proportional)
    val emojiSize = when (mood) {
        MoodType.VERY_SAD -> 22.sp
        MoodType.SAD -> 24.sp
        MoodType.NEUTRAL -> 26.sp
        MoodType.HAPPY -> 30.sp
        MoodType.VERY_HAPPY -> 34.sp
    }

    Surface(
        shape = CircleShape,
        color = Color(0xFFF2F2F2),
        shadowElevation = 8.dp,
        modifier = Modifier
            .size(circleSize)
            .shadow(8.dp, CircleShape)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                style = TextStyle(
                    fontSize = emojiSize
                ),
                color = MindFlowColors.TextPrimary
            )
        }
    }
}
