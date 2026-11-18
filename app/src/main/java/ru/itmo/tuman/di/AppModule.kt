package ru.itmo.tuman.di

import org.koin.dsl.module
import ru.itmo.tuman.core.database.di.databaseModule
import ru.itmo.tuman.core.settings.di.settingsModule
import ru.itmo.tuman.features.decks.di.decksModule
import ru.itmo.tuman.features.progress.di.progressModule
import ru.itmo.tuman.features.review.di.reviewModule

/**
 * Главный модуль Koin. Сюда подключаются модули из core и features,
 * например: includes(databaseModule, decksModule, reviewModule).
 */
val appModule = module {
    includes(databaseModule, settingsModule, decksModule, reviewModule, progressModule)
}
