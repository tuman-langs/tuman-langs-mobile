package ru.itmo.tuman.features.home.ui

import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.itmo.tuman.core.settings.SettingsRepository
import ru.itmo.tuman.core.settings.UserSettings
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val settings = FakeSettings()
    private val today = MutableStateFlow(HomeToday(due = 12, new = 6))
    private val progress = MutableStateFlow(HomeProgress(answers = 8, goal = 20, streak = 6, xp = 520))
    private val decks = MutableStateFlow(listOf(HomeDeck(1, "Базовые слова", 40)))

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun firstLaunchShowsGoalPickerAndPersistsTheSelection() = runTest {
        val vm = start()
        assertEquals(HomeUiState.Onboarding(20, saving = false, saveFailed = false), vm.uiState.value)
        vm.selectGoal(30)
        runCurrent()
        assertEquals(30, (vm.uiState.value as HomeUiState.Onboarding).goal)
        assertFalse(settings.settings.value.onboardingDone)

        vm.completeOnboarding()
        runCurrent()

        assertEquals(30, settings.settings.value.dailyGoal)
        assertTrue(settings.settings.value.onboardingDone)
        assertEquals(HomeUiState.Section.Today, vm.dashboard().section)
    }

    @Test
    fun dashboardShowsCountsGoalStreakXpAndDecks() = runTest {
        settings.settings.update { it.copy(onboardingDone = true) }
        val dashboard = start().dashboard()

        assertEquals(HomeUiState.Section.Today, dashboard.section)
        assertEquals(18, dashboard.today.total)
        assertEquals(6, dashboard.today.minutes)
        assertEquals(12, dashboard.progress.remaining)
        assertEquals(6, dashboard.progress.streak)
        assertEquals(520, dashboard.progress.xp)
        assertEquals(decks.value, dashboard.decks)
    }

    @Test
    fun emptyQueueUpdatesTheDashboardToDone() = runTest {
        settings.settings.update { it.copy(onboardingDone = true) }
        val vm = start()
        today.value = HomeToday(due = 0, new = 0)
        progress.value = progress.value.copy(answers = 20)
        runCurrent()

        assertEquals(HomeUiState.Section.Done, vm.dashboard().section)
        assertEquals(0, vm.dashboard().progress.remaining)
    }

    @Test
    fun noDecksTakesPriorityOverTheEmptyQueue() = runTest {
        settings.settings.update { it.copy(onboardingDone = true) }
        decks.value = emptyList()
        today.value = HomeToday(due = 0, new = 0)

        assertEquals(HomeUiState.Section.NoDecks, start().dashboard().section)
    }

    private fun TestScope.start(): HomeViewModel {
        val vm = HomeViewModel(settings, { today }, { progress }, { decks })
        store.put("home", vm)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect() }
        runCurrent()
        return vm
    }

    private fun HomeViewModel.dashboard() = uiState.value as HomeUiState.Dashboard

    private class FakeSettings : SettingsRepository {
        override val settings = MutableStateFlow(UserSettings())

        override suspend fun setDailyGoal(goal: Int) {
            settings.update { it.copy(dailyGoal = goal) }
        }

        override suspend fun setOnboardingDone() {
            settings.update { it.copy(onboardingDone = true) }
        }

        override suspend fun setRatingHintShown() = Unit
        override suspend fun setReminder(enabled: Boolean, time: LocalTime) = Unit
    }
}
