package ru.itmo.tuman.features.review.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.itmo.tuman.R
import ru.itmo.tuman.core.uikit.RatingButton
import ru.itmo.tuman.core.uikit.RatingTone
import ru.itmo.tuman.core.uikit.TumanPrimaryButton
import ru.itmo.tuman.core.uikit.TumanTopBar
import ru.itmo.tuman.core.uikit.theme.TumanTheme
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
            TumanTopBar(
                title = stringResource(if (state.isPractice) R.string.review_practice_title else R.string.review_title),
                actions = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.review_close),
                        )
                    }
                },
            )
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
        AlertDialog(
            onDismissRequest = onHintDismissed,
            title = { Text(stringResource(R.string.review_hint_title)) },
            text = { Text(stringResource(R.string.review_hint_text)) },
            confirmButton = {
                TextButton(onClick = onHintDismissed) {
                    Text(stringResource(R.string.review_hint_dismiss))
                }
            },
        )
    }
}

@Composable
private fun ReviewCardContent(
    state: ReviewUiState,
    onRevealAnswer: () -> Unit,
    onRatingSelected: (Rating) -> Unit,
) {
    val card = state.currentCard ?: return
    Text(
        text = stringResource(R.string.review_position, state.position, state.total),
        style = MaterialTheme.typography.titleMedium,
    )
    Spacer(Modifier.height(20.dp))
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.review_front_label),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = card.front,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            if (state.isAnswerVisible) {
                Spacer(Modifier.height(18.dp))
                Text(stringResource(R.string.review_back_label), style = MaterialTheme.typography.labelLarge)
                Text(
                    text = card.back,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
                card.example?.let { example ->
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.review_example_label), style = MaterialTheme.typography.labelLarge)
                    Text(text = example, textAlign = TextAlign.Center)
                }
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
        RatingButtons(state, onRatingSelected)
    }
}

@Composable
private fun RatingButtons(
    state: ReviewUiState,
    onRatingSelected: (Rating) -> Unit,
) {
    val ratings = listOf(
        Rating.AGAIN to (R.string.rating_again to RatingTone.Forgot),
        Rating.HARD to (R.string.rating_hard to RatingTone.Hard),
        Rating.GOOD to (R.string.rating_good to RatingTone.Good),
        Rating.EASY to (R.string.rating_easy to RatingTone.Easy),
    )
    ratings.chunked(2).forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            row.forEach { (rating, labelAndTone) ->
                RatingButton(
                    label = stringResource(labelAndTone.first),
                    interval = if (state.isPractice) null else (
                        state.intervals[rating]?.toDisplayText() ?: stringResource(R.string.review_interval_loading)
                    ),
                    tone = labelAndTone.second,
                    onClick = { onRatingSelected(rating) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.review_session_finished), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.review_answers_count, state.answersCount))
        Text(stringResource(R.string.review_correct_percent, accuracy))
        if (!state.isPractice) {
            Text(stringResource(R.string.review_xp, state.earnedXp), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.review_streak_pending))
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
