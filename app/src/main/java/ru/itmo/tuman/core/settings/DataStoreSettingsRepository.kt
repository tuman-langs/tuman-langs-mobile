package ru.itmo.tuman.core.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

/**
 * [SettingsRepository] на DataStore Preferences — небольшой файл «ключ → значение» на телефоне.
 * Нет ключа — берём значение по умолчанию из [UserSettings].
 */
class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<UserSettings> = dataStore.data.map { prefs ->
        val defaults = UserSettings()
        UserSettings(
            dailyGoal = prefs[DAILY_GOAL] ?: defaults.dailyGoal,
            onboardingDone = prefs[ONBOARDING_DONE] ?: defaults.onboardingDone,
            ratingHintShown = prefs[RATING_HINT_SHOWN] ?: defaults.ratingHintShown,
            reminderEnabled = prefs[REMINDER_ENABLED] ?: defaults.reminderEnabled,
            reminderTime = prefs[REMINDER_MINUTES]?.let { LocalTime.of(it / 60, it % 60) } ?: defaults.reminderTime,
        )
    }

    override suspend fun setDailyGoal(goal: Int) {
        require(goal > 0) { "Дневная цель должна быть больше нуля: $goal" }
        dataStore.edit { it[DAILY_GOAL] = goal }
    }

    override suspend fun setOnboardingDone() {
        dataStore.edit { it[ONBOARDING_DONE] = true }
    }

    override suspend fun setRatingHintShown() {
        dataStore.edit { it[RATING_HINT_SHOWN] = true }
    }

    override suspend fun setReminder(enabled: Boolean, time: LocalTime) {
        dataStore.edit {
            it[REMINDER_ENABLED] = enabled
            // Время храним числом минут от полуночи: 19:00 → 1140
            it[REMINDER_MINUTES] = time.hour * 60 + time.minute
        }
    }

    private companion object {
        val DAILY_GOAL = intPreferencesKey("daily_goal")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val RATING_HINT_SHOWN = booleanPreferencesKey("rating_hint_shown")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_MINUTES = intPreferencesKey("reminder_minutes")
    }
}
