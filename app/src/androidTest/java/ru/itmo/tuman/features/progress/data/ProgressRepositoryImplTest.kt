package ru.itmo.tuman.features.progress.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.itmo.tuman.core.database.TumanDatabase
import ru.itmo.tuman.core.database.entity.CardEntity
import ru.itmo.tuman.core.database.entity.DeckEntity
import ru.itmo.tuman.core.database.entity.ReviewEntity
import ru.itmo.tuman.core.settings.SettingsRepository
import ru.itmo.tuman.core.settings.UserSettings
import ru.itmo.tuman.features.progress.domain.model.AchievementType
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset

/** Прогресс (#30) на настоящей базе Room в памяти (эмулятор). Время зафиксировано, пояс UTC. */
@RunWith(AndroidJUnit4::class)
class ProgressRepositoryImplTest {

    private lateinit var db: TumanDatabase
    private val settings = FakeSettings()
    private lateinit var repository: ProgressRepositoryImpl

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, TumanDatabase::class.java).build()
        repository = ProgressRepositoryImpl(db.progressDao(), settings, clock = { NOW }, zone = { ZoneOffset.UTC })
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun progressIsCalculatedFromDatabaseAndUpdatesItself() = runBlocking {
        settings.setDailyGoal(2)
        val deckId = db.deckDao().insert(DeckEntity(title = "Набор", createdAt = NOW_MS))
        // Выученная карточка: интервал 30 дней; и обычная: интервал 2 дня
        val learned = db.cardDao().insert(CardEntity(deckId = deckId, front = "ҡош", back = "т", due = NOW_MS + 30 * DAY, lastReview = NOW_MS))
        val fresh = db.cardDao().insert(CardEntity(deckId = deckId, front = "һыу", back = "т", due = NOW_MS + 2 * DAY, lastReview = NOW_MS))
        // Вчера два ответа (цель выполнена), сегодня один
        db.reviewDao().insert(ReviewEntity(cardId = learned, rating = 4, reviewedAt = NOW_MS - DAY, intervalDays = 30.0))
        db.reviewDao().insert(ReviewEntity(cardId = fresh, rating = 1, reviewedAt = NOW_MS - DAY, intervalDays = 1.0))
        db.reviewDao().insert(ReviewEntity(cardId = fresh, rating = 3, reviewedAt = NOW_MS, intervalDays = 2.0))

        val progress = repository.observeProgress().first()

        assertEquals(1, progress.todayAnswers)
        assertEquals(2, progress.dailyGoal)
        assertEquals(1, progress.currentStreak) // сегодня ещё не выполнено, но серия не обнулилась
        assertEquals(10 + 2 + 10, progress.xp)
        assertEquals(1, progress.learnedCards)
        assertEquals(2.0 / 3, progress.accuracy!!, 1e-9)
        assertTrue(progress.achievements.single { it.type == AchievementType.FIRST_DECK }.isUnlocked)

        // Повторный ответ по той же карточке сегодня (короткий шаг) в зачёт не идёт
        db.reviewDao().insert(ReviewEntity(cardId = fresh, rating = 2, reviewedAt = NOW_MS + 1000, intervalDays = 2.0))
        val repeated = repository.observeProgress().first()
        assertEquals(1, repeated.todayAnswers)
        assertEquals(10 + 2 + 10, repeated.xp)

        // Ответ по другой карточке сегодня — цель выполнена, серия 2, пересчёт без перезапуска
        db.reviewDao().insert(ReviewEntity(cardId = learned, rating = 2, reviewedAt = NOW_MS + 2000, intervalDays = 2.0))
        val updated = repository.observeProgress().first()
        assertEquals(2, updated.todayAnswers)
        assertEquals(2, updated.currentStreak)
    }

    /** Настройки в памяти — для теста не нужен файл DataStore. */
    private class FakeSettings : SettingsRepository {
        private val state = MutableStateFlow(UserSettings())
        override val settings = state
        override suspend fun setDailyGoal(goal: Int) = state.update { it.copy(dailyGoal = goal) }
        override suspend fun setOnboardingDone() = state.update { it.copy(onboardingDone = true) }
        override suspend fun setRatingHintShown() = state.update { it.copy(ratingHintShown = true) }
        override suspend fun setReminder(enabled: Boolean, time: LocalTime) =
            state.update { it.copy(reminderEnabled = enabled, reminderTime = time) }
    }

    private companion object {
        val NOW: Instant = Instant.parse("2026-11-18T12:00:00Z")
        val NOW_MS = NOW.toEpochMilli()
        const val DAY = 24 * 60 * 60 * 1000L
    }
}
