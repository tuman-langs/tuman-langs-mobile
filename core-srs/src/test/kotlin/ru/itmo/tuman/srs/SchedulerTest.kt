package ru.itmo.tuman.srs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant

class SchedulerTest {

    // Фиксированный момент, чтобы тесты не зависели от текущего времени.
    private val now: Instant = Instant.parse("2026-11-01T10:00:00Z")
    private val newCard = CardState.new(now)
    private val w = FsrsParameters.W

    private fun daysLater(days: Long): Instant = now.plus(Duration.ofDays(days))

    private fun intervalDays(state: CardState): Long =
        Duration.between(state.lastReview, state.due).toDays()

    // --- Новая карточка: по тесту на каждую оценку ---

    @Test
    fun newCardAgain_stabilityW0_repeatsInOneMinute() {
        val next = Scheduler.review(newCard, Rating.AGAIN, now)
        assertEquals(w[0], next.stability, 1e-9)
        assertEquals(now.plusSeconds(60), next.due)
        assertEquals(now, next.lastReview)
    }

    @Test
    fun newCardHard_stabilityW1() {
        val next = Scheduler.review(newCard, Rating.HARD, now)
        assertEquals(w[1], next.stability, 1e-9)
        assertEquals(now.plusSeconds(360), next.due)
    }

    @Test
    fun newCardGood_stabilityW2_difficultyFromFormula() {
        val next = Scheduler.review(newCard, Rating.GOOD, now)
        assertEquals(w[2], next.stability, 1e-9)
        assertEquals(now.plusSeconds(600), next.due)
        // D0(3) = w4 − e^(2·w5) + 1 ≈ 2,12
        assertEquals(2.12, next.difficulty, 0.01)
    }

    @Test
    fun newCardEasy_shownTomorrowWithStabilityW3AndMinimumDifficulty() {
        val next = Scheduler.review(newCard, Rating.EASY, now)
        assertEquals(w[3], next.stability, 1e-9)
        assertEquals(1L, intervalDays(next))
        // По формуле D0(4) ≈ −4,8, но сложность не бывает меньше 1.
        assertEquals(FsrsParameters.MIN_DIFFICULTY, next.difficulty, 1e-9)
    }

    // --- Свойства алгоритма ---

    @Test
    fun againDecreasesStability() {
        val reviewed = newCard.copy(stability = 30.0, difficulty = 5.0, lastReview = daysLater(-30), due = now)
        val next = Scheduler.review(reviewed, Rating.AGAIN, now)
        assertTrue("S после «не помню» = ${next.stability}", next.stability < reviewed.stability)
    }

    @Test
    fun againIncreasesDifficulty_easyDecreasesIt() {
        val reviewed = newCard.copy(stability = 10.0, difficulty = 5.0, lastReview = daysLater(-10), due = now)
        assertTrue(Scheduler.review(reviewed, Rating.AGAIN, now).difficulty > 5.0)
        assertTrue(Scheduler.review(reviewed, Rating.EASY, now).difficulty < 5.0)
    }

    @Test
    fun easyIntervalLongerThanGood_goodLongerThanHard() {
        val reviewed = newCard.copy(stability = 10.0, difficulty = 5.0, lastReview = daysLater(-10), due = now)
        val hard = intervalDays(Scheduler.review(reviewed, Rating.HARD, now))
        val good = intervalDays(Scheduler.review(reviewed, Rating.GOOD, now))
        val easy = intervalDays(Scheduler.review(reviewed, Rating.EASY, now))
        assertTrue("трудно=$hard, хорошо=$good, легко=$easy", easy > good && good > hard)
    }

    @Test
    fun dueIsAlwaysAfterNow() {
        val reviewed = newCard.copy(stability = 5.0, difficulty = 7.0, lastReview = daysLater(-5), due = now)
        for (state in listOf(newCard, reviewed)) {
            for (rating in Rating.entries) {
                val next = Scheduler.review(state, rating, now)
                assertTrue("оценка $rating: due=${next.due}", next.due.isAfter(now))
            }
        }
    }

    @Test
    fun retrievabilityIs90PercentAfterStabilityDays() {
        // Определение стабильности: через S дней вероятность вспомнить падает до 90 %.
        assertEquals(0.9, Scheduler.retrievability(elapsedDays = 11.0, stability = 11.0), 1e-9)
        assertEquals(1.0, Scheduler.retrievability(elapsedDays = 0.0, stability = 11.0), 1e-9)
    }

    @Test
    fun intervalEqualsStabilityAt90PercentRetention() {
        assertEquals(11L, Scheduler.intervalDays(11.0))
        assertEquals(1L, Scheduler.intervalDays(0.2))
    }

    @Test
    fun learningAdvancesThenReturnsToDaysAndLapseStartsRelearning() {
        val first = Scheduler.review(newCard, Rating.AGAIN, now)
        val second = Scheduler.review(first, Rating.GOOD, first.due)
        assertEquals(first.due.plusSeconds(600), second.due)
        assertEquals(second.due.plusSeconds(600), Scheduler.review(second, Rating.HARD, second.due).due)
        val graduated = Scheduler.review(second, Rating.GOOD, second.due)
        assertTrue(intervalDays(graduated) >= 1)
        val relearning = Scheduler.review(graduated, Rating.AGAIN, graduated.due)
        assertEquals(graduated.due.plusSeconds(600), relearning.due)
        assertTrue(intervalDays(Scheduler.review(relearning, Rating.GOOD, relearning.due)) >= 1)
    }
}
