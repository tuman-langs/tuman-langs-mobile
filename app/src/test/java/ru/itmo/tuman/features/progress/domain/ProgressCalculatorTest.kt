package ru.itmo.tuman.features.progress.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.itmo.tuman.features.progress.domain.model.AchievementType
import ru.itmo.tuman.features.progress.domain.model.Answer
import ru.itmo.tuman.srs.Rating
import java.time.Instant
import java.time.ZoneId

class ProgressCalculatorTest {

    // Москва, UTC+3: «сегодня» — 18.11.2026
    private val zone = ZoneId.of("Europe/Moscow")
    private val now = Instant.parse("2026-11-18T12:00:00Z")

    private fun answer(rating: Rating, at: String) = Answer(grade = rating.grade, at = Instant.parse(at))

    private fun calculate(answers: List<Answer>, goal: Int = 2, learned: Int = 0, decks: Int = 0) =
        ProgressCalculator.calculate(answers, learned, decks, goal, now, zone)

    @Test
    fun xpPerRatingFollowsRules() {
        assertEquals(10, Xp.forRating(Rating.EASY))
        assertEquals(10, Xp.forRating(Rating.GOOD))
        assertEquals(5, Xp.forRating(Rating.HARD))
        assertEquals(2, Xp.forRating(Rating.AGAIN))
        assertEquals(5, Xp.forGrade(2))
    }

    @Test
    fun emptyHistory() {
        val progress = calculate(emptyList(), decks = 0)

        assertEquals(0, progress.xp)
        assertEquals(0, progress.todayAnswers)
        assertEquals(0, progress.currentStreak)
        assertNull("без ответов доли верных нет", progress.accuracy)
        assertEquals(7, progress.lastWeek.size)
        assertEquals(6, progress.achievements.size)
        assertTrue(progress.achievements.none { it.isUnlocked })
    }

    @Test
    fun xpAndAccuracyFromHistory() {
        val answers = listOf(
            answer(Rating.EASY, "2026-11-18T08:00:00Z"),
            answer(Rating.GOOD, "2026-11-18T08:01:00Z"),
            answer(Rating.HARD, "2026-11-18T08:02:00Z"),
            answer(Rating.AGAIN, "2026-11-18T08:03:00Z"),
        )

        val progress = calculate(answers)

        assertEquals(10 + 10 + 5 + 2, progress.xp)
        assertEquals(0.75, progress.accuracy!!, 1e-9)
        assertEquals(4, progress.todayAnswers)
        assertTrue(progress.isGoalDone)
    }

    /** Смена суток: день делится по часовому поясу телефона, а не по UTC. */
    @Test
    fun answersSplitIntoDaysAtLocalMidnight() {
        val answers = listOf(
            // 17.11 23:59 по Москве
            answer(Rating.GOOD, "2026-11-17T20:59:00Z"),
            answer(Rating.GOOD, "2026-11-17T20:59:30Z"),
            // 18.11 00:01 по Москве — уже «сегодня», хотя по UTC ещё 17.11
            answer(Rating.GOOD, "2026-11-17T21:01:00Z"),
        )

        val progress = calculate(answers, goal = 2)

        assertEquals(1, progress.todayAnswers)
        assertFalse(progress.isGoalDone)
        // Вчера цель выполнена, сегодня ещё нет — серия 1, не 0
        assertEquals(1, progress.currentStreak)
        assertEquals(2, progress.lastWeek[5].answers)
        assertEquals(1, progress.lastWeek[6].answers)
        assertEquals(now.atZone(zone).toLocalDate(), progress.lastWeek.last().date)
    }

    @Test
    fun achievementsShowProgressAndUnlock() {
        val answers = (0 until 12).map { answer(Rating.GOOD, "2026-11-18T09:%02d:00Z".format(it)) }

        val byType = calculate(answers, goal = 20, learned = 42, decks = 1).achievements.associateBy { it.type }

        assertTrue(byType.getValue(AchievementType.FIRST_DECK).isUnlocked)
        assertTrue(byType.getValue(AchievementType.FIRST_10_CARDS).isUnlocked)
        assertEquals(10, byType.getValue(AchievementType.FIRST_10_CARDS).current) // не больше цели
        assertEquals(42, byType.getValue(AchievementType.LEARNED_100).current)
        assertEquals(58, byType.getValue(AchievementType.LEARNED_100).remaining)
        assertEquals(120, byType.getValue(AchievementType.XP_1000).current)
        assertFalse(byType.getValue(AchievementType.STREAK_3_DAYS).isUnlocked)
    }

    @Test
    fun streakAchievementsUseBestStreak() {
        val achievements = ProgressCalculator.achievements(
            deckCount = 0, totalAnswers = 0, bestStreak = 6, learnedCards = 0, xp = 0,
        ).associateBy { it.type }

        assertTrue(achievements.getValue(AchievementType.STREAK_3_DAYS).isUnlocked)
        assertEquals(6, achievements.getValue(AchievementType.WEEK_NO_GAPS).current)
        assertEquals(1, achievements.getValue(AchievementType.WEEK_NO_GAPS).remaining)
    }
}
