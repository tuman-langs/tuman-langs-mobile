package ru.itmo.tuman.features.decks.domain.model

/**
 * Карточка так, как её видят экраны набора и редактора.
 * Поля алгоритма повторения (стабильность, дата показа) сюда не входят:
 * ими занимается повторение (`features/review`), а не редактор.
 *
 * @property id `0` — карточка новая и ещё не сохранена.
 * @property example пример употребления, необязательный.
 */
data class Card(
    val id: Long = 0,
    val deckId: Long,
    val front: String,
    val back: String,
    val example: String? = null,
)
