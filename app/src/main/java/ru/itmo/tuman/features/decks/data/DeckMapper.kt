package ru.itmo.tuman.features.decks.data

import ru.itmo.tuman.core.database.entity.CardEntity
import ru.itmo.tuman.core.database.entity.CardWithReviewCount
import ru.itmo.tuman.core.database.entity.DeckSource
import ru.itmo.tuman.core.database.entity.DeckWithCardCount
import ru.itmo.tuman.features.decks.domain.model.Card
import ru.itmo.tuman.features.decks.domain.model.Deck

// Перевод «строка из базы» ⇄ «модель для экранов».
// Благодаря этим функциям классы Room не выходят за пределы слоя data.

fun DeckWithCardCount.toDomain(): Deck = Deck(
    id = deck.id,
    title = deck.title,
    cardCount = cardCount,
    isFromCatalog = deck.source == DeckSource.CATALOG,
)

fun CardWithReviewCount.toDomain(): Card = Card(
    id = card.id,
    deckId = card.deckId,
    front = card.front,
    back = card.back,
    example = card.example,
    due = card.due,
    lastReview = card.lastReview,
    reviewCount = reviewCount,
)

/**
 * Новая карточка для вставки в базу. Поля алгоритма получают начальные значения,
 * а [now] — время создания: карточка сразу попадает в список «на сегодня».
 */
fun Card.toNewEntity(now: Long): CardEntity = CardEntity(
    deckId = deckId,
    front = front,
    back = back,
    example = example?.takeIf { it.isNotBlank() },
    due = now,
)
