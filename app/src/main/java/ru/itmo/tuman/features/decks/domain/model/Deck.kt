package ru.itmo.tuman.features.decks.domain.model

/**
 * Набор карточек так, как его видят экраны.
 * В отличие от `DeckEntity`, здесь нет ничего от Room — только то, что нужно показать.
 *
 * @property cardCount сколько карточек в наборе — для подписи в списке наборов.
 * @property isFromCatalog `true`, если набор скачан из каталога, `false` — создан пользователем.
 */
data class Deck(
    val id: Long,
    val title: String,
    val cardCount: Int,
    val isFromCatalog: Boolean,
)
