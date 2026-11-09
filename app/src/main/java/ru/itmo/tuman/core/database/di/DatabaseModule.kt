package ru.itmo.tuman.core.database.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.itmo.tuman.core.database.TumanDatabase

/**
 * Что Koin создаёт для базы. `single` — один объект на всё приложение:
 * база открывается один раз, и все репозитории получают одни и те же DAO.
 */
val databaseModule = module {
    single { TumanDatabase.create(androidContext()) }
    single { get<TumanDatabase>().deckDao() }
    single { get<TumanDatabase>().cardDao() }
    single { get<TumanDatabase>().reviewDao() }
}
