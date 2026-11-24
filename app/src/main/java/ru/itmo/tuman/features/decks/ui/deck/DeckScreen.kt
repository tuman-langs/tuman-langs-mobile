package ru.itmo.tuman.features.decks.ui.deck

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import ru.itmo.tuman.core.uikit.DeckCardSkeleton
import ru.itmo.tuman.core.uikit.EmptyState
import ru.itmo.tuman.core.uikit.TumanTopBar
import ru.itmo.tuman.core.uikit.theme.TumanTheme
import ru.itmo.tuman.features.decks.domain.model.Card
import ru.itmo.tuman.features.decks.ui.DeckBadgeHighlight
import ru.itmo.tuman.features.decks.ui.DeckStatusBadge
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
    ) {
        item { DeckHeader(state, onStudy = { onAction(DeckAction.Study) }) }
        item {
            Text(
                text = stringResource(R.string.deck_cards_heading, state.cards.size),
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        itemsIndexed(state.cards, key = { _, item -> item.card.id }) { index, item ->
            val topRadius = if (index == 0) 18.dp else 0.dp
            val isLast = index == state.cards.lastIndex
            val bottomRadius = if (isLast) 18.dp else 0.dp
            Surface(
                onClick = { onAction(DeckAction.OpenCard(item.card.id)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topRadius, topRadius, bottomRadius, bottomRadius),
            ) {
                Column {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.card.front, style = MaterialTheme.typography.titleSmall)
                            Text(
                                item.card.back,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        DeckStatusBadge(
                            text = statusLabel(item.status),
                            highlight = when (item.status) {
                                CardStatus.DueToday -> DeckBadgeHighlight.Due
                                CardStatus.Learned -> DeckBadgeHighlight.Learned
                                else -> DeckBadgeHighlight.Neutral
                            },
                        )
                    }
                    if (!isLast) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
private fun DeckHeader(state: DeckUiState, onStudy: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.primary,
        contentColor = colors.onPrimary,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.deck_due_today, state.dueTodayCount),
                    style = MaterialTheme.typography.headlineSmall,
                )
                val cards = pluralStringResource(R.plurals.decks_card_count, state.cards.size, state.cards.size)
                Text(
                    text = stringResource(R.string.deck_summary, cards, state.learnedCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onPrimary.copy(alpha = 0.8f),
                )
            }
            Button(
                onClick = onStudy,
                enabled = state.dueTodayCount > 0,
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.onPrimary,
                    contentColor = colors.primary,
                    disabledContainerColor = colors.onPrimary.copy(alpha = 0.2f),
                    disabledContentColor = colors.onPrimary.copy(alpha = 0.75f),
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(stringResource(R.string.deck_study), modifier = Modifier.padding(start = 4.dp))
            }
        }
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
