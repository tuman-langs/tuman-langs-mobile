package ru.itmo.tuman.srs

import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant

class SchedulerTest {

    // Фиксированный момент, чтобы тесты не зависели от текущего времени.
    private val now: Instant = Instant.parse("2026-11-01T10:00:00Z")
    private val newCard = CardState.new(now)

    private fun intervalAfter(rating: Rating): Duration =
        Duration.between(now, Scheduler.review(newCard, rating, now).due)

    @Test
    fun easyGivesLongerIntervalThanGood() {
        assertTrue(intervalAfter(Rating.EASY) > intervalAfter(Rating.GOOD))
    }

    @Test
    fun againReturnsCardNoLaterThanTomorrow() {
        // Даже у «прочной» карточки (стабильность 30 дней) «не помню» возвращает её завтра.
        val strongCard = newCard.copy(stability = 30.0, lastReview = now.minus(Duration.ofDays(30)))
        val next = Scheduler.review(strongCard, Rating.AGAIN, now)
        assertTrue(next.due <= now.plus(Duration.ofDays(1)))
    }

    @Test
    fun dueIsAlwaysAfterNow() {
        for (rating in Rating.entries) {
            val next = Scheduler.review(newCard, rating, now)
            assertTrue("оценка $rating: due=${next.due}", next.due.isAfter(now))
        }
    }
}
