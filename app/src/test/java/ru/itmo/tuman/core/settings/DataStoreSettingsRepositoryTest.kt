package ru.itmo.tuman.core.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreSettingsRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    /** Открывает настройки из файла [file]. Закрыть — отменить [scope]. */
    private fun open(file: File, scope: CoroutineScope) =
        DataStoreSettingsRepository(PreferenceDataStoreFactory.create(scope = scope) { file })

    @Test
    fun defaultsWhenNothingSaved() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + SupervisorJob())
        val settings = open(File(folder.root, "a.preferences_pb"), scope).settings.first()

        assertEquals(UserSettings(), settings)
        assertEquals(20, settings.dailyGoal)
        assertEquals(LocalTime.of(19, 0), settings.reminderTime)
        scope.cancel()
    }

    /** «После перезапуска приложения цель на месте»: закрыли хранилище, открыли заново тот же файл. */
    @Test
    fun valuesSurviveRestart() = runTest {
        val file = File(folder.root, "b.preferences_pb")
        val firstRun = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + SupervisorJob())
        open(file, firstRun).apply {
            setDailyGoal(30)
            setOnboardingDone()
            setRatingHintShown()
            setReminder(enabled = false, time = LocalTime.of(8, 30))
        }
        firstRun.cancel()

        val secondRun = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + SupervisorJob())
        val restored = open(file, secondRun).settings.first()

        assertEquals(
            UserSettings(
                dailyGoal = 30,
                onboardingDone = true,
                ratingHintShown = true,
                reminderEnabled = false,
                reminderTime = LocalTime.of(8, 30),
            ),
            restored,
        )
        secondRun.cancel()
    }
}
