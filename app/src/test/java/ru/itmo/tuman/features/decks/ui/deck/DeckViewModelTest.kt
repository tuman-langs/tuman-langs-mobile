package ru.itmo.tuman.features.decks.ui.deck

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.itmo.tuman.features.decks.data.FakeDecksRepository
import ru.itmo.tuman.features.decks.domain.model.Card
import ru.itmo.tuman.srs.CardStatus
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class DeckViewModelTest {

    // Фиксированное «сейчас» и пояс UTC: статусы не зависят от того, когда запущен тест
    private val now = Instant.parse("2026-11-12T10:00:00Z")
    private val zone = ZoneOffset.UTC

    private lateinit var repository: FakeDecksRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = FakeDecksRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(deckId: Long) = DeckViewModel(deckId, repository, now = { now.toEpochMilli() }, zone = zone)

    private fun millis(daysFromNow: Long) = now.plus(Duration.ofDays(daysFromNow)).toEpochMilli()

    private suspend fun addCard(deckId: Long, front: String, lastReviewDays: Long?, dueDays: Long) =
        repository.saveCard(
            Card(
                deckId = deckId,
                front = front,
                back = "перевод",
                due = millis(dueDays),
                lastReview = lastReviewDays?.let { millis(it) },
            ),
        )

    @Test
    fun emptyDeckShowsTitleAndNoCards() = runTest {
        val deckId = repository.createDeck("Мои слова")

        val state = viewModel(deckId).uiState.value

        assertFalse(state.isLoading)
        assertEquals("Мои слова", state.title)
        assertTrue(state.cards.isEmpty())
    }

    @Test
    fun statusesAndCountersAreCalculated() = runTest {
        val deckId = repository.createDeck("Мои слова")
        addCard(deckId, "новая", lastReviewDays = null, dueDays = 0)
        addCard(deckId, "просрочена", lastReviewDays = -5, dueDays = -1)
        addCard(deckId, "через три дня", lastReviewDays = -1, dueDays = 3)
        addCard(deckId, "выучена", lastReviewDays = -30, dueDays = 5) // интервал 35 дней ≥ 21

        val state = viewModel(deckId).uiState.value

        assertEquals(
            listOf(CardStatus.New, CardStatus.DueToday, CardStatus.InDays(3), CardStatus.Learned),
            state.cards.map { it.status },
        )
        // «На сегодня» — как в списке повторения: новая и просроченная
        assertEquals(2, state.dueTodayCount)
        assertEquals(1, state.learnedCount)
    }

    @Test
    fun cardsOfOtherDecksAreNotShown() = runTest {
        val deckId = repository.createDeck("Первый")
        val otherDeckId = repository.createDeck("Второй")
        addCard(otherDeckId, "чужая", lastReviewDays = null, dueDays = 0)

        assertTrue(viewModel(deckId).uiState.value.cards.isEmpty())
    }

    @Test
    fun newCardAppearsWithoutReopeningScreen() = runTest {
        val deckId = repository.createDeck("Мои слова")
        val viewModel = viewModel(deckId)

        addCard(deckId, "новая", lastReviewDays = null, dueDays = 0)

        assertEquals(listOf("новая"), viewModel.uiState.value.cards.map { it.card.front })
    }
}
