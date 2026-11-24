package ru.itmo.tuman.features.progress.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import ru.itmo.tuman.R
import ru.itmo.tuman.core.settings.UserSettings
import ru.itmo.tuman.core.uikit.EmptyState
import ru.itmo.tuman.core.uikit.TumanPrimaryButton
import ru.itmo.tuman.core.uikit.TumanTopBar
import ru.itmo.tuman.features.progress.domain.model.Achievement
import ru.itmo.tuman.features.progress.domain.model.AchievementType
import ru.itmo.tuman.features.progress.domain.model.Progress
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ProgressScreen(
    onAchievementsClick: () -> Unit,
    onStartReviewClick: () -> Unit,
) {
    val viewModel: ProgressViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showGoalDialog by rememberSaveable { mutableStateOf(false) }

    state.errorRes?.let { errorRes ->
        val errorMessage = stringResource(errorRes)
        LaunchedEffect(errorRes) {
            snackbarHostState.showSnackbar(message = errorMessage)
            viewModel.onErrorShown()
        }
    }

    Scaffold(
        topBar = {
            TumanTopBar(
                title = stringResource(R.string.progress_title),
                actions = {
                    val xp = state.progress?.xp ?: 0
                    AssistChip(
                        onClick = {},
                        label = { Text(stringResource(R.string.progress_xp_value, xp)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            state.progress == null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { EmptyState(
                title = stringResource(R.string.progress_error_load),
                text = stringResource(R.string.progress_error_load_text),
            ) }
            state.progress?.totalAnswers == 0 -> ProgressEmpty(
                modifier = Modifier.fillMaxSize().padding(padding),
                onStartReviewClick = onStartReviewClick,
            )
            else -> ProgressContent(
                progress = state.progress!!,
                modifier = Modifier.fillMaxSize().padding(padding),
                onChangeGoal = { showGoalDialog = true },
                onAchievementsClick = onAchievementsClick,
            )
        }
    }

    if (showGoalDialog && state.progress != null) {
        GoalDialog(
            selectedGoal = state.progress!!.dailyGoal,
            onGoalSelected = { goal ->
                viewModel.onDailyGoalChanged(goal)
                showGoalDialog = false
            },
            onDismiss = { showGoalDialog = false },
        )
    }
}

@Composable
private fun ProgressEmpty(modifier: Modifier, onStartReviewClick: () -> Unit) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            EmptyState(
                title = stringResource(R.string.progress_empty_title),
                text = stringResource(R.string.progress_empty_text),
                illustration = R.drawable.home_owl,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AchievementPreview(
                    type = AchievementType.FIRST_DECK,
                    modifier = Modifier.weight(1f),
                    isLocked = true,
                )
                AchievementPreview(
                    type = AchievementType.FIRST_10_CARDS,
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.progress_empty_achievement_first_10),
                    isLocked = true,
                )
                AchievementPreview(
                    type = AchievementType.STREAK_3_DAYS,
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.progress_empty_achievement_streak_3),
                    isLocked = true,
                )
            }
            TumanPrimaryButton(
                text = stringResource(R.string.progress_start_review),
                onClick = onStartReviewClick,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Filled.PlayArrow,
            )
        }
    }
}

