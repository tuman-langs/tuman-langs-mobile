package ru.itmo.tuman.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.itmo.tuman.core.database.entity.AnswerRow
import ru.itmo.tuman.core.database.entity.CardEntity

/**
 * Запросы для прогресса (#30): из них считаются серия, XP, выученные и достижения.
 * Все — `Flow`, поэтому прогресс сам пересчитывается после каждого ответа.
 */
@Dao
interface ProgressDao {

    /** Вся история ответов: карточка, оценка и время. */
    @Query("SELECT card_id, rating, reviewed_at FROM reviews ORDER BY reviewed_at")
    fun observeAnswers(): Flow<List<AnswerRow>>

    /** Карточки, на которые уже отвечали, — среди них ищем выученные. */
    @Query("SELECT * FROM cards WHERE last_review IS NOT NULL")
    fun observeReviewedCards(): Flow<List<CardEntity>>

    /** Сколько наборов — для достижения «Первый набор». */
    @Query("SELECT COUNT(*) FROM decks")
    fun observeDeckCount(): Flow<Int>
}
