package ru.itmo.tuman.srs

import java.time.Duration
import java.time.Instant
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Пересчёт состояния карточки после ответа по упрощённому FSRS-6.
 *
 * Формулы — из описания авторов (ссылка в [FsrsParameters]), обозначения те же:
 * S — стабильность (дни), D — сложность (1–10), R — вероятность вспомнить,
 * G — номер оценки ([Rating.grade]), t — сколько дней прошло с прошлого ответа.
 * Что упрощено и почему — в docs/fsrs.md.
 */
object Scheduler {

    private val w = FsrsParameters.W

    /** Минимальный интервал — один день, поэтому повторов в тот же день не бывает. */
    private const val MIN_INTERVAL_DAYS = 1L

    private const val MINUTES_PER_DAY = 24.0 * 60.0

    /**
     * Множитель кривой забывания. Подобран так, чтобы R(t = S) = 0,9 —
     * это и есть определение стабильности.
     */
    private val FACTOR: Double = 0.9.pow(-1.0 / w[20]) - 1.0

    /**
     * Возвращает новое состояние карточки после оценки [rating], данной в момент [now].
     * Старое состояние [state] не меняется.
     */
    fun review(state: CardState, rating: Rating, now: Instant): CardState {
        val (stability, difficulty) = if (state.isNew) {
            initialStability(rating) to initialDifficulty(rating).clampDifficulty()
        } else {
            val r = retrievability(elapsedDays(state.lastReview!!, now), state.stability)
            // Стабильность считается по сложности ДО ответа, затем пересчитывается сложность.
            val s = if (rating == Rating.AGAIN) {
                stabilityAfterForgetting(state.difficulty, state.stability, r)
            } else {
                stabilityAfterRecall(state.difficulty, state.stability, r, rating)
            }
            s to nextDifficulty(state.difficulty, rating)
        }
        return CardState(
            stability = stability,
            difficulty = difficulty,
            due = now.plus(Duration.ofDays(intervalDays(stability))),
            lastReview = now,
        )
    }

    /** R(t, S) = (1 + FACTOR · t / S)^(−w20): вероятность вспомнить через t дней. */
    fun retrievability(elapsedDays: Double, stability: Double): Double =
        (1.0 + FACTOR * elapsedDays / stability).pow(-w[20])

    /**
     * Интервал до следующего показа в целых днях: I(r, S) = S / FACTOR · (r^(−1/w20) − 1).
     * При r = 0,9 формула даёт ровно S. Округляем до дней, но не меньше одного дня.
     */
    fun intervalDays(stability: Double): Long {
        val days = stability / FACTOR * (FsrsParameters.DESIRED_RETENTION.pow(-1.0 / w[20]) - 1.0)
        return days.roundToLong().coerceAtLeast(MIN_INTERVAL_DAYS)
    }

    /** S0(G) = w[G−1]: начальная стабильность зависит только от первой оценки. */
    private fun initialStability(rating: Rating): Double = w[rating.grade - 1]

    /**
     * D0(G) = w4 − e^(w5·(G−1)) + 1. Без ограничения: для «легко» получается
     * отрицательное число, его обрезаем только там, где D0 становится сложностью карточки.
     */
    private fun initialDifficulty(rating: Rating): Double =
        w[4] - exp(w[5] * (rating.grade - 1)) + 1.0

    /**
     * Новая сложность в три шага:
     * 1) сдвиг ΔD = −w6 · (G − 3): «не помню» и «трудно» повышают, «легко» понижает;
     * 2) торможение у края: чем ближе D к 10, тем меньше сдвиг;
     * 3) лёгкий возврат к D0(«легко»), чтобы сложность не «застревала» навсегда.
     */
    private fun nextDifficulty(difficulty: Double, rating: Rating): Double {
        val delta = -w[6] * (rating.grade - 3)
        val damped = difficulty + delta * (10.0 - difficulty) / 9.0
        val reverted = w[7] * initialDifficulty(Rating.EASY) + (1.0 - w[7]) * damped
        return reverted.clampDifficulty()
    }

    /**
     * S'r = S · (e^w8 · (11 − D) · S^(−w9) · (e^(w10·(1−R)) − 1) · [w15 для «трудно»] · [w16 для «легко»] + 1).
     * Рост тем больше, чем ниже была R: вспомнить почти забытое — запомнить крепче.
     */
    private fun stabilityAfterRecall(difficulty: Double, stability: Double, r: Double, rating: Rating): Double {
        val hardPenalty = if (rating == Rating.HARD) w[15] else 1.0
        val easyBonus = if (rating == Rating.EASY) w[16] else 1.0
        val growth = exp(w[8]) *
            (11.0 - difficulty) *
            stability.pow(-w[9]) *
            (exp(w[10] * (1.0 - r)) - 1.0) *
            hardPenalty *
            easyBonus
        return stability * (growth + 1.0)
    }

    /**
     * S'f = w11 · D^(−w12) · ((S + 1)^w13 − 1) · e^(w14·(1−R)).
     * Наше страховочное правило (его нет в источнике): после «не помню» стабильность
     * не может стать больше, чем была.
     */
    private fun stabilityAfterForgetting(difficulty: Double, stability: Double, r: Double): Double {
        val forgotten = w[11] *
            difficulty.pow(-w[12]) *
            ((stability + 1.0).pow(w[13]) - 1.0) *
            exp(w[14] * (1.0 - r))
        return forgotten.coerceAtMost(stability)
    }

    /** Сколько дней (дробных) прошло между двумя моментами; не меньше нуля. */
    private fun elapsedDays(from: Instant, to: Instant): Double =
        (Duration.between(from, to).toMinutes() / MINUTES_PER_DAY).coerceAtLeast(0.0)

    private fun Double.clampDifficulty(): Double =
        coerceIn(FsrsParameters.MIN_DIFFICULTY, FsrsParameters.MAX_DIFFICULTY)
}
