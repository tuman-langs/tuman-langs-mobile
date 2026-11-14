package ru.itmo.tuman.features.decks.ui.edit

import androidx.annotation.StringRes

/**
 * Всё, что нужно нарисовать в редакторе карточки.
 *
 * @property isNew `true` — создаём карточку, `false` — редактируем сохранённую.
 * @property frontError, backError показать ли «Заполните поле» под лицевой / обратной стороной.
 * @property daysUntilDue для «Следующий показ через N дней»: 0 — сегодня,
 *   `null` — карточку ещё не повторяли или она новая.
 * @property added последняя добавленная карточка — для сообщения «Карточка «…» добавлена · Отменить».
 *   Сообщение показывается один раз, после показа экран вызывает `onMessageShown()`.
 * @property isClosed редактор пора закрыть: изменения сохранены или карточка удалена.
 */
data class CardEditUiState(
    val front: String = "",
    val back: String = "",
    val example: String = "",
    val frontError: Boolean = false,
    val backError: Boolean = false,
    val isNew: Boolean = true,
    val isLoading: Boolean = false,
    val daysUntilDue: Long? = null,
    val reviewCount: Int = 0,
    val added: AddedCard? = null,
    @StringRes val errorRes: Int? = null,
    val isClosed: Boolean = false,
)

/** Только что добавленная карточка: её id нужен, чтобы «Отменить» мог её удалить. */
data class AddedCard(val id: Long, val front: String)

/** Что пользователь может сделать в редакторе. */
sealed interface CardEditAction {
    data class FrontChange(val value: String) : CardEditAction
    data class BackChange(val value: String) : CardEditAction
    data class ExampleChange(val value: String) : CardEditAction
    data object Save : CardEditAction
    data class UndoAdd(val cardId: Long) : CardEditAction
    data object MessageShown : CardEditAction
    data object Delete : CardEditAction
    data object ErrorShown : CardEditAction
    data object Back : CardEditAction
}
