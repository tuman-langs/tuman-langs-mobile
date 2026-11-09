package ru.itmo.tuman.features.decks.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.itmo.tuman.features.decks.data.DecksRepositoryImpl
import ru.itmo.tuman.features.decks.domain.DecksRepository
import ru.itmo.tuman.features.decks.ui.list.DecksViewModel

/** Что Koin создаёт для наборов. `single` — один репозиторий на всё приложение. */
val decksModule = module {
    // Настоящее хранилище через Room. FakeDecksRepository остаётся для юнит-тестов экранов.
    single<DecksRepository> { DecksRepositoryImpl(get(), get()) }
    viewModel { DecksViewModel(get()) }
}
