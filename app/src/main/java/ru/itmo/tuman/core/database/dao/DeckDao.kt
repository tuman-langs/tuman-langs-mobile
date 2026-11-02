package ru.itmo.tuman.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.itmo.tuman.core.database.entity.DeckEntity

/**
 * Запросы к таблице наборов.
 *
 * `suspend` — запрос выполняется один раз и не блокирует экран.
 * `Flow` — подписка: экран получает новый список при каждом изменении таблицы.
 */
@Dao
interface DeckDao {

    /** Добавляет набор и возвращает его новый id. */
    @Insert
    suspend fun insert(deck: DeckEntity): Long

    @Update
    suspend fun update(deck: DeckEntity)

    /** Удаляет набор вместе со всеми его карточками и историей ответов. */
    @Delete
    suspend fun delete(deck: DeckEntity)

    /** Все наборы, новые сверху. */
    @Query("SELECT * FROM decks ORDER BY created_at DESC")
    fun observeAll(): Flow<List<DeckEntity>>

    @Query("SELECT * FROM decks WHERE id = :id")
    suspend fun getById(id: Long): DeckEntity?
}
