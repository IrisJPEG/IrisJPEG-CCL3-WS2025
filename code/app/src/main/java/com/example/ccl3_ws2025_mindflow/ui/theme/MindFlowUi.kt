package com.example.ccl3_ws2025_mindflow.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.Alignment

@Composable
fun MindFlowBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
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
        // Bottom decorative “hills”
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .align(Alignment.BottomCenter)
        ) {
            // Base hill
            drawRoundRect(
                color = MindFlowColors.HillLight,
                topLeft = Offset(-120f, size.height * 0.45f),
                size = androidx.compose.ui.geometry.Size(size.width + 240f, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(260f, 260f),
                style = Fill
            )

            // Dark left cluster circles
            drawCircle(MindFlowColors.HillDark, radius = 180f, center = Offset(size.width * 0.10f, size.height * 0.72f))
            drawCircle(MindFlowColors.HillMid, radius = 120f, center = Offset(size.width * 0.18f, size.height * 0.88f))
            drawCircle(MindFlowColors.HillMid, radius = 90f, center = Offset(size.width * 0.05f, size.height * 0.90f))
        }

        content()
    }
}

@Composable
fun MindFlowCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = MindFlowColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MindFlowColors.Stroke)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SmallGap),
            content = content
        )
    }
}

@Composable
fun PillOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    borderColor: androidx.compose.ui.graphics.Color = MindFlowColors.Surface,
    textColor: androidx.compose.ui.graphics.Color = MindFlowColors.Primary,
    containerColor: androidx.compose.ui.graphics.Color = MindFlowColors.Primary
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(Dimens.PillHeight),
        shape = RoundedCornerShape(Dimens.PillRadius),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = MindFlowColors.OnPrimary),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = textColor)
    }
}

@Composable
fun PillRowSurface(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.PillRadius),
        color = MindFlowColors.SurfaceAlt,
        border = BorderStroke(1.dp, MindFlowColors.Stroke),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                // padding happens INSIDE the border → clean corners
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

