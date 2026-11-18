package ru.itmo.tuman.features.progress.domain.model

/**
 * Шесть достижений из макета (ФТ-9). [target] — сколько нужно набрать, чтобы открыть.
 * Условия — `docs/motivation.md`.
 */
enum class AchievementType(val target: Int) {
    /** Есть хотя бы один набор — создан или скачан. */
    FIRST_DECK(1),

    /** Дано 10 ответов. */
    FIRST_10_CARDS(10),

    /** Лучшая серия — от 3 дней. */
    STREAK_3_DAYS(3),

    /** Лучшая серия — от 7 дней. */
    WEEK_NO_GAPS(7),

    /** Выучено 100 карточек. */
    LEARNED_100(100),

    /** Набрано 1000 XP. */
    XP_1000(1000),
}

/**
 * Достижение и продвижение к нему.
 *
 * @property current сколько уже набрано, не больше [AchievementType.target] — для подписи «6/7».
 */
data class Achievement(
    val type: AchievementType,
    val current: Int,
) {
    val target: Int get() = type.target
    val isUnlocked: Boolean get() = current >= target

    /** Сколько осталось до открытия; 0 — уже открыто. */
    val remaining: Int get() = (target - current).coerceAtLeast(0)
}
