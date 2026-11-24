package ru.itmo.tuman.features.progress.domain

import ru.itmo.tuman.core.utils.Xp
import ru.itmo.tuman.features.progress.domain.model.Achievement
import ru.itmo.tuman.features.progress.domain.model.AchievementType
import ru.itmo.tuman.features.progress.domain.model.Answer
import ru.itmo.tuman.features.progress.domain.model.DayProgress
import ru.itmo.tuman.features.progress.domain.model.Progress
import ru.itmo.tuman.srs.Rating
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Собирает [Progress] из истории ответов и нескольких чисел из базы.
 * Чистая функция: без Room и Android, поэтому проверяется обычными юнит-тестами.
 */
object ProgressCalculator {

    private const val WEEK_DAYS = 7L

    /**
     * @param answers вся история ответов.
     * @param learnedCards сколько карточек сейчас выучено.
     * @param deckCount сколько наборов есть (для «Первый набор»).
     * @param dailyGoal дневная цель из настроек.
     * @param now текущий момент; [zone] — пояс телефона, по нему ответы делятся на дни.
     */
    fun calculate(
        answers: List<Answer>,
        learnedCards: Int,
        deckCount: Int,
        dailyGoal: Int,
        now: Instant,
        zone: ZoneId,
    ): Progress {
        val today = now.atZone(zone).toLocalDate()
        val answersPerDay: Map<LocalDate, Int> =
            answers.groupingBy { it.at.atZone(zone).toLocalDate() }.eachCount()
        val streak = Streak.calculate(answersPerDay, dailyGoal, today)
        val xp = answers.sumOf { Xp.forGrade(it.grade) }
        val correct = answers.count { it.grade != Rating.AGAIN.grade }

        val lastWeek = (WEEK_DAYS - 1 downTo 0).map { back ->
            val date = today.minusDays(back)
            val count = answersPerDay[date] ?: 0
            DayProgress(date = date, answers = count, isGoalDone = count >= dailyGoal)
        }

        return Progress(
            todayAnswers = answersPerDay[today] ?: 0,
            dailyGoal = dailyGoal,
            currentStreak = streak.current,
            bestStreak = streak.best,
            lastWeek = lastWeek,
            xp = xp,
            learnedCards = learnedCards,
            accuracy = if (answers.isEmpty()) null else correct.toDouble() / answers.size,
            totalAnswers = answers.size,
            achievements = achievements(
                deckCount = deckCount,
                totalAnswers = answers.size,
                bestStreak = streak.best,
                learnedCards = learnedCards,
                xp = xp,
            ),
        )
    }

    /** Все 6 достижений по порядку из макета; прогресс не больше цели. */
    fun achievements(deckCount: Int, totalAnswers: Int, bestStreak: Int, learnedCards: Int, xp: Int): List<Achievement> =
        AchievementType.entries.map { type ->
            val value = when (type) {
                AchievementType.FIRST_DECK -> deckCount
                AchievementType.FIRST_10_CARDS -> totalAnswers
                AchievementType.STREAK_3_DAYS, AchievementType.WEEK_NO_GAPS -> bestStreak
                AchievementType.LEARNED_100 -> learnedCards
                AchievementType.XP_1000 -> xp
            }
            Achievement(type = type, current = value.coerceAtMost(type.target))
        }
}
