package ru.itmo.tuman.features.catalog.domain

import kotlinx.coroutines.flow.Flow
import ru.itmo.tuman.features.catalog.domain.model.CatalogSetPayload

interface CatalogSetStorage {
    fun observeDownloadedVersions(): Flow<Map<String, Int>>

    suspend fun import(set: CatalogSetPayload)
}
