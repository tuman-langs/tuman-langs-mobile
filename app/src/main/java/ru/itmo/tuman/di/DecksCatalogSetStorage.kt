package ru.itmo.tuman.di

import kotlinx.coroutines.flow.Flow
import ru.itmo.tuman.features.catalog.domain.CatalogSetStorage
import ru.itmo.tuman.features.catalog.domain.model.CatalogSetPayload
import ru.itmo.tuman.features.decks.domain.DecksRepository
import ru.itmo.tuman.features.decks.domain.model.CatalogCard
import ru.itmo.tuman.features.decks.domain.model.CatalogSet

class DecksCatalogSetStorage(
    private val decksRepository: DecksRepository,
) : CatalogSetStorage {

    override fun observeDownloadedVersions(): Flow<Map<String, Int>> =
        decksRepository.observeDownloadedVersions()

    override suspend fun import(set: CatalogSetPayload) {
        decksRepository.importCatalogSet(
            CatalogSet(
                remoteId = set.remoteId,
                version = set.version,
                title = set.title,
                description = set.description,
                cards = set.cards.map { card ->
                    CatalogCard(
                        remoteId = card.remoteId,
                        front = card.front,
                        back = card.back,
                        example = card.example,
                        exampleTranslation = card.exampleTranslation,
                        pronunciation = card.pronunciation,
                        notes = card.notes,
                    )
                },
            ),
        )
    }
}
