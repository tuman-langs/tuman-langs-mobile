package ru.itmo.tuman

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.itmo.tuman.di.appModule

/**
 * Класс приложения: создаётся системой раньше любого экрана.
 * Здесь запускаем Koin, чтобы экраны могли получать ViewModel и репозитории.
 */
class TumanApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TumanApp)
            modules(appModule)
        }
    }
}
