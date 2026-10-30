package ru.itmo.tuman.features.progress.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.itmo.tuman.R
import ru.itmo.tuman.core.uikit.StubScreen

/** Заглушка: серия дней, XP и достижения. */
@Composable
fun ProgressScreen(onBack: () -> Unit) {
    StubScreen(title = stringResource(R.string.progress_title), onBack = onBack) {
        Text(stringResource(R.string.stub_in_progress))
    }
}
