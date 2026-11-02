package ru.itmo.tuman.features.review.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.itmo.tuman.R
import ru.itmo.tuman.core.uikit.StubScreen

/** Заглушка: сессия повторения карточек на сегодня. */
@Composable
fun ReviewScreen(onBack: () -> Unit) {
    StubScreen(title = stringResource(R.string.review_title), onBack = onBack) {
        Text(stringResource(R.string.stub_in_progress))
    }
}
