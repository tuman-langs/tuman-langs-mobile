package ru.itmo.tuman.features.review.ui

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

    // Все ответы меняют расписание, но в итог сессии и XP карточка входит один раз.
    private val answeredIds = mutableSetOf<Long>()
    private var hintShown = false
    val canPractice: Boolean get() = deckId != null

    init {
        loadSession(practiceOnly = false)
    }

    fun onPracticeAgain() {
        if (!canPractice || !_uiState.value.isFinished) return
        loadSession(practiceOnly = true)
    }

    private fun loadSession(practiceOnly: Boolean) {
        _uiState.value = ReviewUiState()
        viewModelScope.launch {
            try {
                val due = if (practiceOnly) emptyList() else repository.observeTodayCards(deckId).first()
                val isPractice = deckId != null && due.isEmpty()
                val cards = if (isPractice) repository.getPracticeCards(checkNotNull(deckId)) else due
                sessionCards.clear()
                sessionCards.addAll(cards)
                answeredIds.clear()
                _uiState.value = ReviewUiState(isLoading = false, isEmpty = cards.isEmpty(), isPractice = isPractice)
                if (cards.isNotEmpty()) showCard(0)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(isLoading = false, errorRes = R.string.review_error_load) }
            }
        }
    }

    fun onRevealAnswer() {
        if (_uiState.value.currentCard == null || _uiState.value.isSubmitting) return
        _uiState.update {
            it.copy(
                isAnswerVisible = true,
                isHintVisible = !it.isPractice && !hintShown,
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
                val answeredAt = now()
                val interval = if (state.isPractice) null else repository.submitAnswer(card.id, rating, answeredAt)
                val isRepeat = !answeredIds.add(card.id)
                // Короткий шаг можно пройти раньше срока, когда других карточек уже нет.
                if (interval != null && interval <= LEARN_AHEAD_LIMIT) {
                    sessionCards.add(card.copy(learningDue = answeredAt.plus(interval)))
                }

                val nextPosition = state.position
                val counted = if (isRepeat) 0 else 1
                val nextAnswersCount = state.answersCount + counted
                val nextCorrectAnswers = state.correctAnswers + if (isRepeat || rating == Rating.AGAIN) 0 else 1
                val nextXp = if (state.isPractice) 0 else state.earnedXp + XP_PER_ANSWER * counted
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
        val remaining = sessionCards.subList(index, sessionCards.size)
        val nextLearning = remaining.filter { it.learningDue != null }.minByOrNull { it.learningDue!! }
        val currentTime = now()
        val card = if (_uiState.value.isPractice) {
            remaining.first()
        } else if (nextLearning != null && nextLearning.learningDue?.isAfter(currentTime) == false) {
            nextLearning
        } else {
            remaining.firstOrNull { it.learningDue == null } ?: checkNotNull(nextLearning)
        }
        val offset = remaining.indexOf(card)
        if (offset > 0) sessionCards.add(index, sessionCards.removeAt(index + offset))
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
        if (_uiState.value.isPractice) return
        viewModelScope.launch {
            try {
                val intervals = repository.previewIntervals(card.id, now())
                if (_uiState.value.position == index + 1 && !_uiState.value.isFinished && !_uiState.value.isPractice) {
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
        val LEARN_AHEAD_LIMIT: Duration = Duration.ofMinutes(20)
    }
}
