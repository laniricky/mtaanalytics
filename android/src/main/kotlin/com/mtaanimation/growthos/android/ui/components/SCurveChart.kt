package com.mtaanimation.growthos.android.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf

/**
 * A generic chart component for plotting projected vs actual values over time.
 * Uses the Vico charting library.
 */
@Composable
fun SCurveChart(
    projectedPoints: List<Float>,
    actualPoints: List<Float>,
    modifier: Modifier = Modifier
) {
    // Vico expects entries as (x, y) pairs. 
    // We map the list indices to x coordinates.
    val projectedEntries = projectedPoints.mapIndexed { index, value -> 
        FloatEntry(x = index.toFloat(), y = value) 
    }
    
    val actualEntries = actualPoints.mapIndexed { index, value -> 
        FloatEntry(x = index.toFloat(), y = value) 
    }

    // Vico can handle multiple lines if we provide multiple lists of entries to entryModelOf
    val chartEntryModel = remember(projectedEntries, actualEntries) {
        if (actualEntries.isNotEmpty()) {
            entryModelOf(projectedEntries, actualEntries)
        } else {
            entryModelOf(projectedEntries)
        }
    }

    Chart(
        chart = lineChart(),
        model = chartEntryModel,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(),
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
    )
}
