package ru.itmo.tuman.features.decks.ui.list

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.itmo.tuman.R
import ru.itmo.tuman.core.uikit.StubScreen

/** Заглушка списка наборов. Настоящий список — в задаче #13. */
@Composable
fun DecksScreen(onDeckClick: (deckId: Long) -> Unit, onBack: () -> Unit) {
    StubScreen(title = stringResource(R.string.decks_title), onBack = onBack) {
        Text(stringResource(R.string.stub_in_progress))
        // Номер 1 — временный, пока наборов из базы нет.
        Button(onClick = { onDeckClick(1L) }) {
            Text(stringResource(R.string.decks_open_sample))
        }
    }
}
