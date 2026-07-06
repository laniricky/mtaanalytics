package com.mtaanimation.growthos.android.ui.customgoals

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mtaanimation.growthos.android.ui.navigation.AppBottomNavBar
import com.mtaanimation.growthos.android.ui.theme.*
import com.mtaanimation.growthos.shared.models.customgoals.CreateCustomGoalRequest
import com.mtaanimation.growthos.shared.models.customgoals.CustomGoalDto
import com.mtaanimation.growthos.shared.models.customgoals.MilestoneLiveValues
import com.mtaanimation.growthos.shared.models.customgoals.UpdateCustomGoalProgressRequest
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomGoalsScreen(
    navController: NavController,
    viewModel: CustomGoalsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var goalToUpdate by remember { mutableStateOf<CustomGoalDto?>(null) }

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
                            GoalsContent(
                                state = state,
                                viewModel = viewModel,
                                onUpdateClick = { goal -> goalToUpdate = goal }
                            )
                        }
                    }
                }
            }
        }
    }

    // Update Progress Dialog — only for OTHER type goals
    val target = goalToUpdate
    if (target != null) {
        UpdateProgressDialog(
            goal = target,
            onDismiss = { goalToUpdate = null },
            onConfirm = { newValue ->
                viewModel.updateProgress(
                    UpdateCustomGoalProgressRequest(
                        id = target.id,
                        currentValue = newValue
                    )
                )
                goalToUpdate = null
            }
        )
    }

    if (showAddDialog) {
        AddGoalDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, target2, type, deadlineMillis ->
                viewModel.createGoal(
                    CreateCustomGoalRequest(
                        title = title,
                        type = type,
                        targetValue = target2,
                        deadlineEpochMillis = deadlineMillis
                    )
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun GoalsContent(
    state: CustomGoalsUiState.Success,
    viewModel: CustomGoalsViewModel,
    onUpdateClick: (CustomGoalDto) -> Unit
) {
    val liveValues = state.liveValues

    // Compute effective currentValue for each goal using live data where available
    fun effectiveCurrent(goal: CustomGoalDto) = viewModel.liveCurrentValue(goal, liveValues)

    val upcoming = state.goals.filter { effectiveCurrent(it) / it.targetValue.coerceAtLeast(1.0) < 1.0 }
    val completed = state.goals.filter { effectiveCurrent(it) / it.targetValue.coerceAtLeast(1.0) >= 1.0 }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (upcoming.isNotEmpty()) {
            item {
                Text(
                    "ACTIVE MILESTONES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = BrandMuted,
                        letterSpacing = 1.5f.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                )
            }
            items(upcoming) { goal ->
                TimelineGoalCard(
                    goal = goal,
                    effectiveCurrent = effectiveCurrent(goal),
                    isLast = goal == upcoming.last() && completed.isEmpty(),
                    onUpdateClick = if (goal.type !in AUTO_TRACKED_TYPES) {
                        { onUpdateClick(goal) }
                    } else null
                )
            }
        }

        if (completed.isNotEmpty()) {
            item {
                Text(
                    "COMPLETED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = BrandAhead,
                        letterSpacing = 1.5f.sp
                    ),
                    modifier = Modifier.padding(top = 20.dp, bottom = 12.dp, start = 4.dp)
                )
            }
            items(completed) { goal ->
                TimelineGoalCard(
                    goal = goal,
                    effectiveCurrent = effectiveCurrent(goal),
                    isLast = goal == completed.last(),
                    onUpdateClick = null
                )
            }
        }
    }
}

