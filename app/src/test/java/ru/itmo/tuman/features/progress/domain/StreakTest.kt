package ru.itmo.tuman.features.progress.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakTest {

    private val today = LocalDate.of(2026, 11, 18)
    private val goal = 20

    private fun days(vararg pairs: Pair<Long, Int>): Map<LocalDate, Int> =
        pairs.associate { (daysAgo, answers) -> today.minusDays(daysAgo) to answers }

    @Test
    fun emptyHistoryGivesZero() {
        assertEquals(StreakInfo(current = 0, best = 0), Streak.calculate(emptyMap(), goal, today))
    }

    @Test
    fun threeDaysInARowIncludingToday() {
        val history = days(0L to 20, 1L to 25, 2L to 30)

        assertEquals(StreakInfo(current = 3, best = 3), Streak.calculate(history, goal, today))
    }

    @Test
    fun todayNotDoneYetDoesNotResetStreak() {
        // Вчера и позавчера цель выполнена, сегодня пока 5 из 20
        val history = days(0L to 5, 1L to 20, 2L to 20)

        assertEquals(StreakInfo(current = 2, best = 2), Streak.calculate(history, goal, today))
    }

    @Test
    fun missedDayResetsCurrentButKeepsBest() {
        // 5, 4, 3 дня назад — серия из трёх, потом пропуск, вчера снова выполнено
        val history = days(5L to 20, 4L to 20, 3L to 20, 1L to 20)

        assertEquals(StreakInfo(current = 1, best = 3), Streak.calculate(history, goal, today))
    }

    @Test
    fun missedYesterdayAndNothingTodayGivesZero() {
        val history = days(2L to 20, 3L to 20)

        assertEquals(StreakInfo(current = 0, best = 2), Streak.calculate(history, goal, today))
    }

    @Test
    fun dayBelowGoalIsNotCounted() {
        val history = days(0L to 20, 1L to 19, 2L to 20)

        assertEquals(StreakInfo(current = 1, best = 1), Streak.calculate(history, goal, today))
    }
}
