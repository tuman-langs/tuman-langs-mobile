package ru.itmo.tuman.srs

import java.time.Instant
import java.time.ZoneId

/**
 * Список карточек «на сегодня» (ФТ-5).
 */
object DueCards {

    /**
     * Отбирает из [cards] те, что пора повторить сегодня, и возвращает не больше [limit] штук.
     *
     * - «Сегодня» — до конца текущего дня в часовом поясе [zone] (пояс телефона):
     *   карточка с датой показа сегодня в 23:30 попадает в список уже утром.
     * - Сортировка по дате показа: просроченные идут первыми.
     *
     * Функция обобщённая: `core-srs` не знает, как устроена карточка в приложении,
     * поэтому дату показа достаёт переданная функция [dueOf]. Например:
     * `DueCards.select(cards, now, ZoneId.systemDefault(), 20) { it.state.due }`.
     */
    fun <T> select(
        cards: List<T>,
        now: Instant,
        zone: ZoneId,
        limit: Int,
        dueOf: (T) -> Instant,
    ): List<T> {
        require(limit >= 0) { "limit не может быть отрицательным: $limit" }
        val startOfTomorrow = startOfNextDay(now, zone)
        return cards
            .filter { dueOf(it).isBefore(startOfTomorrow) }
            .sortedBy(dueOf)
            .take(limit)
    }

    /** Начало завтрашнего дня (00:00) в поясе [zone] — граница списка «на сегодня». */
    fun startOfNextDay(now: Instant, zone: ZoneId): Instant =
        now.atZone(zone).toLocalDate().plusDays(1).atStartOfDay(zone).toInstant()
}
