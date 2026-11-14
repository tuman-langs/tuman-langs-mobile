package ru.itmo.tuman.features.decks.ui.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import ru.itmo.tuman.R
import ru.itmo.tuman.features.decks.domain.DecksRepository
import ru.itmo.tuman.features.decks.domain.model.Card
import ru.itmo.tuman.features.decks.domain.status
import ru.itmo.tuman.srs.CardStatus
import ru.itmo.tuman.srs.DueCards
import java.time.Instant
import java.time.ZoneId

/**
 * Подписывается на набор и его карточки и считает для каждой статус.
 *
 * @param now текущее время, мс UTC. В тестах подставляем фиксированное, чтобы статус
 *   «сегодня» не зависел от того, когда запущен тест.
 * @param zone часовой пояс телефона: «сегодня» заканчивается в полночь по местному времени.
 */
class DeckViewModel(
    private val deckId: Long,
    repository: DecksRepository,
    private val now: () -> Long = System::currentTimeMillis,
    private val zone: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeckUiState())
    val uiState: StateFlow<DeckUiState> = _uiState.asStateFlow()

    init {
        // Название берём из общего списка наборов: отдельного метода «один набор» в репозитории нет,
        // а список и так обновляется сам, например после переименования.
        val title = repository.observeDecks().map { decks -> decks.find { it.id == deckId }?.title.orEmpty() }

        combine(title, repository.observeCards(deckId)) { deckTitle, cards -> buildState(deckTitle, cards) }
            .onEach { state -> _uiState.value = state }
            .catch { _uiState.update { it.copy(isLoading = false, errorRes = R.string.deck_error_load) } }
            .launchIn(viewModelScope)
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorRes = null) }
    }

    private fun buildState(title: String, cards: List<Card>): DeckUiState {
        val moment = Instant.ofEpochMilli(now())
        val items = cards.map { CardItem(it, it.status(moment, zone)) }
        // «На сегодня» считаем тем же правилом, что и список повторения (core-srs), поэтому сюда
        // попадают и новые карточки: при сохранении им ставят дату показа «сейчас».
        val withDue = cards.filter { it.due != null }
        val dueToday = DueCards.select(withDue, moment, zone, limit = withDue.size) { Instant.ofEpochMilli(it.due!!) }
        return DeckUiState(
            title = title,
            cards = items,
            dueTodayCount = dueToday.size,
            learnedCount = items.count { it.status == CardStatus.Learned },
            isLoading = false,
        )
    }
}
