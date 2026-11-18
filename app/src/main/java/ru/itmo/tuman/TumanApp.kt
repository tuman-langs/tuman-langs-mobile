package ru.itmo.tuman

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.itmo.tuman.core.reminders.ReminderNotifications
import ru.itmo.tuman.core.reminders.ReminderScheduler
import ru.itmo.tuman.di.appModule

/**
 * Класс приложения: создаётся системой раньше любого экрана.
 * Здесь запускаем Koin, чтобы экраны могли получать ViewModel и репозитории.
 */
class TumanApp : Application() {

    /** Фоновые дела приложения, которые не должны тормозить запуск. */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TumanApp)
            modules(appModule)
        }
        // Напоминания (#32): канал уведомлений и расписание по сохранённым настройкам
        ReminderNotifications.createChannel(this)
        appScope.launch { get<ReminderScheduler>().sync() }
    }
}