@Composable
private fun ProgressContent(
    progress: Progress,
    modifier: Modifier,
    onChangeGoal: () -> Unit,
    onAchievementsClick: () -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            GoalCard(progress = progress, onChangeGoal = onChangeGoal)
        }
        item {
            StreakCard(progress = progress)
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    value = progress.learnedCards.toString(),
                    label = stringResource(R.string.progress_learned),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    value = progress.accuracy?.let { "${(it * 100).roundToInt()}%" } ?: "—",
                    label = stringResource(R.string.progress_accuracy),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            val unlocked = progress.achievements.count { it.isUnlocked }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.progress_achievements_count, unlocked, 6),
                    style = MaterialTheme.typography.titleSmall,
                )
                TextButton(onClick = onAchievementsClick) {
                    Text(stringResource(R.string.progress_achievements_all))
                }
            }
        }
        item {
            val unlocked = progress.achievements.filter { it.isUnlocked }.take(3)
            if (unlocked.isEmpty()) {
                Text(
                    text = stringResource(R.string.progress_no_achievements),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    unlocked.forEach { achievement ->
                        AchievementPreview(
                            type = achievement.type,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(3 - unlocked.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalCard(progress: Progress, onChangeGoal: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val fraction = (progress.todayAnswers.toFloat() / progress.dailyGoal.toFloat()).coerceIn(0f, 1f)
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(92.dp)) {
                CircularProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 8.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(progress.todayAnswers.toString(), style = MaterialTheme.typography.headlineSmall)
                    Text(
                        stringResource(R.string.progress_goal_of, progress.dailyGoal),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.progress_daily_goal), style = MaterialTheme.typography.titleSmall)
                val remaining = (progress.dailyGoal - progress.todayAnswers).coerceAtLeast(0)
                Text(
                    text = if (remaining == 0) stringResource(R.string.progress_goal_done)
                    else stringResource(R.string.progress_goal_remaining, remaining),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onChangeGoal, contentPadding = PaddingValues(0.dp)) {
                    Text(stringResource(R.string.progress_change_goal))
                }
            }
        }
    }
}

@Composable
private fun StreakCard(progress: Progress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = stringResource(R.string.progress_streak, progress.currentStreak),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(R.string.progress_best_streak, progress.bestStreak),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                progress.lastWeek.forEach { day ->
                    val title = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("ru"))
                        .take(2)
                        .replaceFirstChar { it.uppercase() }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .alpha(if (day.isGoalDone) 1f else 0.55f),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (day.isGoalDone) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            } else {
                                Box(
                                    modifier = Modifier.size(10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {}
                            }
                        }
                        Text(title, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.displaySmall)
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AchievementPreview(
    type: AchievementType,
    modifier: Modifier,
    title: String = achievementTitle(type),
    isLocked: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) colors.surfaceVariant else colors.surface,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                achievementEmoji(type),
                modifier = Modifier.alpha(if (isLocked) 0.45f else 1f),
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun GoalDialog(
    selectedGoal: Int,
    onGoalSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.progress_change_goal)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                UserSettings.GOAL_OPTIONS.forEach { goal ->
                    TextButton(
                        onClick = { onGoalSelected(goal) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            stringResource(R.string.progress_goal_option, goal),
                            color = if (goal == selectedGoal) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
fun AchievementsScreen(onBack: () -> Unit) {
    val viewModel: ProgressViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TumanTopBar(
                title = stringResource(R.string.progress_achievements_title),
                onBack = onBack,
            )
        },
    ) { padding ->
        val progress = state.progress
        if (progress == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(progress.achievements, key = { it.type.name }) { achievement ->
                    AchievementRow(achievement)
                }
            }
        }
    }
}

@Composable
private fun AchievementRow(achievement: Achievement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                achievementEmoji(achievement.type),
                modifier = Modifier.alpha(if (achievement.isUnlocked) 1f else 0.55f),
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(achievementTitle(achievement.type), style = MaterialTheme.typography.titleSmall)
                Text(
                    achievementDescription(achievement.type),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!achievement.isUnlocked) {
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { achievement.current.toFloat() / achievement.target.toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = stringResource(
                            R.string.progress_achievement_progress,
                            achievement.current,
                            achievement.target,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (achievement.isUnlocked) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Filled.Check,
                    contentDescription = stringResource(R.string.progress_achievement_unlocked),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun achievementEmoji(type: AchievementType): String = when (type) {
    AchievementType.FIRST_DECK -> "📚"
    AchievementType.FIRST_10_CARDS -> "🌱"
    AchievementType.STREAK_3_DAYS -> "🔥"
    AchievementType.WEEK_NO_GAPS -> "🏅"
    AchievementType.LEARNED_100 -> "🦉"
    AchievementType.XP_1000 -> "⭐"
}

@Composable
private fun achievementTitle(type: AchievementType): String = stringResource(type.titleRes())

@StringRes
internal fun AchievementType.titleRes(): Int = when (this) {
    AchievementType.FIRST_DECK -> R.string.achievement_first_deck
    AchievementType.FIRST_10_CARDS -> R.string.achievement_first_10
    AchievementType.STREAK_3_DAYS -> R.string.achievement_streak_3
    AchievementType.WEEK_NO_GAPS -> R.string.achievement_week
    AchievementType.LEARNED_100 -> R.string.achievement_learned_100
    AchievementType.XP_1000 -> R.string.achievement_xp_1000
}

@Composable
private fun achievementDescription(type: AchievementType): String = when (type) {
    AchievementType.FIRST_DECK -> stringResource(R.string.achievement_first_deck_desc)
    AchievementType.FIRST_10_CARDS -> stringResource(R.string.achievement_first_10_desc)
    AchievementType.STREAK_3_DAYS -> stringResource(R.string.achievement_streak_3_desc)
    AchievementType.WEEK_NO_GAPS -> stringResource(R.string.achievement_week_desc)
    AchievementType.LEARNED_100 -> stringResource(R.string.achievement_learned_100_desc)
    AchievementType.XP_1000 -> stringResource(R.string.achievement_xp_1000_desc)
}
