package ru.itmo.tuman.features.catalog.ui

import ru.itmo.tuman.features.catalog.domain.CatalogFailure
import ru.itmo.tuman.features.catalog.domain.model.CatalogSetSummary

data class CatalogUiState(
    val sets: List<CatalogSetSummary> = emptyList(),
    val downloadedVersions: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val isWakingServer: Boolean = false,
    val isOffline: Boolean = false,
    val loadFailure: CatalogFailure? = null,
    val downloadingSetIds: Set<String> = emptySet(),
    val wakingSetIds: Set<String> = emptySet(),
    val downloadFailureSetId: String? = null,
    val downloadedVersionsUnavailable: Boolean = false,
)
