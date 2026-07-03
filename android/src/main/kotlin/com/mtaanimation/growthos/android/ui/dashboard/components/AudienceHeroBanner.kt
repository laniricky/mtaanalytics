package com.mtaanimation.growthos.android.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mtaanimation.growthos.android.ui.theme.BrandCyan
import com.mtaanimation.growthos.android.ui.theme.BrandMuted
import com.mtaanimation.growthos.android.ui.theme.BrandSurface
import com.mtaanimation.growthos.android.ui.theme.BrandViolet
import com.mtaanimation.growthos.android.ui.theme.BrandWhite
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Hero banner showing combined audience count with a cyan/violet gradient headline,
 * the deadline date, and remaining followers/months at a glance.
 */
@Composable
fun AudienceHeroBanner(
    combinedFollowers: Long,
    combinedTarget: Long,
    remainingFollowers: Long,
    remainingMonths: Long,
    deadlineEpochMillis: Long,
    modifier: Modifier = Modifier
) {
    val deadlineDate = Instant.ofEpochMilli(deadlineEpochMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("MMMM yyyy"))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        BrandCyan.copy(alpha = 0.15f),
                        BrandViolet.copy(alpha = 0.12f)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "COMBINED AUDIENCE",
                style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted)
            )
            Text(
                text = combinedFollowers.formatLarge(),
                style = MaterialTheme.typography.displayLarge.copy(
                    brush = Brush.horizontalGradient(listOf(BrandCyan, BrandViolet)),
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "of ${combinedTarget.formatLarge()} by $deadlineDate",
                style = MaterialTheme.typography.bodyMedium.copy(color = BrandMuted)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Column {
                    Text(
                        text = remainingFollowers.formatLarge(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = BrandWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "remaining",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Column {
                    Text(
                        text = "$remainingMonths",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = BrandWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "months left",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

private fun Long.formatLarge(): String = when {
    this >= 1_000_000 -> "%.2fM".format(this / 1_000_000.0)
    this >= 1_000 -> "%.1fK".format(this / 1_000.0)
    else -> this.toString()
}
