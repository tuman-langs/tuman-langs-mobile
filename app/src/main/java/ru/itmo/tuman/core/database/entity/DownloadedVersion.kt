package ru.itmo.tuman.core.database.entity

import androidx.room.ColumnInfo

/**
 * Не таблица, а результат запроса `DeckDao.observeDownloadedVersions`:
 * какой набор каталога скачан и какой версии.
 */
data class DownloadedVersion(
    @ColumnInfo(name = "remote_id")
    val remoteId: String,
    @ColumnInfo(name = "remote_version")
    val version: Int,
)
