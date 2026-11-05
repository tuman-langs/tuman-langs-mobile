package ru.itmo.tuman.srs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class DueCardsTest {

    /** Простая карточка для тестов: имя и дата показа. */
    private data class TestCard(val name: String, val due: Instant)

    // 1 ноября, 10:00 по Москве (UTC+3).
    private val zone: ZoneId = ZoneId.of("Europe/Moscow")
    private val now: Instant = Instant.parse("2026-11-01T07:00:00Z")

    private fun card(name: String, dueUtc: String) = TestCard(name, Instant.parse(dueUtc))

    private fun select(cards: List<TestCard>, limit: Int = 100): List<String> =
        DueCards.select(cards, now, zone, limit) { it.due }.map { it.name }

    @Test
    fun emptyInput_emptyResult() {
        assertTrue(select(emptyList()).isEmpty())
    }

    @Test
    fun tomorrowCardsAreExcluded() {
        val cards = listOf(
            card("сегодня вечером", "2026-11-01T20:30:00Z"), // 23:30 по Москве — ещё сегодня
            card("завтра в полночь", "2026-11-01T21:00:00Z"), // 00:00 2 ноября по Москве
            card("через неделю", "2026-11-08T07:00:00Z"),
        )
        assertEquals(listOf("сегодня вечером"), select(cards))
    }

    @Test
    fun overdueCardsComeFirst() {
        val cards = listOf(
            card("сегодня", "2026-11-01T12:00:00Z"),
            card("просрочена на 3 дня", "2026-10-29T07:00:00Z"),
            card("просрочена на 1 день", "2026-10-31T07:00:00Z"),
        )
        assertEquals(
            listOf("просрочена на 3 дня", "просрочена на 1 день", "сегодня"),
            select(cards),
        )
    }

    @Test
    fun limitKeepsMostOverdue() {
        val cards = listOf(
            card("a", "2026-11-01T05:00:00Z"),
            card("b", "2026-10-30T05:00:00Z"),
            card("c", "2026-10-31T05:00:00Z"),
        )
        assertEquals(listOf("b", "c"), select(cards, limit = 2))
        assertTrue(select(cards, limit = 0).isEmpty())
    }

    @Test
    fun cardAfterReviewIsNotDueToday() {
        // Связка со Scheduler: после любого ответа карточка уходит минимум на завтра.
        for (rating in Rating.entries) {
            val reviewed = Scheduler.review(CardState.new(now), rating, now)
            val due = DueCards.select(listOf(reviewed), now, zone, 10) { it.due }
            assertTrue("оценка $rating: due=${reviewed.due}", due.isEmpty())
        }
    }
}
