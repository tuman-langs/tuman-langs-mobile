package ru.itmo.tuman.srs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class CardStatusTest {

    // 1 ноября, 10:00 по Москве.
    private val zone: ZoneId = ZoneId.of("Europe/Moscow")
    private val now: Instant = Instant.parse("2026-11-01T07:00:00Z")

    /** Карточка, на которую ответили [answeredDaysAgo] дней назад с интервалом [intervalDays]. */
    private fun reviewed(answeredDaysAgo: Long, intervalDays: Long): CardState {
        val last = now.minus(Duration.ofDays(answeredDaysAgo))
        return CardState(
            stability = intervalDays.toDouble(),
            difficulty = 5.0,
            due = last.plus(Duration.ofDays(intervalDays)),
            lastReview = last,
        )
    }

    @Test
    fun newCardIsNew() {
        assertEquals(CardStatus.New, CardState.new(now).status(now, zone))
        assertFalse(CardState.new(now).isLearned())
    }

    @Test
    fun overdueAndTodayCardsAreDueToday() {
        assertEquals(CardStatus.DueToday, reviewed(answeredDaysAgo = 5, intervalDays = 2).status(now, zone))
        assertEquals(CardStatus.DueToday, reviewed(answeredDaysAgo = 3, intervalDays = 3).status(now, zone))
    }

    @Test
    fun futureCardShowsDaysLeft() {
        // Ответили вчера с интервалом 4 дня → показ через 3 дня.
        assertEquals(CardStatus.InDays(3), reviewed(answeredDaysAgo = 1, intervalDays = 4).status(now, zone))
    }

    @Test
    fun learnedFrom21Days() {
        assertFalse(reviewed(answeredDaysAgo = 0, intervalDays = 20).isLearned())
        assertTrue(reviewed(answeredDaysAgo = 0, intervalDays = 21).isLearned())
        assertEquals(CardStatus.Learned, reviewed(answeredDaysAgo = 0, intervalDays = 21).status(now, zone))
        assertEquals(CardStatus.InDays(20), reviewed(answeredDaysAgo = 0, intervalDays = 20).status(now, zone))
    }

    @Test
    fun dueTodayWinsOverLearned() {
        // Выученная карточка, но срок подошёл — её нужно повторить.
        val card = reviewed(answeredDaysAgo = 30, intervalDays = 30)
        assertTrue(card.isLearned())
        assertEquals(CardStatus.DueToday, card.status(now, zone))
    }

    @Test
    fun againMakesLearnedCardNotLearned() {
        val learned = reviewed(answeredDaysAgo = 30, intervalDays = 30)
        val afterAgain = Scheduler.review(learned, Rating.AGAIN, now)
        assertFalse(afterAgain.isLearned())
    }
}
