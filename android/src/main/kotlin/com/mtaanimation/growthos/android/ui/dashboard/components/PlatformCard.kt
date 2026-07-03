package com.mtaanimation.growthos.android.ui.dashboard.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.clip
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mtaanimation.growthos.android.ui.theme.*
import com.mtaanimation.growthos.shared.projection.GrowthStatus
import com.mtaanimation.growthos.shared.projection.PlatformProjection

/**
 * Platform card showing the platform name, current followers, 2036 target,
 * an animated linear progress bar, and the platform-specific status badge.
 *
 * The progress bar animates from 0 on first composition.
 */
@Composable
fun PlatformCard(
    platform: PlatformProjection,
    modifier: Modifier = Modifier
) {
    val progress = if (platform.target2036 > 0)
        (platform.currentFollowers.toFloat() / platform.target2036.toFloat()).coerceIn(0f, 1f)
    else 0f

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    val statusColor = when (GrowthStatus.valueOf(platform.platformType.let {
        // Re-derive status color from the projection's status field
        platform.toString() // status is on PlatformProjection directly
        "UNKNOWN"
    })) {
        GrowthStatus.AHEAD -> BrandAhead
        GrowthStatus.ON_TRACK -> BrandOnTrack
        GrowthStatus.BEHIND -> BrandBehind
        else -> BrandMuted
    }

    // Derive platform accent color
    val accentColor = platformAccentColor(platform.platformType)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(BrandSurface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Platform dot indicator
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Text(
                    text = platform.platformType,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            StatusBadge(
                status = try { GrowthStatus.valueOf(platform.platformType) }
                catch (_: Exception) { GrowthStatus.UNKNOWN }
            )
        }

        // Follower counts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = platform.currentFollowers.formatFollowers(),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(text = "current", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = platform.target2036.formatFollowers(),
                    style = MaterialTheme.typography.titleMedium.copy(color = BrandMuted)
                )
                Text(text = "target 2036", style = MaterialTheme.typography.labelSmall)
            }
        }

        // Animated progress bar
        LinearProgressIndicator(
            progress = { animatedProgress.value },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = accentColor,
            trackColor = BrandDivider,
            strokeCap = StrokeCap.Round
        )

        // Per-platform key targets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TargetChip(label = "Monthly", value = platform.requiredMonthlyGain.formatFollowers())
            TargetChip(label = "Weekly", value = platform.requiredWeeklyGain.formatFollowers())
            TargetChip(label = "Daily", value = platform.requiredDailyGain.formatFollowers())
        }
    }
}

@Composable
private fun TargetChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.labelLarge)
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

private fun Long.formatFollowers(): String = when {
    this >= 1_000_000 -> "%.2fM".format(this / 1_000_000.0)
    this >= 1_000 -> "%.1fK".format(this / 1_000.0)
    else -> this.toString()
}

private fun platformAccentColor(platformType: String): Color = when (platformType.uppercase()) {
    "YOUTUBE" -> Color(0xFFFF0000)
    "TIKTOK" -> Color(0xFF00F2EA)
    "FACEBOOK" -> Color(0xFF1877F2)
    "INSTAGRAM" -> Color(0xFFE1306C)
    "X" -> Color(0xFF9EC4F7)
    else -> BrandCyan
}