@Composable
private fun TimelineGoalCard(
    goal: CustomGoalDto,
    effectiveCurrent: Double,
    isLast: Boolean,
    onUpdateClick: (() -> Unit)?
) {
    val progress = (effectiveCurrent / goal.targetValue.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
    val isComplete = progress >= 1f
    val isAutoTracked = goal.type in AUTO_TRACKED_TYPES

    val progressColor = when {
        isComplete -> BrandAhead
        progress >= 0.5f -> BrandOnTrack
        else -> BrandOrange
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        // Timeline dot + connector line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isComplete) BrandAhead else BrandOrange)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(if (onUpdateClick != null) 116.dp else 100.dp)
                        .background(BrandSurface)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(BrandSurface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Title + badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    goal.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = BrandWhite,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Auto-tracked live badge
                    if (isAutoTracked) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1B4332))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(BrandAhead)
                            )
                            Text(
                                "Live",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = BrandAhead,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    // Goal type badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BrandOrange.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            goal.type.take(4),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = BrandOrange,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            // Progress bar + labels
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = progressColor,
                    trackColor = BrandSurfaceVariant
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "${(progress * 100).toInt()}% complete",
                        style = MaterialTheme.typography.labelSmall.copy(color = progressColor)
                    )
                    Text(
                        "${effectiveCurrent.formatCompact()} / ${goal.targetValue.formatCompact()} ${goal.type.lowercase()}",
                        style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted)
                    )
                }
            }

            // Show "Log Progress" only for OTHER type goals
            if (onUpdateClick != null) {
                TextButton(
                    onClick = onUpdateClick,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.textButtonColors(contentColor = BrandOrange)
                ) {
                    Text(
                        "Log Progress →",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            } else if (isAutoTracked && !isComplete) {
                Text(
                    "Auto-tracked from your app data",
                    style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
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

    // Simplified types — FOLLOWERS merges followers+subscribers
    val typeOptions = listOf("VIEWS", "REVENUE", "EPISODES", "FOLLOWERS", "OTHER")
    val defaultDeadline = LocalDate.now().plusYears(1)
        .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BrandSurface,
        title = {
            Text(
                "New Milestone",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = BrandOrange,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Info banner for auto-tracked types
                if (selectedType in AUTO_TRACKED_TYPES) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1B4332).copy(alpha = 0.5f))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BrandAhead)
                        )
                        Text(
                            "Progress is tracked automatically from your ${selectedType.lowercase()} data.",
                            style = MaterialTheme.typography.labelSmall.copy(color = BrandAhead)
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Milestone Title") },
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
                    placeholder = { Text("e.g. 1000000000") },
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

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
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
                    val t = targetInput.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && t > 0) {
                        onConfirm(title.trim(), t, selectedType, defaultDeadline)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                enabled = title.isNotBlank() && (targetInput.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Create", color = BrandCharcoal, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = BrandMuted) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpdateProgressDialog(
    goal: CustomGoalDto,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var input by remember { mutableStateOf(goal.currentValue.toLong().toString()) }
    val newValue = input.toDoubleOrNull() ?: 0.0
    val newProgress = (newValue / goal.targetValue.coerceAtLeast(1.0)).coerceIn(0.0, 1.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BrandSurface,
        title = {
            Text(
                goal.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = BrandOrange,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(
                        progress = { newProgress.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = BrandOrange,
                        trackColor = BrandSurfaceVariant
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "${(newProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(color = BrandOrange)
                        )
                        Text(
                            "Target: ${goal.targetValue.formatCompact()} ${goal.type.lowercase()}",
                            style = MaterialTheme.typography.labelSmall.copy(color = BrandMuted)
                        )
                    }
                }

                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Current ${goal.type.lowercase()} count") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = { if (newValue > 0) onConfirm(newValue) },
                enabled = newValue > 0,
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
            ) {
                Text("Save Progress", color = BrandCharcoal, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = BrandMuted) }
        }
    )
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
                "No milestones yet",
                style = MaterialTheme.typography.titleMedium.copy(color = BrandWhite)
            )
            Text(
                "Tap + to set your first big goal",
                style = MaterialTheme.typography.bodyMedium.copy(color = BrandMuted)
            )
            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add Milestone", color = BrandCharcoal, fontWeight = FontWeight.Bold)
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
