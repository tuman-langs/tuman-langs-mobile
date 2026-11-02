package ru.itmo.tuman.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.itmo.tuman.core.database.dao.CardDao
import ru.itmo.tuman.core.database.dao.DeckDao
import ru.itmo.tuman.core.database.dao.ReviewDao
import ru.itmo.tuman.core.database.entity.CardEntity
import ru.itmo.tuman.core.database.entity.DeckEntity
import ru.itmo.tuman.core.database.entity.ReviewEntity

/**
 * База приложения: файл `tuman.db` на телефоне.
 *
 * Связи таблиц: decks 1 → N cards 1 → N reviews.
 *
 * При любом изменении таблиц нужно поднять [version] и добавить миграцию,
 * иначе у пользователей, которые уже установили приложение, оно упадёт
 * при запуске. Описание каждой версии схемы Room сохраняет в `app/schemas/`.
 */
@Database(
    entities = [DeckEntity::class, CardEntity::class, ReviewEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class TumanDatabase : RoomDatabase() {

    abstract fun deckDao(): DeckDao
    abstract fun cardDao(): CardDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        private const val DATABASE_NAME = "tuman.db"

        /** Создаёт базу. Вызывать один раз на всё приложение (через Koin). */
        fun create(context: Context): TumanDatabase =
            Room.databaseBuilder(context, TumanDatabase::class.java, DATABASE_NAME).build()
    }
}
