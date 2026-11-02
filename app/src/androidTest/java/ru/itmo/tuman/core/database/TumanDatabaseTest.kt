package ru.itmo.tuman.core.database

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.itmo.tuman.core.database.entity.CardEntity
import ru.itmo.tuman.core.database.entity.DeckEntity
import ru.itmo.tuman.core.database.entity.DeckSource
import ru.itmo.tuman.core.database.entity.ReviewEntity

/**
 * Проверка базы на эмуляторе: Android Studio → правый клик по файлу → Run.
 *
 * База создаётся в памяти (inMemory) и пропадает после теста,
 * поэтому настоящие данные приложения тест не трогает.
 *
 * Слова здесь — только тестовые строки с башкирскими буквами,
 * а не проверенный контент для наборов.
 */
@RunWith(AndroidJUnit4::class)
class TumanDatabaseTest {

    private lateinit var db: TumanDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, TumanDatabase::class.java).build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun cardWithBashkirLettersIsSavedAndReadBack() = runBlocking {
        val deckId = db.deckDao().insert(
            DeckEntity(title = "Базовые слова", createdAt = NOW, source = DeckSource.CATALOG),
        )
        // ҡ и ә — буквы башкирского алфавита, которых нет в русском
        val cardId = db.cardDao().insert(
            CardEntity(
                deckId = deckId,
                front = "ҡала",
                back = "город",
                example = "Беҙҙең ҡала матур",
                due = NOW,
            ),
        )
        db.cardDao().insert(CardEntity(deckId = deckId, front = "әсәй", back = "мама", due = NOW))

        val saved = db.cardDao().getById(cardId)
        assertNotNull(saved)
        assertEquals("ҡала", saved!!.front)
        assertEquals("Беҙҙең ҡала матур", saved.example)

        val cards = db.cardDao().observeByDeck(deckId).first()
        assertEquals(listOf("ҡала", "әсәй"), cards.map { it.front })

        val deck = db.deckDao().getById(deckId)
        assertEquals(DeckSource.CATALOG, deck!!.source)
    }

    @Test
    fun newCardHasInitialAlgorithmFields() = runBlocking {
        val deckId = db.deckDao().insert(DeckEntity(title = "Набор", createdAt = NOW))
        val cardId = db.cardDao().insert(CardEntity(deckId = deckId, front = "һыу", back = "вода", due = NOW))

        val card = db.cardDao().getById(cardId)!!
        assertEquals(1.0, card.stability, 0.0)
        assertEquals(5.0, card.difficulty, 0.0)
        assertEquals(NOW, card.due)
        assertNull(card.lastReview)
        assertNull(card.example)
    }

    @Test
    fun reviewIsSavedForCard() = runBlocking {
        val deckId = db.deckDao().insert(DeckEntity(title = "Набор", createdAt = NOW))
        val cardId = db.cardDao().insert(CardEntity(deckId = deckId, front = "ҡош", back = "птица", due = NOW))

        db.reviewDao().insert(ReviewEntity(cardId = cardId, rating = 3, reviewedAt = NOW, intervalDays = 2.5))

        val reviews = db.reviewDao().getByCard(cardId)
        assertEquals(1, reviews.size)
        assertEquals(3, reviews[0].rating)
        assertEquals(2.5, reviews[0].intervalDays, 0.0)
    }

    @Test
    fun deletingDeckDeletesItsCardsAndReviews() = runBlocking {
        val deck = DeckEntity(title = "Удаляемый", createdAt = NOW)
        val deckId = db.deckDao().insert(deck)
        val cardId = db.cardDao().insert(CardEntity(deckId = deckId, front = "ат", back = "лошадь", due = NOW))
        db.reviewDao().insert(ReviewEntity(cardId = cardId, rating = 1, reviewedAt = NOW, intervalDays = 0.0))

        db.deckDao().delete(deck.copy(id = deckId))

        assertNull(db.deckDao().getById(deckId))
        assertNull(db.cardDao().getById(cardId))
        assertTrue(db.reviewDao().getByCard(cardId).isEmpty())
    }

    private companion object {
        // 02.11.2026 00:00 UTC — фиксированное время, чтобы тест не зависел от часов
        const val NOW = 1_793_577_600_000L
    }
}
