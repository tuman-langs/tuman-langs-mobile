package ru.itmo.tuman.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Таблица `reviews` — история ответов (ФТ-7): одна строка на каждый ответ.
 *
 * Из неё считаются число повторений карточки, доля верных ответов и XP,
 * поэтому отдельных счётчиков в карточке нет. Удалили карточку — её история
 * удаляется вместе с ней (`onDelete = CASCADE`).
 *
 * @property rating оценка от 1 до 4: 1 — «не помню», 2 — «трудно»,
 *   3 — «хорошо», 4 — «легко» (порядок как в `Rating` из core-srs).
 * @property reviewedAt время ответа, миллисекунды с 1970 года (UTC).
 * @property intervalDays через сколько дней карточку покажут снова после этого ответа.
 */
@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(
            entity = CardEntity::class,
            parentColumns = ["id"],
            childColumns = ["card_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("card_id")],
)
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "card_id")
    val cardId: Long,
    val rating: Int,
    @ColumnInfo(name = "reviewed_at")
    val reviewedAt: Long,
    @ColumnInfo(name = "interval_days")
    val intervalDays: Double,
)
