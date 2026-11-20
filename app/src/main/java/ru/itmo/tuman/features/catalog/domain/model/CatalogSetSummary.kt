package ru.itmo.tuman.features.catalog.domain.model

data class CatalogSetSummary(
    val remoteId: String,
    val title: String,
    val description: String,
    val version: Int,
    val cardCount: Int,
)
