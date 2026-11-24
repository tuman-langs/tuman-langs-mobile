package ru.itmo.tuman.features.review.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.itmo.tuman.R
import ru.itmo.tuman.core.uikit.RatingButton
import ru.itmo.tuman.core.uikit.RatingTone
import ru.itmo.tuman.core.uikit.TumanPrimaryButton
import ru.itmo.tuman.core.uikit.theme.TumanHardSoft
import ru.itmo.tuman.core.uikit.theme.TumanTheme
import ru.itmo.tuman.core.uikit.theme.TumanXp
import ru.itmo.tuman.srs.Rating
import java.time.Duration

@Composable
fun ReviewScreen(
    deckId: Long?,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onProgress: () -> Unit,
    viewModel: ReviewViewModel = koinViewModel(
        key = "review-${deckId ?: "all"}",
        parameters = { if (deckId == null) parametersOf() else parametersOf(deckId) },
    ),
) {
    val state by viewModel.uiState.collectAsState()
    ReviewContent(
        state = state,
        onRevealAnswer = viewModel::onRevealAnswer,
        onRatingSelected = viewModel::onRatingSelected,
        onHintDismissed = viewModel::onHintDismissed,
        onErrorShown = viewModel::onErrorShown,
        onBack = onBack,
        onHome = onHome,
        onProgress = onProgress,
        onPracticeAgain = if (viewModel.canPractice) viewModel::onPracticeAgain else null,
    )
}

