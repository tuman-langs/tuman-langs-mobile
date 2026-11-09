package ru.itmo.tuman.features.decks.domain.model

/**
 * Карточка так, как её видят экраны набора и редактора.
 *
 * Последние три поля — только для чтения: по ним экран рисует статус карточки
 * («новая / сегодня / через 3 д») и «Повторений 4» в редакторе.
 * `saveCard` их не меняет — ими занимается повторение (`features/review`).
 *
 * @property id `0` — карточка новая и ещё не сохранена.
 * @property example пример употребления, необязательный.
 * @property due когда показать снова, мс UTC; `null` — карточка ещё не сохранена.
 * @property lastReview время последнего ответа, мс UTC; `null` — карточку ещё не повторяли («новая»).
 * @property reviewCount сколько раз на карточку отвечали.
 */
data class Card(
    val id: Long = 0,
    val deckId: Long,
    val front: String,
    val back: String,
    val example: String? = null,
    val due: Long? = null,
    val lastReview: Long? = null,
    val reviewCount: Int = 0,
)
