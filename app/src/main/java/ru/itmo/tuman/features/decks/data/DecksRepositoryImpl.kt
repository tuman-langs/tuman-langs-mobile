package ru.itmo.tuman.features.decks.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.itmo.tuman.core.database.dao.CardDao
import ru.itmo.tuman.core.database.dao.DeckDao
import ru.itmo.tuman.core.database.entity.DeckEntity
import ru.itmo.tuman.features.decks.domain.DecksRepository
import ru.itmo.tuman.features.decks.domain.model.Card
import ru.itmo.tuman.features.decks.domain.model.Deck

/**
 * Реализация [DecksRepository] через базу Room.
 *
 * @param now откуда брать текущее время (мс UTC). По умолчанию — системные часы;
 *   в тестах подставляем фиксированное время, чтобы результат не зависел от часов.
 */
class DecksRepositoryImpl(
    private val deckDao: DeckDao,
    private val cardDao: CardDao,
    private val now: () -> Long = System::currentTimeMillis,
) : DecksRepository {

    override fun observeDecks(): Flow<List<Deck>> =
        deckDao.observeAllWithCardCount().map { rows -> rows.map { it.toDomain() } }

    override suspend fun createDeck(title: String): Long =
        deckDao.insert(DeckEntity(title = title.trim(), createdAt = now()))

    override suspend fun renameDeck(id: Long, title: String) =
        deckDao.rename(id, title.trim())

    override suspend fun deleteDeck(id: Long) =
        deckDao.deleteById(id)

    override fun observeCards(deckId: Long): Flow<List<Card>> =
        cardDao.observeByDeck(deckId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getCard(id: Long): Card? =
        cardDao.getById(id)?.toDomain()

    override suspend fun saveCard(card: Card): Long {
        if (card.id == 0L) {
            return cardDao.insert(card.toNewEntity(now()))
        }
        cardDao.updateText(
            id = card.id,
            front = card.front,
            back = card.back,
            example = card.example?.takeIf { it.isNotBlank() },
        )
        return card.id
    }

    override suspend fun deleteCard(id: Long) =
        cardDao.deleteById(id)
}
