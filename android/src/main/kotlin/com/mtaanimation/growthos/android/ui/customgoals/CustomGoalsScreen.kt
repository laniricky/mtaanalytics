package com.mtaanimation.growthos.android.ui.customgoals

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mtaanimation.growthos.android.ui.dashboard.components.GoalProgressRing
import com.mtaanimation.growthos.android.ui.theme.*
import com.mtaanimation.growthos.shared.models.customgoals.CustomGoalDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomGoalsScreen(
    navController: NavController,
    viewModel: CustomGoalsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState !is CustomGoalsUiState.Loading) isRefreshing = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Milestone Goals", color = BrandWhite) },
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
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = BrandOrange)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandCharcoal)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Show Add Goal Dialog */ },
                containerColor = BrandOrange,
                contentColor = BrandCharcoal
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        },
        containerColor = BrandCharcoal
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
                label = "goals_content"
            ) { state ->
                when (state) {
                    is CustomGoalsUiState.Loading -> LoadingState()
                    is CustomGoalsUiState.Error -> ErrorState(state.message) { viewModel.loadData() }
                    is CustomGoalsUiState.Success -> GoalsContent(state)
                }
            }
        }
    }
}

@Composable
private fun GoalsContent(state: CustomGoalsUiState.Success) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(state.goals) { goal ->
            GoalCard(goal)
        }
    }
}

@Composable
private fun GoalCard(goal: CustomGoalDto) {
    val progress = if (goal.targetValue > 0) (goal.currentValue / goal.targetValue).toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BrandSurface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = goal.title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = BrandWhite)
        )
        
        GoalProgressRing(
            progress = progress,
            label = "${(progress * 100).toInt()}%",
            modifier = Modifier.size(100.dp)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Current: ${goal.currentValue.formatCompact()}", style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted))
            Text(text = "Target: ${goal.targetValue.formatCompact()}", style = MaterialTheme.typography.labelMedium.copy(color = BrandOrange, fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = BrandOrange)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = BrandBehind, modifier = Modifier.size(48.dp))
            Text(text = message, color = BrandMuted)
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)) {
                Text("Retry", color = BrandCharcoal)
            }
        }
    }
}

private fun Double.formatCompact(): String {
    val num = this.toLong()
    return when {
        num >= 1_000_000_000 -> String.format("%.1fB", num / 1_000_000_000.0)
        num >= 1_000_000 -> String.format("%.1fM", num / 1_000_000.0)
        num >= 1_000 -> String.format("%.1fK", num / 1_000.0)
        else -> num.toString()
    }
}
