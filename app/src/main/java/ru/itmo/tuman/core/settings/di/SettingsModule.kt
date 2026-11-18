package ru.itmo.tuman.core.settings.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.itmo.tuman.core.settings.DataStoreSettingsRepository
import ru.itmo.tuman.core.settings.SettingsRepository

/** Настройки — один файл `settings.preferences_pb` и один репозиторий на всё приложение. */
val settingsModule = module {
    single<SettingsRepository> {
        DataStoreSettingsRepository(
            PreferenceDataStoreFactory.create { androidContext().preferencesDataStoreFile("settings") },
        )
    }
}
