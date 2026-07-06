package com.mtaanimation.growthos.android.ui.goals

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mtaanimation.growthos.android.ui.navigation.AppBottomNavBar
import com.mtaanimation.growthos.android.ui.theme.*
import com.mtaanimation.growthos.shared.projection.PlatformProjection
import java.time.Instant

private val PLATFORMS = listOf("YOUTUBE", "TIKTOK", "FACEBOOK", "INSTAGRAM", "X")
private val PLATFORM_LABELS = mapOf(
    "YOUTUBE" to "YouTube",
    "TIKTOK" to "TikTok",
    "FACEBOOK" to "Facebook",
    "INSTAGRAM" to "Instagram",
    "X" to "X (Twitter)"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    navController: NavController,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val submitState by viewModel.submitState.collectAsState()
    val dashboardState by viewModel.dashboardState.collectAsState()

    // Map from platform -> input string (persists as you switch tabs)
    var inputsMap by remember { mutableStateOf(PLATFORMS.associateWith { "" }) }
    var selectedPlatform by remember { mutableStateOf("YOUTUBE") }

    val platformProjection: PlatformProjection? =
        dashboardState?.platformProjections?.find { it.platformType == selectedPlatform }

    val currentInput = inputsMap[selectedPlatform] ?: ""
    val inputLong = currentInput.toLongOrNull() ?: 0L
    val milestone = platformProjection?.milestoneTargetFollowers ?: 0L
    val filledCount = inputsMap.values.count { it.isNotEmpty() }

    LaunchedEffect(submitState) {
        if (submitState is TrackingSubmitState.Success) {
            // Clear inputs after successful save
            inputsMap = PLATFORMS.associateWith { "" }
            kotlinx.coroutines.delay(2000)
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Weekly Check-In", color = BrandWhite, style = MaterialTheme.typography.titleMedium)
                        if (filledCount > 0) {
                            Text(
                                "$filledCount of ${PLATFORMS.size} platforms entered",
                                color = BrandOrange,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandCharcoal)
            )
        },
        bottomBar = { AppBottomNavBar(navController) },
        containerColor = BrandCharcoal
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Platform Tabs ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BrandSurface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PLATFORMS.forEach { platform ->
                    val isSelected = selectedPlatform == platform
                    val hasValue = inputsMap[platform]?.isNotEmpty() == true
                    val tabColor by animateColorAsState(
                        if (isSelected) BrandOrange else if (hasValue) BrandAhead.copy(alpha = 0.3f) else Color.Transparent,
                        animationSpec = tween(200), label = "tab_color"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(tabColor)
                            .clickable { selectedPlatform = platform }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = platform.take(3),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) BrandCharcoal else if (hasValue) BrandAhead else BrandMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── Context Card ───────────────────────────────────────
            if (platformProjection != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BrandSurface.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Last Recorded", style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted))
                        Text(
                            platformProjection.currentFollowers.fmtCompact(),
                            style = MaterialTheme.typography.titleMedium.copy(color = BrandWhite, fontWeight = FontWeight.Bold)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Milestone Today", style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted))
                        Text(
                            platformProjection.milestoneTargetFollowers.fmtCompact(),
                            style = MaterialTheme.typography.titleMedium.copy(color = BrandOrange, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(42.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Big Number Display ─────────────────────────────────
            val displayValue = if (currentInput.isEmpty()) "—" else inputLong.fmtWithCommas()
            AnimatedContent(targetState = displayValue, label = "numDisplay") { display ->
                Text(
                    text = display,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentInput.isEmpty()) BrandMuted else BrandWhite
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ── Real-Time Delta Chip ───────────────────────────────
            if (inputLong > 0 && milestone > 0) {
                val delta = inputLong - milestone
                val chipColor = if (delta >= 0) BrandAhead else BrandBehind
                val prefix = if (delta >= 0) "+" else ""
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(chipColor.copy(alpha = 0.15f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        "$prefix${delta.fmtWithCommas()} vs S-Curve milestone",
                        style = MaterialTheme.typography.labelLarge.copy(color = chipColor, fontWeight = FontWeight.SemiBold)
                    )
                }
            } else {
                Text(
                    "Enter current ${PLATFORM_LABELS[selectedPlatform]} followers",
                    style = MaterialTheme.typography.labelMedium.copy(color = BrandMuted),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Custom Numpad ──────────────────────────────────────
            Numpad(
                onKeyPress = { key ->
                    val cur = inputsMap[selectedPlatform] ?: ""
                    if (cur.length < 12) {
                        inputsMap = inputsMap + (selectedPlatform to cur + key)
                    }
                },
                onBackspace = {
                    val cur = inputsMap[selectedPlatform] ?: ""
                    if (cur.isNotEmpty()) {
                        inputsMap = inputsMap + (selectedPlatform to cur.dropLast(1))
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Status & Save Button ───────────────────────────────
            when (val state = submitState) {
                is TrackingSubmitState.Error ->
                    Text(state.message, color = BrandBehind, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                TrackingSubmitState.Success ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandAhead, modifier = Modifier.size(18.dp))
                        Text("All stats saved!", color = BrandAhead, style = MaterialTheme.typography.bodyMedium)
                    }
                else -> {}
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val statsToSave = inputsMap.mapValues { it.value.toLongOrNull() ?: 0L }.filter { it.value > 0 }
                    viewModel.recordBatchStats(statsToSave, Instant.now().toEpochMilli())
                },
                enabled = filledCount > 0 && submitState !is TrackingSubmitState.Submitting,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (submitState is TrackingSubmitState.Submitting) {
                    CircularProgressIndicator(color = BrandCharcoal, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        if (filledCount > 1) "Save All $filledCount Platforms" else "Save Check-In",
                        color = BrandCharcoal,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun Numpad(onKeyPress: (String) -> Unit, onBackspace: () -> Unit) {
    val keys = listOf("1","2","3","4","5","6","7","8","9","","0","DEL")
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        userScrollEnabled = false
    ) {
        items(keys) { key ->
            when {
                key.isEmpty() -> Spacer(modifier = Modifier.height(56.dp))
                key == "DEL" -> Box(
                    modifier = Modifier.height(56.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(BrandSurface)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = androidx.compose.material.ripple.rememberRipple(color = BrandOrange)
                        ) { onBackspace() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Backspace, contentDescription = "Backspace", tint = BrandWhite, modifier = Modifier.size(22.dp))
                }
                else -> Box(
                    modifier = Modifier.height(56.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(BrandSurface)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = androidx.compose.material.ripple.rememberRipple(color = BrandOrange)
                        ) { onKeyPress(key) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(key, style = MaterialTheme.typography.titleLarge.copy(color = BrandWhite, fontWeight = FontWeight.Medium))
                }
            }
        }
    }
}

private fun Long.fmtCompact(): String = when {
    this >= 1_000_000_000 -> "%.2fB".format(this / 1_000_000_000.0)
    this >= 1_000_000 -> "%.2fM".format(this / 1_000_000.0)
    this >= 1_000 -> "%.1fK".format(this / 1_000.0)
    else -> this.toString()
}

private fun Long.fmtWithCommas(): String = String.format("%,d", this)
