package ru.itmo.tuman.core.uikit.bashkirKeyboard

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class InsertLetterTest {

    @Test
    fun insertsIntoEmptyField() {
        val result = insertLetter(TextFieldValue(), 'ғ')

        assertEquals("ғ", result.text)
        assertEquals(TextRange(1), result.selection)
    }

    @Test
    fun insertsAtCursorInMiddleOfWord() {
        val result = insertLetter(
            value = TextFieldValue("ҡа", selection = TextRange(1)),
            letter = 'ҙ',
        )

        assertEquals("ҡҙа", result.text)
        assertEquals(TextRange(2), result.selection)
    }

    @Test
    fun insertsAtEndOfWord() {
        val result = insertLetter(
            value = TextFieldValue("ҡа", selection = TextRange(2)),
            letter = 'ң',
        )

        assertEquals("ҡаң", result.text)
        assertEquals(TextRange(3), result.selection)
    }

    @Test
    fun replacesSelectedText() {
        val result = insertLetter(
            value = TextFieldValue("abc", selection = TextRange(1, 3)),
            letter = 'ә',
        )

        assertEquals("aә", result.text)
        assertEquals(TextRange(2), result.selection)
    }

    @Test
    fun replacesBackwardSelection() {
        val result = insertLetter(
            value = TextFieldValue("abc", selection = TextRange(3, 1)),
            letter = 'ө',
        )

        assertEquals("aө", result.text)
        assertEquals(TextRange(2), result.selection)
    }
}
