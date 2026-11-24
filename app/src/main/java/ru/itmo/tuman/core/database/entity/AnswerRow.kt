package ru.itmo.tuman.core.database.entity

import androidx.room.ColumnInfo

/** Не таблица, а результат запроса `ProgressDao.observeAnswers`: карточка, оценка и время ответа. */
data class AnswerRow(
    @ColumnInfo(name = "card_id")
    val cardId: Long,
    val rating: Int,
    @ColumnInfo(name = "reviewed_at")
    val reviewedAt: Long,
)
