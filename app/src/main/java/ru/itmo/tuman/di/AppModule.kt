package ru.itmo.tuman.di

import org.koin.dsl.module

/**
 * Главный модуль Koin. Сюда подключаются модули из core и features,
 * например: includes(databaseModule, decksModule).
 */
val appModule = module {
}
