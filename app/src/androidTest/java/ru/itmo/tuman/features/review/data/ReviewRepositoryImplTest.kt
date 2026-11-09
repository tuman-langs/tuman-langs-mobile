package ru.itmo.tuman.features.review.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.itmo.tuman.core.database.TumanDatabase
import ru.itmo.tuman.core.database.entity.CardEntity
import ru.itmo.tuman.core.database.entity.DeckEntity
import ru.itmo.tuman.features.review.domain.ReviewRepository.Companion.DAILY_GOAL
import ru.itmo.tuman.features.review.domain.model.TodayCount
import ru.itmo.tuman.srs.Rating
import java.time.Instant
import java.time.ZoneOffset

/**
 * Проверка повторения на настоящей базе Room в памяти (эмулятор).
 * Время зафиксировано: 09.11.2026 12:00 UTC, пояс UTC — тесты не зависят от часов телефона.
 * Слова — тестовые строки, а не проверенный контент.
 */
@RunWith(AndroidJUnit4::class)
class ReviewRepositoryImplTest {

    private lateinit var db: TumanDatabase
    private var now: Instant = NOW

    private fun repository() = ReviewRepositoryImpl(db, clock = { now }, zone = { ZoneOffset.UTC })

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, TumanDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun newDeck(): Long = db.deckDao().insert(DeckEntity(title = "Набор", createdAt = NOW_MS))

    private suspend fun newCard(deckId: Long, front: String, due: Long = NOW_MS, lastReview: Long? = null): Long =
        db.cardDao().insert(CardEntity(deckId = deckId, front = front, back = "перевод", due = due, lastReview = lastReview))

    @Test
    fun goodMovesDueToFutureAndAddsOneHistoryRow() = runBlocking {
        val cardId = newCard(newDeck(), "ҡош")

        repository().submitAnswer(cardId, Rating.GOOD, NOW)

        val card = db.cardDao().getById(cardId)!!
        assertTrue("дата показа должна сдвинуться в будущее", card.due > NOW_MS)
        assertEquals(NOW_MS, card.lastReview)
        val history = db.reviewDao().getByCard(cardId)
        assertEquals(1, history.size)
        assertEquals(Rating.GOOD.grade, history[0].rating)
        assertEquals(NOW_MS, history[0].reviewedAt)
        assertTrue(history[0].intervalDays >= 1.0)
        // Карточка ушла из списка на сегодня
        assertTrue(repository().observeTodayCards().first().isEmpty())
    }

    /**
     * По docs/fsrs.md (п. «Нет повторов в тот же день») минимальный интервал — 1 день,
     * а после «не помню» интервал падает до 1–2 дней. Поэтому забытая карточка
     * возвращается в список через короткий срок, а не сегодня же.
     */
    @Test
    fun againReturnsCardSoon() = runBlocking {
        val deckId = newDeck()
        // Карточку уже знали неплохо (S = 30 дней), но забыли
        val cardId = newCard(deckId, "һыу")
        db.cardDao().updateSchedule(cardId, stability = 30.0, difficulty = 5.0, due = NOW_MS - DAY, lastReview = NOW_MS - 31 * DAY)

        repository().submitAnswer(cardId, Rating.AGAIN, NOW)

        val due = db.cardDao().getById(cardId)!!.due
        assertTrue("после «не помню» интервал короткий", due - NOW_MS in DAY..3 * DAY)
        assertTrue(repository().observeTodayCards().first().isEmpty())
        now = Instant.ofEpochMilli(due)
        assertEquals(listOf(cardId), repository().observeTodayCards().first().map { it.id })
    }

    @Test
    fun tomorrowCardIsNotInTodayList() = runBlocking {
        val deckId = newDeck()
        val today = newCard(deckId, "сегодня", due = NOW_MS + HOUR * 11, lastReview = NOW_MS - DAY) // 23:00 сегодня
        newCard(deckId, "завтра", due = NOW_MS + HOUR * 13, lastReview = NOW_MS - DAY) // 01:00 завтра

        assertEquals(listOf(today), repository().observeTodayCards().first().map { it.id })
    }

    @Test
    fun emptyDeckGivesEmptyList() = runBlocking {
        val empty = newDeck()
        newCard(newDeck(), "в другом наборе")

        assertTrue(repository().observeTodayCards(deckId = empty).first().isEmpty())
    }

    @Test
    fun dueCardsGoFirstOverdueFirstThenNewUpToDailyGoal() = runBlocking {
        val deckId = newDeck()
        val new = (1..DAILY_GOAL).map { newCard(deckId, "новая $it") }
        val dueLater = newCard(deckId, "просрочена на день", due = NOW_MS - DAY, lastReview = NOW_MS - 3 * DAY)
        val dueEarlier = newCard(deckId, "просрочена на 2 дня", due = NOW_MS - 2 * DAY, lastReview = NOW_MS - 4 * DAY)

        val ids = repository().observeTodayCards().first().map { it.id }

        assertEquals(DAILY_GOAL, ids.size)
        assertEquals(listOf(dueEarlier, dueLater), ids.take(2))
        assertEquals(new.take(DAILY_GOAL - 2), ids.drop(2))
        assertEquals(TodayCount(due = 2, new = DAILY_GOAL - 2), repository().observeTodayCount().first())
    }

    @Test
    fun previewGivesIntervalForEachRatingAndChangesNothing() = runBlocking {
        val cardId = newCard(newDeck(), "ат")

        val intervals = repository().previewIntervals(cardId, NOW)

        assertEquals(Rating.entries.toSet(), intervals.keys)
        assertTrue(intervals.getValue(Rating.AGAIN) <= intervals.getValue(Rating.EASY))
        assertEquals(NOW_MS, db.cardDao().getById(cardId)!!.due)
        assertTrue(db.reviewDao().getByCard(cardId).isEmpty())
    }

    @Test
    fun answerForMissingCardWritesNothing() = runBlocking {
        val result = runCatching { repository().submitAnswer(cardId = 999, rating = Rating.GOOD, now = NOW) }

        assertTrue(result.isFailure)
        assertTrue(db.reviewDao().getByCard(999).isEmpty())
    }

    private companion object {
        val NOW: Instant = Instant.parse("2026-11-09T12:00:00Z")
        val NOW_MS = NOW.toEpochMilli()
        const val HOUR = 60 * 60 * 1000L
        const val DAY = 24 * HOUR
    }
}
