package ru.itmo.tuman.features.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.itmo.tuman.R
import ru.itmo.tuman.core.settings.UserSettings
import ru.itmo.tuman.core.uikit.DeckCard
import ru.itmo.tuman.core.uikit.TumanButtonSize
import ru.itmo.tuman.core.uikit.TumanPrimaryButton
import ru.itmo.tuman.core.uikit.TumanSecondaryButton
import ru.itmo.tuman.core.uikit.TumanTopBar

@Composable
fun HomeScreen(
    state: HomeUiState,
    onSelectGoal: (Int) -> Unit,
    onCompleteOnboarding: () -> Unit,
    onStartReviewClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCatalogClick: () -> Unit,
    onCreateDeckClick: () -> Unit,
    onDecksClick: () -> Unit,
    onDeckClick: (Long) -> Unit,
    onProgressClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            if (state is HomeUiState.Dashboard) {
                HomeTopBar(state.progress, onSettingsClick, onProgressClick)
            }
        },
    ) { padding ->
        val content = Modifier.fillMaxSize().padding(padding)
        when (state) {
            HomeUiState.Loading -> Box(content, contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is HomeUiState.Onboarding -> Onboarding(content, state, onSelectGoal, onCompleteOnboarding)
            is HomeUiState.Dashboard -> when (state.section) {
                HomeUiState.Section.Today -> TodayContent(content, state, onStartReviewClick, onDecksClick, onDeckClick)
                HomeUiState.Section.Done -> DoneContent(content, state, onCatalogClick, onDecksClick)
                HomeUiState.Section.NoDecks -> NoDecksContent(content, onCatalogClick, onCreateDeckClick)
            }
        }
    }
}

@Composable
private fun HomeTopBar(progress: HomeProgress, onSettings: () -> Unit, onProgress: () -> Unit) {
    TumanTopBar(title = stringResource(R.string.home_wordmark)) {
        TextButton(onClick = onProgress) {
            Text(
                stringResource(R.string.home_streak_badge, progress.streak),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
        Text(
            stringResource(R.string.home_xp, progress.xp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        IconButton(onClick = onSettings) {
            Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.home_settings))
        }
    }
}

@Composable
private fun Onboarding(
    modifier: Modifier,
    state: HomeUiState.Onboarding,
    onSelectGoal: (Int) -> Unit,
    onStart: () -> Unit,
) {
    Column(
        modifier.verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Mascot(R.drawable.home_owl, Modifier.size(150.dp).clip(CircleShape))
            HomeCard {
                Text(
                    stringResource(R.string.home_greeting),
                    modifier = Modifier.padding(14.dp),
                    textAlign = TextAlign.Center,
                )
            }
            Text(stringResource(R.string.home_choose_goal), style = MaterialTheme.typography.titleLarge)
            Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UserSettings.GOAL_OPTIONS.forEach { goal ->
                    FilterChip(
                        selected = goal == state.goal,
                        onClick = { onSelectGoal(goal) },
                        label = { Text(goal.toString()) },
                        enabled = !state.saving,
                    )
                }
            }
            Caption(R.string.home_change_goal)
        }
        Column(
            modifier = Modifier.padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.saveFailed) {
                Text(stringResource(R.string.home_goal_save_error), color = MaterialTheme.colorScheme.error)
            }
            TumanPrimaryButton(
                stringResource(R.string.home_get_started),
                onStart,
                Modifier.fillMaxWidth(),
                enabled = !state.saving,
                size = TumanButtonSize.Large,
            )
            Caption(R.string.home_offline)
        }
    }
}

@Composable
private fun TodayContent(
    modifier: Modifier,
    state: HomeUiState.Dashboard,
    onStartReview: () -> Unit,
    onDecks: () -> Unit,
    onDeck: (Long) -> Unit,
) {
    Column(
        modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HomeCard {
            Mascot(R.drawable.home_tulpar, Modifier.fillMaxWidth().height(130.dp))
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Caption(R.string.home_today)
                val today = state.today
                Text(
                    pluralStringResource(R.plurals.decks_card_count, today.total, today.total),
                    style = MaterialTheme.typography.headlineLarge,
                )
                Text(
                    stringResource(R.string.home_today_breakdown, today.due, today.new, today.minutes),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                TumanPrimaryButton(
                    stringResource(R.string.home_start_review),
                    onStartReview,
                    Modifier.fillMaxWidth().padding(top = 6.dp),
                    size = TumanButtonSize.Large,
                )
            }
        }
        GoalCard(state.progress)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.home_my_decks),
                Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
            TextButton(onClick = onDecks) { Text(stringResource(R.string.home_all_decks)) }
        }
        state.decks.take(3).forEach { deck ->
            DeckCard(
                title = deck.title,
                subtitle = pluralStringResource(R.plurals.decks_card_count, deck.cardCount, deck.cardCount),
                badge = deck.title.take(1).uppercase(),
                onClick = { onDeck(deck.id) },
            )
        }
    }
}

@Composable
private fun GoalCard(progress: HomeProgress) {
    HomeCard {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.home_daily_goal), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.home_goal_fraction, progress.answers, progress.goal))
            }
            LinearProgressIndicator(
                progress = { (progress.answers.toFloat() / progress.goal).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
            if (progress.remaining == 0) {
                Caption(R.string.home_goal_reached)
            } else {
                Text(
                    pluralStringResource(R.plurals.home_goal_remaining, progress.remaining, progress.remaining),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun DoneContent(
    modifier: Modifier,
    state: HomeUiState.Dashboard,
    onCatalog: () -> Unit,
    onDecks: () -> Unit,
) {
    val hasCards = state.decks.any { it.cardCount > 0 }
    EmptyContent(modifier, R.drawable.home_owl) {
        Text(
            stringResource(if (hasCards) R.string.home_done else R.string.home_no_cards),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
        )
        if (state.progress.remaining == 0) {
            val days = state.progress.streak
            Text(
                pluralStringResource(R.plurals.home_goal_done_streak, days, days),
                textAlign = TextAlign.Center,
            )
        }
        TumanSecondaryButton(stringResource(R.string.home_new_words), onCatalog)
        if (!hasCards) {
            TextButton(onClick = onDecks) { Text(stringResource(R.string.home_my_decks)) }
        }
    }
}

@Composable
private fun NoDecksContent(modifier: Modifier, onCatalog: () -> Unit, onCreateDeck: () -> Unit) {
    EmptyContent(modifier, R.drawable.home_tulpar) {
        Text(stringResource(R.string.home_no_decks), style = MaterialTheme.typography.headlineSmall)
        Caption(R.string.home_no_decks_hint)
        TumanPrimaryButton(stringResource(R.string.home_open_catalog), onCatalog, Modifier.fillMaxWidth())
        TextButton(onClick = onCreateDeck) { Text(stringResource(R.string.home_create_deck)) }
    }
}

@Composable
private fun EmptyContent(modifier: Modifier, image: Int, content: @Composable () -> Unit) {
    Column(
        modifier.verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Mascot(image, Modifier.size(116.dp).clip(CircleShape))
        content()
    }
}

@Composable
private fun Mascot(image: Int, modifier: Modifier) {
    Image(painterResource(image), contentDescription = null, modifier, contentScale = ContentScale.Crop)
}

@Composable
private fun Caption(text: Int) {
    Text(
        stringResource(text),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun HomeCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) { content() }
}
