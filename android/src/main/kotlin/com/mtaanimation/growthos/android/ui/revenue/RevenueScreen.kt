package com.mtaanimation.growthos.android.ui.revenue

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mtaanimation.growthos.android.ui.navigation.AppBottomNavBar
import com.mtaanimation.growthos.android.ui.theme.*
import com.mtaanimation.growthos.shared.models.revenue.RecordRevenueRequest
import com.mtaanimation.growthos.shared.models.revenue.RevenueEntryDto
import java.text.NumberFormat
import java.time.YearMonth
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueScreen(
    navController: NavController,
    viewModel: RevenueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    var showLogDialog by remember { mutableStateOf(false) }

    LaunchedEffect(submitState) {
        if (submitState is RevenueSubmitState.Success) {
            showLogDialog = false
            kotlinx.coroutines.delay(1500)
            viewModel.resetSubmitState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revenue Engine", color = BrandWhite) },
                actions = {
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = BrandOrange)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandCharcoal)
            )
        },
        bottomBar = { AppBottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showLogDialog = true },
                containerColor = BrandOrange,
                contentColor = BrandCharcoal
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Revenue")
            }
        },
        containerColor = BrandCharcoal
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
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

    if (showLogDialog) {
        LogRevenueDialog(
            submitState = submitState,
            onDismiss = { showLogDialog = false },
            onConfirm = { request -> viewModel.recordRevenue(request) }
        )
    }
}

