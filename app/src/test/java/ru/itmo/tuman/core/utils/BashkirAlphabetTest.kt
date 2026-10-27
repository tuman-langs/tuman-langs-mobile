package ru.itmo.tuman.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BashkirAlphabetTest {

    // В списке ровно 9 букв, без повторов
    @Test
    fun lettersHaveNineUniqueItems() {
        assertEquals(9, BashkirAlphabet.letters.size)
        assertEquals(9, BashkirAlphabet.letters.toSet().size)
    }

    // «ҡаҙ» — слово с башкирскими буквами
    @Test
    fun wordWithBashkirLettersIsDetected() {
        assertTrue(BashkirAlphabet.containsBashkirLetter("ҡаҙ"))
    }

    // «кот» — только русские буквы
    @Test
    fun russianWordIsNotDetected() {
        assertFalse(BashkirAlphabet.containsBashkirLetter("кот"))
    }

    // «ӘСӘЙ» — заглавные башкирские буквы тоже находятся
    @Test
    fun uppercaseBashkirLettersAreDetected() {
        assertTrue(BashkirAlphabet.containsBashkirLetter("ӘСӘЙ"))
    }

    @Test
    fun latinSchwaIsNotDetected() {
        // «әсәй», но вместо кириллической «ә» (U+04D9) стоит латинская «ə» (U+0259)
        val wordWithLatinSchwa = "əсəй"
        assertFalse(BashkirAlphabet.containsBashkirLetter(wordWithLatinSchwa))
    }
}
