package ru.itmo.tuman.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Таблица `decks` — набор карточек (ФТ-1).
 *
 * @property id номер набора в базе, Room выдаёт его сам при вставке.
 * @property title название, которое видит пользователь.
 * @property language код языка набора, в MVP всегда `ba` (башкирский).
 * @property source откуда набор: создан пользователем или скачан из каталога.
 * @property createdAt дата создания, миллисекунды с 1970 года (UTC).
 */
@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val language: String = LANGUAGE_BASHKIR,
    val source: DeckSource = DeckSource.OWN,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
) {
    companion object {
        const val LANGUAGE_BASHKIR = "ba"
    }
}

/** Откуда появился набор. В базе хранится строкой (см. `Converters`). */
enum class DeckSource {
    /** Пользователь создал набор сам. */
    OWN,

    /** Набор скачан из каталога на сервере. */
    CATALOG,
}
