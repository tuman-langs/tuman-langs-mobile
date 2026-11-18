package ru.itmo.tuman.core.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.itmo.tuman.core.settings.SettingsRepository

/**
 * Работа, которую WorkManager запускает раз в день в выбранное время.
 *
 * Если напоминание включено и на сегодня есть карточки — показывает уведомление.
 * Карточек нет — не беспокоим. Зависимости берём из Koin (запущен в `TumanApp`).
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    private val settings: SettingsRepository by inject()
    private val infoSource: ReminderInfoSource by inject()

    override suspend fun doWork(): Result {
        if (!settings.settings.first().reminderEnabled) return Result.success()
        val info = infoSource.current()
        if (info.cardsToday > 0) {
            ReminderNotifications.show(applicationContext, info)
        }
        return Result.success()
    }
}
