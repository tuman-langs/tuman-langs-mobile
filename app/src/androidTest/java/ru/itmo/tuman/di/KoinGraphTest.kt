package ru.itmo.tuman.di

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import ru.itmo.tuman.core.settings.SettingsRepository
import ru.itmo.tuman.features.progress.domain.ProgressRepository
import ru.itmo.tuman.features.review.domain.ReviewRepository

/**
 * Koin запускается в `TumanApp` и при тестах на эмуляторе. Проверяем, что настройки,
 * прогресс и повторение действительно создаются — до того, как их подключат экраны.
 */
@RunWith(AndroidJUnit4::class)
class KoinGraphTest {

    private val koin get() = GlobalContext.get()

    @Test
    fun settingsProgressAndReviewAreCreated() = runBlocking {
        val goal = koin.get<SettingsRepository>().settings.first().dailyGoal
        val progress = koin.get<ProgressRepository>().observeProgress().first()
        koin.get<ReviewRepository>().observeTodayCount().first()

        assertEquals(goal, progress.dailyGoal)
    }
}
