package ru.itmo.tuman.features.decks.ui.edit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.itmo.tuman.core.navigation.CardEditRoute
import ru.itmo.tuman.features.decks.data.FakeDecksRepository
import ru.itmo.tuman.features.decks.domain.model.Card
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class CardEditViewModelTest {

    private val now = Instant.parse("2026-11-12T10:00:00Z")
    private lateinit var repository: FakeDecksRepository
    private var deckId = 0L

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = FakeDecksRepository()
        deckId = repository.createDeck("Мои слова")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun editor(cardId: Long = CardEditRoute.NEW_CARD) =
        CardEditViewModel(deckId, cardId, repository, now = { now.toEpochMilli() }, zone = ZoneOffset.UTC)

    private suspend fun savedCards() = repository.observeCards(deckId).first()

    private fun CardEditViewModel.fill(front: String, back: String, example: String = "") {
        onFrontChange(front)
        onBackChange(back)
        onExampleChange(example)
    }

    @Test
    fun emptyBackShowsErrorAndSavesNothing() = runTest {
        val editor = editor()
        editor.fill(front = "Слово с ҡ", back = "   ")

        editor.onSave()

        assertTrue(editor.uiState.value.backError)
        assertFalse(editor.uiState.value.frontError)
        assertTrue(savedCards().isEmpty())
    }

    @Test
    fun typingClearsError() {
        val editor = editor()
        editor.onSave()

        editor.onFrontChange("ҡ")

        assertFalse(editor.uiState.value.frontError)
        assertTrue(editor.uiState.value.backError)
    }

    @Test
    fun savedCardAppearsInDeckAndFieldsAreCleared() = runTest {
        val editor = editor()
        editor.fill(front = "  Слово с ҡ, ң, һ  ", back = " перевод ", example = "  ")

        editor.onSave()

        val card = savedCards().single()
        assertEquals("Слово с ҡ, ң, һ", card.front)
        assertEquals("перевод", card.back)
        assertNull(card.example) // пустой пример не храним
        val state = editor.uiState.value
        assertEquals("", state.front)
        assertEquals("", state.back)
        assertEquals(AddedCard(card.id, "Слово с ҡ, ң, һ"), state.added)
    }

    @Test
    fun undoRemovesAddedCard() = runTest {
        val editor = editor()
        editor.fill(front = "слово", back = "перевод")
        editor.onSave()
        val added = editor.uiState.value.added!!

        editor.onUndoAdd(added.id)
        editor.onMessageShown()

        assertTrue(savedCards().isEmpty())
        assertNull(editor.uiState.value.added)
    }

    @Test
    fun existingCardIsLoadedWithSchedule() = runTest {
        val id = repository.saveCard(
            Card(
                deckId = deckId,
                front = "слово",
                back = "перевод",
                example = "пример",
                due = now.plus(Duration.ofDays(3)).toEpochMilli(),
                lastReview = now.minus(Duration.ofDays(1)).toEpochMilli(),
                reviewCount = 4,
            ),
        )

        val state = editor(id).uiState.value

        assertFalse(state.isNew)
        assertFalse(state.isLoading)
        assertEquals("слово", state.front)
        assertEquals("пример", state.example)
        assertEquals(3L, state.daysUntilDue)
        assertEquals(4, state.reviewCount)
    }

    @Test
    fun editingChangesTextAndClosesEditor() = runTest {
        val id = repository.saveCard(Card(deckId = deckId, front = "слово", back = "перевод"))
        val editor = editor(id)

        editor.onBackChange("новый перевод")
        editor.onSave()

        assertEquals(listOf("новый перевод"), savedCards().map { it.back })
        assertTrue(editor.uiState.value.isClosed)
    }

    @Test
    fun deleteRemovesCardAndClosesEditor() = runTest {
        val id = repository.saveCard(Card(deckId = deckId, front = "слово", back = "перевод"))
        val editor = editor(id)

        editor.onDelete()

        assertTrue(savedCards().isEmpty())
        assertTrue(editor.uiState.value.isClosed)
    }
}
