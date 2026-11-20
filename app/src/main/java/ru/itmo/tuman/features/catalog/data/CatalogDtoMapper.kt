package ru.itmo.tuman.features.catalog.data

import ru.itmo.tuman.features.catalog.domain.model.CatalogSetSummary
import ru.itmo.tuman.features.catalog.domain.model.CatalogCardPayload
import ru.itmo.tuman.features.catalog.domain.model.CatalogSetPayload

internal fun CatalogSetSummaryDto.toDomain() = CatalogSetSummary(
    remoteId = id,
    title = title,
    description = description,
    version = version,
    cardCount = cardCount,
)

internal fun CatalogSetDto.toDomain() = CatalogSetPayload(
    remoteId = id,
    version = version,
    title = title,
    description = description,
    cards = cards.map { card ->
        CatalogCardPayload(
            remoteId = card.id,
            front = card.front,
            back = card.back,
            example = card.example,
            exampleTranslation = card.exampleTranslation,
            pronunciation = card.pronunciation,
            notes = card.notes,
        )
    },
)
