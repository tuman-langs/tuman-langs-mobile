package ru.itmo.tuman.core.reminders

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ru.itmo.tuman.R
import java.util.Locale

/** Канал и само уведомление-напоминание. */
object ReminderNotifications {

    const val CHANNEL_ID = "daily_reminder"
    const val NOTIFICATION_ID = 1001

    /** Создаёт канал уведомлений. Вызывается в `TumanApp` при каждом запуске — повторно ничего не ломает. */
    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = context.getString(R.string.reminder_channel_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /**
     * Можно ли показывать уведомления: на Android 13+ дано разрешение POST_NOTIFICATIONS
     * и пользователь не выключил уведомления приложения в настройках телефона.
     */
    fun canNotify(context: Context): Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled()

    /**
     * Показывает «N карточек ждут повторения…». Нажатие открывает приложение.
     * Без разрешения молча ничего не делает — приложение не падает.
     */
    @SuppressLint("MissingPermission") // проверяем через canNotify перед показом
    fun show(context: Context, info: ReminderInfo) {
        if (!canNotify(context)) return
        val resources = russianResources(context)
        val cards = resources.getQuantityString(R.plurals.reminder_cards, info.cardsToday, info.cardsToday)
        val text = if (info.streakDays > 0) {
            cards + " " + resources.getQuantityString(R.plurals.reminder_streak, info.streakDays, info.streakDays)
        } else {
            cards + " " + context.getString(R.string.reminder_no_streak)
        }

        // Открываем приложение тем же способом, что и иконка на рабочем столе
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val contentIntent = launch?.let {
            PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    /**
     * Форму слова («день / дня / дней») Android выбирает по языку телефона. На английском телефоне
     * форм всего две, и получилось бы «5 дня». Приложение только на русском, поэтому для текстов
     * уведомления явно берём русские правила.
     */
    private fun russianResources(context: Context): Resources {
        val config = Configuration(context.resources.configuration).apply { setLocale(Locale("ru")) }
        return context.createConfigurationContext(config).resources
    }
}
