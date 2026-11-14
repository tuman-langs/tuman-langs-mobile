package ru.itmo.tuman.features.decks.ui.deck

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.itmo.tuman.R
import ru.itmo.tuman.core.uikit.DeckCard
import ru.itmo.tuman.core.uikit.DeckCardSkeleton
import ru.itmo.tuman.core.uikit.EmptyState
import ru.itmo.tuman.core.uikit.TumanPrimaryButton
import ru.itmo.tuman.core.uikit.TumanTopBar
import ru.itmo.tuman.core.uikit.theme.TumanTheme
import ru.itmo.tuman.features.decks.domain.model.Card
import ru.itmo.tuman.srs.CardStatus

/**
 * Экран одного набора: шапка со счётчиками и кнопкой «Учить», список карточек.
 * ViewModel получает [deckId] через Koin (`parametersOf`).
 */
@Composable
fun DeckScreen(
    deckId: Long,
    onCardClick: (cardId: Long) -> Unit,
    onAddCardClick: () -> Unit,
    onStudyClick: () -> Unit,
    onBack: () -> Unit,
    viewModel: DeckViewModel = koinViewModel { parametersOf(deckId) },
) {
    val state by viewModel.uiState.collectAsState()
    DeckContent(state) { action ->
        when (action) {
            is DeckAction.OpenCard -> onCardClick(action.cardId)
            DeckAction.AddCard -> onAddCardClick()
            DeckAction.Study -> onStudyClick()
            DeckAction.Back -> onBack()
            DeckAction.ErrorShown -> viewModel.onErrorShown()
        }
    }
}

/** Разметка экрана без ViewModel — её показывает @Preview. */
@Composable
fun DeckContent(state: DeckUiState, onAction: (DeckAction) -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.errorRes) {
        state.errorRes?.let { errorRes ->
            snackbarHostState.showSnackbar(context.getString(errorRes))
            onAction(DeckAction.ErrorShown)
        }
    }

    Scaffold(
        topBar = { TumanTopBar(title = state.title, onBack = { onAction(DeckAction.Back) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.cards.isNotEmpty()) {
                FloatingActionButton(onClick = { onAction(DeckAction.AddCard) }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.deck_add_card))
                }
            }
        },
    ) { innerPadding ->
        val modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when {
            state.isLoading -> DeckLoading(modifier)
            state.cards.isEmpty() -> DeckEmpty(modifier, onAdd = { onAction(DeckAction.AddCard) })
            else -> DeckCardList(state, modifier, onAction)
        }
    }
}

@Composable
private fun DeckLoading(modifier: Modifier) {
    Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(3) { DeckCardSkeleton() }
    }
}

@Composable
private fun DeckEmpty(modifier: Modifier, onAdd: () -> Unit) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        EmptyState(
            title = stringResource(R.string.deck_empty_title),
            text = stringResource(R.string.deck_empty_text),
            actionText = stringResource(R.string.deck_add_card),
            onAction = onAdd,
        )
    }
}

@Composable
private fun DeckCardList(state: DeckUiState, modifier: Modifier, onAction: (DeckAction) -> Unit) {
    LazyColumn(
        modifier = modifier,
        // Снизу запас, чтобы кнопка «+» не закрывала последнюю карточку
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { DeckHeader(state, onStudy = { onAction(DeckAction.Study) }) }
        items(state.cards, key = { it.card.id }) { item ->
            DeckCard(
                title = item.card.front,
                subtitle = item.card.back,
                badge = item.card.front.take(1).uppercase(),
                onClick = { onAction(DeckAction.OpenCard(item.card.id)) },
            ) {
                Text(
                    text = statusLabel(item.status),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DeckHeader(state: DeckUiState, onStudy: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.deck_due_today, state.dueTodayCount),
            style = MaterialTheme.typography.headlineSmall,
        )
        val cards = pluralStringResource(R.plurals.decks_card_count, state.cards.size, state.cards.size)
        Text(
            text = stringResource(R.string.deck_summary, cards, state.learnedCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TumanPrimaryButton(
            text = stringResource(R.string.deck_study),
            onClick = onStudy,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.dueTodayCount > 0,
        )
    }
}

@Composable
private fun statusLabel(status: CardStatus): String = when (status) {
    CardStatus.New -> stringResource(R.string.card_status_new)
    CardStatus.DueToday -> stringResource(R.string.card_status_today)
    CardStatus.Learned -> stringResource(R.string.card_status_learned)
    is CardStatus.InDays -> stringResource(R.string.card_status_in_days, status.days)
}

@Preview(showBackground = true)
@Composable
private fun DeckContentListPreview() {
    TumanTheme {
        DeckContent(
            state = DeckUiState(
                title = "Мои слова",
                // Заглушки для превью, не языковой контент: проверяем только, что буквы видны
                cards = listOf(
                    CardItem(Card(id = 1, deckId = 1, front = "Слово с ҡ", back = "перевод 1"), CardStatus.New),
                    CardItem(Card(id = 2, deckId = 1, front = "Слово с ң", back = "перевод 2"), CardStatus.DueToday),
                    CardItem(Card(id = 3, deckId = 1, front = "Слово с һ", back = "перевод 3"), CardStatus.InDays(3)),
                    CardItem(Card(id = 4, deckId = 1, front = "Слово с ә", back = "перевод 4"), CardStatus.Learned),
                ),
                dueTodayCount = 2,
                learnedCount = 1,
                isLoading = false,
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DeckContentEmptyPreview() {
    TumanTheme {
        DeckContent(state = DeckUiState(title = "Мои слова", isLoading = false), onAction = {})
    }
}
