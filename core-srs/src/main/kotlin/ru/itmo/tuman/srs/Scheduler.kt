package ru.itmo.tuman.srs

import java.time.Duration
import java.time.Instant
import kotlin.math.roundToLong

/**
 * Пересчёт состояния карточки после ответа.
 *
 * ВНИМАНИЕ: это временное упрощённое правило для задачи #6, а не формулы FSRS.
 * Коэффициенты ниже подобраны вручную, чтобы проверить устройство модуля и тестов.
 * Настоящие формулы FSRS (по описанию авторов) появятся в задаче #14.
 */
object Scheduler {

    /** Во сколько раз растёт стабильность при успешном ответе. */
    private const val HARD_FACTOR = 1.2
    private const val GOOD_FACTOR = 2.5
    private const val EASY_FACTOR = 4.0

    /** Во сколько раз падает стабильность при «не помню». */
    private const val AGAIN_FACTOR = 0.5

    /** Стабильность не опускается ниже одного дня. */
    private const val MIN_STABILITY_DAYS = 1.0

    private const val MIN_DIFFICULTY = 1.0
    private const val MAX_DIFFICULTY = 10.0

    private val ONE_DAY: Duration = Duration.ofDays(1)

    /**
     * Возвращает новое состояние карточки после оценки [rating], данной в момент [now].
     * Старое состояние [state] не меняется.
     */
    fun review(state: CardState, rating: Rating, now: Instant): CardState {
        val newStability = nextStability(state.stability, rating)
        val interval = if (rating == Rating.AGAIN) ONE_DAY else daysToDuration(newStability)
        return CardState(
            stability = newStability,
            difficulty = nextDifficulty(state.difficulty, rating),
            due = now.plus(interval),
            lastReview = now,
        )
    }

    private fun nextStability(stability: Double, rating: Rating): Double {
        val factor = when (rating) {
            Rating.AGAIN -> AGAIN_FACTOR
            Rating.HARD -> HARD_FACTOR
            Rating.GOOD -> GOOD_FACTOR
            Rating.EASY -> EASY_FACTOR
        }
        return (stability * factor).coerceAtLeast(MIN_STABILITY_DAYS)
    }

    /** «Не помню» и «трудно» делают карточку сложнее, «легко» — проще. */
    private fun nextDifficulty(difficulty: Double, rating: Rating): Double {
        val delta = when (rating) {
            Rating.AGAIN -> 1.0
            Rating.HARD -> 0.5
            Rating.GOOD -> 0.0
            Rating.EASY -> -1.0
        }
        return (difficulty + delta).coerceIn(MIN_DIFFICULTY, MAX_DIFFICULTY)
    }

    /** Дни (дробные) → Duration с точностью до минуты, но не меньше одного дня. */
    private fun daysToDuration(days: Double): Duration {
        val minutes = (days * 24 * 60).roundToLong()
        return Duration.ofMinutes(minutes).coerceAtLeast(ONE_DAY)
    }
}
