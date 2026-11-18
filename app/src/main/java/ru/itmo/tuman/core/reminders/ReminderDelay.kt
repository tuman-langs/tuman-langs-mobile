package ru.itmo.tuman.core.reminders

import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime

/** Сколько ждать до ближайшего напоминания. Чистая функция — проверяется юнит-тестами. */
object ReminderDelay {

    /**
     * Время до ближайшего наступления [time] **строго после** [now].
     *
     * Сейчас 18:00, напоминание в 19:00 → 1 ч; сейчас 20:00 → 23 ч (завтра).
     * Сейчас ровно 19:00 → 24 ч: напоминание, сохранённое в ту же минуту, не срабатывает
     * сразу, а приходит завтра — иначе пользователь получит уведомление прямо в момент настройки.
     *
     * Считаем через [ZonedDateTime], поэтому переход на летнее/зимнее время учитывается.
     */
    fun untilNext(now: ZonedDateTime, time: LocalTime): Duration {
        val todayAtTime = now.toLocalDate().atTime(time).atZone(now.zone)
        val next = if (todayAtTime.isAfter(now)) todayAtTime else now.toLocalDate().plusDays(1).atTime(time).atZone(now.zone)
        return Duration.between(now, next)
    }
}
