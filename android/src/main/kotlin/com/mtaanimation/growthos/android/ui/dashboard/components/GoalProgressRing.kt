package com.mtaanimation.growthos.android.ui.dashboard.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtaanimation.growthos.android.ui.theme.BrandCyan
import com.mtaanimation.growthos.android.ui.theme.BrandDivider
import com.mtaanimation.growthos.android.ui.theme.BrandViolet
import com.mtaanimation.growthos.android.ui.theme.BrandWhite

/**
 * Animated circular progress ring used as the primary goal indicator on the dashboard.
 *
 * Animates from 0 to [percentage] on composition using an eased 1200ms animation.
 * Renders a gradient arc (cyan → violet) on a dark track ring.
 */
@Composable
fun GoalProgressRing(
    percentage: Double,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 14.dp,
    ringSize: Dp = 180.dp
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(percentage) {
        animatedProgress.animateTo(
            targetValue = percentage.toFloat().coerceIn(0f, 100f),
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(ringSize)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val diameter = size.minDimension - strokePx
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)
            val startAngle = -90f
            val sweepAngle = (animatedProgress.value / 100f) * 360f

            // Track (background ring)
            drawArc(
                color = BrandDivider,
                startAngle = startAngle,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Gradient progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(BrandCyan, BrandViolet, BrandCyan),
                ),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        // Centre text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${animatedProgress.value.toInt()}%",
                style = MaterialTheme.typography.headlineLarge.copy(
                    brush = Brush.horizontalGradient(listOf(BrandCyan, BrandViolet)),
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp
                )
            )
            Text(
                text = "complete",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
