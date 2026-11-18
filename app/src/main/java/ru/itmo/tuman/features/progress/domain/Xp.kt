package ru.itmo.tuman.features.progress.domain

import ru.itmo.tuman.srs.Rating

/** Очки опыта за ответ (`docs/motivation.md`). */
object Xp {

    /** «легко» и «хорошо» — 10, «трудно» — 5, «не помню» — 2. */
    fun forRating(rating: Rating): Int = when (rating) {
        Rating.EASY, Rating.GOOD -> 10
        Rating.HARD -> 5
        Rating.AGAIN -> 2
    }

    /** То же по номеру оценки 1–4 — так оценка хранится в таблице `reviews`. */
    fun forGrade(grade: Int): Int =
        forRating(Rating.entries.firstOrNull { it.grade == grade } ?: Rating.AGAIN)
}
