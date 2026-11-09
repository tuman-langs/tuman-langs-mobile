package ru.itmo.tuman.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.itmo.tuman.core.database.entity.CardEntity
import ru.itmo.tuman.core.database.entity.CardWithReviewCount

/** Запросы к таблице карточек. */
@Dao
interface CardDao {

    /** Добавляет карточку и возвращает её новый id. */
    @Insert
    suspend fun insert(card: CardEntity): Long

    /** Добавляет сразу много карточек — например, весь скачанный набор. */
    @Insert
    suspend fun insertAll(cards: List<CardEntity>): List<Long>

    @Update
    suspend fun update(card: CardEntity)

    /** Удаляет карточку вместе с её историей ответов. */
    @Delete
    suspend fun delete(card: CardEntity)

    /** Карточки набора в порядке добавления. */
    @Query("SELECT * FROM cards WHERE deck_id = :deckId ORDER BY id")
    fun observeByDeck(deckId: Long): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getById(id: Long): CardEntity?

    /** Сколько карточек в наборе — для подписи в списке наборов. */
    @Query("SELECT COUNT(*) FROM cards WHERE deck_id = :deckId")
    fun observeCountByDeck(deckId: Long): Flow<Int>

    /**
     * Меняет только текст карточки. Поля алгоритма (stability, due и т. д.)
     * не трогает: исправление опечатки не должно сбрасывать прогресс повторения.
     */
    @Query("UPDATE cards SET front = :front, back = :back, example = :example WHERE id = :id")
    suspend fun updateText(id: Long, front: String, back: String, example: String?)

    /** Удаляет карточку по id вместе с её историей ответов (каскад). */
    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Карточки набора вместе с числом ответов на каждую.
     * В запросе участвует и `reviews`, поэтому список обновится и после нового ответа.
     */
    @Query(
        """
        SELECT cards.*, COUNT(reviews.id) AS review_count
        FROM cards LEFT JOIN reviews ON reviews.card_id = cards.id
        WHERE cards.deck_id = :deckId
        GROUP BY cards.id
        ORDER BY cards.id
        """,
    )
    fun observeByDeckWithReviewCount(deckId: Long): Flow<List<CardWithReviewCount>>

    /** Одна карточка с числом ответов — для редактора. */
    @Query(
        """
        SELECT cards.*, COUNT(reviews.id) AS review_count
        FROM cards LEFT JOIN reviews ON reviews.card_id = cards.id
        WHERE cards.id = :id
        GROUP BY cards.id
        """,
    )
    suspend fun getByIdWithReviewCount(id: Long): CardWithReviewCount?
}
