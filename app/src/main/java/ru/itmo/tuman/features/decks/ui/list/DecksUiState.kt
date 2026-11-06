package ru.itmo.tuman.features.decks.ui.list

import androidx.annotation.StringRes
import ru.itmo.tuman.features.decks.domain.model.Deck

/**
 * Всё, что нужно нарисовать на экране наборов.
 *
 * @property errorRes текст ошибки из strings.xml; `null` — ошибки нет.
 */
data class DecksUiState(
    val decks: List<Deck> = emptyList(),
    val isLoading: Boolean = true,
    @StringRes val errorRes: Int? = null,
)

/** Что пользователь может сделать на экране наборов. */
sealed interface DecksAction {
    data class Open(val id: Long) : DecksAction
    data class Create(val name: String) : DecksAction
    data class Rename(val id: Long, val name: String) : DecksAction
    data class Delete(val id: Long) : DecksAction
    data object ErrorShown : DecksAction
}
