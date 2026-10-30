package ru.itmo.tuman.features.decks.ui.deck

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.itmo.tuman.R
import ru.itmo.tuman.core.uikit.StubScreen

/** Заглушка экрана одного набора: здесь будет список его карточек. */
@Composable
fun DeckScreen(deckId: Long, onAddCardClick: () -> Unit, onBack: () -> Unit) {
    StubScreen(title = stringResource(R.string.deck_title, deckId), onBack = onBack) {
        Text(stringResource(R.string.stub_in_progress))
        Button(onClick = onAddCardClick) {
            Text(stringResource(R.string.deck_add_card))
        }
    }
}
