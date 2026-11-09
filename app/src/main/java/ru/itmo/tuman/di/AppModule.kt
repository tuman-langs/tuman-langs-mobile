package ru.itmo.tuman.di

import org.koin.dsl.module
import ru.itmo.tuman.core.database.di.databaseModule
import ru.itmo.tuman.features.decks.di.decksModule
import ru.itmo.tuman.features.review.di.reviewModule

/**
 * Главный модуль Koin. Сюда подключаются модули из core и features,
 * например: includes(databaseModule, decksModule, reviewModule).
 */
val appModule = module {
    includes(databaseModule, decksModule, reviewModule)
}
