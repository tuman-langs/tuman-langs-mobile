package ru.itmo.tuman.features.review.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.itmo.tuman.R
import ru.itmo.tuman.features.review.domain.ReviewRepository
import ru.itmo.tuman.features.review.domain.model.ReviewCard
import ru.itmo.tuman.features.review.domain.model.TodayCount
import ru.itmo.tuman.srs.Rating
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewViewModelTest {

    private val now = Instant.parse("2026-11-15T12:00:00Z")
    private lateinit var repository: FakeReviewRepository
    private lateinit var viewModel: ReviewViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = FakeReviewRepository(listOf(card(1), card(2)))
        viewModel = ReviewViewModel(repository, now = { now })
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun revealingAnswerShowsFirstUseHintOnce() {
        viewModel.onRevealAnswer()

        assertTrue(viewModel.uiState.value.isAnswerVisible)
        assertTrue(viewModel.uiState.value.isHintVisible)
        viewModel.onHintDismissed()
        viewModel.onRatingSelected(Rating.GOOD)
        viewModel.onRevealAnswer()

        assertFalse(viewModel.uiState.value.isHintVisible)
        assertTrue(viewModel.uiState.value.isAnswerVisible)
    }

    @Test
    fun ratingAdvancesToNextCardAndTracksAnswers() {
        answer(Rating.GOOD)

        assertEquals(2L, viewModel.uiState.value.currentCard?.id)
        assertEquals(2, viewModel.uiState.value.position)
        assertEquals(2, viewModel.uiState.value.total)
        assertEquals(1, viewModel.uiState.value.answersCount)
        assertEquals(10, viewModel.uiState.value.earnedXp)
    }

    @Test
    fun againAddsCardToEndAndSessionFinishesAfterRepeat() {
        answer(Rating.AGAIN)
        assertEquals(2L, viewModel.uiState.value.currentCard?.id)
        assertEquals(3, viewModel.uiState.value.total)

        answer(Rating.GOOD)
        assertEquals(1L, viewModel.uiState.value.currentCard?.id)
        assertEquals(3, viewModel.uiState.value.position)

        answer(Rating.GOOD)
        assertTrue(viewModel.uiState.value.isFinished)
        assertNull(viewModel.uiState.value.currentCard)
        assertEquals(listOf(1L to Rating.AGAIN, 2L to Rating.GOOD, 1L to Rating.GOOD), repository.submitted)
        assertEquals(2, viewModel.uiState.value.answersCount)
        assertEquals(1, viewModel.uiState.value.correctAnswers)
        assertEquals(20, viewModel.uiState.value.earnedXp)
    }

    @Test
    fun againOnRepeatSavesAnswerAndReturnsCardToQueue() {
        answer(Rating.AGAIN)
        answer(Rating.GOOD)
        answer(Rating.AGAIN)

        assertEquals(1L, viewModel.uiState.value.currentCard?.id)
        assertEquals(4, viewModel.uiState.value.total)
        assertEquals(3, repository.submitted.size)

        answer(Rating.EASY)
        assertTrue(viewModel.uiState.value.isFinished)
        assertEquals(4, repository.submitted.size)
    }

    @Test
    fun shortGoodIntervalReturnsCardAfterOtherCards() {
        repository.goodInterval = Duration.ofMinutes(10)
        answer(Rating.GOOD)
        assertEquals(2L, viewModel.uiState.value.currentCard?.id)
        repository.goodInterval = Duration.ofDays(2)
        answer(Rating.GOOD)
        assertEquals(1L, viewModel.uiState.value.currentCard?.id)
        answer(Rating.GOOD)
        assertTrue(viewModel.uiState.value.isFinished)
        assertEquals(3, repository.submitted.size)
    }

    @Test
    fun emptyListShowsTodayDoneState() {
        val emptyViewModel = ReviewViewModel(FakeReviewRepository(emptyList()))

        assertFalse(emptyViewModel.uiState.value.isLoading)
        assertTrue(emptyViewModel.uiState.value.isEmpty)
    }

    @Test
    fun deckWithNoDueCardsCanBePracticedAndRestartedWithoutSavingRatings() {
        val practiceRepo = FakeReviewRepository(emptyList(), listOf(card(1)))
        val practice = ReviewViewModel(practiceRepo, deckId = 42, now = { now })

        repeat(2) {
            assertTrue(practice.uiState.value.isPractice)
            assertEquals(1L, practice.uiState.value.currentCard?.id)
            assertEquals(0, practice.uiState.value.answersCount)
            practice.onRevealAnswer()
            practice.onRatingSelected(Rating.AGAIN)
            assertTrue(practice.uiState.value.isFinished)
            assertEquals(0, practice.uiState.value.earnedXp)
            practice.onPracticeAgain()
        }

        assertTrue(practiceRepo.submitted.isEmpty())
    }

    @Test
    fun scheduledDeckSessionCanStartPracticeFromSummary() {
        val deckRepo = FakeReviewRepository(listOf(card(1)), listOf(card(1), card(2)))
        val deckSession = ReviewViewModel(deckRepo, deckId = 42, now = { now })
        assertFalse(deckSession.uiState.value.isPractice)
        deckSession.onRevealAnswer()
        deckSession.onRatingSelected(Rating.EASY)
        assertTrue(deckSession.uiState.value.isFinished)

        deckSession.onPracticeAgain()
        assertTrue(deckSession.uiState.value.isPractice)
        assertEquals(2, deckSession.uiState.value.total)
        assertEquals(listOf(1L to Rating.EASY), deckRepo.submitted)
    }

    @Test
    fun repositoryFailureIsReported() {
        val failingRepository = object : ReviewRepository by repository {
            override suspend fun submitAnswer(cardId: Long, rating: Rating, now: Instant): Duration {
                error("database unavailable")
            }
        }
        val failingViewModel = ReviewViewModel(failingRepository, now = { now })

        failingViewModel.onRevealAnswer()
        failingViewModel.onHintDismissed()
        failingViewModel.onRatingSelected(Rating.GOOD)

        assertEquals(R.string.review_error_save, failingViewModel.uiState.value.errorRes)
        assertEquals(1L, failingViewModel.uiState.value.currentCard?.id)
        assertFalse(failingViewModel.uiState.value.isSubmitting)
    }

    private fun answer(rating: Rating) {
        viewModel.onRevealAnswer()
        viewModel.onHintDismissed()
        viewModel.onRatingSelected(rating)
    }

    private fun card(id: Long) = ReviewCard(
        id = id,
        front = "слово $id",
        back = "перевод $id",
    )

    private class FakeReviewRepository(
        cards: List<ReviewCard>,
        private val practiceCards: List<ReviewCard> = cards,
    ) : ReviewRepository {
        private val cardsFlow = MutableStateFlow(cards)
        val submitted = mutableListOf<Pair<Long, Rating>>()
        var goodInterval: Duration = Duration.ofDays(3)

        override fun observeTodayCards(deckId: Long?): Flow<List<ReviewCard>> = cardsFlow

        override suspend fun getPracticeCards(deckId: Long): List<ReviewCard> = practiceCards

        override fun observeTodayCount(): Flow<TodayCount> = MutableStateFlow(TodayCount(0, 0))

        override suspend fun previewIntervals(cardId: Long, now: Instant): Map<Rating, Duration> =
            Rating.entries.associateWith(::intervalFor)

        override suspend fun submitAnswer(cardId: Long, rating: Rating, now: Instant): Duration {
            submitted += cardId to rating
            return intervalFor(rating)
        }

        private fun intervalFor(rating: Rating): Duration = when (rating) {
            Rating.AGAIN -> Duration.ofMinutes(1)
            Rating.GOOD -> goodInterval
            else -> Duration.ofDays(rating.grade.toLong())
        }
    }
}
