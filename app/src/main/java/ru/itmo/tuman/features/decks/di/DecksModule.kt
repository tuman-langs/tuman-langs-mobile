package ru.itmo.tuman.features.decks.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.itmo.tuman.features.decks.data.DecksRepositoryImpl
import ru.itmo.tuman.features.decks.domain.DecksRepository
import ru.itmo.tuman.features.decks.ui.deck.DeckViewModel
import ru.itmo.tuman.features.decks.ui.edit.CardEditViewModel
import ru.itmo.tuman.features.decks.ui.list.DecksViewModel

/** Что Koin создаёт для наборов. `single` — один репозиторий на всё приложение. */
val decksModule = module {
    // Настоящее хранилище через Room. FakeDecksRepository остаётся для юнит-тестов экранов.
    single<DecksRepository> { DecksRepositoryImpl(get(), get()) }
    viewModel { DecksViewModel(get()) }
    // Параметры в скобках экран передаёт через parametersOf(...) — это id из маршрута
    viewModel { (deckId: Long) -> DeckViewModel(deckId, get()) }
    viewModel { (deckId: Long, cardId: Long) -> CardEditViewModel(deckId, cardId, get()) }
}
