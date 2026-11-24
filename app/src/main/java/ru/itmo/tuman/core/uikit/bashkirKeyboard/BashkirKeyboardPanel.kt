package ru.itmo.tuman.core.uikit.bashkirKeyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.itmo.tuman.core.utils.BashkirAlphabet
import ru.itmo.tuman.core.uikit.theme.TumanTheme

@Composable
fun BashkirKeyboardPanel(
    onLetterClick: (Char) -> Unit,
    modifier: Modifier = Modifier,
) {
    BashkirKeyboardButtons(
        onLetterClick = onLetterClick,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 4.dp, vertical = 2.dp),
    )
}

@Composable
private fun BashkirKeyboardButtons(
    onLetterClick: (Char) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        BashkirAlphabet.letters.forEach { letter ->
            TextButton(
                onClick = { onLetterClick(letter) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp),
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(
                    text = letter.toString(),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BashkirKeyboardPanelPreview() {
    TumanTheme {
        var firstField by remember { mutableStateOf(TextFieldValue("ҡа")) }
        var secondField by remember { mutableStateOf(TextFieldValue()) }
        var focusedField by remember { mutableIntStateOf(0) }

        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = firstField,
                onValueChange = { firstField = it },
                label = { Text("Лицевая сторона") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (it.isFocused) focusedField = 0 },
            )
            OutlinedTextField(
                value = secondField,
                onValueChange = { secondField = it },
                label = { Text("Обратная сторона") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (it.isFocused) focusedField = 1 },
            )
            BashkirKeyboardButtons(
                onLetterClick = { letter ->
                    if (focusedField == 0) {
                        firstField = insertLetter(firstField, letter)
                    } else {
                        secondField = insertLetter(secondField, letter)
                    }
                },
            )
        }
    }
}
