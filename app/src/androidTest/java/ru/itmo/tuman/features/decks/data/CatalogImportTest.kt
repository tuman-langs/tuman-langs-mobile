package ru.itmo.tuman.features.decks.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.itmo.tuman.core.database.TumanDatabase
import ru.itmo.tuman.core.database.entity.ReviewEntity
import ru.itmo.tuman.features.decks.domain.model.CatalogCard
import ru.itmo.tuman.features.decks.domain.model.CatalogSet

/**
 * Сохранение набора из каталога (#28) на настоящей базе Room в памяти (эмулятор).
 * Строки — тестовые, а не проверенный контент (кроме самих башкирских букв).
 */
@RunWith(AndroidJUnit4::class)
class CatalogImportTest {

    private lateinit var db: TumanDatabase
    private lateinit var repository: DecksRepositoryImpl

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, TumanDatabase::class.java).build()
        repository = DecksRepositoryImpl(db, now = { NOW })
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun letters(version: Int = 1, back: (Int) -> String = { "тест $it" }) = CatalogSet(
        remoteId = "ba-letters",
        version = version,
        title = "Буквы",
        description = "описание",
        cards = BASHKIR_LETTERS.mapIndexed { i, letter ->
            CatalogCard(remoteId = "ba-letters-%04d".format(i + 1), front = letter, back = back(i + 1), pronunciation = "[$letter]")
        },
    )

    /** Тест из задачи: набор из 9 букв → 9 карточек, буквы читаются без искажений. */
    @Test
    fun newSetCreatesDeckAndAllCards() = runBlocking {
        val deckId = repository.importCatalogSet(letters())

        val cards = db.cardDao().getByDeck(deckId).sortedBy { it.remoteId }
        assertEquals(9, cards.size)
        assertEquals(BASHKIR_LETTERS, cards.map { it.front })
        assertTrue(cards.all { it.due == NOW && it.lastReview == null })
        assertEquals("[ҡ]", cards.single { it.front == "ҡ" }.pronunciation)

        val deck = repository.observeDecks().first().single()
        assertTrue(deck.isFromCatalog)
        assertEquals(9, deck.cardCount)
        assertEquals(mapOf("ba-letters" to 1), repository.observeDownloadedVersions().first())
    }

    /** Тест из задачи: повторный импорт той же версии — дублей нет. */
    @Test
    fun sameVersionChangesNothing() = runBlocking {
        val first = repository.importCatalogSet(letters())

        val second = repository.importCatalogSet(letters(back = { "другое $it" }))

        assertEquals(first, second)
        assertEquals(1, repository.observeDecks().first().size)
        val cards = db.cardDao().getByDeck(first)
        assertEquals(9, cards.size)
        assertTrue("текст не должен меняться без новой версии", cards.all { it.back.startsWith("тест") })
    }

    /** Тест из задачи: версия 2 с исправленным back — текст обновился, прогресс и история остались. */
    @Test
    fun newVersionUpdatesTextAndKeepsProgress() = runBlocking {
        val deckId = repository.importCatalogSet(letters())
        val card = db.cardDao().getByDeck(deckId).single { it.remoteId == "ba-letters-0005" }
        // Имитируем, что карточку уже повторяли
        db.cardDao().updateSchedule(card.id, stability = 7.5, difficulty = 4.0, due = NOW + DAY * 7, lastReview = NOW)
        db.reviewDao().insert(ReviewEntity(cardId = card.id, rating = 3, reviewedAt = NOW, intervalDays = 7.0))

        val sameDeck = repository.importCatalogSet(letters(version = 2, back = { "исправлено $it" }))

        assertEquals(deckId, sameDeck)
        val updated = db.cardDao().getById(card.id)!!
        assertEquals("исправлено 5", updated.back)
        assertEquals(7.5, updated.stability, 0.0)
        assertEquals(4.0, updated.difficulty, 0.0)
        assertEquals(NOW + DAY * 7, updated.due)
        assertEquals(NOW, updated.lastReview)
        assertEquals(1, db.reviewDao().getByCard(card.id).size)
        assertEquals(mapOf("ba-letters" to 2), repository.observeDownloadedVersions().first())
    }

    @Test
    fun newVersionAddsNewCardsAndRemovesMissing() = runBlocking {
        val deckId = repository.importCatalogSet(letters())
        val removed = db.cardDao().getByDeck(deckId).single { it.remoteId == "ba-letters-0001" }
        db.reviewDao().insert(ReviewEntity(cardId = removed.id, rating = 1, reviewedAt = NOW, intervalDays = 1.0))
        val v2 = letters(version = 2).let { set ->
            set.copy(cards = set.cards.drop(1) + CatalogCard(remoteId = "ba-letters-0010", front = "новая", back = "тест"))
        }

        repository.importCatalogSet(v2)

        val remoteIds = db.cardDao().getByDeck(deckId).map { it.remoteId }.toSet()
        assertEquals(9, remoteIds.size)
        assertTrue("ba-letters-0010" in remoteIds)
        assertTrue("ba-letters-0001" !in remoteIds)
        assertNull(db.cardDao().getById(removed.id))
        assertTrue(db.reviewDao().getByCard(removed.id).isEmpty())
    }

    private companion object {
        const val NOW = 1_794_960_000_000L // 18.11.2026
        const val DAY = 24 * 60 * 60 * 1000L
        val BASHKIR_LETTERS = listOf("ә", "ө", "ү", "ғ", "ҡ", "ң", "ҙ", "ҫ", "һ")
    }
}
