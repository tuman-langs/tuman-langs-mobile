package ru.itmo.tuman.core.uikit.bashkirKeyboard

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

fun insertLetter(value: TextFieldValue, letter: Char): TextFieldValue {
    val selectionStart = minOf(value.selection.start, value.selection.end)
    val selectionEnd = maxOf(value.selection.start, value.selection.end)
    val textStart = selectionStart.coerceIn(0, value.text.length)
    val textEnd = selectionEnd.coerceIn(textStart, value.text.length)
    val updatedText = value.text.replaceRange(textStart, textEnd, letter.toString())
    val cursorPosition = textStart + 1

    return value.copy(
        text = updatedText,
        selection = TextRange(cursorPosition),
        composition = null,
    )
}
