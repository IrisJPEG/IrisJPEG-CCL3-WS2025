package com.example.ccl3_ws2025_mindflow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = MindFlowColors.Primary,
    onPrimary = MindFlowColors.OnPrimary,
    background = Color.Transparent,
    surface = MindFlowColors.Surface,
    onSurface = MindFlowColors.TextPrimary,
    secondary = MindFlowColors.TextSecondary,
    onSecondary = MindFlowColors.TextPrimary,
    outline = MindFlowColors.Stroke
)

private val DarkScheme = darkColorScheme(
    primary = MindFlowColors.Primary,
    onPrimary = MindFlowColors.OnPrimary,
    background = Color.Transparent,
    surface = MindFlowColors.Surface,
    onSurface = MindFlowColors.TextPrimary,
    outline = MindFlowColors.Stroke
)

@Composable
fun CCL3WS2025MindFlowTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = Typography,
        shapes = MindFlowShapes,
        content = content
    )
}
