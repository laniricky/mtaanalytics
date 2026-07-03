package com.mtaanimation.growthos.android.ui.uploads

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mtaanimation.growthos.android.ui.theme.*
import com.mtaanimation.growthos.shared.models.uploads.UploadsEntryDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadsScreen(
    navController: NavController,
    viewModel: UploadsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val ytTarget by viewModel.ytTarget.collectAsState(initial = 2)
    val ttTarget by viewModel.ttTarget.collectAsState(initial = 5)
    val fbTarget by viewModel.fbTarget.collectAsState(initial = 3)
    val igTarget by viewModel.igTarget.collectAsState(initial = 3)
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState !is UploadsUiState.Loading) isRefreshing = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Tracker", color = BrandWhite) },
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
                label = "uploads_content"
            ) { state ->
                when (state) {
                    is UploadsUiState.Loading -> LoadingState()
                    is UploadsUiState.Error -> ErrorState(state.message) { viewModel.loadData() }
                    is UploadsUiState.Success -> UploadsContent(state, ytTarget, ttTarget, fbTarget, igTarget)
                }
            }
        }
    }
}

@Composable
private fun UploadsContent(state: UploadsUiState.Success, yt: Int, tt: Int, fb: Int, ig: Int) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(state.uploads) { entry ->
            UploadsCard(entry, yt, tt, fb, ig)
        }
    }
}

@Composable
private fun UploadsCard(entry: UploadsEntryDto, yt: Int, tt: Int, fb: Int, ig: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BrandSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Week of ${entry.weekStartDate}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BrandWhite)
        )
        
        Divider(color = BrandDivider)

        UploadProgressBar("YouTube", entry.youtubeUploads, yt, Color(0xFFFF0000))
        UploadProgressBar("TikTok", entry.tiktokUploads, tt, Color(0xFF00F2EA))
        UploadProgressBar("Facebook", entry.facebookUploads, fb, Color(0xFF1877F2))
        UploadProgressBar("Instagram", entry.instagramUploads, ig, Color(0xFFE1306C))
    }
}

@Composable
private fun UploadProgressBar(platform: String, current: Int, target: Int, color: androidx.compose.ui.graphics.Color) {
    val progress = (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = platform, style = MaterialTheme.typography.labelMedium.copy(color = BrandWhite))
            Text(text = "$current / $target", style = MaterialTheme.typography.labelMedium.copy(color = BrandMuted))
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = if (progress >= 1f) BrandAhead else color,
            trackColor = BrandSurfaceVariant
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
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = BrandBehind, modifier = Modifier.size(48.dp))
            Text(text = message, color = BrandMuted)
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)) {
                Text("Retry", color = BrandCharcoal)
            }
        }
    }
}
