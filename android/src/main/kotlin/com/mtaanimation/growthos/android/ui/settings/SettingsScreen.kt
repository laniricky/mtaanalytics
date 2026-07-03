package com.mtaanimation.growthos.android.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mtaanimation.growthos.android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = BrandWhite) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandCharcoal)
            )
        },
        containerColor = BrandCharcoal
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                SectionTitle("Preferences")
                SettingsToggleCard(
                    title = "Dark Mode",
                    subtitle = "Use the dark charcoal theme",
                    checked = uiState.isDarkMode,
                    onCheckedChange = { viewModel.updateDarkMode(it) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsToggleCard(
                    title = "Push Notifications",
                    subtitle = "Receive weekly milestone alerts",
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = { viewModel.updateNotifications(it) }
                )
            }

            item {
                SectionTitle("Upload Tracker Targets")
                UploadTargetsCard(
                    yt = uiState.ytWeeklyTarget,
                    tt = uiState.ttWeeklyTarget,
                    fb = uiState.fbWeeklyTarget,
                    ig = uiState.igWeeklyTarget,
                    onSave = { y, t, f, i -> viewModel.updateWeeklyTargets(y, t, f, i) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.logout {
                            navController.navigate(com.mtaanimation.growthos.android.ui.navigation.Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Log Out", color = BrandWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(color = BrandOrange, fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun SettingsToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BrandSurface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium.copy(color = BrandWhite, fontWeight = FontWeight.Bold))
            Text(text = subtitle, style = MaterialTheme.typography.labelMedium.copy(color = BrandMuted))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = BrandCharcoal,
                checkedTrackColor = BrandOrange,
                uncheckedThumbColor = BrandMuted,
                uncheckedTrackColor = BrandSurfaceVariant
            )
        )
    }
}

@Composable
private fun UploadTargetsCard(
    yt: Int, tt: Int, fb: Int, ig: Int,
    onSave: (Int, Int, Int, Int) -> Unit
) {
    var ytInput by remember { mutableStateOf(yt.toString()) }
    var ttInput by remember { mutableStateOf(tt.toString()) }
    var fbInput by remember { mutableStateOf(fb.toString()) }
    var igInput by remember { mutableStateOf(ig.toString()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BrandSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Weekly Goals (videos/week)", style = MaterialTheme.typography.labelMedium.copy(color = BrandMuted))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TargetInput("YouTube", ytInput, { ytInput = it }, Modifier.weight(1f))
            TargetInput("TikTok", ttInput, { ttInput = it }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TargetInput("Facebook", fbInput, { fbInput = it }, Modifier.weight(1f))
            TargetInput("Instagram", igInput, { igInput = it }, Modifier.weight(1f))
        }

        Button(
            onClick = {
                onSave(
                    ytInput.toIntOrNull() ?: yt,
                    ttInput.toIntOrNull() ?: tt,
                    fbInput.toIntOrNull() ?: fb,
                    igInput.toIntOrNull() ?: ig
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
        ) {
            Text("Save Targets", color = BrandCharcoal, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TargetInput(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = BrandWhite,
            unfocusedTextColor = BrandWhite,
            focusedBorderColor = BrandOrange,
            unfocusedBorderColor = BrandSurfaceVariant,
            focusedLabelColor = BrandOrange,
            unfocusedLabelColor = BrandMuted
        )
    )
}
