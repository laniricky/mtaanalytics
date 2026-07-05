package com.mtaanimation.growthos.android.ui.customgoals

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
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
import com.mtaanimation.growthos.android.ui.dashboard.components.GoalProgressRing
import com.mtaanimation.growthos.android.ui.navigation.AppBottomNavBar
import com.mtaanimation.growthos.android.ui.theme.*
import com.mtaanimation.growthos.shared.models.customgoals.CreateCustomGoalRequest
import com.mtaanimation.growthos.shared.models.customgoals.CustomGoalDto
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomGoalsScreen(
    navController: NavController,
    viewModel: CustomGoalsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState !is CustomGoalsUiState.Loading) isRefreshing = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Milestone Goals", color = BrandWhite) },
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
        bottomBar = { AppBottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BrandOrange,
                contentColor = BrandCharcoal
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        },
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
                    is CustomGoalsUiState.Loading -> LoadingState()
                    is CustomGoalsUiState.Error -> ErrorState(state.message) { viewModel.loadData() }
                    is CustomGoalsUiState.Success -> {
                        if (state.goals.isEmpty()) {
                            EmptyState { showAddDialog = true }
                        } else {
                            GoalsContent(state)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddGoalDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, target, type, deadlineMillis ->
                viewModel.createGoal(
                    CreateCustomGoalRequest(
                        title = title,
                        type = type,
                        targetValue = target,
                        deadlineEpochMillis = deadlineMillis
                    )
                )
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, target: Double, type: String, deadlineMillis: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("VIEWS") }
    var typeExpanded by remember { mutableStateOf(false) }

    val typeOptions = listOf("VIEWS", "REVENUE", "EPISODES", "FOLLOWERS", "SUBSCRIBERS", "OTHER")
    // Default deadline: 1 year from now
    val defaultDeadline = LocalDate.now().plusYears(1)
        .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BrandSurface,
        title = {
            Text(
                "New Milestone Goal",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = BrandOrange,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    placeholder = { Text("e.g. 1 Billion Views") },
                    singleLine = true,
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
                    value = targetInput,
                    onValueChange = { targetInput = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Target Value") },
                    placeholder = { Text("e.g. 1000000") },
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

                // Type dropdown
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Goal Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BrandWhite,
                            unfocusedTextColor = BrandWhite,
                            focusedBorderColor = BrandOrange,
                            unfocusedBorderColor = BrandSurfaceVariant,
                            focusedLabelColor = BrandOrange,
                            unfocusedLabelColor = BrandMuted
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false },
                        modifier = Modifier.background(BrandSurface)
                    ) {
                        typeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = BrandWhite) },
                                onClick = {
                                    selectedType = option
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetInput.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && target > 0) {
                        onConfirm(title.trim(), target, selectedType, defaultDeadline)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                enabled = title.isNotBlank() && (targetInput.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Create Goal", color = BrandCharcoal, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = BrandMuted)
            }
        }
    )
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
    val progress = if (goal.targetValue > 0) (goal.currentValue / goal.targetValue).toFloat().coerceIn(0f, 1f) else 0f

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
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = BrandWhite),
            maxLines = 2
        )

        GoalProgressRing(
            percentage = (progress * 100).toDouble(),
            modifier = Modifier.size(100.dp)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Current: ${goal.currentValue.formatCompact()}",
                style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted)
            )
            Text(
                text = "Target: ${goal.targetValue.formatCompact()} ${goal.type.lowercase()}",
                style = MaterialTheme.typography.labelMedium.copy(color = BrandOrange, fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun EmptyState(onAdd: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Flag,
                contentDescription = null,
                tint = BrandOrange.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Text(
                "No milestone goals yet",
                style = MaterialTheme.typography.titleMedium.copy(color = BrandWhite)
            )
            Text(
                "Tap + to create your first goal",
                style = MaterialTheme.typography.bodyMedium.copy(color = BrandMuted)
            )
            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add Goal", color = BrandCharcoal, fontWeight = FontWeight.Bold)
            }
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
