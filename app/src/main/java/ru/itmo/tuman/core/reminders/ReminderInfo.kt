package ru.itmo.tuman.core.reminders

/**
 * Что написать в напоминании.
 *
 * @property cardsToday сколько карточек ждёт сегодня; 0 — не беспокоим.
 * @property streakDays текущая серия дней.
 */
data class ReminderInfo(
    val cardsToday: Int,
    val streakDays: Int,
)

/**
 * Откуда напоминание берёт числа. Сами числа считают повторение (#21) и прогресс (#30),
 * но `core` по правилам архитектуры не знает о `features`. Поэтому здесь только интерфейс,
 * а связывает его с ними `di/AppModule.kt` — единственное место, которое знает обо всём.
 */
fun interface ReminderInfoSource {
    suspend fun current(): ReminderInfo
}
