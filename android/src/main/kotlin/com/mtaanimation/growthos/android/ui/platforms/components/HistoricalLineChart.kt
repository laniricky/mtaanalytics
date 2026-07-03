package com.mtaanimation.growthos.android.ui.platforms.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.mtaanimation.growthos.shared.models.PlatformStatsDto

/**
 * A beautiful, smooth line chart for historical platform stats.
 * Uses cubic bezier curves for a smooth line and draws a gradient fill under it.
 */
@Composable
fun HistoricalLineChart(
    stats: List<PlatformStatsDto>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (stats.size < 2) return

    val minFollowers = stats.minOf { it.currentFollowers }.toFloat()
    val maxFollowers = stats.maxOf { it.currentFollowers }.toFloat()
    val minTime = stats.minOf { it.dateRecordedEpochMillis }.toFloat()
    val maxTime = stats.maxOf { it.dateRecordedEpochMillis }.toFloat()

    Canvas(modifier = modifier.fillMaxSize().padding(top = 16.dp, bottom = 16.dp)) {
        val width = size.width
        val height = size.height

        // Calculate points
        val points = stats.map { stat ->
            val x = if (maxTime == minTime) width / 2f else {
                ((stat.dateRecordedEpochMillis.toFloat() - minTime) / (maxTime - minTime)) * width
            }
            val y = if (maxFollowers == minFollowers) height / 2f else {
                height - (((stat.currentFollowers.toFloat() - minFollowers) / (maxFollowers - minFollowers)) * height)
            }
            Offset(x, y)
        }

        // Create smooth path
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val previous = points[i - 1]
                val current = points[i]
                
                // Control points for bezier curve
                val controlPointX = (previous.x + current.x) / 2f
                
                cubicTo(
                    controlPointX, previous.y,
                    controlPointX, current.y,
                    current.x, current.y
                )
            }
        }

        // Draw gradient fill
        val fillPath = Path().apply {
            addPath(path)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.3f),
                    Color.Transparent
                )
            )
        )

        // Draw line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Draw points
        points.forEach { point ->
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = point
            )
        }
    }
}
