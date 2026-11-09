package ru.itmo.tuman.features.review.domain.model

/**
 * Сколько карточек ждёт сегодня — для главного экрана: «12 на повторение · 6 новых».
 * Числа совпадают со списком, который получит экран повторения.
 *
 * @property due карточки, которые уже повторяли и которым пора снова.
 * @property new карточки, на которые ещё ни разу не отвечали.
 */
data class TodayCount(
    val due: Int,
    val new: Int,
)
