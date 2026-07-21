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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtaanimation.growthos.android.ui.theme.*
import com.mtaanimation.growthos.shared.projection.GrowthStatus
import com.mtaanimation.growthos.shared.projection.PlatformProjection
import kotlin.math.abs

/**
 * Platform card showing the platform name, current followers, 2036 target,
 * an animated linear progress bar, delta analytics (variance vs today's S-curve milestone),
 * and a velocity comparison (actual vs required monthly growth).
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

    val accentColor = platformAccentColor(platform.platformType)

    // Delta color: green if ahead, red if behind, muted if on-track / unknown
    val deltaColor = when {
        platform.varianceFollowers > 0 -> BrandAhead
        platform.varianceFollowers < 0 -> BrandBehind
        else -> BrandMuted
    }
    val deltaPrefix = if (platform.varianceFollowers >= 0) "+" else ""
    val deltaText = "$deltaPrefix${platform.variancePercentage.formatPct()}% ($deltaPrefix${platform.varianceFollowers.formatFollowers()})"

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(BrandSurface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header row: platform name + status badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
            StatusBadge(status = platform.status)
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
                Text(text = "current", style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = platform.target2036.formatFollowers(),
                    style = MaterialTheme.typography.titleMedium.copy(color = BrandMuted)
                )
                Text(text = "target 2036", style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted))
            }
        }

        // Animated progress bar
        LinearProgressIndicator(
            progress = animatedProgress.value,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = accentColor,
            trackColor = BrandDivider,
            strokeCap = StrokeCap.Round
        )

        // Delta Analytics: vs today's S-Curve milestone
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(deltaColor.copy(alpha = 0.08f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "vs Milestone Today",
                    style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted)
                )
                Text(
                    text = deltaText,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = deltaColor
                    )
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Milestone",
                    style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted)
                )
                Text(
                    text = platform.milestoneTargetFollowers.formatFollowers(),
                    style = MaterialTheme.typography.labelLarge.copy(color = BrandMuted)
                )
            }
        }

        // Velocity comparison: actual monthly gain vs required monthly gain
        val actualGain = platform.actualMonthlyGain
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            VelocityChip(
                label = "Actual/Month",
                value = actualGain?.let { "${it.toLong().formatFollowers()}/mo" } ?: "No data",
                color = when {
                    actualGain == null -> BrandMuted
                    actualGain >= platform.requiredMonthlyGain -> BrandAhead
                    else -> BrandBehind
                }
            )
            VelocityChip(label = "Required/Month", value = "${platform.requiredMonthlyGain.formatFollowers()}/mo", color = BrandMuted)
            VelocityChip(label = "Required/Day", value = "${platform.requiredDailyGain.formatFollowers()}/day", color = BrandMuted)
        }
        
        // S-Curve Chart — passes the full 120-month list; the component handles windowing internally
        if (platform.monthlyProjectionPoints.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "12-Month Growth Curve",
                style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted)
            )
            com.mtaanimation.growthos.android.ui.components.SCurveChart(
                allPoints = platform.monthlyProjectionPoints
            )
        }
    }
}

@Composable
private fun VelocityChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold, color = color)
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted, fontSize = 10.sp))
    }
}

private fun Long.formatFollowers(): String = when {
    this >= 1_000_000 -> "%.2fM".format(this / 1_000_000.0)
    this >= 1_000 -> "%.1fK".format(this / 1_000.0)
    else -> this.toString()
}

private fun Double.formatPct(): String = "%.1f".format(this)

private fun platformAccentColor(platformType: String): Color = when (platformType.uppercase()) {
    "YOUTUBE"   -> Color(0xFFFF0000)
    "TIKTOK"    -> Color(0xFF00F2EA)
    "FACEBOOK"  -> Color(0xFF1877F2)
    "INSTAGRAM" -> Color(0xFFE1306C)
    "X"         -> Color(0xFF9EC4F7)
    else        -> BrandOrange
}
