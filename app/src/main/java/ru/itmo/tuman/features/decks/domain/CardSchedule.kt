package ru.itmo.tuman.features.decks.domain

import ru.itmo.tuman.features.decks.domain.model.Card
import ru.itmo.tuman.srs.CardState
import ru.itmo.tuman.srs.CardStatus
import ru.itmo.tuman.srs.status
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Статус карточки для списка в наборе: «новая», «сегодня», «через N д», «выучена».
 *
 * Само правило живёт в `core-srs` (договорились в #14), чтобы экран набора, XP и достижения
 * считали статус одинаково. Здесь мы только переводим нашу [Card] в `CardState` алгоритма.
 * Правилу статуса нужны лишь даты `due` и `lastReview`, поэтому стабильность и сложность
 * берём значениями по умолчанию — на результат они не влияют.
 */
fun Card.status(now: Instant, zone: ZoneId): CardStatus {
    val dueMillis = due
    val lastReviewMillis = lastReview
    if (dueMillis == null || lastReviewMillis == null) return CardStatus.New
    val state = CardState(
        stability = CardState.INITIAL_STABILITY_DAYS,
        difficulty = CardState.INITIAL_DIFFICULTY,
        due = Instant.ofEpochMilli(dueMillis),
        lastReview = Instant.ofEpochMilli(lastReviewMillis),
    )
    return state.status(now, zone)
}

/**
 * Через сколько календарных дней карточку покажут снова: 0 — сегодня (или уже пора).
 * `null` — карточку ещё ни разу не повторяли, дата показа пока ничего не значит.
 */
fun Card.daysUntilDue(now: Instant, zone: ZoneId): Long? {
    val dueMillis = due ?: return null
    if (lastReview == null) return null
    val today = now.atZone(zone).toLocalDate()
    val dueDay = Instant.ofEpochMilli(dueMillis).atZone(zone).toLocalDate()
    return ChronoUnit.DAYS.between(today, dueDay).coerceAtLeast(0)
}
