package ru.itmo.tuman.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.itmo.tuman.core.database.entity.ReviewEntity

/** Запросы к истории ответов. */
@Dao
interface ReviewDao {

    /** Сохраняет один ответ и возвращает его id. */
    @Insert
    suspend fun insert(review: ReviewEntity): Long

    /** Все ответы по карточке, по порядку. */
    @Query("SELECT * FROM reviews WHERE card_id = :cardId ORDER BY reviewed_at")
    suspend fun getByCard(cardId: Long): List<ReviewEntity>
}
