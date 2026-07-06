package com.mtaanimation.growthos.android.ui.episodes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mtaanimation.growthos.android.ui.theme.*
import com.mtaanimation.growthos.shared.models.episodes.EpisodeLinkDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeDetailScreen(
    episodeId: String,
    navController: NavController,
    viewModel: EpisodesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val episode = uiState.episodes.find { it.id == episodeId }

    if (episode == null) {
        // Handle not found or loading
        Box(Modifier.fillMaxSize().background(BrandCharcoal), contentAlignment = Alignment.Center) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = BrandOrange)
            } else {
                Text("Episode not found", color = BrandMuted)
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(episode.title, color = BrandWhite) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandOrange)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandCharcoal)
            )
        },
        containerColor = BrandCharcoal
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(BrandSurface).padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("TOTAL VIEWS", style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted))
                        Text(episode.totalViews.fmtCompact(), style = MaterialTheme.typography.displayMedium.copy(color = BrandOnTrack, fontWeight = FontWeight.Bold))
                    }
                }
            }

            item {
                Text("PLATFORMS", style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted), modifier = Modifier.padding(top = 8.dp))
            }

            val platforms = listOf("YouTube", "TikTok", "Instagram", "Facebook", "X")
            items(platforms) { platform ->
                val link = episode.links.find { it.platform == platform }
                PlatformInputCard(
                    platform = platform,
                    link = link,
                    onSave = { views, url ->
                        viewModel.upsertLink(episode.id, platform, url, views)
                    }
                )
            }
        }
    }
}

@Composable
fun PlatformInputCard(platform: String, link: EpisodeLinkDto?, onSave: (Long, String?) -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var viewsInput by remember(link) { mutableStateOf(link?.viewCount?.toString() ?: "") }
    var urlInput by remember(link) { mutableStateOf(link?.url ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BrandSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(platform, style = MaterialTheme.typography.titleMedium.copy(color = BrandWhite, fontWeight = FontWeight.Bold))
            if (!isEditing) {
                Text(link?.viewCount?.fmtCompact() ?: "0", style = MaterialTheme.typography.titleMedium.copy(color = BrandOnTrack, fontWeight = FontWeight.Bold))
            }
        }

        if (isEditing) {
            OutlinedTextField(
                value = viewsInput,
                onValueChange = { viewsInput = it },
                label = { Text("Views", style = MaterialTheme.typography.labelSmall) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = { Text("URL (Optional)", style = MaterialTheme.typography.labelSmall) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = BrandMuted) },
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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.align(Alignment.End)) {
                TextButton(onClick = { isEditing = false }) {
                    Text("Cancel", color = BrandMuted)
                }
                Button(
                    onClick = {
                        val views = viewsInput.toLongOrNull() ?: 0L
                        onSave(views, urlInput.takeIf { it.isNotBlank() })
                        isEditing = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text("Save", color = BrandCharcoal, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val urlDisplay = if (!link?.url.isNullOrBlank()) "Link attached" else "No link"
                Text(urlDisplay, style = MaterialTheme.typography.bodySmall.copy(color = BrandMuted))
                
                TextButton(onClick = { isEditing = true }) {
                    Text("Edit", color = BrandOrange)
                }
            }
        }
    }
}

private fun Long.fmtCompact(): String = when {
    this >= 1_000_000 -> "%.2fM".format(this / 1_000_000.0)
    this >= 1_000 -> "%.1fK".format(this / 1_000.0)
    else -> this.toString()
}
