package ru.itmo.tuman.features.catalog.domain.model

data class CatalogSetPayload(
    val remoteId: String,
    val version: Int,
    val title: String,
    val description: String?,
    val cards: List<CatalogCardPayload>,
)

data class CatalogCardPayload(
    val remoteId: String,
    val front: String,
    val back: String,
    val example: String?,
    val exampleTranslation: String?,
    val pronunciation: String?,
    val notes: String?,
)
