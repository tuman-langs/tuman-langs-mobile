package ru.itmo.tuman.core.navigation

import kotlinx.serialization.Serializable

// Маршруты экранов. Каждый маршрут — класс с @Serializable: Navigation Compose
// сам превращает его в адрес экрана, а параметры (например deckId) проверяет компилятор.

@Serializable
object HomeRoute

@Serializable
object DecksRoute

@Serializable
data class DeckRoute(val deckId: Long)

/** Редактор карточки. cardId = [NEW_CARD] — создаём новую карточку. */
@Serializable
data class CardEditRoute(val deckId: Long, val cardId: Long = NEW_CARD) {
    companion object {
        const val NEW_CARD = -1L
    }
}

@Serializable
object CatalogRoute

@Serializable
object ReviewRoute

@Serializable
object ProgressRoute

@Serializable
object SettingsRoute
