package ru.itmo.tuman.core.reminders

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderDelayTest {

    private val moscow = ZoneId.of("Europe/Moscow")
    private val at19 = LocalTime.of(19, 0)

    private fun now(hour: Int, minute: Int = 0, second: Int = 0) =
        ZonedDateTime.of(2026, 11, 18, hour, minute, second, 0, moscow)

    @Test
    fun beforeTimeWaitsUntilToday() {
        assertEquals(Duration.ofHours(1), ReminderDelay.untilNext(now(18), at19))
    }

    @Test
    fun afterTimeWaitsUntilTomorrow() {
        assertEquals(Duration.ofHours(23), ReminderDelay.untilNext(now(20), at19))
    }

    /** Решение задачи: ровно в момент напоминания — следующее через сутки, а не сразу. */
    @Test
    fun exactlyAtTimeWaitsFullDay() {
        assertEquals(Duration.ofHours(24), ReminderDelay.untilNext(now(19), at19))
    }

    @Test
    fun secondsAreCounted() {
        assertEquals(Duration.ofSeconds(30), ReminderDelay.untilNext(now(18, 59, 30), at19))
    }

    @Test
    fun midnightReminder() {
        assertEquals(Duration.ofMinutes(30), ReminderDelay.untilNext(now(23, 30), LocalTime.MIDNIGHT))
    }

    /** В Берлине 25.10.2026 часы переводят назад: от 20:00 до 19:00 следующего дня проходит 24 ч, а не 23. */
    @Test
    fun daylightSavingChangeIsRespected() {
        val berlin = ZonedDateTime.of(2026, 10, 24, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))

        assertEquals(Duration.ofHours(24), ReminderDelay.untilNext(berlin, at19))
    }
}
