package ru.itmo.tuman.srs

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Статус карточки для списка в наборе (#23), XP и достижений (#30).
 * Правило предложено в комментарии к задаче #14.
 */
sealed interface CardStatus {
    /** На карточку ещё ни разу не отвечали. */
    data object New : CardStatus

    /** Пора повторить сегодня (в том числе просроченная). */
    data object DueToday : CardStatus

    /** Интервал до следующего показа — от [LEARNED_INTERVAL_DAYS] дней. */
    data object Learned : CardStatus

    /** Покажем через [days] календарных дней (1 — завтра). */
    data class InDays(val days: Long) : CardStatus

    companion object {
        /** С какого интервала карточка считается выученной: три недели. */
        const val LEARNED_INTERVAL_DAYS = 21L
    }
}

/**
 * Выучена ли карточка: интервал между последним ответом и следующим показом
 * не меньше [CardStatus.LEARNED_INTERVAL_DAYS] дней. После «не помню» интервал
 * падает, и карточка снова перестаёт быть выученной.
 */
fun CardState.isLearned(): Boolean {
    val last = lastReview ?: return false
    return Duration.between(last, due).toDays() >= CardStatus.LEARNED_INTERVAL_DAYS
}

/**
 * Статус карточки в момент [now] в часовом поясе телефона [zone].
 * Порядок важен: «сегодня» важнее «выучена» — повторить карточку всё равно нужно.
 */
fun CardState.status(now: Instant, zone: ZoneId): CardStatus = when {
    isNew -> CardStatus.New
    due.isBefore(DueCards.startOfNextDay(now, zone)) -> CardStatus.DueToday
    isLearned() -> CardStatus.Learned
    else -> CardStatus.InDays(calendarDaysBetween(now, due, zone))
}

/** Сколько календарных дней от даты [from] до даты [to] в поясе [zone]. */
private fun calendarDaysBetween(from: Instant, to: Instant, zone: ZoneId): Long =
    ChronoUnit.DAYS.between(from.atZone(zone).toLocalDate(), to.atZone(zone).toLocalDate())
