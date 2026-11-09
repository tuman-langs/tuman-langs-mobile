package ru.itmo.tuman.features.review.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.itmo.tuman.core.database.TumanDatabase
import ru.itmo.tuman.core.database.entity.CardEntity
import ru.itmo.tuman.core.database.entity.ReviewEntity
import ru.itmo.tuman.features.review.domain.ReviewRepository
import ru.itmo.tuman.features.review.domain.ReviewRepository.Companion.DAILY_GOAL
import ru.itmo.tuman.features.review.domain.model.ReviewCard
import ru.itmo.tuman.features.review.domain.model.TodayCount
import ru.itmo.tuman.srs.CardState
import ru.itmo.tuman.srs.DueCards
import ru.itmo.tuman.srs.Rating
import ru.itmo.tuman.srs.Scheduler
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/**
 * Реализация [ReviewRepository]: база Room + алгоритм из `core-srs`.
 *
 * @param clock текущее время; в тестах подставляем фиксированное.
 * @param zone часовой пояс телефона — по нему считается, где кончается «сегодня».
 */
class ReviewRepositoryImpl(
    private val db: TumanDatabase,
    private val clock: () -> Instant = Instant::now,
    private val zone: () -> ZoneId = ZoneId::systemDefault,
) : ReviewRepository {

    private val cardDao = db.cardDao()
    private val reviewDao = db.reviewDao()

    // Граница «сегодня» берётся в момент подписки. Экран подписывается заново
    // при каждом открытии, поэтому после полуночи список обновится.
    private fun startOfNextDayMillis(): Long =
        DueCards.startOfNextDay(clock(), zone()).toEpochMilli()

    override fun observeTodayCards(deckId: Long?): Flow<List<ReviewCard>> =
        reviewDao.observeTodayCards(startOfNextDayMillis(), deckId, DAILY_GOAL)
            .map { rows -> rows.map { it.toReviewCard() } }

    override fun observeTodayCount(): Flow<TodayCount> =
        reviewDao.observeTodayCounts(startOfNextDayMillis()).map { counts ->
            // Те же правила, что у списка: сначала карточки на повторение, новые — на остаток цели
            val due = counts.dueCount.coerceAtMost(DAILY_GOAL)
            val new = counts.newCount.coerceAtMost(DAILY_GOAL - due)
            TodayCount(due = due, new = new)
        }

    override suspend fun previewIntervals(cardId: Long, now: Instant): Map<Rating, Duration> {
        val state = requireCard(cardId).toCardState()
        return Rating.entries.associateWith { rating ->
            Duration.between(now, Scheduler.review(state, rating, now).due)
        }
    }

    override suspend fun submitAnswer(cardId: Long, rating: Rating, now: Instant) {
        // Транзакция: или сохранится и новое состояние карточки, и строка истории, или ничего
        db.withTransaction {
            val next = Scheduler.review(requireCard(cardId).toCardState(), rating, now)
            cardDao.updateSchedule(
                id = cardId,
                stability = next.stability,
                difficulty = next.difficulty,
                due = next.due.toEpochMilli(),
                lastReview = now.toEpochMilli(),
            )
            reviewDao.insert(
                ReviewEntity(
                    cardId = cardId,
                    rating = rating.grade,
                    reviewedAt = now.toEpochMilli(),
                    intervalDays = Duration.between(now, next.due).toMillis() / MILLIS_PER_DAY,
                ),
            )
        }
    }

    private suspend fun requireCard(cardId: Long): CardEntity =
        requireNotNull(cardDao.getById(cardId)) { "Карточки $cardId нет в базе" }

    private companion object {
        const val MILLIS_PER_DAY = 24.0 * 60 * 60 * 1000
    }
}

private fun CardEntity.toReviewCard() = ReviewCard(id = id, front = front, back = back, example = example)

private fun CardEntity.toCardState() = CardState(
    stability = stability,
    difficulty = difficulty,
    due = Instant.ofEpochMilli(due),
    lastReview = lastReview?.let(Instant::ofEpochMilli),
)
