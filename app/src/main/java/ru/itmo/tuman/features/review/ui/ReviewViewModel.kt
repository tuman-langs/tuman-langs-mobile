package ru.itmo.tuman.features.review.ui

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
import ru.itmo.tuman.core.settings.SettingsRepository
import ru.itmo.tuman.core.utils.Xp
import ru.itmo.tuman.features.review.domain.ReviewRepository
import ru.itmo.tuman.features.review.domain.model.ReviewCard
import ru.itmo.tuman.srs.Rating
import java.io.IOException
import java.time.Duration
import java.time.Instant

/**
 * Сессия повторения.
 *
 * Как считается итог (правила — `docs/motivation.md`):
 * - каждый ответ сохраняется и меняет расписание карточки, в том числе короткие шаги;
 * - в итог сессии (ответы, точность, XP) карточка входит один раз — по первому ответу;
 * - XP в итоге берётся из прогресса (после сессии минус до неё), поэтому совпадает с экраном
 *   прогресса, который засчитывает первый ответ по карточке за день;
 * - тренировка набора ([ReviewUiState.isPractice]) ничего не сохраняет и XP не даёт.
 */
class ReviewViewModel(
    private val repository: ReviewRepository,
    private val deckId: Long? = null,
    private val now: () -> Instant = Instant::now,
    private val settingsRepository: SettingsRepository? = null,
    private val readProgress: suspend () -> ReviewProgressSnapshot? = { null },
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private val sessionCards = mutableListOf<ReviewCard>()

    // Карточки, на которые уже ответили в этой сессии: повторный ответ (короткий шаг)
    // сохраняется для алгоритма, но в итог сессии не входит.
    private val answeredIds = mutableSetOf<Long>()
    private var hintShown: Boolean? = null
    private var progressBeforeSession: ReviewProgressSnapshot? = null

    /** Тренировка доступна, когда сессия открыта из конкретного набора. */
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
                // Из набора, где на сегодня ничего нет, сразу открываем тренировку
                val isPractice = deckId != null && due.isEmpty()
                val cards = if (isPractice) repository.getPracticeCards(checkNotNull(deckId)) else due
                sessionCards.clear()
                sessionCards.addAll(cards)
                answeredIds.clear()
                progressBeforeSession = null
                if (hintShown == null) hintShown = readHintShown()
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
        val state = _uiState.value
        if (state.currentCard == null || state.isSubmitting || state.isAnswerVisible) return
        // Подсказка про оценки — один раз, и не в тренировке: там оценки ничего не меняют
        val showHint = !state.isPractice && hintShown != true
        if (showHint) hintShown = true
        _uiState.update { it.copy(isAnswerVisible = true, isHintVisible = showHint) }
        if (showHint) {
            viewModelScope.launch {
                try {
                    settingsRepository?.setRatingHintShown()
                } catch (_: IOException) {
                    // Подсказка всё равно больше не появится в этой сессии.
                }
            }
        }
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
                val isRepeat = card.id in answeredIds
                val interval = if (state.isPractice) {
                    null
                } else {
                    if (answeredIds.isEmpty()) progressBeforeSession = loadProgress()
                    repository.submitAnswer(card.id, rating, answeredAt)
                }
                answeredIds += card.id
                // Короткий шаг можно пройти раньше срока, когда других карточек уже нет.
                if (interval != null && interval <= LEARN_AHEAD_LIMIT) {
                    sessionCards.add(card.copy(learningDue = answeredAt.plus(interval)))
                }

                val nextPosition = state.position
                val counted = if (isRepeat) 0 else 1
                val nextAnswersCount = state.answersCount + counted
                val nextCorrectAnswers = state.correctAnswers + if (isRepeat || rating == Rating.AGAIN) 0 else 1
                val nextXp = when {
                    state.isPractice || isRepeat -> state.earnedXp
                    else -> state.earnedXp + Xp.forRating(rating)
                }
                if (nextPosition >= sessionCards.size) {
                    finishSession(state.isPractice, nextAnswersCount, nextCorrectAnswers, nextXp)
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

    private suspend fun finishSession(isPractice: Boolean, answers: Int, correct: Int, sessionXp: Int) {
        val before = progressBeforeSession
        val after = if (isPractice) null else loadProgress()
        // Прогресс засчитывает первый ответ по карточке за день. Если карточку уже повторяли
        // сегодня в другой сессии, XP за неё не добавится — итог покажет ровно столько же.
        val earnedXp = when {
            isPractice -> 0
            before?.xp != null && after?.xp != null -> (after.xp - before.xp).coerceAtLeast(0)
            else -> sessionXp
        }
        val summaryStats = after?.let { progress ->
            ReviewSummaryStats(
                goalDone = progress.goalDone,
                streak = progress.streak,
                unlockedAchievement = before?.unlockedAchievements?.let { unlocked ->
                    progress.unlockedAchievements.firstOrNull { it !in unlocked }
                },
            )
        }
        _uiState.update {
            it.copy(
                currentCard = null,
                position = sessionCards.size,
                total = sessionCards.size,
                isSubmitting = false,
                isFinished = true,
                answersCount = answers,
                correctAnswers = correct,
                earnedXp = earnedXp,
                summaryStats = summaryStats,
            )
        }
    }

    private suspend fun readHintShown(): Boolean = try {
        settingsRepository?.settings?.first()?.ratingHintShown == true
    } catch (_: IOException) {
        false
    }

    private suspend fun loadProgress(): ReviewProgressSnapshot? = try {
        readProgress()
    } catch (error: CancellationException) {
        throw error
    } catch (_: Exception) {
        null
    }

    private fun showCard(index: Int) {
        val remaining = sessionCards.subList(index, sessionCards.size)
        val nextLearning = remaining.filter { it.learningDue != null }.minByOrNull { it.learningDue!! }
        val currentTime = now()
        // Сначала короткий шаг, срок которого наступил; иначе обычная карточка;
        // если остались только короткие шаги — ближайший из них, даже раньше срока.
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
        /** Короткие шаги до 20 минут показываем в этой же сессии. */
        val LEARN_AHEAD_LIMIT: Duration = Duration.ofMinutes(20)
    }
}
