package ru.itmo.tuman.features.settings.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.itmo.tuman.core.reminders.ReminderScheduler
import ru.itmo.tuman.core.settings.SettingsRepository
import ru.itmo.tuman.core.settings.UserSettings
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun enablingReminderSchedulesSelectedTime() {
        val settings = FakeSettingsRepository(UserSettings(reminderEnabled = false, reminderTime = LocalTime.of(8, 0)))
        val scheduler = FakeReminderScheduler(hasPermission = true)
        val viewModel = SettingsViewModel(settings, scheduler)

        viewModel.setEnabled(true)

        assertTrue(viewModel.uiState.value.enabled)
        assertEquals(LocalTime.of(8, 0), scheduler.enabledAt)
    }

    @Test
    fun changingTimeReschedulesWhenEnabled() {
        val settings = FakeSettingsRepository(UserSettings(reminderEnabled = true, reminderTime = LocalTime.of(19, 0)))
        val scheduler = FakeReminderScheduler(hasPermission = true)
        val viewModel = SettingsViewModel(settings, scheduler)

        viewModel.setTime(LocalTime.of(21, 30))

        assertEquals(LocalTime.of(21, 30), scheduler.enabledAt)
    }

    @Test
    fun disablingReminderCancelsScheduleAndKeepsTime() {
        val settings = FakeSettingsRepository(UserSettings(reminderEnabled = true, reminderTime = LocalTime.of(12, 0)))
        val scheduler = FakeReminderScheduler(hasPermission = true)
        val viewModel = SettingsViewModel(settings, scheduler)

        viewModel.setEnabled(false)

        assertFalse(viewModel.uiState.value.enabled)
        assertTrue(scheduler.disabled)
    }

    @Test
    fun permissionDeniedIsReflectedInUi() {
        val settings = FakeSettingsRepository()
        val scheduler = FakeReminderScheduler(hasPermission = false)
        val viewModel = SettingsViewModel(settings, scheduler)

        viewModel.onPermissionResult(granted = false)

        assertFalse(viewModel.uiState.value.hasPermission)
    }

    private class FakeSettingsRepository(initial: UserSettings = UserSettings()) : SettingsRepository {
        var current = initial
        private val flow = MutableStateFlow(current)
        override val settings = flow

        override suspend fun setDailyGoal(goal: Int) = Unit
        override suspend fun setOnboardingDone() = Unit
        override suspend fun setRatingHintShown() = Unit
        override suspend fun setReminder(enabled: Boolean, time: LocalTime) {
            current = current.copy(reminderEnabled = enabled, reminderTime = time)
            flow.value = current
        }
    }

    private class FakeReminderScheduler(private var hasPermission: Boolean) : ReminderScheduler {
        var enabledAt: LocalTime? = null
        var disabled = false

        override suspend fun enable(time: LocalTime) {
            enabledAt = time
            disabled = false
        }

        override suspend fun disable() {
            disabled = true
        }

        override suspend fun sync() = Unit
        override fun hasPermission(): Boolean = hasPermission
    }
}
