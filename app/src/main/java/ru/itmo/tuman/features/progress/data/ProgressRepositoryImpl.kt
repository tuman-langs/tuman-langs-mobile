package ru.itmo.tuman.features.progress.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.itmo.tuman.core.database.dao.ProgressDao
import ru.itmo.tuman.core.database.entity.CardEntity
import ru.itmo.tuman.core.settings.SettingsRepository
import ru.itmo.tuman.features.progress.domain.ProgressCalculator
import ru.itmo.tuman.features.progress.domain.ProgressRepository
import ru.itmo.tuman.features.progress.domain.model.Answer
import ru.itmo.tuman.features.progress.domain.model.Progress
import ru.itmo.tuman.srs.CardState
import ru.itmo.tuman.srs.isLearned
import java.time.Instant
import java.time.ZoneId

/**
 * [ProgressRepository] на базе Room и настроек.
 *
 * Берёт из базы историю ответов, карточки и число наборов, из настроек — дневную цель,
 * и отдаёт всё в [ProgressCalculator]. Любое изменение (новый ответ, новый набор,
 * другая цель) — и прогресс пересчитывается сам.
 *
 * @param clock текущее время; [zone] — пояс телефона. В тестах подставляем фиксированные.
 */
class ProgressRepositoryImpl(
    private val progressDao: ProgressDao,
    private val settings: SettingsRepository,
    private val clock: () -> Instant = Instant::now,
    private val zone: () -> ZoneId = ZoneId::systemDefault,
) : ProgressRepository {

    override fun observeProgress(): Flow<Progress> =
        combine(
            progressDao.observeAnswers(),
            progressDao.observeReviewedCards(),
            progressDao.observeDeckCount(),
            settings.settings,
        ) { answers, reviewedCards, deckCount, userSettings ->
            ProgressCalculator.calculate(
                answers = answers.map { Answer(grade = it.rating, at = Instant.ofEpochMilli(it.reviewedAt)) },
                learnedCards = reviewedCards.count { it.toCardState().isLearned() },
                deckCount = deckCount,
                dailyGoal = userSettings.dailyGoal,
                now = clock(),
                zone = zone(),
            )
        }
}

// «Выучена» считаем правилом из core-srs (договорились в #14), чтобы оно было одно на всё приложение
private fun CardEntity.toCardState() = CardState(
    stability = stability,
    difficulty = difficulty,
    due = Instant.ofEpochMilli(due),
    lastReview = lastReview?.let(Instant::ofEpochMilli),
)
