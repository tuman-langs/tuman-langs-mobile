package ru.itmo.tuman.features.review.domain

import kotlinx.coroutines.flow.Flow
import ru.itmo.tuman.features.review.domain.model.ReviewCard
import ru.itmo.tuman.features.review.domain.model.TodayCount
import ru.itmo.tuman.srs.Rating
import java.time.Duration
import java.time.Instant

/**
 * Всё, что экран повторения и главный экран могут попросить у хранилища (ФТ-5, ФТ-7).
 *
 * Даты считает алгоритм из `core-srs` (Дворак М.), а этот репозиторий
 * соединяет его с базой: достаёт карточки и сохраняет результат.
 */
interface ReviewRepository {

    /**
     * Карточки на сегодня: сначала те, что пора повторить (просроченные первыми),
     * затем новые — пока не наберётся дневная цель [DAILY_GOAL].
     * [deckId] = `null` — из всех наборов.
     */
    fun observeTodayCards(deckId: Long? = null): Flow<List<ReviewCard>>

    /** Сколько на повторение и сколько новых — для главного экрана. */
    fun observeTodayCount(): Flow<TodayCount>

    /** Через сколько покажем карточку при каждой из 4 оценок — подписи под кнопками. */
    suspend fun previewIntervals(cardId: Long, now: Instant): Map<Rating, Duration>

    /** Пересчитывает карточку через `Scheduler.review` и сохраняет ответ — одной транзакцией. */
    suspend fun submitAnswer(cardId: Long, rating: Rating, now: Instant)

    companion object {
        /** Дневная цель: сколько карточек за день. Пока константа, настройку добавим позже (#30). */
        const val DAILY_GOAL = 20
    }
}
