package ru.itmo.tuman.core.reminders

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import ru.itmo.tuman.core.settings.SettingsRepository
import ru.itmo.tuman.core.settings.UserSettings
import java.time.LocalTime

/** Настройки в памяти для тестов напоминаний. */
class FakeSettingsRepository(initial: UserSettings = UserSettings()) : SettingsRepository {
    val state = MutableStateFlow(initial)
    override val settings = state
    override suspend fun setDailyGoal(goal: Int) = state.update { it.copy(dailyGoal = goal) }
    override suspend fun setOnboardingDone() = state.update { it.copy(onboardingDone = true) }
    override suspend fun setRatingHintShown() = state.update { it.copy(ratingHintShown = true) }
    override suspend fun setReminder(enabled: Boolean, time: LocalTime) =
        state.update { it.copy(reminderEnabled = enabled, reminderTime = time) }
}
