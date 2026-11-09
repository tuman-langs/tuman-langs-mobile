package ru.itmo.tuman.core.database.entity

import androidx.room.ColumnInfo

/**
 * Не таблица, а результат запроса `ReviewDao.observeTodayCounts`:
 * сколько карточек пора повторить и сколько новых — без учёта дневной цели.
 */
data class TodayCounts(
    @ColumnInfo(name = "due_count")
    val dueCount: Int,
    @ColumnInfo(name = "new_count")
    val newCount: Int,
)
