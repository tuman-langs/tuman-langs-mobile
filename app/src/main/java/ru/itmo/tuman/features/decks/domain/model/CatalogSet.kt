package ru.itmo.tuman.features.decks.domain.model

/**
 * Набор, скачанный из каталога, — то, что приходит с сервера и сохраняется в базу.
 * Поля как в `content/README.md` (tuman-langs-backend, формат утверждён в backend#2).
 *
 * @property remoteId id набора в каталоге, например `ba-letters`.
 * @property version версия набора: контент-лид поднимает её при любом изменении.
 */
data class CatalogSet(
    val remoteId: String,
    val version: Int,
    val title: String,
    val description: String? = null,
    val cards: List<CatalogCard>,
)

/**
 * Карточка набора из каталога.
 *
 * @property remoteId id карточки в каталоге (`ba-letters-0001`). Не меняется после публикации,
 *   поэтому по нему при обновлении набора находим ту же карточку и сохраняем её прогресс.
 */
data class CatalogCard(
    val remoteId: String,
    val front: String,
    val back: String,
    val example: String? = null,
    val exampleTranslation: String? = null,
    val pronunciation: String? = null,
    val notes: String? = null,
)
