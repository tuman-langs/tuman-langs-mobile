package ru.itmo.tuman.features.progress.domain

import java.time.LocalDate

/** Текущая и лучшая серия дней. */
data class StreakInfo(
    val current: Int,
    val best: Int,
)

/** Расчёт серии дней (`docs/motivation.md`, раздел «Серия дней»). */
object Streak {

    /**
     * @param answersPerDay сколько ответов дано в каждый день (дни без ответов можно не передавать).
     * @param goal дневная цель: день засчитан, если ответов не меньше.
     * @param today сегодняшняя дата в поясе телефона.
     */
    fun calculate(answersPerDay: Map<LocalDate, Int>, goal: Int, today: LocalDate): StreakInfo {
        fun isDone(day: LocalDate) = (answersPerDay[day] ?: 0) >= goal

        // Сегодня ещё не выполнено — серию это не обнуляет, считаем со вчера
        var day = if (isDone(today)) today else today.minusDays(1)
        var current = 0
        while (isDone(day)) {
            current++
            day = day.minusDays(1)
        }

        var best = 0
        var run = 0
        var previous: LocalDate? = null
        for (date in answersPerDay.keys.filter(::isDone).sorted()) {
            run = if (previous != null && date == previous.plusDays(1)) run + 1 else 1
            best = maxOf(best, run)
            previous = date
        }
        return StreakInfo(current = current, best = maxOf(best, current))
    }
}
