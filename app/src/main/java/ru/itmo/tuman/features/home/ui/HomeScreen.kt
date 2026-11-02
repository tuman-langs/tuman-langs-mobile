package ru.itmo.tuman.features.home.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ru.itmo.tuman.R
import ru.itmo.tuman.core.uikit.StubScreen
import ru.itmo.tuman.core.uikit.theme.TumanTheme

/** Главный экран. Пока только кнопки перехода; дневная цель и серия появятся позже. */
@Composable
fun HomeScreen(
    onStartReviewClick: () -> Unit,
    onDecksClick: () -> Unit,
    onCatalogClick: () -> Unit,
    onProgressClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    StubScreen(title = stringResource(R.string.app_name)) {
        Button(onClick = onStartReviewClick, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.home_start_review))
        }
        OutlinedButton(onClick = onDecksClick, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.home_decks))
        }
        OutlinedButton(onClick = onCatalogClick, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.home_catalog))
        }
        OutlinedButton(onClick = onProgressClick, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.home_progress))
        }
        OutlinedButton(onClick = onSettingsClick, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.home_settings))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    TumanTheme {
        HomeScreen({}, {}, {}, {}, {})
    }
}
