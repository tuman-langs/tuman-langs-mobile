package ru.itmo.tuman.di

import org.koin.dsl.module
import ru.itmo.tuman.features.decks.di.decksModule

/**
 * Главный модуль Koin. Сюда подключаются модули из core и features,
 * например: includes(databaseModule, decksModule).
 */
val appModule = module {
    includes(decksModule)
}
