package ru.itmo.tuman.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.itmo.tuman.core.database.entity.CardEntity

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
}
