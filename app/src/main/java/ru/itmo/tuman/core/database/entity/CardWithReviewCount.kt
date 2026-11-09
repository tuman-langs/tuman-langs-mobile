package ru.itmo.tuman.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

/**
 * Не таблица, а результат запроса `CardDao.observeByDeckWithReviewCount`:
 * карточка и сколько раз на неё отвечали (число строк в `reviews`).
 */
data class CardWithReviewCount(
    @Embedded
    val card: CardEntity,
    @ColumnInfo(name = "review_count")
    val reviewCount: Int,
)
