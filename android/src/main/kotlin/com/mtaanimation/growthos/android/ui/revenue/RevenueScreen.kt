package com.mtaanimation.growthos.android.ui.revenue

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mtaanimation.growthos.android.ui.theme.*
import com.mtaanimation.growthos.shared.models.revenue.RevenueEntryDto
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueScreen(
    navController: NavController,
    viewModel: RevenueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState !is RevenueUiState.Loading) isRefreshing = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revenue", color = BrandWhite) },
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
                label = "revenue_content"
            ) { state ->
                when (state) {
                    is RevenueUiState.Loading -> LoadingState()
                    is RevenueUiState.Error -> ErrorState(state.message) { viewModel.loadData() }
                    is RevenueUiState.Success -> RevenueContent(state)
                }
            }
        }
    }
}

@Composable
private fun RevenueContent(state: RevenueUiState.Success) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // All-Time Revenue Hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(BrandOnTrack.copy(alpha = 0.2f), BrandViolet.copy(alpha = 0.1f))
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ALL-TIME REVENUE", style = MaterialTheme.typography.labelMedium.copy(color = BrandMuted))
                    Text(
                        text = state.totalAllTime.formatCurrency(),
                        style = MaterialTheme.typography.displayMedium.copy(
                            color = BrandOnTrack,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        items(state.history) { entry ->
            RevenueMonthCard(entry)
        }
    }
}

@Composable
private fun RevenueMonthCard(entry: RevenueEntryDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BrandSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = entry.monthYear,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = entry.totalRevenue.formatCurrency(),
                style = MaterialTheme.typography.titleLarge.copy(color = BrandOnTrack, fontWeight = FontWeight.Bold)
            )
        }
        
        Divider(color = BrandDivider)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            RevenueSourceCol("YouTube", entry.youtubeRevenue)
            RevenueSourceCol("Sponsors", entry.sponsors)
            RevenueSourceCol("Merch", entry.merchandise)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            RevenueSourceCol("TikTok", entry.tiktokRevenue)
            RevenueSourceCol("Facebook", entry.facebookRevenue)
            RevenueSourceCol("Other", entry.otherIncome + entry.websiteIncome + entry.instagramRevenue)
        }
    }
}

@Composable
private fun RevenueSourceCol(label: String, amount: Double) {
    Column {
        Text(text = amount.formatCurrency(), style = MaterialTheme.typography.bodyMedium.copy(color = BrandWhite))
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted))
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

private fun Double.formatCurrency(): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(this)
}
