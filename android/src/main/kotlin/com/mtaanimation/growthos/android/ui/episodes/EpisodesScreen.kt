package com.mtaanimation.growthos.android.ui.episodes

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
            Text(episode.totalViews.fmtCompact(), style = MaterialTheme.typography.titleLarge.copy(color = BrandOnTrack, fontWeight = FontWeight.Bold))
            Text("Total Views", style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted))
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

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BrandSurface,
        title = {
            Text("New Episode", style = MaterialTheme.typography.titleLarge.copy(color = BrandOrange, fontWeight = FontWeight.Bold))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Episode Title", style = MaterialTheme.typography.labelSmall) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BrandWhite,
                        unfocusedTextColor = BrandWhite,
                        focusedBorderColor = BrandOrange,
                        unfocusedBorderColor = BrandSurfaceVariant,
                        focusedLabelColor = BrandOrange,
                        unfocusedLabelColor = BrandMuted,
                        cursorColor = BrandOrange
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)", style = MaterialTheme.typography.labelSmall) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BrandWhite,
                        unfocusedTextColor = BrandWhite,
                        focusedBorderColor = BrandOrange,
                        unfocusedBorderColor = BrandSurfaceVariant,
                        focusedLabelColor = BrandOrange,
                        unfocusedLabelColor = BrandMuted,
                        cursorColor = BrandOrange
                    ),
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, description.takeIf { it.isNotBlank() }) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
            ) {
                Text("Create", color = BrandCharcoal, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = BrandMuted) }
        }
    )
}

private fun Long.fmtCompact(): String = when {
    this >= 1_000_000 -> "%.2fM".format(this / 1_000_000.0)
    this >= 1_000 -> "%.1fK".format(this / 1_000.0)
    else -> this.toString()
}
