package ru.itmo.tuman.features.review.domain.model

import java.time.Instant

/**
 * Карточка для экрана повторения.
 *
 * Своя модель, а не `Card` из `features/decks`: по правилам архитектуры
 * одна функция не импортирует другую.
 */
data class ReviewCard(
    val id: Long,
    val front: String,
    val back: String,
    val example: String? = null,
    val deckTitle: String = "",
    val learningDue: Instant? = null,
)
