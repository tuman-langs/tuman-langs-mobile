package ru.itmo.tuman.core.database.entity

import androidx.room.ColumnInfo

/** Не таблица, а результат запроса `ProgressDao.observeAnswers`: оценка и время ответа. */
data class AnswerRow(
    val rating: Int,
    @ColumnInfo(name = "reviewed_at")
    val reviewedAt: Long,
)
