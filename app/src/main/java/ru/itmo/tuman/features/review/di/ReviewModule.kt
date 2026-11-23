package ru.itmo.tuman.features.review.di

import kotlinx.coroutines.flow.map
import org.koin.dsl.module
import ru.itmo.tuman.core.settings.SettingsRepository
import ru.itmo.tuman.features.review.data.ReviewRepositoryImpl
import ru.itmo.tuman.features.review.domain.ReviewRepository

val reviewModule = module {
    single<ReviewRepository> {
        // Дневная цель — из настроек (#30)
        ReviewRepositoryImpl(get(), dailyGoal = get<SettingsRepository>().settings.map { it.dailyGoal })
    }
}
