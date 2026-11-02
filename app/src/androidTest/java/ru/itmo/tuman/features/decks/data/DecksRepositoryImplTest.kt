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
import ru.itmo.tuman.features.decks.domain.model.Card

/**
 * Проверка репозитория наборов на настоящей базе Room в памяти (эмулятор).
 * Слова — тестовые строки с башкирскими буквами, а не проверенный контент.
 */
@RunWith(AndroidJUnit4::class)
class DecksRepositoryImplTest {

    private lateinit var db: TumanDatabase
    private lateinit var repository: DecksRepositoryImpl

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, TumanDatabase::class.java).build()
        repository = DecksRepositoryImpl(db.deckDao(), db.cardDao(), now = { NOW })
    }

    @After
    fun tearDown() {
        db.close()
    }

    /** Тест из задачи #12: удалили набор — его карточек больше нет. */
    @Test
    fun deletingDeckDeletesItsCards() = runBlocking {
        val deckId = repository.createDeck("Базовые слова")
        val cardId = repository.saveCard(Card(deckId = deckId, front = "ҡала", back = "город"))
        assertEquals(1, repository.observeCards(deckId).first().size)

        repository.deleteDeck(deckId)

        assertTrue(repository.observeCards(deckId).first().isEmpty())
        assertNull(repository.getCard(cardId))
        assertTrue(repository.observeDecks().first().isEmpty())
    }

    @Test
    fun decksComeWithCardCountIncludingEmptyDeck() = runBlocking {
        val full = repository.createDeck("Полный")
        repository.createDeck("Пустой")
        repository.saveCard(Card(deckId = full, front = "әсәй", back = "мама"))
        repository.saveCard(Card(deckId = full, front = "һыу", back = "вода"))

        val counts = repository.observeDecks().first().associate { it.title to it.cardCount }

        assertEquals(mapOf("Полный" to 2, "Пустой" to 0), counts)
    }

    @Test
    fun renameDeckChangesTitle() = runBlocking {
        val deckId = repository.createDeck("Черновик")

        repository.renameDeck(deckId, "  Глаголы  ")

        assertEquals("Глаголы", repository.observeDecks().first().single().title)
    }

    @Test
    fun editingCardTextKeepsAlgorithmFields() = runBlocking {
        val deckId = repository.createDeck("Набор")
        val cardId = repository.saveCard(Card(deckId = deckId, front = "ҡош", back = "птиц"))
        // Имитируем, что карточку уже повторяли: алгоритм сдвинул дату показа
        val reviewed = db.cardDao().getById(cardId)!!.copy(stability = 4.2, due = NOW + DAY, lastReview = NOW)
        db.cardDao().update(reviewed)

        repository.saveCard(Card(id = cardId, deckId = deckId, front = "ҡош", back = "птица", example = " "))

        val saved = db.cardDao().getById(cardId)!!
        assertEquals("птица", saved.back)
        assertNull(saved.example)
        assertEquals(4.2, saved.stability, 0.0)
        assertEquals(NOW + DAY, saved.due)
        assertEquals(NOW, saved.lastReview)
    }

    @Test
    fun deleteCardRemovesOnlyThatCard() = runBlocking {
        val deckId = repository.createDeck("Набор")
        val first = repository.saveCard(Card(deckId = deckId, front = "ат", back = "лошадь"))
        repository.saveCard(Card(deckId = deckId, front = "эт", back = "собака"))

        repository.deleteCard(first)

        assertEquals(listOf("эт"), repository.observeCards(deckId).first().map { it.front })
    }

    private companion object {
        const val NOW = 1_793_577_600_000L
        const val DAY = 24 * 60 * 60 * 1000L
    }
}
