package com.mtaanimation.growthos.android.ui.customgoals

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomGoalsScreen(
    navController: NavController,
    viewModel: CustomGoalsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Milestones", color = BrandWhite) },
                actions = {
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = BrandOrange)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandCharcoal)
            )
        },
        bottomBar = { AppBottomNavBar(navController) },
        containerColor = BrandCharcoal
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(200)) },
                label = "milestones_content"
            ) { state ->
                when (state) {
                    is MilestonesUiState.Loading -> LoadingState()
                    is MilestonesUiState.Error -> ErrorState(state.message) { viewModel.loadData() }
                    is MilestonesUiState.Success -> MilestonesContent(state.categories)
                }
            }
        }
    }
}

@Composable
private fun MilestonesContent(categories: List<CategoryProgress>) {
    val activeCategories = categories.filter { it.activeTier != null }
    val allCompleted = categories.flatMap { cat ->
        cat.completedTiers.map { tier -> cat to tier }
    }.sortedByDescending { (_, tier) -> tier.target }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // ── ACTIVE MILESTONES ───────────────────────────────────────────
        if (activeCategories.isNotEmpty()) {
            item {
                Text(
                    "ACTIVE MILESTONES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = BrandMuted,
                        letterSpacing = 1.5f.sp
                    ),
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }
            items(activeCategories) { progress ->
                ActiveMilestoneCard(progress)
            }
        }

        // ── COMPLETED ──────────────────────────────────────────────────
        if (allCompleted.isNotEmpty()) {
            item {
                Text(
                    "COMPLETED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = BrandAhead,
                        letterSpacing = 1.5f.sp
                    ),
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                )
            }
            items(allCompleted) { (cat, tier) ->
                CompletedMilestonePill(emoji = cat.category.emoji, label = tier.label)
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun ActiveMilestoneCard(progress: CategoryProgress) {
    val tier = progress.activeTier ?: return
    val fraction = progress.progressFraction
    val prevTarget = progress.completedTiers.lastOrNull()?.target ?: 0.0

    val progressColor = when {
        fraction >= 0.75f -> BrandOnTrack
        fraction >= 0.4f  -> BrandOrange
        else              -> BrandBehind
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BrandSurface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(progress.category.emoji, fontSize = 28.sp)
                Column {
                    Text(
                        progress.category.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted, letterSpacing = 1.sp)
                    )
                    Text(
                        tier.label,
                        style = MaterialTheme.typography.titleMedium.copy(color = BrandWhite, fontWeight = FontWeight.Bold)
                    )
                }
            }
            // Progress % pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(progressColor.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "${(fraction * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium.copy(color = progressColor, fontWeight = FontWeight.Bold)
                )
            }
        }

        // Progress bar
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = BrandSurfaceVariant
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    progress.currentValue.fmtCompact(progress.category.key),
                    style = MaterialTheme.typography.labelSmall.copy(color = progressColor, fontWeight = FontWeight.SemiBold)
                )
                Text(
                    "Target: ${tier.target.fmtCompact(progress.category.key)}",
                    style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted)
                )
            }
        }

        // Unlocks next badge
        if (progress.completedTiers.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(BrandAhead))
                Text(
                    "Prev: ${progress.completedTiers.last().label} ✓",
                    style = MaterialTheme.typography.labelSmall.copy(color = BrandAhead)
                )
            }
        }

        // Live indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
            Text(
                "Live — auto-tracked",
                style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted)
            )
        }
    }
}

@Composable
private fun CompletedMilestonePill(emoji: String, label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BrandSurface.copy(alpha = 0.6f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(emoji, fontSize = 18.sp)
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium.copy(color = BrandMuted)
            )
        }
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = "Completed",
            tint = BrandAhead,
            modifier = Modifier.size(20.dp)
        )
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = BrandBehind, modifier = Modifier.size(48.dp))
            Text(text = message, color = BrandMuted)
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)) {
                Text("Retry", color = BrandCharcoal)
            }
        }
    }
}

/** Format a raw number into a compact label, with $ prefix for REVENUE. */
private fun Double.fmtCompact(categoryKey: String): String {
    val prefix = if (categoryKey == "REVENUE") "$" else ""
    val num = this.toLong()
    return prefix + when {
        num >= 1_000_000_000 -> "%.1fB".format(num / 1_000_000_000.0)
        num >= 1_000_000     -> "%.1fM".format(num / 1_000_000.0)
        num >= 1_000         -> "%.1fK".format(num / 1_000.0)
        else                 -> num.toString()
    }
}
