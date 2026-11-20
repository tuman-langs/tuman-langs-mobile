package ru.itmo.tuman.features.settings.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.itmo.tuman.features.settings.ui.SettingsViewModel

val settingsUiModule = module {
    viewModel { SettingsViewModel(settingsRepository = get(), reminderScheduler = get()) }
}
