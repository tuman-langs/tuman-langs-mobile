package ru.itmo.tuman.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.itmo.tuman.core.database.entity.DeckEntity
import ru.itmo.tuman.core.database.entity.DeckWithCardCount
import ru.itmo.tuman.core.database.entity.DownloadedVersion

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

    /**
     * Все наборы с числом карточек, новые сверху.
     * LEFT JOIN — чтобы пустой набор (0 карточек) тоже попал в список.
     */
    @Query(
        """
        SELECT decks.*, COUNT(cards.id) AS card_count,
            COUNT(CASE WHEN cards.due < :dueBefore THEN 1 END) AS due_today_count
        FROM decks LEFT JOIN cards ON cards.deck_id = decks.id
        GROUP BY decks.id
        ORDER BY decks.created_at DESC, decks.id DESC
        """,
    )
    fun observeAllWithCardCount(dueBefore: Long): Flow<List<DeckWithCardCount>>

    @Query("UPDATE decks SET title = :title WHERE id = :id")
    suspend fun rename(id: Long, title: String)

    /** Удаляет набор по id вместе с карточками и историей (каскад). */
    @Query("DELETE FROM decks WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Набор каталога по его id в каталоге (`ba-letters`); `null` — ещё не скачан. */
    @Query("SELECT * FROM decks WHERE remote_id = :remoteId")
    suspend fun getByRemoteId(remoteId: String): DeckEntity?

    /** Обновляет название, описание и версию набора каталога после его обновления. */
    @Query("UPDATE decks SET title = :title, description = :description, remote_version = :version WHERE id = :id")
    suspend fun updateCatalogInfo(id: Long, title: String, description: String?, version: Int)

    /** Скачанные наборы каталога и их версии. */
    @Query("SELECT remote_id, remote_version FROM decks WHERE remote_id IS NOT NULL")
    fun observeDownloadedVersions(): Flow<List<DownloadedVersion>>
}
