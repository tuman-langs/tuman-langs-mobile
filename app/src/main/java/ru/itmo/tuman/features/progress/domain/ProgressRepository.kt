package ru.itmo.tuman.features.progress.domain

import kotlinx.coroutines.flow.Flow
import ru.itmo.tuman.features.progress.domain.model.Progress

/**
 * Прогресс пользователя для экранов прогресса (#34), итога сессии (#29) и главного (#33).
 * Считается заново из истории ответов при каждом изменении — отдельных счётчиков нет.
 */
interface ProgressRepository {

    /** Цель на сегодня, серия, XP, выученные, доля верных и достижения. Обновляется сама. */
    fun observeProgress(): Flow<Progress>
}
