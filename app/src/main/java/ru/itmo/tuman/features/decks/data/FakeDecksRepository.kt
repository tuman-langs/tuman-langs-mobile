package ru.itmo.tuman.features.decks.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import ru.itmo.tuman.features.decks.domain.DecksRepository
import ru.itmo.tuman.features.decks.domain.model.Card
import ru.itmo.tuman.features.decks.domain.model.CatalogSet
import ru.itmo.tuman.features.decks.domain.model.Deck

/**
 * Хранилище в памяти: нужно для юнит-тестов и для экрана, пока нет настоящей базы (#12).
 * После перезапуска приложения данные пропадают — так и задумано.
 */
class FakeDecksRepository : DecksRepository {

    private val decks = MutableStateFlow<List<Deck>>(emptyList())
    private val cards = MutableStateFlow<List<Card>>(emptyList())
    private var lastDeckId = 0L
    private var lastCardId = 0L
    private val downloaded = MutableStateFlow<Map<String, Pair<Long, Int>>>(emptyMap()) // remoteId → (id набора, версия)

    override fun observeDecks(): Flow<List<Deck>> =
        combine(decks, cards) { decks, cards ->
            decks.map { deck -> deck.copy(cardCount = cards.count { it.deckId == deck.id }) }
        }

    override suspend fun createDeck(title: String): Long {
        val id = ++lastDeckId
        val deck = Deck(id = id, title = title, cardCount = 0, isFromCatalog = false)
        decks.update { listOf(deck) + it }
        return id
    }

    override suspend fun renameDeck(id: Long, title: String) {
        decks.update { list -> list.map { if (it.id == id) it.copy(title = title) else it } }
    }

    override suspend fun deleteDeck(id: Long) {
        decks.update { list -> list.filterNot { it.id == id } }
        cards.update { list -> list.filterNot { it.deckId == id } }
    }

    override fun observeCards(deckId: Long): Flow<List<Card>> =
        cards.map { list -> list.filter { it.deckId == deckId } }

    override suspend fun getCard(id: Long): Card? = cards.value.find { it.id == id }

    override suspend fun saveCard(card: Card): Long {
        if (card.id == 0L) {
            val id = ++lastCardId
            cards.update { it + card.copy(id = id) }
            return id
        }
        cards.update { list -> list.map { if (it.id == card.id) card else it } }
        return card.id
    }

    override suspend fun deleteCard(id: Long) {
        cards.update { list -> list.filterNot { it.id == id } }
    }

    // Упрощённо: обновление версии заменяет карточки целиком — прогресс в фейке не хранится
    override suspend fun importCatalogSet(set: CatalogSet): Long {
        val known = downloaded.value[set.remoteId]
        if (known != null && known.second >= set.version) return known.first
        val deckId = known?.first ?: ++lastDeckId
        if (known == null) {
            decks.update { listOf(Deck(id = deckId, title = set.title, cardCount = 0, isFromCatalog = true)) + it }
        }
        cards.update { list ->
            list.filterNot { it.deckId == deckId } + set.cards.map {
                Card(id = ++lastCardId, deckId = deckId, front = it.front, back = it.back, example = it.example)
            }
        }
        downloaded.update { it + (set.remoteId to (deckId to set.version)) }
        return deckId
    }

    override fun observeDownloadedVersions(): Flow<Map<String, Int>> =
        downloaded.map { map -> map.mapValues { it.value.second } }
}
