package ru.itmo.tuman.core.reminders.di

import androidx.work.WorkManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.itmo.tuman.core.reminders.ReminderScheduler
import ru.itmo.tuman.core.reminders.WorkManagerReminderScheduler

/** Планировщик напоминаний — один на приложение. `ReminderInfoSource` задаётся в `di/AppModule.kt`. */
val remindersModule = module {
    single<ReminderScheduler> {
        WorkManagerReminderScheduler(androidContext(), WorkManager.getInstance(androidContext()), get())
    }
}
