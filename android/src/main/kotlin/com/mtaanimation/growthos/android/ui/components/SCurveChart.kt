package com.mtaanimation.growthos.android.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mtaanimation.growthos.shared.projection.ProjectionPoint
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.time.Instant
import java.time.ZoneOffset

/**
 * S-Curve chart showing a 12-month rolling window (4 months back, 8 months forward).
 *
 * - X-axis: month index within the window (0–11)
 * - Y-axis: dynamically clamped to max(currentActual, milestone8MonthsForward) × 1.1
 *   so the chart is always zoomed into the relevant range.
 * - Labels: formatted as K / M to avoid raw large numbers.
 *
 * Accepts the full list of ProjectionPoints from the backend and performs all
 * windowing and Y-axis calculation internally.
 */
@Composable
fun SCurveChart(
    allPoints: List<ProjectionPoint>,
    modifier: Modifier = Modifier
) {
    // --- Build rolling 12-month window ---
    val now = Instant.now()
    val nowYearMonth = run {
        val zdt = now.atZone(ZoneOffset.UTC)
        zdt.year * 12 + zdt.monthValue
    }

    // Each ProjectionPoint has a dateEpochMillis; convert to year-month index for comparison
    fun ProjectionPoint.yearMonthIndex(): Int {
        val zdt = Instant.ofEpochMilli(dateEpochMillis).atZone(ZoneOffset.UTC)
        return zdt.year * 12 + zdt.monthValue
    }

    // Window: [nowYearMonth - 4, nowYearMonth + 8] inclusive → 13 months
    val windowStart = nowYearMonth - 4
    val windowEnd = nowYearMonth + 8

    val windowedPoints = allPoints.filter {
        val ym = it.yearMonthIndex()
        ym in windowStart..windowEnd
    }

    if (windowedPoints.isEmpty()) return

    // --- Dynamic Y-axis ceiling ---
    // Find the milestone value 8 months from now (the furthest point we show in the future)
    val futureMilestone = windowedPoints
        .filter { it.yearMonthIndex() >= nowYearMonth }
        .maxOfOrNull { it.projectedValue }
        ?: windowedPoints.last().projectedValue

    val maxActual = windowedPoints.mapNotNull { it.actualValue }.maxOrNull() ?: 0L

    // Ceiling = whichever is higher, plus 10% headroom
    val yMax = (maxOf(futureMilestone, maxActual) * 1.1).toFloat()

    // --- Build Vico entry lists ---
    val projectedEntries = windowedPoints.mapIndexed { i, pt ->
        FloatEntry(x = i.toFloat(), y = pt.projectedValue.toFloat().coerceAtMost(yMax))
    }

    // Actual entries — only for months that have been logged
    val actualEntries = windowedPoints.mapIndexedNotNull { i, pt ->
        pt.actualValue?.let { FloatEntry(x = i.toFloat(), y = it.toFloat()) }
    }

    val chartEntryModel = remember(projectedEntries, actualEntries) {
        if (actualEntries.isNotEmpty()) {
            entryModelOf(projectedEntries, actualEntries)
        } else {
            entryModelOf(projectedEntries)
        }
    }

    // --- Y-axis formatter: abbreviate to K / M ---
    val yFormatter = AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
        when {
            value >= 1_000_000f -> "${"%.1f".format(value / 1_000_000f)}M"
            value >= 1_000f     -> "${"%.0f".format(value / 1_000f)}K"
            else                -> value.toInt().toString()
        }
    }

    Chart(
        chart = lineChart(),
        model = chartEntryModel,
        startAxis = rememberStartAxis(
            valueFormatter = yFormatter,
            itemPlacer = AxisItemPlacer.Vertical.default(maxItemCount = 5)
        ),
        bottomAxis = rememberBottomAxis(
            itemPlacer = AxisItemPlacer.Horizontal.default(spacing = 2)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
    )
}
