package ru.itmo.tuman.core.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.time.LocalTime

/**
 * Только для debug-сборки: быстро проверить напоминание без ожидания (#32).
 *
 * Уведомление прямо сейчас:
 *   adb shell am broadcast -a ru.itmo.tuman.debug.REMINDER_NOW -p ru.itmo.tuman
 * Напоминание через 2 минуты (как настоящее, через расписание):
 *   adb shell am broadcast -a ru.itmo.tuman.debug.REMINDER_IN --ei minutes 2 -p ru.itmo.tuman
 * Выключить напоминание:
 *   adb shell am broadcast -a ru.itmo.tuman.debug.REMINDER_OFF -p ru.itmo.tuman
 */
class DebugReminderReceiver : BroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_NOW -> WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<ReminderWorker>().build())
            ACTION_IN -> {
                val minutes = intent.getIntExtra(EXTRA_MINUTES, 2).toLong()
                val time = LocalTime.now().plusMinutes(minutes).withSecond(0).withNano(0)
                runAsync { get<ReminderScheduler>().enable(time) }
            }
            ACTION_OFF -> runAsync { get<ReminderScheduler>().disable() }
        }
    }

    /** Приёмник живёт недолго — goAsync даёт дождаться записи в настройки. */
    private fun runAsync(block: suspend () -> Unit) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                block()
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val ACTION_NOW = "ru.itmo.tuman.debug.REMINDER_NOW"
        const val ACTION_IN = "ru.itmo.tuman.debug.REMINDER_IN"
        const val ACTION_OFF = "ru.itmo.tuman.debug.REMINDER_OFF"
        const val EXTRA_MINUTES = "minutes"
    }
}
