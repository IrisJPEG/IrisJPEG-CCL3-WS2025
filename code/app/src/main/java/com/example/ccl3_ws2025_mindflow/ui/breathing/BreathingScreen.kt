package com.example.ccl3_ws2025_mindflow.ui.breathing

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun BreathingScreen(exercise: BreathingExercise) {

    val scale = remember { Animatable(0.3f) }

    LaunchedEffect(exercise.id) {
        scale.snapTo(0.3f)

        while (true) {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = exercise.breathInDuration * 1000,
                    easing = LinearEasing
                )
            )
            scale.animateTo(
                targetValue = 0.3f,
                animationSpec = tween(
                    durationMillis = exercise.breathOutDuration * 1000,
                    easing = LinearEasing
                )
            )
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

            // Parent container that scales
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                    },
                contentAlignment = Alignment.Center
            ) {

                // Biggest circle (outer)
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2C9AB7).copy(alpha = 0.35f))
                )

                // Middle circle
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CB8C4).copy(alpha = 0.45f))
                )

                // Smallest circle (inner)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8EDAE0).copy(alpha = 0.6f))
                )
            }
        }
    }
}