package com.mtaanimation.growthos.android.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mtaanimation.growthos.android.ui.dashboard.components.*
import com.mtaanimation.growthos.android.ui.navigation.AppBottomNavBar
import com.mtaanimation.growthos.android.ui.navigation.Screen
import com.mtaanimation.growthos.android.ui.theme.*
import com.mtaanimation.growthos.shared.projection.DashboardProjection
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Main Dashboard screen — the CEO command centre.
 *
 * Layout (scrollable, LazyColumn):
 * 1. TopAppBar with title + refresh action
 * 2. AudienceHeroBanner — combined followers headline
 * 3. GoalProgressRing + StatusBadge — centred goal ring
 * 4. 2×2 MetricCard grid — Monthly Target, Daily Target, Projected Finish, Growth Rate
 * 5. Platform breakdown — one PlatformCard per platform
 * 6. BottomNavigationBar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState !is DashboardUiState.Loading) isRefreshing = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Growth OS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            brush = Brush.horizontalGradient(listOf(BrandOrange, BrandGray))
                        )
                    )
                },
                actions = {
                    IconButton(onClick = {
                        isRefreshing = true
                        viewModel.refresh()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh",
                            tint = BrandOrange)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandCharcoal,
                    titleContentColor = BrandWhite
                )
            )
        },
        bottomBar = { AppBottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.LogStats.route) },
                containerColor = BrandOrange,
                contentColor = BrandCharcoal
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Stats")
            }
        },
        containerColor = BrandCharcoal
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn(tween(400)) togetherWith fadeOut(tween(200))
                },
                label = "dashboard_content"
            ) { state ->
                when (state) {
                    is DashboardUiState.Loading -> LoadingState()
                    is DashboardUiState.Error -> ErrorState(
                        message = state.message,
                        onRetry = { viewModel.refresh() }
                    )
                    is DashboardUiState.Success -> DashboardContent(
                        projection = state.projection,
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    projection: DashboardProjection,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandCharcoal),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Banner
        item {
            AudienceHeroBanner(
                combinedFollowers = projection.combinedCurrentFollowers,
                combinedTarget = projection.combinedTarget,
                remainingFollowers = projection.remainingFollowers,
                remainingMonths = projection.remainingMonths,
                deadlineEpochMillis = projection.deadlineEpochMillis,
                combinedVarianceFollowers = projection.combinedVarianceFollowers,
                combinedVariancePercentage = projection.combinedVariancePercentage
            )
        }

        // 2. Goal Ring + Status
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GoalProgressRing(percentage = projection.percentageComplete)
                StatusBadge(status = projection.status)
            }
        }

        // 3. Metric cards grid
        item {
            val projectedDate = projection.projectedFinishDateEpochMillis?.let { epochMs ->
                Instant.ofEpochMilli(epochMs)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("MMM yyyy"))
            } ?: "N/A"

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "KEY TARGETS",
                    style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted),
                    modifier = Modifier.padding(start = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        label = "Monthly Target",
                        value = projection.requiredMonthlyGain.formatFollowers(),
                        subLabel = "followers / month",
                        accentColor = BrandOrange,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Daily Target",
                        value = projection.requiredDailyGain.formatFollowers(),
                        subLabel = "followers / day",
                        accentColor = BrandGray,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val finishDate = projection.projectedFinishDateEpochMillis
                    MetricCard(
                        label = "Projected Finish",
                        value = projectedDate,
                        subLabel = "at current rate",
                        accentColor = when {
                            finishDate == null -> BrandMuted
                            finishDate <= projection.deadlineEpochMillis -> BrandAhead
                            else -> BrandBehind
                        },
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Remaining",
                        value = projection.remainingFollowers.formatFollowers(),
                        subLabel = "${projection.remainingMonths} months left",
                        accentColor = BrandOnTrack,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Platform breakdown
        item {
            Text(
                text = "PLATFORM BREAKDOWN",
                style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted),
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        items(projection.platformProjections) { platform ->
            PlatformCard(platform = platform)
        }

        // Bottom spacing
        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = BrandOrange)
            Text("Loading dashboard…", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = BrandBehind,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = BrandMuted
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
            ) {
                Text("Retry", color = BrandCharcoal, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}



private fun Long.formatFollowers(): String = when {
    this >= 1_000_000 -> "%.2fM".format(this / 1_000_000.0)
    this >= 1_000 -> "%.1fK".format(this / 1_000.0)
    else -> this.toString()
}
