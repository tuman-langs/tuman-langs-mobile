package ru.itmo.tuman.features.progress.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.itmo.tuman.core.settings.SettingsRepository
import ru.itmo.tuman.core.settings.UserSettings
import ru.itmo.tuman.features.progress.domain.ProgressRepository
import ru.itmo.tuman.features.progress.domain.model.Progress
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun newUserStateShowsEmptyProgress() {
        val progress = progress(totalAnswers = 0)
        val viewModel = ProgressViewModel(FakeProgressRepository(progress), FakeSettingsRepository())

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(progress, viewModel.uiState.value.progress)
        assertNull(viewModel.uiState.value.errorRes)
    }

    @Test
    fun existingProgressIsExposed() {
        val progress = progress(totalAnswers = 25, todayAnswers = 8, dailyGoal = 20)
        val viewModel = ProgressViewModel(FakeProgressRepository(progress), FakeSettingsRepository())

        assertEquals(25, viewModel.uiState.value.progress?.totalAnswers)
        assertEquals(8, viewModel.uiState.value.progress?.todayAnswers)
        assertFalse(viewModel.uiState.value.progress?.isGoalDone ?: true)
    }

    @Test
    fun changingGoalUpdatesSettingsRepository() {
        val settings = FakeSettingsRepository()
        val viewModel = ProgressViewModel(
            FakeProgressRepository(progress(totalAnswers = 25)),
            settings,
        )

        viewModel.onDailyGoalChanged(50)

        assertEquals(50, settings.current.dailyGoal)
    }

    private fun progress(
        totalAnswers: Int,
        todayAnswers: Int = 0,
        dailyGoal: Int = 20,
    ) = Progress(
        todayAnswers = todayAnswers,
        dailyGoal = dailyGoal,
        currentStreak = 0,
        bestStreak = 0,
        lastWeek = emptyList(),
        xp = 0,
        learnedCards = 0,
        accuracy = if (totalAnswers == 0) null else 0.8,
        totalAnswers = totalAnswers,
        achievements = emptyList(),
    )

    private class FakeProgressRepository(progress: Progress) : ProgressRepository {
        private val flow = MutableStateFlow(progress)
        override fun observeProgress(): Flow<Progress> = flow
    }

    private class FakeSettingsRepository : SettingsRepository {
        var current = UserSettings()
        override val settings = MutableStateFlow(current)

        override suspend fun setDailyGoal(goal: Int) {
            current = current.copy(dailyGoal = goal)
            (settings as MutableStateFlow<UserSettings>).value = current
        }

        override suspend fun setOnboardingDone() = Unit
        override suspend fun setRatingHintShown() = Unit
        override suspend fun setReminder(enabled: Boolean, time: LocalTime) {
            current = current.copy(reminderEnabled = enabled, reminderTime = time)
        }
    }
}
