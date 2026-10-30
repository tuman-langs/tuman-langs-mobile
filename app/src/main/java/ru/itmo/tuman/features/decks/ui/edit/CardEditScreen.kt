package ru.itmo.tuman.features.decks.ui.edit

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.itmo.tuman.R
import ru.itmo.tuman.core.uikit.StubScreen

/** Заглушка редактора карточки: лицевая и обратная сторона, пример, панель букв. */
@Composable
fun CardEditScreen(onBack: () -> Unit) {
    StubScreen(title = stringResource(R.string.card_edit_title), onBack = onBack) {
        Text(stringResource(R.string.stub_in_progress))
    }
}
