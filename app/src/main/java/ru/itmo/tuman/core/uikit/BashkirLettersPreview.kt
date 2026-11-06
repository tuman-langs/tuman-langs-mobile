package ru.itmo.tuman.core.uikit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.itmo.tuman.core.uikit.theme.TumanTheme

// Проверка ТЗ 6.3: все буквы видны в основных стилях текста, без квадратов и «?».
private const val BASHKIR_LETTERS = "Ә ә Ө ө Ү ү Ғ ғ Ҡ ҡ Ң ң Ҙ ҙ Ҫ ҫ Һ һ"

@Preview(showBackground = true, backgroundColor = 0xFFFBF8F3, widthDp = 420)
@Composable
private fun BashkirLettersPreview() {
    TumanTheme {
        val typography = MaterialTheme.typography
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                typography.titleLarge,
                typography.titleSmall,
                typography.bodyLarge,
                typography.bodyMedium,
                typography.bodySmall,
            ).forEach { style -> Text(BASHKIR_LETTERS, style = style) }
        }
    }
}
