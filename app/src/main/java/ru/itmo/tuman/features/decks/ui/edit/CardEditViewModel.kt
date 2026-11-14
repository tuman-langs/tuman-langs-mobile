package ru.itmo.tuman.features.decks.ui.edit

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.itmo.tuman.R
import ru.itmo.tuman.core.navigation.CardEditRoute
import ru.itmo.tuman.features.decks.domain.DecksRepository
import ru.itmo.tuman.features.decks.domain.daysUntilDue
import ru.itmo.tuman.features.decks.domain.model.Card
import java.time.Instant
import java.time.ZoneId

/**
 * Создание и редактирование карточки.
 *
 * @param cardId [CardEditRoute.NEW_CARD] — создаём новую карточку в наборе [deckId],
 *   иначе загружаем и редактируем существующую.
 * @param now текущее время, мс UTC — для «Следующий показ через N дней»; в тестах фиксированное.
 */
class CardEditViewModel(
    private val deckId: Long,
    private val cardId: Long,
    private val repository: DecksRepository,
    private val now: () -> Long = System::currentTimeMillis,
    private val zone: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {

    private val isNew = cardId == CardEditRoute.NEW_CARD

    private val _uiState = MutableStateFlow(CardEditUiState(isNew = isNew, isLoading = !isNew))
    val uiState: StateFlow<CardEditUiState> = _uiState.asStateFlow()

    /** Карточка как она лежит в базе — при сохранении меняем в ней только текст. */
    private var loaded: Card? = null

    init {
        if (!isNew) load()
    }

    fun onFrontChange(value: String) = _uiState.update { it.copy(front = value, frontError = false) }

    fun onBackChange(value: String) = _uiState.update { it.copy(back = value, backError = false) }

    fun onExampleChange(value: String) = _uiState.update { it.copy(example = value) }

    /** Проверяет обе стороны и сохраняет. Пробелы по краям обрезаем, пустой пример не храним. */
    fun onSave() {
        val state = _uiState.value
        val front = state.front.trim()
        val back = state.back.trim()
        val example = state.example.trim().ifEmpty { null }
        if (front.isEmpty() || back.isEmpty()) {
            _uiState.update { it.copy(frontError = front.isEmpty(), backError = back.isEmpty()) }
            return
        }
        launchSafely(R.string.card_edit_error_save) {
            val original = loaded
            if (original == null) {
                val id = repository.saveCard(Card(deckId = deckId, front = front, back = back, example = example))
                // Поля очищаем, чтобы сразу набирать следующую карточку
                _uiState.update { CardEditUiState(added = AddedCard(id, front)) }
            } else {
                repository.saveCard(original.copy(front = front, back = back, example = example))
                _uiState.update { it.copy(isClosed = true) }
            }
        }
    }

    /** «Отменить» в сообщении о добавлении: удаляем только что созданную карточку. */
    fun onUndoAdd(cardId: Long) {
        launchSafely(R.string.card_edit_error_delete) { repository.deleteCard(cardId) }
    }

    fun onMessageShown() = _uiState.update { it.copy(added = null) }

    /** Удаление уже подтверждено пользователем в диалоге. */
    fun onDelete() {
        if (isNew) return
        launchSafely(R.string.card_edit_error_delete) {
            repository.deleteCard(cardId)
            _uiState.update { it.copy(isClosed = true) }
        }
    }

    fun onErrorShown() = _uiState.update { it.copy(errorRes = null) }

    private fun load() {
        launchSafely(R.string.card_edit_error_load) {
            val card = repository.getCard(cardId)
            if (card == null) {
                _uiState.update { it.copy(isLoading = false, errorRes = R.string.card_edit_error_load) }
                return@launchSafely
            }
            loaded = card
            _uiState.update {
                it.copy(
                    front = card.front,
                    back = card.back,
                    example = card.example.orEmpty(),
                    isLoading = false,
                    daysUntilDue = card.daysUntilDue(Instant.ofEpochMilli(now()), zone),
                    reviewCount = card.reviewCount,
                )
            }
        }
    }

    /** Запускает действие; если хранилище упало, показываем ошибку, а не роняем приложение. */
    private fun launchSafely(@StringRes errorRes: Int, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e // отмену корутины нельзя глотать, иначе она не остановится
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorRes = errorRes) }
            }
        }
    }
}
