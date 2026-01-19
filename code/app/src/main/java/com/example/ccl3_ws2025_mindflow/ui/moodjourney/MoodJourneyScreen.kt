package com.example.ccl3_ws2025_mindflow.ui.moodjourney

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material.icons.outlined.SentimentNeutral
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.SentimentVeryDissatisfied
import androidx.compose.material.icons.outlined.SentimentVerySatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.ccl3_ws2025_mindflow.R
import com.example.ccl3_ws2025_mindflow.data.mood.MoodType
import com.example.ccl3_ws2025_mindflow.ui.theme.Dimens
import com.example.ccl3_ws2025_mindflow.ui.theme.MindFlowColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MoodJourneyScreen(
    navController: NavController,
    viewModel: MoodJourneyViewModel
) {
    val state by viewModel.uiState.collectAsState()

    // incoming dateKey = yyyy-MM-dd
    val inFormatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }
    // small, friendly display
    val outFormatter = remember {
        DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Background image
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

                    // zig-zag positioning
                    val offsetX = if (index % 2 == 0) (-55).dp else 55.dp

                    // format date safely
                    val formattedDate = runCatching {
                        LocalDate.parse(day.dateKey, inFormatter).format(outFormatter)
                    }.getOrElse { day.dateKey }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(
                            modifier = Modifier.offset(x = offsetX),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            MoodNode(mood = day.mood)

                            Spacer(modifier = Modifier.height(6.dp))

                            // tiny date under icon
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.labelSmall,
                                color = MindFlowColors.TextMuted,
                                textAlign = TextAlign.Center
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
                tint = MindFlowColors.OnPrimary
            )
        }
    }
}

@Composable
private fun MoodNode(mood: MoodType) {
    val circleSize = when (mood) {
        MoodType.VERY_SAD -> 52.dp
        MoodType.SAD -> 56.dp
        MoodType.NEUTRAL -> 60.dp
        MoodType.HAPPY -> 66.dp
        MoodType.VERY_HAPPY -> 70.dp
    }

    val iconSize = when (mood) {
        MoodType.VERY_SAD -> 28.dp
        MoodType.SAD -> 30.dp
        MoodType.NEUTRAL -> 32.dp
        MoodType.HAPPY -> 36.dp
        MoodType.VERY_HAPPY -> 40.dp
    }

    Box(
        modifier = Modifier
            .size(circleSize),
        contentAlignment = Alignment.Center
    ) {
        // Lilipad background
        Image(
            painter = painterResource(id = R.drawable.lilipad),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Emoji icon on top
        Icon(
            imageVector = moodToIcon(mood),
            contentDescription = null,
            tint = Color.White, // emoji is white
            modifier = Modifier.size(iconSize)
        )
    }
}


private fun moodToIcon(mood: MoodType): ImageVector {
    return when (mood) {
        MoodType.VERY_SAD -> Icons.Outlined.SentimentVeryDissatisfied
        MoodType.SAD -> Icons.Outlined.SentimentDissatisfied
        MoodType.NEUTRAL -> Icons.Outlined.SentimentNeutral
        MoodType.HAPPY -> Icons.Outlined.SentimentSatisfied
        MoodType.VERY_HAPPY -> Icons.Outlined.SentimentVerySatisfied
    }
}
