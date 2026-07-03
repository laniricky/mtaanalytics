package com.mtaanimation.growthos.android.ui.dashboard.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mtaanimation.growthos.android.ui.theme.*
import com.mtaanimation.growthos.shared.projection.GrowthStatus

/**
 * Pill-shaped status badge that animates its background colour when [status] changes.
 * AHEAD → BrandAhead (green)
 * ON_TRACK → BrandOnTrack (blue)
 * BEHIND → BrandBehind (red)
 * UNKNOWN → BrandMuted (grey)
 */
@Composable
fun StatusBadge(status: GrowthStatus, modifier: Modifier = Modifier) {
    val targetColor = when (status) {
        GrowthStatus.AHEAD -> BrandAhead
        GrowthStatus.ON_TRACK -> BrandOnTrack
        GrowthStatus.BEHIND -> BrandBehind
        GrowthStatus.UNKNOWN -> BrandMuted
    }
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(400),
        label = "status_color"
    )
    val icon: ImageVector = when (status) {
        GrowthStatus.AHEAD -> Icons.Default.TrendingUp
        GrowthStatus.ON_TRACK -> Icons.Default.TrendingFlat
        GrowthStatus.BEHIND -> Icons.Default.TrendingDown
        GrowthStatus.UNKNOWN -> Icons.Default.TrendingFlat
    }
    val label = status.name.replace("_", " ")

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(animatedColor.copy(alpha = 0.18f))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = animatedColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                color = animatedColor,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}
