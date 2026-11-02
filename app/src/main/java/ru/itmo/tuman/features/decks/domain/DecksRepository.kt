package ru.itmo.tuman.features.decks.domain

import kotlinx.coroutines.flow.Flow
import ru.itmo.tuman.features.decks.domain.model.Card
import ru.itmo.tuman.features.decks.domain.model.Deck

/**
 * Всё, что экраны наборов и карточек могут попросить у хранилища.
 *
 * Это интерфейс: экран знает только «что можно сделать», а не «как».
 * Настоящая реализация — `DecksRepositoryImpl` (через Room), для тестов экранов
 * можно подставить фейковую в памяти.
 *
 * `Flow` — подписка: при любом изменении данных экран сам получит новый список.
 * `suspend` — действие выполняется один раз и не подвешивает интерфейс.
 */
interface DecksRepository {

    /** Все наборы с числом карточек, новые сверху. */
    fun observeDecks(): Flow<List<Deck>>

    /** Создаёт пустой набор и возвращает его id. */
    suspend fun createDeck(title: String): Long

    suspend fun renameDeck(id: Long, title: String)

    /** Удаляет набор вместе с его карточками и историей ответов по ним. */
    suspend fun deleteDeck(id: Long)

    /** Карточки набора в порядке добавления. */
    fun observeCards(deckId: Long): Flow<List<Card>>

    /** Одна карточка — для редактора. `null`, если такой нет. */
    suspend fun getCard(id: Long): Card?

    /**
     * Сохраняет карточку: если `card.id == 0` — создаёт новую, иначе изменяет текст существующей.
     * Возвращает id карточки.
     */
    suspend fun saveCard(card: Card): Long

    /** Удаляет карточку вместе с её историей ответов. */
    suspend fun deleteCard(id: Long)
}
