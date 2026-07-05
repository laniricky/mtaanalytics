package com.mtaanimation.growthos.android.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mtaanimation.growthos.android.ui.navigation.AppBottomNavBar
import com.mtaanimation.growthos.android.ui.theme.*
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    navController: NavController,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val submitState by viewModel.submitState.collectAsState()

    var selectedPlatform by remember { mutableStateOf("YOUTUBE") }
    var currentFollowersInput by remember { mutableStateOf("") }
    var target2036Input by remember { mutableStateOf("") }
    
    val platforms = listOf("YOUTUBE", "TIKTOK", "FACEBOOK", "INSTAGRAM", "X")

    LaunchedEffect(submitState) {
        if (submitState is TrackingSubmitState.Success) {
            currentFollowersInput = ""
            // Keep target the same for convenience
            kotlinx.coroutines.delay(2000)
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Monthly Stats", color = BrandWhite) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandCharcoal)
            )
        },
        bottomBar = { AppBottomNavBar(navController) },
        containerColor = BrandCharcoal
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Enter your follower counts for the current month. These data points build your historical charts and drive the projection engine.",
                style = MaterialTheme.typography.bodyMedium.copy(color = BrandMuted)
            )

            // Platform Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select Platform", style = MaterialTheme.typography.labelMedium.copy(color = BrandWhite))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BrandSurface)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    platforms.forEach { platform ->
                        val isSelected = selectedPlatform == platform
                        TextButton(
                            onClick = { selectedPlatform = platform },
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = if (isSelected) BrandOrange.copy(alpha = 0.2f) else Color.Transparent,
                                contentColor = if (isSelected) BrandOrange else BrandMuted
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = platform.take(3),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Inputs
            OutlinedTextField(
                value = currentFollowersInput,
                onValueChange = { if (it.all { char -> char.isDigit() }) currentFollowersInput = it },
                label = { Text("Current Followers (End of Month)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandOrange,
                    focusedLabelColor = BrandOrange,
                    unfocusedBorderColor = BrandDivider,
                    unfocusedLabelColor = BrandMuted
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = target2036Input,
                onValueChange = { if (it.all { char -> char.isDigit() }) target2036Input = it },
                label = { Text("2036 Target for $selectedPlatform") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandOrange,
                    focusedLabelColor = BrandOrange,
                    unfocusedBorderColor = BrandDivider,
                    unfocusedLabelColor = BrandMuted
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            // Status message
            when (val state = submitState) {
                is TrackingSubmitState.Error -> {
                    Text(
                        text = state.message,
                        color = BrandBehind,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                TrackingSubmitState.Success -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandAhead)
                        Text("Stats recorded successfully", color = BrandAhead)
                    }
                }
                else -> {}
            }

            // Submit Button
            Button(
                onClick = {
                    val followers = currentFollowersInput.toLongOrNull() ?: 0L
                    val target = target2036Input.toLongOrNull() ?: 0L
                    viewModel.recordMonthlyStats(
                        platformType = selectedPlatform,
                        currentFollowers = followers,
                        target2036 = target,
                        dateEpochMillis = Instant.now().toEpochMilli()
                    )
                },
                enabled = currentFollowersInput.isNotEmpty() && target2036Input.isNotEmpty() && submitState !is TrackingSubmitState.Submitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (submitState is TrackingSubmitState.Submitting) {
                    CircularProgressIndicator(color = BrandCharcoal, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        "Record Monthly Stats",
                        color = BrandCharcoal,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
