package ru.itmo.tuman.features.decks.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.itmo.tuman.R
import ru.itmo.tuman.core.uikit.TumanButtonSize
import ru.itmo.tuman.core.uikit.TumanDangerButton
import ru.itmo.tuman.core.uikit.TumanPrimaryButton
import ru.itmo.tuman.core.uikit.TumanTextField
import ru.itmo.tuman.core.uikit.TumanTopBar
import ru.itmo.tuman.core.uikit.theme.TumanTheme

/** Редактор карточки. [cardId] = `CardEditRoute.NEW_CARD` — создаём новую. */
@Composable
fun CardEditScreen(
    deckId: Long,
    cardId: Long,
    onBack: () -> Unit,
    viewModel: CardEditViewModel = koinViewModel { parametersOf(deckId, cardId) },
) {
    val state by viewModel.uiState.collectAsState()

    // После сохранения изменений или удаления редактор закрывается сам
    LaunchedEffect(state.isClosed) {
        if (state.isClosed) onBack()
    }

    CardEditContent(state) { action ->
        when (action) {
            is CardEditAction.FrontChange -> viewModel.onFrontChange(action.value)
            is CardEditAction.BackChange -> viewModel.onBackChange(action.value)
            is CardEditAction.ExampleChange -> viewModel.onExampleChange(action.value)
            CardEditAction.Save -> viewModel.onSave()
            is CardEditAction.UndoAdd -> viewModel.onUndoAdd(action.cardId)
            CardEditAction.MessageShown -> viewModel.onMessageShown()
            CardEditAction.Delete -> viewModel.onDelete()
            CardEditAction.ErrorShown -> viewModel.onErrorShown()
            CardEditAction.Back -> onBack()
        }
    }
}

/** Разметка редактора без ViewModel — её показывает @Preview. */
@Composable
fun CardEditContent(state: CardEditUiState, onAction: (CardEditAction) -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    // «Карточка «…» добавлена · Отменить»: если нажали «Отменить», удаляем её
    LaunchedEffect(state.added) {
        val added = state.added ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = context.getString(R.string.card_edit_added, added.front),
            actionLabel = context.getString(R.string.action_undo),
            duration = SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed) onAction(CardEditAction.UndoAdd(added.id))
        onAction(CardEditAction.MessageShown)
    }

    LaunchedEffect(state.errorRes) {
        state.errorRes?.let { errorRes ->
            snackbarHostState.showSnackbar(context.getString(errorRes))
            onAction(CardEditAction.ErrorShown)
        }
    }

    Scaffold(
        topBar = {
            TumanTopBar(
                title = stringResource(if (state.isNew) R.string.card_edit_title_new else R.string.card_edit_title),
                onBack = { onAction(CardEditAction.Back) },
                actions = {
                    if (!state.isNew) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.card_edit_delete))
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        val modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        if (state.isLoading) {
            Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            CardEditForm(state, modifier, onAction)
        }
    }

    if (showDeleteDialog) {
        DeleteCardDialog(
            front = state.front,
            onConfirm = {
                showDeleteDialog = false
                onAction(CardEditAction.Delete)
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun CardEditForm(state: CardEditUiState, modifier: Modifier, onAction: (CardEditAction) -> Unit) {
    val fieldError = stringResource(R.string.card_edit_field_error)
    Column(
        modifier = modifier
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TumanTextField(
            value = state.front,
            onValueChange = { onAction(CardEditAction.FrontChange(it)) },
            label = stringResource(R.string.card_edit_front),
            error = if (state.frontError) fieldError else null,
        )
        TumanTextField(
            value = state.back,
            onValueChange = { onAction(CardEditAction.BackChange(it)) },
            label = stringResource(R.string.card_edit_back),
            error = if (state.backError) fieldError else null,
        )
        TumanTextField(
            value = state.example,
            onValueChange = { onAction(CardEditAction.ExampleChange(it)) },
            label = stringResource(R.string.card_edit_example),
            singleLine = false,
        )
        // TODO(#20): сюда встанет панель башкирских букв от Дворак М.,
        //  буква будет вставляться в поле, где сейчас курсор.

        if (!state.isNew) CardScheduleInfo(state)

        TumanPrimaryButton(
            text = stringResource(R.string.action_save),
            onClick = { onAction(CardEditAction.Save) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** «Следующий показ через N дней» и «Повторений N» — только для сохранённой карточки. */
@Composable
private fun CardScheduleInfo(state: CardEditUiState) {
    val days = state.daysUntilDue
    val nextReview = when {
        days == null -> stringResource(R.string.card_edit_next_new)
        days == 0L -> stringResource(R.string.card_edit_next_today)
        else -> pluralStringResource(R.plurals.card_edit_next_in_days, days.toInt(), days.toInt())
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(nextReview, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = stringResource(R.string.card_edit_review_count, state.reviewCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DeleteCardDialog(front: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.card_edit_delete_title)) },
        text = { Text(stringResource(R.string.card_edit_delete_text, front)) },
        confirmButton = {
            TumanDangerButton(
                text = stringResource(R.string.action_delete),
                onClick = onConfirm,
                size = TumanButtonSize.Small,
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun CardEditNewErrorPreview() {
    TumanTheme {
        CardEditContent(state = CardEditUiState(front = "Слово с ҡ", backError = true), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun CardEditExistingPreview() {
    TumanTheme {
        CardEditContent(
            state = CardEditUiState(
                front = "Слово с ң",
                back = "перевод",
                isNew = false,
                daysUntilDue = 3,
                reviewCount = 4,
            ),
            onAction = {},
        )
    }
}
