package ru.itmo.tuman.core.settings

import java.time.LocalTime

/**
 * Настройки пользователя. Хранятся на телефоне (DataStore) и переживают перезапуск.
 *
 * @property dailyGoal дневная цель — сколько ответов в день; выбирается из [GOAL_OPTIONS].
 * @property onboardingDone первый запуск пройден (цель выбрана).
 * @property ratingHintShown подсказка к оценкам на экране повторения уже показана.
 * @property reminderEnabled ежедневное напоминание включено.
 * @property reminderTime время напоминания.
 */
data class UserSettings(
    val dailyGoal: Int = DEFAULT_DAILY_GOAL,
    val onboardingDone: Boolean = false,
    val ratingHintShown: Boolean = false,
    val reminderEnabled: Boolean = true,
    val reminderTime: LocalTime = DEFAULT_REMINDER_TIME,
) {
    companion object {
        const val DEFAULT_DAILY_GOAL = 20

        /** Варианты цели на первом запуске и в «Изменить цель» (макет). */
        val GOAL_OPTIONS = listOf(10, 20, 30, 50)

        /** По макету напоминание по умолчанию включено на 19:00. */
        val DEFAULT_REMINDER_TIME: LocalTime = LocalTime.of(19, 0)
    }
}
