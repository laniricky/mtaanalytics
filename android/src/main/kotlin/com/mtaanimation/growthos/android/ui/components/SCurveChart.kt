package com.mtaanimation.growthos.android.ui.components

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtaanimation.growthos.android.ui.theme.BrandMuted
import com.mtaanimation.growthos.android.ui.theme.BrandOrange
import com.mtaanimation.growthos.shared.projection.ProjectionPoint
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.time.Instant
import java.time.ZoneOffset

/**
 * S-Curve chart displaying a 12-month rolling window (4 months back, 8 forward).
 *
 * Two lines are rendered:
 *   1. S-Curve Target (blue) — the logistic growth path computed by the backend's
 *      calculateFixedLogisticProjection() formula. This is NOT a linear projection;
 *      it follows the same sigmoid curve that defines the 10-year journey targets.
 *   2. Your Progress (orange) — actual follower counts logged by the user each week
 *      (aggregated per calendar month by the backend).
 *
 * Y-axis is dynamically clamped to max(currentActual, 8-month milestone) × 1.1
 * so the chart always zooms into the relevant range rather than showing the
 * full 10-year scale (which would crush early-stage data).
 *
 * Cubic bezier interpolation (via Vico's DefaultPointConnector) ensures the
 * S-curve shape is smooth between monthly data points.
 */
@Composable
fun SCurveChart(
    allPoints: List<ProjectionPoint>,
    modifier: Modifier = Modifier
) {
    // ── Rolling 12-month window ────────────────────────────────────────────
    val now = Instant.now()
    val nowYearMonth = now.atZone(ZoneOffset.UTC).let { it.year * 12 + it.monthValue }

    fun ProjectionPoint.yearMonthIndex(): Int =
        Instant.ofEpochMilli(dateEpochMillis).atZone(ZoneOffset.UTC)
            .let { it.year * 12 + it.monthValue }

    val windowStart = nowYearMonth - 4
    val windowEnd   = nowYearMonth + 8

    val windowedPoints = allPoints.filter { it.yearMonthIndex() in windowStart..windowEnd }
    if (windowedPoints.isEmpty()) return

    // ── Dynamic Y-axis ceiling ─────────────────────────────────────────────
    // Ceiling = whichever is higher (8-month forward milestone or current actual) + 10% headroom.
    // This keeps the chart zoomed into the near-term range instead of the distant 2036 peak.
    val futureMilestone = windowedPoints
        .filter { it.yearMonthIndex() >= nowYearMonth }
        .maxOfOrNull { it.projectedValue } ?: windowedPoints.last().projectedValue

    val maxActual = windowedPoints.mapNotNull { it.actualValue }.maxOrNull() ?: 0L
    val yMax = (maxOf(futureMilestone, maxActual) * 1.1).toFloat()

    // ── Build Vico entry lists ─────────────────────────────────────────────
    // Projected: all 13 monthly S-curve points in the window.
    // x = window index so both series share the same axis.
    val projectedEntries = windowedPoints.mapIndexed { i, pt ->
        FloatEntry(x = i.toFloat(), y = pt.projectedValue.toFloat().coerceAtMost(yMax))
    }

    // Actual: only months where the user has logged data, at the matching x position.
    val actualEntries = windowedPoints.mapIndexedNotNull { i, pt ->
        pt.actualValue?.let { FloatEntry(x = i.toFloat(), y = it.toFloat()) }
    }

    val hasActual = actualEntries.isNotEmpty()

    val chartEntryModel = remember(projectedEntries, actualEntries) {
        if (hasActual) entryModelOf(projectedEntries, actualEntries)
        else entryModelOf(projectedEntries)
    }

    // ── Line specs ────────────────────────────────────────────────────────
    // S-Curve Target: muted blue, thinner — this is the "goal rail"
    val projectedSpec = lineSpec(
        lineColor = Color(0xFF6B9ECC),
        lineThickness = 2.dp,
    )

    // Your Progress: brand orange, thicker — this is where you actually are
    val actualSpec = lineSpec(
        lineColor = BrandOrange,
        lineThickness = 3.dp,
    )

    val chart = if (hasActual) {
        lineChart(lines = listOf(projectedSpec, actualSpec))
    } else {
        lineChart(lines = listOf(projectedSpec))
    }

    // ── Y-axis formatter (K / M abbreviations) ────────────────────────────
    val yFormatter = AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
        when {
            value >= 1_000_000f -> "${"%.1f".format(value / 1_000_000f)}M"
            value >= 1_000f     -> "${"%.0f".format(value / 1_000f)}K"
            else                -> value.toInt().toString()
        }
    }

    // ── Compose layout: legend + chart ───────────────────────────────────
    Column(modifier = modifier) {
        // Legend row
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            ChartLegendItem(color = Color(0xFF6B9ECC), label = "S-Curve Target")
            ChartLegendItem(color = BrandOrange,       label = "Your Progress")
        }

        Chart(
            chart = chart,
            model = chartEntryModel,
            startAxis = rememberStartAxis(
                valueFormatter = yFormatter,
                itemPlacer = AxisItemPlacer.Vertical.default(maxItemCount = 5)
            ),
            bottomAxis = rememberBottomAxis(
                itemPlacer = AxisItemPlacer.Horizontal.default(spacing = 2)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )
    }
}

@Composable
private fun ChartLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = BrandMuted,
                fontSize = 10.sp
            )
        )
    }
}
