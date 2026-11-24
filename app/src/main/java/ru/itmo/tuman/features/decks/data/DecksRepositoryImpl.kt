package ru.itmo.tuman.features.decks.data

import androidx.room.withTransaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.itmo.tuman.core.database.TumanDatabase
import ru.itmo.tuman.core.database.entity.DeckEntity
import ru.itmo.tuman.core.database.entity.DeckSource
import ru.itmo.tuman.features.decks.domain.DecksRepository
import ru.itmo.tuman.features.decks.domain.model.Card
import ru.itmo.tuman.features.decks.domain.model.CatalogSet
import ru.itmo.tuman.features.decks.domain.model.Deck
import ru.itmo.tuman.srs.DueCards
import java.time.Instant
import java.time.ZoneId

/**
 * Реализация [DecksRepository] через базу Room.
 *
 * @param now откуда брать текущее время (мс UTC). По умолчанию — системные часы;
 *   в тестах подставляем фиксированное время, чтобы результат не зависел от часов.
 */
class DecksRepositoryImpl(
    private val db: TumanDatabase,
    private val now: () -> Long = System::currentTimeMillis,
) : DecksRepository {

    private val deckDao = db.deckDao()
    private val cardDao = db.cardDao()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeDecks(): Flow<List<Deck>> = flow {
        while (true) {
            val currentTime = now()
            val nextDay = DueCards.startOfNextDay(Instant.ofEpochMilli(currentTime), ZoneId.systemDefault())
            emit(nextDay.toEpochMilli())
            delay(nextDay.toEpochMilli() - currentTime)
        }
    }.flatMapLatest { dueBefore ->
        deckDao.observeAllWithCardCount(dueBefore).map { rows -> rows.map { it.toDomain() } }
    }

    override suspend fun createDeck(title: String): Long =
        deckDao.insert(DeckEntity(title = title.trim(), createdAt = now()))

    override suspend fun renameDeck(id: Long, title: String) =
        deckDao.rename(id, title.trim())

    override suspend fun deleteDeck(id: Long) =
        deckDao.deleteById(id)

    override fun observeCards(deckId: Long): Flow<List<Card>> =
        cardDao.observeByDeckWithReviewCount(deckId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getCard(id: Long): Card? =
        cardDao.getByIdWithReviewCount(id)?.toDomain()

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

    override suspend fun importCatalogSet(set: CatalogSet): Long =
        // Транзакция: или набор сохранится целиком, или не изменится ничего
        db.withTransaction {
            val existing = deckDao.getByRemoteId(set.remoteId)
            when {
                existing == null -> insertCatalogSet(set)
                (existing.remoteVersion ?: 0) >= set.version -> existing.id
                else -> updateCatalogSet(existing.id, set)
            }
        }

    override fun observeDownloadedVersions(): Flow<Map<String, Int>> =
        deckDao.observeDownloadedVersions().map { rows -> rows.associate { it.remoteId to it.version } }

    /** Новый набор: создаём его и все карточки. Карточки сразу попадают в список «на сегодня». */
    private suspend fun insertCatalogSet(set: CatalogSet): Long {
        val createdAt = now()
        val deckId = deckDao.insert(
            DeckEntity(
                title = set.title,
                source = DeckSource.CATALOG,
                createdAt = createdAt,
                description = set.description,
                remoteId = set.remoteId,
                remoteVersion = set.version,
            ),
        )
        cardDao.insertAll(set.cards.map { it.toNewEntity(deckId, createdAt) })
        return deckId
    }

    /**
     * Новая версия набора. Карточки сопоставляем по `remote_id`:
     * найденным меняем только текст (прогресс и история остаются), новые добавляем,
     * пропавшие из набора удаляем.
     */
    private suspend fun updateCatalogSet(deckId: Long, set: CatalogSet): Long {
        val oldByRemoteId = cardDao.getByDeck(deckId).filter { it.remoteId != null }.associateBy { it.remoteId!! }
        val newRemoteIds = set.cards.map { it.remoteId }.toSet()

        set.cards.forEach { card ->
            val old = oldByRemoteId[card.remoteId]
            if (old == null) {
                cardDao.insert(card.toNewEntity(deckId, now()))
            } else {
                cardDao.updateCatalogText(
                    id = old.id,
                    front = card.front,
                    back = card.back,
                    example = card.example,
                    exampleTranslation = card.exampleTranslation,
                    pronunciation = card.pronunciation,
                    notes = card.notes,
                )
            }
        }
        val removed = oldByRemoteId.filterKeys { it !in newRemoteIds }.values.map { it.id }
        if (removed.isNotEmpty()) cardDao.deleteByIds(removed)

        deckDao.updateCatalogInfo(deckId, set.title, set.description, set.version)
        return deckId
    }
}
