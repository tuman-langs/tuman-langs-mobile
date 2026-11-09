package ru.itmo.tuman.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.itmo.tuman.core.database.entity.CardEntity
import ru.itmo.tuman.core.database.entity.ReviewEntity
import ru.itmo.tuman.core.database.entity.TodayCounts

/** Запросы для повторения: история ответов и список «на сегодня». */
@Dao
interface ReviewDao {

    /** Сохраняет один ответ и возвращает его id. */
    @Insert
    suspend fun insert(review: ReviewEntity): Long

    /** Все ответы по карточке, по порядку. */
    @Query("SELECT * FROM reviews WHERE card_id = :cardId ORDER BY reviewed_at")
    suspend fun getByCard(cardId: Long): List<ReviewEntity>

    /**
     * Карточки на сегодня — те, у которых дата показа раньше [startOfNextDay].
     *
     * Порядок: сначала уже повторявшиеся (`last_review IS NOT NULL` даёт 0, а `IS NULL` — 1),
     * среди них просроченные первыми; затем новые в порядке добавления.
     * Поэтому `LIMIT` берёт сначала все карточки на повторение, а новые — только на остаток цели.
     *
     * [deckId] = `null` — из всех наборов.
     */
    @Query(
        """
        SELECT * FROM cards
        WHERE due < :startOfNextDay AND (:deckId IS NULL OR deck_id = :deckId)
        ORDER BY last_review IS NULL, due, id
        LIMIT :limit
        """,
    )
    fun observeTodayCards(startOfNextDay: Long, deckId: Long?, limit: Int): Flow<List<CardEntity>>

    /** Сколько карточек на повторение и сколько новых до [startOfNextDay], по всем наборам. */
    @Query(
        """
        SELECT
            COALESCE(SUM(CASE WHEN last_review IS NOT NULL THEN 1 ELSE 0 END), 0) AS due_count,
            COALESCE(SUM(CASE WHEN last_review IS NULL THEN 1 ELSE 0 END), 0) AS new_count
        FROM cards
        WHERE due < :startOfNextDay
        """,
    )
    fun observeTodayCounts(startOfNextDay: Long): Flow<TodayCounts>
}
