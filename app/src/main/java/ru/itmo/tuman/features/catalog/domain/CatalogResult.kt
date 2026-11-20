package ru.itmo.tuman.features.catalog.domain

sealed interface CatalogFailure {
    data object NoInternet : CatalogFailure
    data object SetNotFound : CatalogFailure
    data object RequestFailed : CatalogFailure
    data object SaveFailed : CatalogFailure
}

sealed interface CatalogResult<out T> {
    data class Success<T>(val value: T) : CatalogResult<T>
    data class Failure(val reason: CatalogFailure) : CatalogResult<Nothing>
}
