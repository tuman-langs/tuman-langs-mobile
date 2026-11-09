package ru.itmo.tuman.features.decks.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import ru.itmo.tuman.R
import ru.itmo.tuman.core.uikit.DeckCard
import ru.itmo.tuman.core.uikit.DeckCardSkeleton
import ru.itmo.tuman.core.uikit.EmptyState
import ru.itmo.tuman.core.uikit.TumanTopBar
import ru.itmo.tuman.core.uikit.theme.TumanTheme
import ru.itmo.tuman.features.decks.domain.model.Deck

/** Экран со списком наборов: связывает ViewModel с разметкой из [DecksContent]. */
@Composable
fun DecksScreen(
    onDeckClick: (deckId: Long) -> Unit,
    viewModel: DecksViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    DecksContent(state) { action ->
        when (action) {
            is DecksAction.Open -> onDeckClick(action.id)
            is DecksAction.Create -> viewModel.onCreate(action.name)
            is DecksAction.Rename -> viewModel.onRename(action.id, action.name)
            is DecksAction.Delete -> viewModel.onDelete(action.id)
            DecksAction.ErrorShown -> viewModel.onErrorShown()
        }
    }
}

/** Какой диалог открыт. Это деталь отображения, поэтому она живёт здесь, а не во ViewModel. */
private sealed interface DecksDialog {
    data object Create : DecksDialog
    data class Menu(val deck: Deck) : DecksDialog
    data class Rename(val deck: Deck) : DecksDialog
    data class Delete(val deck: Deck) : DecksDialog
}

/** Разметка экрана без ViewModel — её показывает @Preview. */
@Composable
fun DecksContent(state: DecksUiState, onAction: (DecksAction) -> Unit) {
    var dialog by remember { mutableStateOf<DecksDialog?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.errorRes) {
        state.errorRes?.let { errorRes ->
            snackbarHostState.showSnackbar(context.getString(errorRes))
            onAction(DecksAction.ErrorShown)
        }
    }

    Scaffold(
        topBar = { TumanTopBar(title = stringResource(R.string.decks_title)) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.decks.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.decks_fab)) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    onClick = { dialog = DecksDialog.Create },
                )
            }
        },
    ) { innerPadding ->
        val modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when {
            state.isLoading -> DecksLoading(modifier)
            state.decks.isEmpty() -> DecksEmpty(modifier, onCreate = { dialog = DecksDialog.Create })
            else -> DecksList(
                decks = state.decks,
                modifier = modifier,
                onOpen = { onAction(DecksAction.Open(it.id)) },
                onMenu = { dialog = DecksDialog.Menu(it) },
            )
        }
    }

    val close = { dialog = null }
    when (val current = dialog) {
        null -> Unit
        DecksDialog.Create -> DeckNameDialog(
            title = stringResource(R.string.decks_create_title),
            initialName = "",
            onSave = {
                onAction(DecksAction.Create(it))
                close()
            },
            onDismiss = close,
        )
        is DecksDialog.Menu -> DeckMenuSheet(
            deck = current.deck,
            onRename = { dialog = DecksDialog.Rename(current.deck) },
            onDelete = { dialog = DecksDialog.Delete(current.deck) },
            onDismiss = close,
        )
        is DecksDialog.Rename -> DeckNameDialog(
            title = stringResource(R.string.decks_rename_title),
            initialName = current.deck.title,
            onSave = {
                onAction(DecksAction.Rename(current.deck.id, it))
                close()
            },
            onDismiss = close,
        )
        is DecksDialog.Delete -> DeleteDeckDialog(
            deck = current.deck,
            onConfirm = {
                onAction(DecksAction.Delete(current.deck.id))
                close()
            },
            onDismiss = close,
        )
    }
}

@Composable
private fun DecksLoading(modifier: Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(3) { DeckCardSkeleton() }
    }
}

@Composable
private fun DecksEmpty(modifier: Modifier, onCreate: () -> Unit) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        EmptyState(
            title = stringResource(R.string.decks_empty_title),
            text = stringResource(R.string.decks_empty_text),
            actionText = stringResource(R.string.decks_create_action),
            onAction = onCreate,
        )
    }
}

@Composable
private fun DecksList(
    decks: List<Deck>,
    modifier: Modifier,
    onOpen: (Deck) -> Unit,
    onMenu: (Deck) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        // Снизу запас, чтобы кнопка «Набор» не закрывала последнюю карточку
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(decks, key = { it.id }) { deck ->
            DeckCard(
                title = deck.title,
                subtitle = deckSubtitle(deck),
                badge = deck.title.take(1).uppercase(),
                onClick = { onOpen(deck) },
            ) {
                IconButton(onClick = { onMenu(deck) }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.decks_menu_description),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun deckSubtitle(deck: Deck): String {
    val cards = pluralStringResource(R.plurals.decks_card_count, deck.cardCount, deck.cardCount)
    val source = if (deck.isFromCatalog) R.string.decks_source_catalog else R.string.decks_source_own
    return stringResource(source, cards)
}

@Preview(showBackground = true)
@Composable
private fun DecksContentListPreview() {
    TumanTheme {
        DecksContent(
            state = DecksUiState(
                decks = listOf(
                    Deck(id = 1, title = "Базовые слова", cardCount = 40, isFromCatalog = true),
                    Deck(id = 2, title = "Ҡош-ҡорттар", cardCount = 1, isFromCatalog = false),
                ),
                isLoading = false,
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DecksContentEmptyPreview() {
    TumanTheme {
        DecksContent(state = DecksUiState(isLoading = false), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun DecksContentLoadingPreview() {
    TumanTheme {
        DecksContent(state = DecksUiState(), onAction = {})
    }
}