@Composable
fun ReviewContent(
    state: ReviewUiState,
    onRevealAnswer: () -> Unit,
    onRatingSelected: (Rating) -> Unit,
    onHintDismissed: () -> Unit,
    onErrorShown: () -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onProgress: () -> Unit,
    onPracticeAgain: (() -> Unit)? = null,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.errorRes) {
        state.errorRes?.let { errorRes ->
            snackbarHostState.showSnackbar(context.getString(errorRes))
            onErrorShown()
        }
    }

    Scaffold(
        topBar = {
            if (!state.isFinished) {
                ReviewTopBar(state, onBack)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.isFinished -> ReviewSummary(state, onHome, onProgress, onPracticeAgain)
                state.isEmpty -> ReviewEmpty(onHome)
                state.currentCard != null -> ReviewCardContent(
                    state = state,
                    onRevealAnswer = onRevealAnswer,
                    onRatingSelected = onRatingSelected,
                )
                else -> Text(stringResource(R.string.review_error_load))
            }
        }
    }

    if (state.isHintVisible) {
        RatingHintSheet(onDismiss = onHintDismissed)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewTopBar(state: ReviewUiState, onBack: () -> Unit) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.review_close))
            }
        },
        title = {
            val title = if (state.isPractice) R.string.review_practice_title else R.string.review_title
            if (state.currentCard == null || state.total <= 0) {
                Text(stringResource(title))
            } else Column {
                if (state.isPractice) {
                    Text(
                        text = stringResource(title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LinearProgressIndicator(
                        progress = { (state.position.toFloat() / state.total).coerceIn(0f, 1f) },
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = stringResource(R.string.review_position, state.position, state.total),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    )
}

@Composable
private fun ReviewCardContent(
    state: ReviewUiState,
    onRevealAnswer: () -> Unit,
    onRatingSelected: (Rating) -> Unit,
) {
    val card = state.currentCard ?: return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !state.isAnswerVisible, onClick = onRevealAnswer),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 208.dp)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            ) {
                if (card.deckTitle.isNotBlank()) {
                    Surface(
                        modifier = Modifier.align(Alignment.Start),
                        color = TumanHardSoft,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            text = card.deckTitle,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Text(
                    text = card.front,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                if (state.isAnswerVisible) {
                    HorizontalDivider()
                    Text(
                        text = card.back,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                    )
                    card.example?.let { example ->
                        Text(
                            text = example,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.review_front_tap_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        if (!state.isAnswerVisible) {
            TumanPrimaryButton(
                text = stringResource(R.string.review_show_answer),
                onClick = onRevealAnswer,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Text(
                text = stringResource(R.string.review_rating_prompt),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.align(Alignment.Start),
            )
            Spacer(Modifier.height(8.dp))
            RatingButtons(state, onRatingSelected)
        }
    }
}

private data class RatingOption(
    val rating: Rating,
    @StringRes val label: Int,
    @StringRes val explanation: Int,
    val tone: RatingTone,
)

private val ratingOptions = listOf(
    RatingOption(Rating.AGAIN, R.string.rating_again, R.string.review_hint_again, RatingTone.Forgot),
    RatingOption(Rating.HARD, R.string.rating_hard, R.string.review_hint_hard, RatingTone.Hard),
    RatingOption(Rating.GOOD, R.string.rating_good, R.string.review_hint_good, RatingTone.Good),
    RatingOption(Rating.EASY, R.string.rating_easy, R.string.review_hint_easy, RatingTone.Easy),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RatingHintSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, top = 4.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.review_hint_title),
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.review_hint_text),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ratingOptions.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = option.tone.background,
                        contentColor = option.tone.color,
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            text = stringResource(option.label),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Text(
                        text = stringResource(option.explanation),
                        modifier = Modifier.weight(2f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            TumanPrimaryButton(
                text = stringResource(R.string.review_hint_dismiss),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun RatingButtons(
    state: ReviewUiState,
    onRatingSelected: (Rating) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ratingOptions.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { option ->
                    RatingButton(
                        label = stringResource(option.label),
                        // В тренировке оценка не меняет расписание — интервалы не показываем
                        interval = if (state.isPractice) null else (
                            state.intervals[option.rating]?.toDisplayText()
                                ?: stringResource(R.string.review_interval_loading)
                        ),
                        tone = option.tone,
                        onClick = { onRatingSelected(option.rating) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun Duration.toDisplayText(): String = when {
    isZero || isNegative -> stringResource(R.string.review_interval_less_minute)
    toDays() > 0 -> stringResource(R.string.review_interval_days, toDays())
    toHours() > 0 -> stringResource(R.string.review_interval_hours, toHours())
    toMinutes() > 0 -> stringResource(R.string.review_interval_minutes, toMinutes())
    else -> stringResource(R.string.review_interval_less_minute)
}

@Composable
private fun ReviewEmpty(onHome: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.review_today_done), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.review_empty_text), textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        TumanPrimaryButton(
            text = stringResource(R.string.review_go_home),
            onClick = onHome,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ReviewSummary(
    state: ReviewUiState,
    onHome: () -> Unit,
    onProgress: () -> Unit,
    onPracticeAgain: (() -> Unit)?,
) {
    val accuracy = if (state.answersCount == 0) 0
    else state.correctAnswers * 100 / state.answersCount
    val stats = state.summaryStats
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.home_tulpar),
            contentDescription = null,
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        if (stats?.goalDone == true) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = stringResource(R.string.review_goal_done),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Text(
            text = stringResource(R.string.review_session_finished),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        // Тренировка не даёт XP и не влияет на серию — эти плашки не показываем
        if (!state.isPractice) Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SummaryMetric(
                text = stringResource(R.string.review_xp, state.earnedXp),
                modifier = Modifier.weight(1f),
                color = TumanXp,
            )
            SummaryMetric(
                text = if (stats == null) stringResource(R.string.review_streak_pending)
                else stringResource(R.string.review_summary_streak, stats.streak),
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SummaryMetric(
                text = stringResource(R.string.review_answers_count, state.answersCount),
                modifier = Modifier.weight(1f),
            )
            SummaryMetric(
                text = stringResource(R.string.review_correct_percent, accuracy),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        stats?.unlockedAchievement?.let { achievement ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TumanHardSoft,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = stringResource(R.string.review_new_achievement),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(text = achievement, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (onPracticeAgain != null) {
            TumanPrimaryButton(
                text = stringResource(R.string.review_practice_again),
                onClick = onPracticeAgain,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (!state.isPractice) {
            if (onPracticeAgain == null) {
                TumanPrimaryButton(
                    text = stringResource(R.string.review_go_progress),
                    onClick = onProgress,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                TextButton(onClick = onProgress) { Text(stringResource(R.string.review_go_progress)) }
            }
        }
        TextButton(onClick = onHome) {
            Text(stringResource(R.string.review_go_home))
        }
    }
}

@Composable
private fun SummaryMetric(
    text: String,
    modifier: Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = color,
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReviewContentPreview() {
    TumanTheme {
        ReviewContent(
            state = ReviewUiState(
                currentCard = ru.itmo.tuman.features.review.domain.model.ReviewCard(
                    id = 1,
                    front = "Слово",
                    back = "Перевод",
                    example = "Пример",
                    deckTitle = "Базовые слова",
                ),
                position = 1,
                total = 12,
                isLoading = false,
                isAnswerVisible = true,
                intervals = mapOf(
                    Rating.AGAIN to Duration.ZERO,
                    Rating.HARD to Duration.ofDays(1),
                    Rating.GOOD to Duration.ofDays(3),
                    Rating.EASY to Duration.ofDays(7),
                ),
            ),
            onRevealAnswer = {},
            onRatingSelected = {},
            onHintDismissed = {},
            onErrorShown = {},
            onBack = {},
            onHome = {},
            onProgress = {},
        )
    }
}
