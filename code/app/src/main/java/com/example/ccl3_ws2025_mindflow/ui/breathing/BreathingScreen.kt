package com.example.ccl3_ws2025_mindflow.ui.breathing

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ccl3_ws2025_mindflow.ui.theme.MindFlowColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingScreen(
    exercise: BreathingExercise,
    navController: NavController
) {

    val circleScale = remember { Animatable(0.6f) } // circle scale
    var helperText by remember { mutableStateOf("Breathe in") }

    val maxScale = 1f
    val minScale = 0.6f

    LaunchedEffect(exercise.id) {
        circleScale.snapTo(minScale)

        while (true) {
            // --- Inhale ---
            helperText = "Breathe in"
            circleScale.animateTo(
                targetValue = maxScale,
                animationSpec = tween(durationMillis = exercise.breathInDuration * 1000, easing = LinearEasing)
            )

            // --- Hold ---
            if (exercise.holdDuration > 0) {
                helperText = "Hold"
                circleScale.snapTo(maxScale)
                delay(exercise.holdDuration * 1000L)
            }

            // --- Exhale ---
            helperText = "Breathe out"
            circleScale.animateTo(
                targetValue = minScale,
                animationSpec = tween(durationMillis = exercise.breathOutDuration * 1000, easing = LinearEasing)
            )
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MindFlowColors.BgTop,
                        MindFlowColors.BgMid,
                        MindFlowColors.BgBottom
                    )
                )
            )
    ) {
        CenterAlignedTopAppBar(
            title = { Text(exercise.name, style = MaterialTheme.typography.titleLarge ) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = helperText,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.graphicsLayer {
                    // Scale text slightly with the circle
                    val extraScale = 0.2f
                    val scale = 1f + (circleScale.value - minScale) / (maxScale - minScale) * extraScale
                    scaleX = scale
                    scaleY = scale

                    // Fade text with the circle
                    alpha = 0.7f + (circleScale.value - minScale) / (maxScale - minScale) * 0.3f
                }
            )
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        scaleX = circleScale.value
                        scaleY = circleScale.value
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.45f))
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.6f))
                )
            }
        }
    }
}
