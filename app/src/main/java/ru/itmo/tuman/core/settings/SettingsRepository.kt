package ru.itmo.tuman.core.settings

import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

/**
 * Настройки пользователя: дневная цель, первый запуск, подсказка к оценкам, напоминания.
 *
 * Лежит в `core`, потому что нужна многим: главному экрану и прогрессу (цель),
 * повторению (подсказка, цель для списка «на сегодня») и напоминаниям в `core/reminders` (#32).
 */
interface SettingsRepository {

    /** Текущие настройки; присылает новые при каждом изменении. */
    val settings: Flow<UserSettings>

    suspend fun setDailyGoal(goal: Int)

    suspend fun setOnboardingDone()

    suspend fun setRatingHintShown()

    suspend fun setReminder(enabled: Boolean, time: LocalTime)
}
