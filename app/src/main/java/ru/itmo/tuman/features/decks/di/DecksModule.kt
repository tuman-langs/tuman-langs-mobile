package ru.itmo.tuman.features.decks.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.itmo.tuman.features.decks.data.FakeDecksRepository
import ru.itmo.tuman.features.decks.domain.DecksRepository
import ru.itmo.tuman.features.decks.ui.list.DecksViewModel

/** Что Koin создаёт для наборов. `single` — один репозиторий на всё приложение. */
val decksModule = module {
    // Когда #12 будет в main, здесь нужно подставить DecksRepositoryImpl.
    single<DecksRepository> { FakeDecksRepository() }
    viewModel { DecksViewModel(get()) }
}
