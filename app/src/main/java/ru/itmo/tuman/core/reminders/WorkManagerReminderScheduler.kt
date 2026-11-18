package ru.itmo.tuman.core.reminders

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.first
import ru.itmo.tuman.core.settings.SettingsRepository
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * [ReminderScheduler] на WorkManager: ежедневная (раз в 24 ч) работа [ReminderWorker]
 * с начальной задержкой до ближайшего выбранного времени.
 *
 * WorkManager сам хранит запланированную работу и восстанавливает её после перезагрузки телефона.
 *
 * @param now текущий момент в поясе телефона; в тестах подставляем фиксированный.
 */
class WorkManagerReminderScheduler(
    private val context: Context,
    private val workManager: WorkManager,
    private val settings: SettingsRepository,
    private val now: () -> ZonedDateTime = ZonedDateTime::now,
) : ReminderScheduler {

    override suspend fun enable(time: LocalTime) {
        settings.setReminder(enabled = true, time = time)
        // Новое время — старое расписание выбрасываем и ставим заново
        schedule(time, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE)
    }

    override suspend fun disable() {
        val time = settings.settings.first().reminderTime
        settings.setReminder(enabled = false, time = time)
        workManager.cancelUniqueWork(WORK_NAME)
    }

    override suspend fun sync() {
        val current = settings.settings.first()
        if (current.reminderEnabled) {
            // KEEP: уже запланированное не сдвигаем
            schedule(current.reminderTime, ExistingPeriodicWorkPolicy.KEEP)
        } else {
            workManager.cancelUniqueWork(WORK_NAME)
        }
    }

    override fun hasPermission(): Boolean = ReminderNotifications.canNotify(context)

    private fun schedule(time: LocalTime, policy: ExistingPeriodicWorkPolicy) {
        val delay = ReminderDelay.untilNext(now(), time)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(WORK_NAME, policy, request)
    }

    companion object {
        /** Имя единственной работы напоминания — по нему её находят, заменяют и отменяют. */
        const val WORK_NAME = "daily_reminder"
    }
}
