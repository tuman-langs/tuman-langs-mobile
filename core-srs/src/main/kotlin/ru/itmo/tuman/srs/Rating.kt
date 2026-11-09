package ru.itmo.tuman.srs

/**
 * Оценка ответа пользователя — четыре кнопки на экране повторения (ФТ-6).
 *
 * @property grade номер оценки G в формулах FSRS: 1 — «не помню» … 4 — «легко».
 */
enum class Rating(val grade: Int) {
    /** «Не помню» */
    AGAIN(1),

    /** «Трудно» */
    HARD(2),

    /** «Хорошо» */
    GOOD(3),

    /** «Легко» */
    EASY(4),
}
