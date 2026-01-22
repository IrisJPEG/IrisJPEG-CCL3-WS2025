package com.example.ccl3_ws2025_mindflow.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material.icons.outlined.SentimentNeutral
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.SentimentVeryDissatisfied
import androidx.compose.material.icons.outlined.SentimentVerySatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ccl3_ws2025_mindflow.data.mood.MoodType
import com.example.ccl3_ws2025_mindflow.ui.theme.Dimens
import com.example.ccl3_ws2025_mindflow.ui.theme.MindFlowColors

@Composable
fun MoodOverlay(
    onSelect: (MoodType) -> Unit,
    onDismissOnce: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(Dimens.CardRadius),
            colors = CardDefaults.cardColors(containerColor = MindFlowColors.Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .widthIn(min = 280.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "How are you feeling today?",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MoodIconButton(Icons.Outlined.SentimentVeryDissatisfied) { onSelect(MoodType.VERY_SAD) }
                    MoodIconButton(Icons.Outlined.SentimentDissatisfied) { onSelect(MoodType.SAD) }
                    MoodIconButton(Icons.Outlined.SentimentNeutral) { onSelect(MoodType.NEUTRAL) }
                    MoodIconButton(Icons.Outlined.SentimentSatisfied) { onSelect(MoodType.HAPPY) }
                    MoodIconButton(Icons.Outlined.SentimentVerySatisfied) { onSelect(MoodType.VERY_HAPPY) }
                }

                // Optional
                TextButton(onClick = onDismissOnce, modifier = Modifier.align(Alignment.End)) {
                    Text("Not now", color = MindFlowColors.TextMuted)
                }
            }
        }
    }
}

@Composable
private fun MoodIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
        Icon(icon, contentDescription = null, tint = MindFlowColors.TextPrimary, modifier = Modifier.size(34.dp))
    }
}
