package ru.itmo.tuman.features.review.ui

import ru.itmo.tuman.features.review.domain.model.ReviewCard
import ru.itmo.tuman.srs.Rating
import java.time.Duration

data class ReviewUiState(
    val currentCard: ReviewCard? = null,
    val position: Int = 0,
    val total: Int = 0,
    val isLoading: Boolean = true,
    val isAnswerVisible: Boolean = false,
    val isHintVisible: Boolean = false,
    val isEmpty: Boolean = false,
    val isFinished: Boolean = false,
    val isSubmitting: Boolean = false,
    val intervals: Map<Rating, Duration> = emptyMap(),
    val answersCount: Int = 0,
    val correctAnswers: Int = 0,
    val earnedXp: Int = 0,
    val errorRes: Int? = null,
    val summaryStats: ReviewSummaryStats? = null,
)

/** Текущий прогресс после сессии и первое достижение, открытое за эту сессию. */
data class ReviewSummaryStats(
    val goalDone: Boolean,
    val streak: Int,
    val unlockedAchievement: String?,
)

/** Данные прогресса до первого и после последнего ответа, без зависимости от экрана прогресса. */
data class ReviewProgressSnapshot(
    val goalDone: Boolean,
    val streak: Int,
    val unlockedAchievements: List<String>,
)
