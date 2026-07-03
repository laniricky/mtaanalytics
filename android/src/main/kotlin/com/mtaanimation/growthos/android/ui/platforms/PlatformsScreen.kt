package com.mtaanimation.growthos.android.ui.platforms

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mtaanimation.growthos.android.ui.dashboard.components.MetricCard
import com.mtaanimation.growthos.android.ui.dashboard.components.StatusBadge
import com.mtaanimation.growthos.android.ui.platforms.components.HistoricalLineChart
import com.mtaanimation.growthos.android.ui.theme.*
import com.mtaanimation.growthos.shared.projection.GrowthStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformsScreen(
    navController: NavController,
    viewModel: PlatformsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState !is PlatformsUiState.Loading) isRefreshing = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Platforms", color = BrandWhite) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandWhite)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isRefreshing = true
                        viewModel.loadData()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = BrandCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandDeepNavy)
            )
        },
        containerColor = BrandDeepNavy
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.loadData()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(200)) },
                label = "platforms_content"
            ) { state ->
                when (state) {
                    is PlatformsUiState.Loading -> LoadingState()
                    is PlatformsUiState.Error -> ErrorState(state.message) { viewModel.loadData() }
                    is PlatformsUiState.Success -> PlatformsContent(state.platforms)
                }
            }
        }
    }
}

@Composable
private fun PlatformsContent(platforms: List<PlatformDetailState>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(platforms) { platformDetail ->
            PlatformDetailCard(platformDetail)
        }
    }
}

@Composable
private fun PlatformDetailCard(detail: PlatformDetailState) {
    val accentColor = platformAccentColor(detail.projection.platformType)
    val status = try { GrowthStatus.valueOf(detail.projection.platformType.let {
        detail.projection.toString() // derive status
        "UNKNOWN"
    }) } catch (_: Exception) { GrowthStatus.UNKNOWN }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BrandSurface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = detail.projection.platformType,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            )
            // Hardcoded status due to projection model limitation (status is not exposed directly for platforms yet)
            StatusBadge(status = GrowthStatus.UNKNOWN) 
        }

        // Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                label = "Current",
                value = detail.projection.currentFollowers.formatFollowers(),
                accentColor = accentColor,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Target",
                value = detail.projection.target2036.formatFollowers(),
                accentColor = BrandMuted,
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                label = "Monthly Gain",
                value = detail.projection.requiredMonthlyGain.formatFollowers(),
                accentColor = accentColor.copy(alpha = 0.7f),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Daily Gain",
                value = detail.projection.requiredDailyGain.formatFollowers(),
                accentColor = accentColor.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f)
            )
        }

        // Chart Section
        if (detail.historicalStats.size >= 2) {
            Text(
                text = "HISTORICAL GROWTH",
                style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted),
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                HistoricalLineChart(
                    stats = detail.historicalStats,
                    lineColor = accentColor
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Not enough data for chart (need at least 2 entries)",
                    style = MaterialTheme.typography.labelMedium.copy(color = BrandMuted)
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = BrandCyan)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = BrandBehind, modifier = Modifier.size(48.dp))
            Text(text = message, color = BrandMuted)
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)) {
                Text("Retry", color = BrandDeepNavy)
            }
        }
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
