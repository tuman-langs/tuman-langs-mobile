package ru.itmo.tuman.features.decks.ui.deck

import androidx.annotation.StringRes
import ru.itmo.tuman.features.decks.domain.model.Card
import ru.itmo.tuman.srs.CardStatus

/**
 * Всё, что нужно нарисовать на экране набора.
 *
 * @property dueTodayCount сколько карточек пора повторить сегодня — «N на сегодня» в шапке.
 * @property learnedCount сколько карточек выучено — «N карточек · выучено M».
 * @property errorRes текст ошибки из strings.xml; `null` — ошибки нет.
 */
data class DeckUiState(
    val title: String = "",
    val cards: List<CardItem> = emptyList(),
    val dueTodayCount: Int = 0,
    val learnedCount: Int = 0,
    val isLoading: Boolean = true,
    @StringRes val errorRes: Int? = null,
)

/** Карточка в списке вместе с уже посчитанным статусом. */
data class CardItem(val card: Card, val status: CardStatus)

/** Что пользователь может сделать на экране набора. */
sealed interface DeckAction {
    data class OpenCard(val cardId: Long) : DeckAction
    data object AddCard : DeckAction
    data object Study : DeckAction
    data object Back : DeckAction
    data object ErrorShown : DeckAction
}
