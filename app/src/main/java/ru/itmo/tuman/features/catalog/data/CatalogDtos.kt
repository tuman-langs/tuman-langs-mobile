package ru.itmo.tuman.features.catalog.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CatalogSetSummaryDto(
    val id: String,
    val language: String,
    val title: String,
    val description: String,
    val version: Int,
    @SerialName("card_count") val cardCount: Int,
)

@Serializable
data class CatalogSetDto(
    val id: String,
    val version: Int,
    val title: String,
    val description: String? = null,
    val cards: List<CatalogCardDto>,
)

@Serializable
data class CatalogCardDto(
    val id: String,
    val front: String,
    val back: String,
    val tags: List<String> = emptyList(),
    val pronunciation: String? = null,
    val notes: String? = null,
    val example: String? = null,
    @SerialName("example_translation") val exampleTranslation: String? = null,
)