@Composable
private fun RevenueContent(state: RevenueUiState.Success) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero: All-Time Revenue
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(BrandOnTrack.copy(alpha = 0.25f), BrandSurface)))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("ALL-TIME REVENUE", style = MaterialTheme.typography.labelMedium.copy(color = BrandMuted, letterSpacing = 1.5.sp))
                    Text(
                        state.totalAllTime.fmt(),
                        style = MaterialTheme.typography.displayMedium.copy(color = BrandOnTrack, fontWeight = FontWeight.Bold)
                    )
                    if (state.lastMonthRevenue > 0) {
                        Text(
                            "Last month: ${state.lastMonthRevenue.fmt()}",
                            style = MaterialTheme.typography.bodySmall.copy(color = BrandMuted)
                        )
                    }
                }
            }
        }

        // ARPU Metrics Row
        if (state.arpuPer1000 > 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        label = "ARPU / 1K Followers",
                        value = state.arpuPer1000.fmt(),
                        subtitle = "Current monetization rate",
                        color = BrandOrange,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Projected at 55M",
                        value = state.projected2036Monthly.fmtCompact(),
                        subtitle = "Monthly revenue in 2036",
                        color = BrandAhead,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Audience context
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BrandSurface)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(20.dp))
                        Column {
                            Text("Current Audience", style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted))
                            Text(state.currentAudience.fmtCompact(), style = MaterialTheme.typography.titleSmall.copy(color = BrandWhite, fontWeight = FontWeight.Bold))
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Needed to 55M", style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted))
                        Text(
                            (55_000_000L - state.currentAudience).coerceAtLeast(0L).fmtCompact(),
                            style = MaterialTheme.typography.titleSmall.copy(color = BrandBehind, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Section: Revenue History
        if (state.history.isNotEmpty()) {
            item {
                Text(
                    "MONTHLY BREAKDOWN",
                    style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted, letterSpacing = 1.5.sp),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            items(state.history) { entry ->
                RevenueMonthCard(entry)
            }
        } else {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("No revenue logged yet", style = MaterialTheme.typography.titleMedium.copy(color = BrandMuted))
                        Text("Tap + to add your first month", style = MaterialTheme.typography.bodySmall.copy(color = BrandMuted))
                    }
                }
            }
        }

        // Bottom padding for FAB
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun MetricCard(label: String, value: String, subtitle: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BrandSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted))
        Text(value, style = MaterialTheme.typography.titleLarge.copy(color = color, fontWeight = FontWeight.Bold))
        Text(subtitle, style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted.copy(alpha = 0.7f)))
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
            Text(entry.monthYear, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BrandWhite))
            Text(entry.totalRevenue.fmt(), style = MaterialTheme.typography.titleLarge.copy(color = BrandOnTrack, fontWeight = FontWeight.Bold))
        }

        HorizontalDivider(color = BrandDivider)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            RevenueSourceCol("YouTube", entry.youtubeRevenue)
            RevenueSourceCol("TikTok", entry.tiktokRevenue)
            RevenueSourceCol("Facebook", entry.facebookRevenue)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            RevenueSourceCol("Instagram", entry.instagramRevenue)
            RevenueSourceCol("Sponsors", entry.sponsors)
            RevenueSourceCol("Merch", entry.merchandise)
        }
        if (entry.websiteIncome + entry.otherIncome > 0) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                RevenueSourceCol("Website", entry.websiteIncome)
                RevenueSourceCol("Other", entry.otherIncome)
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RevenueSourceCol(label: String, amount: Double) {
    Column(modifier = Modifier.width(90.dp)) {
        Text(amount.fmt(), style = MaterialTheme.typography.bodyMedium.copy(color = if (amount > 0) BrandWhite else BrandMuted))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogRevenueDialog(
    submitState: RevenueSubmitState,
    onDismiss: () -> Unit,
    onConfirm: (RecordRevenueRequest) -> Unit
) {
    // Default to current month
    val currentMonth = YearMonth.now().toString() // "2026-07"

    var monthYear by remember { mutableStateOf(currentMonth) }
    var youtube by remember { mutableStateOf("") }
    var tiktok by remember { mutableStateOf("") }
    var facebook by remember { mutableStateOf("") }
    var instagram by remember { mutableStateOf("") }
    var sponsors by remember { mutableStateOf("") }
    var merch by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var other by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BrandSurface,
        title = {
            Text(
                "Log Monthly Revenue",
                style = MaterialTheme.typography.titleLarge.copy(color = BrandOrange, fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RevenueInputField("Month (YYYY-MM)", monthYear, false) { monthYear = it }
                RevenueInputField("YouTube AdSense", youtube) { youtube = it }
                RevenueInputField("TikTok Revenue", tiktok) { tiktok = it }
                RevenueInputField("Facebook Revenue", facebook) { facebook = it }
                RevenueInputField("Instagram Revenue", instagram) { instagram = it }
                RevenueInputField("Sponsorships", sponsors) { sponsors = it }
                RevenueInputField("Merchandise", merch) { merch = it }
                RevenueInputField("Website / Courses", website) { website = it }
                RevenueInputField("Other Income", other) { other = it }

                if (submitState is RevenueSubmitState.Error) {
                    Text(submitState.message, color = BrandBehind, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        RecordRevenueRequest(
                            monthYear = monthYear,
                            youtubeRevenue = youtube.toDoubleOrNull() ?: 0.0,
                            tiktokRevenue = tiktok.toDoubleOrNull() ?: 0.0,
                            facebookRevenue = facebook.toDoubleOrNull() ?: 0.0,
                            instagramRevenue = instagram.toDoubleOrNull() ?: 0.0,
                            sponsors = sponsors.toDoubleOrNull() ?: 0.0,
                            merchandise = merch.toDoubleOrNull() ?: 0.0,
                            websiteIncome = website.toDoubleOrNull() ?: 0.0,
                            otherIncome = other.toDoubleOrNull() ?: 0.0
                        )
                    )
                },
                enabled = monthYear.isNotBlank() && submitState !is RevenueSubmitState.Submitting,
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
            ) {
                if (submitState is RevenueSubmitState.Submitting) {
                    CircularProgressIndicator(color = BrandCharcoal, modifier = Modifier.size(18.dp))
                } else {
                    Text("Save Revenue", color = BrandCharcoal, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = BrandMuted) }
        }
    )
}

@Composable
private fun RevenueInputField(label: String, value: String, isNumeric: Boolean = true, onValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        singleLine = true,
        keyboardOptions = if (isNumeric) KeyboardOptions(keyboardType = KeyboardType.Decimal) else KeyboardOptions.Default,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = BrandWhite,
            unfocusedTextColor = BrandWhite,
            focusedBorderColor = BrandOrange,
            unfocusedBorderColor = BrandSurfaceVariant,
            focusedLabelColor = BrandOrange,
            unfocusedLabelColor = BrandMuted,
            cursorColor = BrandOrange
        )
    )
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
            Text(message, color = BrandMuted)
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)) {
                Text("Retry", color = BrandCharcoal)
            }
        }
    }
}

private fun Double.fmt(): String = NumberFormat.getCurrencyInstance(Locale.US).format(this)
private fun Double.fmtCompact(): String {
    return when {
        this >= 1_000_000 -> "$" + "%.1fM".format(this / 1_000_000)
        this >= 1_000 -> "$" + "%.1fK".format(this / 1_000)
        else -> NumberFormat.getCurrencyInstance(Locale.US).format(this)
    }
}
private fun Long.fmtCompact(): String = when {
    this >= 1_000_000 -> "%.2fM".format(this / 1_000_000.0)
    this >= 1_000 -> "%.1fK".format(this / 1_000.0)
    else -> this.toString()
}
