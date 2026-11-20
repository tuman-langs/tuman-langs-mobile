package ru.itmo.tuman.features.progress.di

import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel
import ru.itmo.tuman.core.database.TumanDatabase
import ru.itmo.tuman.features.progress.data.ProgressRepositoryImpl
import ru.itmo.tuman.features.progress.domain.ProgressRepository
import ru.itmo.tuman.features.progress.ui.ProgressViewModel

/** Что Koin создаёт для прогресса. ViewModel экрана прогресса добавит Дворак М. (#34). */
val progressModule = module {
    single<ProgressRepository> { ProgressRepositoryImpl(get<TumanDatabase>().progressDao(), get()) }
    viewModel { ProgressViewModel(progressRepository = get(), settingsRepository = get()) }
}
