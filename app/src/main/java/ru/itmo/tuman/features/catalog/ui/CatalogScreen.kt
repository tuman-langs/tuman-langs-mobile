package ru.itmo.tuman.features.catalog.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.itmo.tuman.R
import ru.itmo.tuman.core.uikit.StubScreen

/** Заглушка: каталог готовых наборов с сервера. */
@Composable
fun CatalogScreen(onBack: () -> Unit) {
    StubScreen(title = stringResource(R.string.catalog_title), onBack = onBack) {
        Text(stringResource(R.string.stub_in_progress))
    }
}
