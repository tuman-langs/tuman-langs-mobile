package ru.itmo.tuman.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

/**
 * Не таблица, а результат запроса `DeckDao.observeAllWithCardCount`:
 * набор и сколько в нём карточек. Room сам заполнит оба поля по именам столбцов.
 */
data class DeckWithCardCount(
    @Embedded
    val deck: DeckEntity,
    @ColumnInfo(name = "card_count")
    val cardCount: Int,
)
