package com.example.ccl3_ws2025_mindflow.ui.breathing

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun BreathingScreen(exercise: BreathingExercise) {
    var phase by remember { mutableStateOf(true) } // true = inhale, false = exhale

    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (phase) exercise.breathInDuration * 1000 else exercise.breathOutDuration * 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    // alternate inhale/exhale every duration
    LaunchedEffect(Unit) {
        while (true) {
            delay(exercise.breathInDuration * 1000L)
            phase = false
            delay(exercise.breathOutDuration * 1000L)
            phase = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = exercise.name, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(text = exercise.instructions, style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(32.dp))

            Box(contentAlignment = Alignment.Center) {
                listOf(100.dp, 70.dp, 40.dp).forEach { size ->
                    Box(
                        modifier = Modifier
                            .size(size)
                            .clip(CircleShape)
                            .alpha(0.3f)
                            .background(Color.Blue)
                            .graphicsLayer() {
                                this.scaleX = scale
                                this.scaleY = scale
                            }
                    )
                }
            }
        }
    }
}
