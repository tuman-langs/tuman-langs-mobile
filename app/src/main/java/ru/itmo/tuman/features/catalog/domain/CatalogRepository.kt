package ru.itmo.tuman.features.catalog.domain

import kotlinx.coroutines.flow.Flow
import ru.itmo.tuman.features.catalog.domain.model.CatalogSetSummary

interface CatalogRepository {
    fun observeDownloadedVersions(): Flow<Map<String, Int>>

    suspend fun loadSets(): CatalogResult<List<CatalogSetSummary>>

    suspend fun downloadSet(remoteId: String): CatalogResult<Unit>
}
