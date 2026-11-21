package ru.itmo.tuman.di

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.itmo.tuman.core.reminders.ReminderInfo
import ru.itmo.tuman.core.reminders.ReminderInfoSource
import ru.itmo.tuman.core.reminders.di.remindersModule
import ru.itmo.tuman.core.database.di.databaseModule
import ru.itmo.tuman.core.network.di.networkModule
import ru.itmo.tuman.core.settings.di.settingsModule
import ru.itmo.tuman.features.catalog.di.catalogModule
import ru.itmo.tuman.features.catalog.domain.CatalogSetStorage
import ru.itmo.tuman.features.decks.di.decksModule
import ru.itmo.tuman.features.decks.domain.DecksRepository
import ru.itmo.tuman.features.home.ui.HomeDeck
import ru.itmo.tuman.features.home.ui.HomeProgress
import ru.itmo.tuman.features.home.ui.HomeToday
import ru.itmo.tuman.features.home.ui.HomeViewModel
import ru.itmo.tuman.features.progress.di.progressModule
import ru.itmo.tuman.features.progress.domain.ProgressRepository
import ru.itmo.tuman.features.review.di.reviewModule
import ru.itmo.tuman.features.review.domain.ReviewRepository
import ru.itmo.tuman.features.settings.di.settingsUiModule

/**
 * Главный модуль Koin. Сюда подключаются модули из core и features,
 * например: includes(databaseModule, decksModule, reviewModule).
 */
val appModule = module {
    includes(
        databaseModule,
        networkModule,
        settingsModule,
        remindersModule,
        decksModule,
        catalogModule,
        reviewModule,
        progressModule,
        settingsUiModule,
    )
    viewModel {
        val reviewRepository = get<ReviewRepository>()
        val progressRepository = get<ProgressRepository>()
        val decksRepository = get<DecksRepository>()
        HomeViewModel(
            settingsRepository = get(),
            observeToday = { reviewRepository.observeTodayCount().map { HomeToday(it.due, it.new) } },
            observeProgress = {
                progressRepository.observeProgress().map {
                    HomeProgress(it.todayAnswers, it.dailyGoal, it.currentStreak, it.xp)
                }
            },
            observeDecks = {
                decksRepository.observeDecks().map { decks ->
                    decks.map { HomeDeck(it.id, it.title, it.cardCount) }
                }
            },
        )
    }

    single<CatalogSetStorage> { DecksCatalogSetStorage(get()) }

    // Напоминанию (core/reminders) нужны числа из повторения и прогресса (features).
    // core не может импортировать features, поэтому связываем их здесь.
    single<ReminderInfoSource> {
        ReminderInfoSource {
            val today = get<ReviewRepository>().observeTodayCount().first()
            val progress = get<ProgressRepository>().observeProgress().first()
            ReminderInfo(cardsToday = today.due + today.new, streakDays = progress.currentStreak)
        }
    }
}
