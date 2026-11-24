package ru.itmo.tuman.features.progress.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Всё для экранов прогресса, итога сессии и главного (ФТ-8, ФТ-9, ФТ-10).
 * Правила расчёта — `docs/motivation.md`.
 *
 * @property todayAnswers сколько разных карточек повторено сегодня.
 * @property dailyGoal дневная цель из настроек.
 * @property currentStreak текущая серия дней; сегодняшний невыполненный день её не обнуляет.
 * @property bestStreak самая длинная серия за всё время.
 * @property lastWeek последние 7 дней, от самого старого к сегодняшнему, — для полосы недели.
 * @property learnedCards сколько карточек выучено (интервал от 21 дня, правило из core-srs).
 * @property accuracy доля верных ответов от 0 до 1; `null`, пока ответов нет.
 * @property totalAnswers сколько ответов в зачёте: первый ответ по карточке за день.
 * @property achievements все 6 достижений с прогрессом.
 */
data class Progress(
    val todayAnswers: Int,
    val dailyGoal: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val lastWeek: List<DayProgress>,
    val xp: Int,
    val learnedCards: Int,
    val accuracy: Double?,
    val totalAnswers: Int,
    val achievements: List<Achievement>,
) {
    /** Цель на сегодня выполнена. */
    val isGoalDone: Boolean get() = todayAnswers >= dailyGoal
}

/** Один день в полосе недели: дата и выполнена ли в этот день цель. */
data class DayProgress(
    val date: LocalDate,
    val answers: Int,
    val isGoalDone: Boolean,
)

/** Один ответ из истории: какая карточка, оценка 1–4 (как `Rating.grade`) и когда он дан. */
data class Answer(
    val cardId: Long,
    val grade: Int,
    val at: Instant,
)
