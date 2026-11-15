package ru.itmo.tuman.features.review.ui

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
import ru.itmo.tuman.features.review.domain.ReviewRepository
import ru.itmo.tuman.features.review.domain.model.ReviewCard
import ru.itmo.tuman.srs.Rating
import java.time.Duration
import java.time.Instant

class ReviewViewModel(
    private val repository: ReviewRepository,
    private val deckId: Long? = null,
    private val now: () -> Instant = Instant::now,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private val sessionCards = mutableListOf<ReviewCard>()

    // Карточки, ответ на которые уже сохранён в этой сессии. Если такая карточка
    // снова в очереди (после «не помню»), это повтор в конце сессии: ответ не пишем
    // в базу и не считаем в итоге, чтобы алгоритм и история видели один ответ за день.
    private val answeredIds = mutableSetOf<Long>()
    private var sessionStarted = false
    private var hintShown = false

    init {
        repository.observeTodayCards(deckId)
            .onEach { cards ->
                if (!sessionStarted) {
                    sessionStarted = true
                    sessionCards.addAll(cards)
                    if (cards.isEmpty()) {
                        _uiState.update { it.copy(isLoading = false, isEmpty = true) }
                    } else {
                        showCard(0)
                    }
                }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorRes = R.string.review_error_load,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onRevealAnswer() {
        if (_uiState.value.currentCard == null || _uiState.value.isSubmitting) return
        _uiState.update {
            it.copy(
                isAnswerVisible = true,
                isHintVisible = !hintShown,
            )
        }
        hintShown = true
    }

    fun onHintDismissed() {
        _uiState.update { it.copy(isHintVisible = false) }
    }

    fun onRatingSelected(rating: Rating) {
        val state = _uiState.value
        val card = state.currentCard ?: return
        if (!state.isAnswerVisible || state.isSubmitting || state.isFinished) return

        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            try {
                val isRepeat = card.id in answeredIds
                if (!isRepeat) {
                    repository.submitAnswer(card.id, rating, now())
                    answeredIds += card.id
                }
                if (rating == Rating.AGAIN) sessionCards.add(card)

                val nextPosition = state.position
                val counted = if (isRepeat) 0 else 1
                val nextAnswersCount = state.answersCount + counted
                val nextCorrectAnswers = state.correctAnswers + if (isRepeat || rating == Rating.AGAIN) 0 else 1
                val nextXp = state.earnedXp + XP_PER_ANSWER * counted
                if (nextPosition >= sessionCards.size) {
                    _uiState.update {
                        it.copy(
                            currentCard = null,
                            position = nextPosition,
                            total = sessionCards.size,
                            isSubmitting = false,
                            isFinished = true,
                            answersCount = nextAnswersCount,
                            correctAnswers = nextCorrectAnswers,
                            earnedXp = nextXp,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            total = sessionCards.size,
                            answersCount = nextAnswersCount,
                            correctAnswers = nextCorrectAnswers,
                            earnedXp = nextXp,
                        )
                    }
                    showCard(nextPosition)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorRes = R.string.review_error_save,
                    )
                }
            }
        }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorRes = null) }
    }

    private fun showCard(index: Int) {
        val card = sessionCards[index]
        _uiState.update {
            it.copy(
                currentCard = card,
                position = index + 1,
                total = sessionCards.size,
                isLoading = false,
                isAnswerVisible = false,
                isHintVisible = false,
                isSubmitting = false,
                intervals = emptyMap(),
            )
        }
        viewModelScope.launch {
            try {
                val intervals = repository.previewIntervals(card.id, now())
                if (_uiState.value.currentCard?.id == card.id) {
                    _uiState.update { it.copy(intervals = intervals) }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(errorRes = R.string.review_error_intervals) }
            }
        }
    }

    private companion object {
        const val XP_PER_ANSWER = 10
    }
}
