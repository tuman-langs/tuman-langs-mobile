package ru.itmo.tuman.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Таблица `decks` — набор карточек (ФТ-1).
 *
 * @property id номер набора в базе, Room выдаёт его сам при вставке.
 * @property title название, которое видит пользователь.
 * @property language код языка набора, в MVP всегда `ba` (башкирский).
 * @property source откуда набор: создан пользователем или скачан из каталога.
 * @property createdAt дата создания, миллисекунды с 1970 года (UTC).
 * @property description краткое описание; есть у наборов из каталога.
 * @property remoteId id набора в каталоге (`ba-basic-words`), у своих наборов `null`.
 *   По нему понимаем, что набор уже скачан.
 * @property remoteVersion поле `version` из файла набора. Если на сервере версия больше —
 *   вышло обновление. У своих наборов `null`.
 *
 * Формат файла набора — `content/README.md` в tuman-langs-backend (утверждён в backend#2).
 */
@Entity(
    tableName = "decks",
    // Один и тот же набор каталога нельзя скачать дважды.
    // У своих наборов remote_id = NULL, а NULL в уникальном индексе не мешают друг другу.
    indices = [Index(value = ["remote_id"], unique = true)],
)
data class DeckEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val language: String = LANGUAGE_BASHKIR,
    val source: DeckSource = DeckSource.OWN,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    val description: String? = null,
    @ColumnInfo(name = "remote_id")
    val remoteId: String? = null,
    @ColumnInfo(name = "remote_version")
    val remoteVersion: Int? = null,
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
