package ru.itmo.tuman.features.home.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ru.itmo.tuman.R
import ru.itmo.tuman.core.uikit.StubScreen
import ru.itmo.tuman.core.uikit.TumanButtonSize
import ru.itmo.tuman.core.uikit.TumanPrimaryButton
import ru.itmo.tuman.core.uikit.theme.TumanTheme

/** Главный экран. Пока только кнопка повторения; цель и серия появятся в задаче #33. */
@Composable
fun HomeScreen(
    onStartReviewClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    StubScreen(
        title = stringResource(R.string.app_name),
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.home_settings),
                )
            }
        },
    ) {
        TumanPrimaryButton(
            text = stringResource(R.string.home_start_review),
            onClick = onStartReviewClick,
            modifier = Modifier.fillMaxWidth(),
            size = TumanButtonSize.Large,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    TumanTheme {
        HomeScreen({}, {})
    }
}
