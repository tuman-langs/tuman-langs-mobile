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
    fun newCardAgain_stabilityW0_shownTomorrow() {
        val next = Scheduler.review(newCard, Rating.AGAIN, now)
        assertEquals(w[0], next.stability, 1e-9)
        // S0 = 0,21 дня округляется до минимального интервала — 1 день.
        assertEquals(1L, intervalDays(next))
        assertEquals(now, next.lastReview)
    }

    @Test
    fun newCardHard_stabilityW1() {
        val next = Scheduler.review(newCard, Rating.HARD, now)
        assertEquals(w[1], next.stability, 1e-9)
        assertEquals(1L, intervalDays(next))
    }

    @Test
    fun newCardGood_stabilityW2_difficultyFromFormula() {
        val next = Scheduler.review(newCard, Rating.GOOD, now)
        assertEquals(w[2], next.stability, 1e-9)
        assertEquals(2L, intervalDays(next))
        // D0(3) = w4 − e^(2·w5) + 1 ≈ 2,12
        assertEquals(2.12, next.difficulty, 0.01)
    }

    @Test
    fun newCardEasy_stabilityW3_difficultyClampedToOne() {
        val next = Scheduler.review(newCard, Rating.EASY, now)
        assertEquals(w[3], next.stability, 1e-9)
        assertEquals(8L, intervalDays(next))
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

    /** Пример из docs/fsrs.md: «хорошо» → «хорошо» через 2 дня → «не помню» через 11 дней. */
    @Test
    fun exampleFromDocs() {
        val day0 = Scheduler.review(newCard, Rating.GOOD, now)
        assertEquals(2L, intervalDays(day0))

        val day2 = Scheduler.review(day0, Rating.GOOD, day0.due)
        assertEquals(11.0, day2.stability, 0.1)

        val day13 = Scheduler.review(day2, Rating.AGAIN, day2.due)
        assertEquals(1.5, day13.stability, 0.1)
        assertEquals(7.4, day13.difficulty, 0.1)
    }
}
