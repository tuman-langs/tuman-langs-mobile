package ru.itmo.tuman.srs

import java.time.Instant

/**
 * Всё, что алгоритму нужно знать о карточке. Эти же поля хранятся в базе
 * (`CardEntity`, согласовано с Бондаренко Е. в задаче #5).
 *
 * @property stability стабильность в днях: насколько прочно слово запомнилось.
 *   Чем больше, тем дольше можно не повторять.
 * @property difficulty сложность карточки для этого пользователя, от 1 (легко) до 10 (трудно).
 * @property due когда показать карточку в следующий раз.
 * @property lastReview когда был последний ответ; `null` — карточку ещё ни разу не повторяли.
 */
data class CardState(
    val stability: Double,
    val difficulty: Double,
    val due: Instant,
    val lastReview: Instant?,
) {
    /** Карточка новая, если на неё ещё ни разу не отвечали. */
    val isNew: Boolean get() = lastReview == null

    companion object {
        /**
         * Значения-заглушки для новой карточки в базе. Алгоритм их не использует:
         * при первом ответе S и D считаются заново по оценке (см. [Scheduler]).
         */
        const val INITIAL_STABILITY_DAYS = 1.0
        const val INITIAL_DIFFICULTY = 5.0

        /** Новая карточка: её можно показать сразу, в момент [now]. */
        fun new(now: Instant): CardState = CardState(
            stability = INITIAL_STABILITY_DAYS,
            difficulty = INITIAL_DIFFICULTY,
            due = now,
            lastReview = null,
        )
    }
}
