package com.mtaanimation.growthos.android.ui.episodes

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mtaanimation.growthos.android.ui.navigation.AppBottomNavBar
import com.mtaanimation.growthos.android.ui.theme.*
import com.mtaanimation.growthos.shared.models.episodes.EpisodeDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodesScreen(
    navController: NavController,
    viewModel: EpisodesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Content Episodes", color = BrandWhite) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandCharcoal)
            )
        },
        bottomBar = { AppBottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = BrandOrange,
                contentColor = BrandCharcoal
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Episode")
            }
        },
        containerColor = BrandCharcoal
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(200)) },
                label = "episodes_content"
            ) { state ->
                when {
                    state.isLoading && state.episodes.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = BrandOrange)
                        }
                    }
                    state.error != null && state.episodes.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = BrandBehind, modifier = Modifier.size(48.dp))
                                Text(state.error, color = BrandMuted)
                                Button(onClick = { viewModel.fetchEpisodes() }, colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)) {
                                    Text("Retry", color = BrandCharcoal)
                                }
                            }
                        }
                    }
                    state.episodes.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("No episodes created yet", style = MaterialTheme.typography.titleMedium.copy(color = BrandMuted))
                                Text("Tap + to add your first episode", style = MaterialTheme.typography.bodySmall.copy(color = BrandMuted))
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                TotalViewsCard(
                                    totalViews = state.episodes.sumOf { it.totalViews },
                                    episodeCount = state.episodes.size
                                )
                            }
                            items(state.episodes) { episode ->
                                EpisodeCard(
                                    episode = episode,
                                    onClick = { navController.navigate("episode_detail/${episode.id}") }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateEpisodeDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, description ->
                viewModel.createEpisode(title, description, Instant.now().toEpochMilli())
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun EpisodeCard(episode: EpisodeDto, onClick: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy").withZone(ZoneId.systemDefault())
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BrandSurface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(episode.title, style = MaterialTheme.typography.titleMedium.copy(color = BrandWhite, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(4.dp))
            if (!episode.description.isNullOrBlank()) {
                Text(episode.description!!, style = MaterialTheme.typography.bodySmall.copy(color = BrandMuted), maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text("Published: ${formatter.format(Instant.ofEpochMilli(episode.publishedAt))}", style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted))
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(episode.totalViews.fmtCompact(), style = MaterialTheme.typography.titleLarge.copy(color = BrandAhead, fontWeight = FontWeight.Bold))
            Text("Total Views", style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted))
        }
    }
}

@Composable
private fun TotalViewsCard(totalViews: Long, episodeCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        BrandOrange.copy(alpha = 0.18f),
                        BrandOnTrack.copy(alpha = 0.10f)
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BrandOrange.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = null,
                    tint = BrandOrange,
                    modifier = Modifier.size(26.dp)
                )
            }
            Column {
                Text(
                    "TOTAL VIEWS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = BrandMuted,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    totalViews.fmtCompact(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = BrandWhite,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                episodeCount.toString(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = BrandAhead,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                "Episodes",
                style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEpisodeDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BrandSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BrandSurfaceVariant)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Icon + Header
            Box(
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(BrandOrange.copy(alpha = 0.3f), BrandOrange.copy(alpha = 0.1f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Movie,
                    contentDescription = null,
                    tint = BrandOrange,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                "New Episode",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = BrandWhite,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                "Give your episode a name and optional description.",
                style = MaterialTheme.typography.bodySmall.copy(color = BrandMuted),
                modifier = Modifier.padding(bottom = 28.dp)
            )

            // Title field
            Text(
                "EPISODE TITLE",
                style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted, letterSpacing = 1.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("e.g. Ojode", color = BrandMuted) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    keyboardType = KeyboardType.Text
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = BrandWhite,
                    unfocusedTextColor = BrandWhite,
                    focusedBorderColor = BrandOrange,
                    unfocusedBorderColor = BrandSurfaceVariant,
                    cursorColor = BrandOrange
                ),
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    color = BrandWhite,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            )

            // Description field
            Text(
                "DESCRIPTION  •  OPTIONAL",
                style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted, letterSpacing = 1.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("What is this episode about?", color = BrandMuted) },
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = BrandWhite,
                    unfocusedTextColor = BrandWhite,
                    focusedBorderColor = BrandOrange,
                    unfocusedBorderColor = BrandSurfaceVariant,
                    cursorColor = BrandOrange
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )

            // Save button
            Button(
                onClick = { onConfirm(title.trim(), description.trim().takeIf { it.isNotBlank() }) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    "Create Episode",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = BrandCharcoal,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

private fun Long.fmtCompact(): String = when {
    this >= 1_000_000 -> "%.2fM".format(this / 1_000_000.0)
    this >= 1_000 -> "%.1fK".format(this / 1_000.0)
    else -> this.toString()
}
