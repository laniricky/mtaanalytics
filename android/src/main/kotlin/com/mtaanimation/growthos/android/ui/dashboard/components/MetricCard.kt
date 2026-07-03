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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mtaanimation.growthos.android.ui.theme.BrandDivider
import com.mtaanimation.growthos.android.ui.theme.BrandMuted
import com.mtaanimation.growthos.android.ui.theme.BrandSurface
import com.mtaanimation.growthos.android.ui.theme.BrandWhite

/**
 * Reusable metric tile used across the dashboard for numbers like
 * "Remaining", "Monthly Target", "Daily Target", "Projected Finish".
 *
 * [accentColor] tints the top border gradient for visual variety.
 */
@Composable
fun MetricCard(
    label: String,
    value: String,
    subLabel: String? = null,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BrandSurface)
    ) {
        // Accent top-border via a thin gradient strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(accentColor, accentColor.copy(alpha = 0f))
                    )
                )
        )
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = BrandWhite,
                    fontWeight = FontWeight.Bold
                )
            )
            if (subLabel != null) {
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
