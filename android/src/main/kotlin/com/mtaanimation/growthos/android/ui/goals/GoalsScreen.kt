package com.mtaanimation.growthos.android.ui.goals

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mtaanimation.growthos.android.ui.navigation.AppBottomNavBar
import com.mtaanimation.growthos.android.ui.theme.*
import com.mtaanimation.growthos.shared.projection.DashboardProjection
import com.mtaanimation.growthos.shared.projection.PlatformProjection
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    navController: NavController,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val submitState by viewModel.submitState.collectAsState()
    val dashboardState by viewModel.dashboardState.collectAsState()

    var selectedPlatform by remember { mutableStateOf("YOUTUBE") }
    var currentFollowersInput by remember { mutableStateOf("") }

    val platforms = listOf("YOUTUBE", "TIKTOK", "FACEBOOK", "INSTAGRAM", "X")

    val platformProjection: PlatformProjection? = dashboardState?.platformProjections?.find { it.platformType == selectedPlatform }

    LaunchedEffect(submitState) {
        if (submitState is TrackingSubmitState.Success) {
            currentFollowersInput = ""
            kotlinx.coroutines.delay(2000)
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weekly Check-In", color = BrandWhite) },
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Platform Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BrandSurface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                platforms.forEach { platform ->
                    val isSelected = selectedPlatform == platform
                    TextButton(
                        onClick = { 
                            selectedPlatform = platform 
                            currentFollowersInput = ""
                        },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (isSelected) BrandOrange.copy(alpha = 0.2f) else Color.Transparent,
                            contentColor = if (isSelected) BrandOrange else BrandMuted
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = platform.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    }
                }
            }

            // Context Mini-Card
            if (platformProjection != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BrandSurface.copy(alpha = 0.5f))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Current Record", style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted))
                        Text(
                            platformProjection.currentFollowers.formatFollowers(),
                            style = MaterialTheme.typography.titleMedium.copy(color = BrandWhite, fontWeight = FontWeight.Bold)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Milestone Today", style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted))
                        Text(
                            platformProjection.milestoneTargetFollowers.formatFollowers(),
                            style = MaterialTheme.typography.titleMedium.copy(color = BrandWhite, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Massive Centered Input Display
            val displayValue = if (currentFollowersInput.isEmpty()) "0" else currentFollowersInput.toLongOrNull()?.formatFollowersWithCommas() ?: "0"
            Text(
                text = displayValue,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentFollowersInput.isEmpty()) BrandMuted else BrandWhite
                )
            )

            // Real-Time Delta Chip
            val inputLong = currentFollowersInput.toLongOrNull() ?: 0L
            val milestone = platformProjection?.milestoneTargetFollowers ?: 0L
            if (inputLong > 0 && milestone > 0) {
                val delta = inputLong - milestone
                val deltaColor = if (delta >= 0) BrandAhead else BrandBehind
                val prefix = if (delta >= 0) "+" else ""
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(deltaColor.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "$prefix${delta.formatFollowersWithCommas()} vs milestone",
                        style = MaterialTheme.typography.labelLarge.copy(color = deltaColor, fontWeight = FontWeight.SemiBold)
                    )
                }
            } else {
                Text("Enter total followers", style = MaterialTheme.typography.labelMedium.copy(color = BrandMuted))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Custom Numpad
            Numpad(
                onKeyPress = { key ->
                    if (currentFollowersInput.length < 12) {
                        currentFollowersInput += key
                    }
                },
                onBackspace = {
                    if (currentFollowersInput.isNotEmpty()) {
                        currentFollowersInput = currentFollowersInput.dropLast(1)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status message & Submit Button
            when (val state = submitState) {
                is TrackingSubmitState.Error -> {
                    Text(
                        text = state.message,
                        color = BrandBehind,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                TrackingSubmitState.Success -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandAhead)
                        Text("Stats saved!", color = BrandAhead)
                    }
                }
                else -> {}
            }

            Button(
                onClick = {
                    val followers = currentFollowersInput.toLongOrNull() ?: 0L
                    if (followers > 0) {
                        viewModel.recordWeeklyStats(
                            platformType = selectedPlatform,
                            currentFollowers = followers,
                            dateEpochMillis = Instant.now().toEpochMilli()
                        )
                    }
                },
                enabled = inputLong > 0 && submitState !is TrackingSubmitState.Submitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (submitState is TrackingSubmitState.Submitting) {
                    CircularProgressIndicator(color = BrandCharcoal, modifier = Modifier.size(28.dp))
                } else {
                    Text(
                        "Save Weekly Stats",
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
private fun Numpad(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit
) {
    val keys = listOf(
        "1", "2", "3",
        "4", "5", "6",
        "7", "8", "9",
        "", "0", "DEL"
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
    ) {
        items(keys) { key ->
            if (key.isEmpty()) {
                Spacer(modifier = Modifier.size(64.dp))
            } else if (key == "DEL") {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = androidx.compose.material.ripple.rememberRipple(color = BrandOrange)
                        ) { onBackspace() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Backspace, contentDescription = "Backspace", tint = BrandWhite, modifier = Modifier.size(28.dp))
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = androidx.compose.material.ripple.rememberRipple(color = BrandOrange)
                        ) { onKeyPress(key) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = key,
                        style = MaterialTheme.typography.headlineMedium.copy(color = BrandWhite, fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }
}

private fun Long.formatFollowers(): String = when {
    this >= 1_000_000 -> "%.2fM".format(this / 1_000_000.0)
    this >= 1_000 -> "%.1fK".format(this / 1_000.0)
    else -> this.toString()
}

private fun Long.formatFollowersWithCommas(): String {
    return String.format("%,d", this)
}
