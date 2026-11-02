package ru.itmo.tuman.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Таблица `cards` — карточка внутри набора (ФТ-2).
 *
 * Последние четыре поля нужны алгоритму повторения из `core-srs`
 * (те же, что в `CardState`, согласованы с Дворак М. в задаче #5):
 * - [stability] — стабильность в днях, у новой карточки 1.0;
 * - [difficulty] — сложность от 1 до 10, у новой 5.0;
 * - [due] — когда показать в следующий раз; у новой — время создания,
 *   поэтому она сразу попадает в список «на сегодня»;
 * - [lastReview] — время последнего ответа; `null` значит «карточка новая».
 *
 * Карточка принадлежит набору ([deckId]). Удалили набор — Room сам удалит
 * его карточки (`onDelete = CASCADE`).
 *
 * У карточек из каталога есть ещё [remoteId] — id из файла набора (`ba-letters-0001`).
 * Он не меняется после публикации, поэтому при обновлении набора по нему находим
 * ту же карточку и сохраняем её прогресс и историю. У своих карточек — `null`.
 * [exampleTranslation], [pronunciation], [notes] тоже приходят из каталога и необязательны.
 */
@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deck_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("deck_id"),
        // По due отбирается список «на сегодня», индекс ускоряет этот запрос
        Index("due"),
        // id карточки уникален внутри набора каталога
        Index(value = ["deck_id", "remote_id"], unique = true),
    ],
)
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "deck_id")
    val deckId: Long,
    val front: String,
    val back: String,
    /** Пример употребления, необязательный. */
    val example: String? = null,
    @ColumnInfo(name = "example_translation")
    val exampleTranslation: String? = null,
    val pronunciation: String? = null,
    val notes: String? = null,
    @ColumnInfo(name = "remote_id")
    val remoteId: String? = null,
    val stability: Double = INITIAL_STABILITY_DAYS,
    val difficulty: Double = INITIAL_DIFFICULTY,
    /** Миллисекунды с 1970 года (UTC). */
    val due: Long,
    /** Миллисекунды с 1970 года (UTC) или `null`, если ответов ещё не было. */
    @ColumnInfo(name = "last_review")
    val lastReview: Long? = null,
) {
    companion object {
        // Совпадают с CardState.INITIAL_* в core-srs. Модуль :app пока не зависит
        // от :core-srs, поэтому значения повторены здесь.
        const val INITIAL_STABILITY_DAYS = 1.0
        const val INITIAL_DIFFICULTY = 5.0
    }
}
