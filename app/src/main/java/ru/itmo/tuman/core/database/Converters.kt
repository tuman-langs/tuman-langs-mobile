package ru.itmo.tuman.core.database

import androidx.room.TypeConverter
import ru.itmo.tuman.core.database.entity.DeckSource

/**
 * Room умеет хранить только простые типы (числа, строки).
 * Здесь объясняем ему, как сохранить enum и прочитать его обратно.
 */
class Converters {

    @TypeConverter
    fun deckSourceToString(source: DeckSource): String = source.name

    @TypeConverter
    fun stringToDeckSource(value: String): DeckSource = DeckSource.valueOf(value)
}
