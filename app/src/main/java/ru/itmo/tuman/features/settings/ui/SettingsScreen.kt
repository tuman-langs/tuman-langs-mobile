package ru.itmo.tuman.features.settings.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.itmo.tuman.R
import ru.itmo.tuman.core.uikit.StubScreen

/** Заглушка: дневная цель и время напоминаний. */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    StubScreen(title = stringResource(R.string.settings_title), onBack = onBack) {
        Text(stringResource(R.string.stub_in_progress))
    }
}
