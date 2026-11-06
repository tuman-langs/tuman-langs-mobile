package ru.itmo.tuman.features.decks.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.itmo.tuman.R
import ru.itmo.tuman.core.uikit.TumanButtonSize
import ru.itmo.tuman.core.uikit.TumanDangerButton
import ru.itmo.tuman.core.uikit.TumanPrimaryButton
import ru.itmo.tuman.core.uikit.TumanTextField
import ru.itmo.tuman.features.decks.domain.model.Deck

/** Один диалог и для создания, и для переименования: отличаются заголовок и начальный текст. */
@Composable
internal fun DeckNameDialog(
    title: String,
    initialName: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var showError by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            TumanTextField(
                value = name,
                onValueChange = {
                    name = it
                    showError = false
                },
                label = stringResource(R.string.decks_name_label),
                error = if (showError) stringResource(R.string.decks_name_error) else null,
            )
        },
        confirmButton = {
            TumanPrimaryButton(
                text = stringResource(R.string.action_save),
                onClick = { if (name.isBlank()) showError = true else onSave(name) },
                size = TumanButtonSize.Small,
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
internal fun DeleteDeckDialog(deck: Deck, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val cards = pluralStringResource(R.plurals.decks_card_count, deck.cardCount, deck.cardCount)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.decks_delete_title, deck.title)) },
        text = { Text(stringResource(R.string.decks_delete_text, cards)) },
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

/** Нижняя шторка с действиями над набором. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeckMenuSheet(
    deck: Deck,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(bottom = 16.dp)) {
            Text(
                text = deck.title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.decks_menu_rename)) },
                leadingContent = { Icon(Icons.Filled.Edit, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onRename),
            )
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.decks_menu_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                modifier = Modifier.clickable(onClick = onDelete),
            )
        }
    }
}
