package ru.itmo.tuman.features.decks.ui.list

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.itmo.tuman.R
import ru.itmo.tuman.features.decks.domain.DecksRepository

/** Подписывается на список наборов и выполняет действия пользователя через репозиторий. */
class DecksViewModel(private val repository: DecksRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DecksUiState())
    val uiState: StateFlow<DecksUiState> = _uiState.asStateFlow()

    init {
        repository.observeDecks()
            .onEach { decks -> _uiState.update { it.copy(decks = decks, isLoading = false) } }
            .catch { _uiState.update { it.copy(isLoading = false, errorRes = R.string.decks_error_load) } }
            .launchIn(viewModelScope)
    }

    fun onCreate(name: String) {
        val title = name.trim()
        if (title.isEmpty()) return
        launchSafely(R.string.decks_error_save) { repository.createDeck(title) }
    }

    fun onRename(id: Long, name: String) {
        val title = name.trim()
        if (title.isEmpty()) return
        launchSafely(R.string.decks_error_save) { repository.renameDeck(id, title) }
    }

    fun onDelete(id: Long) {
        launchSafely(R.string.decks_error_delete) { repository.deleteDeck(id) }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorRes = null) }
    }

    /** Запускает действие; если хранилище упало, показываем ошибку, а не роняем приложение. */
    private fun launchSafely(@StringRes errorRes: Int, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e // отмену корутины нельзя глотать, иначе она не остановится
            } catch (e: Exception) {
                _uiState.update { it.copy(errorRes = errorRes) }
            }
        }
    }
}
