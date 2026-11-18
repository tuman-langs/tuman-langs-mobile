package ru.itmo.tuman.core.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalTime

/**
 * Настройки на DataStore. Тест на эмуляторе, а не юнит-тест: на Windows DataStore
 * не может переименовать временный файл при записи («Unable to rename … .tmp»),
 * и `./gradlew test` падал бы у всех, кто работает на Windows. На телефоне такой проблемы нет.
 */
@RunWith(AndroidJUnit4::class)
class DataStoreSettingsRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    /** Открывает настройки из файла [file]. Закрыть — отменить [scope]. */
    private fun open(file: File, scope: CoroutineScope) =
        DataStoreSettingsRepository(PreferenceDataStoreFactory.create(scope = scope) { file })

    private fun newScope() = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Test
    fun defaultsWhenNothingSaved() = runBlocking {
        val scope = newScope()
        val settings = open(File(folder.root, "a.preferences_pb"), scope).settings.first()

        assertEquals(UserSettings(), settings)
        assertEquals(20, settings.dailyGoal)
        assertEquals(LocalTime.of(19, 0), settings.reminderTime)
        scope.cancel()
    }

    /** «После перезапуска приложения цель на месте»: закрыли хранилище, открыли заново тот же файл. */
    @Test
    fun valuesSurviveRestart() = runBlocking {
        val file = File(folder.root, "b.preferences_pb")
        val firstRun = newScope()
        open(file, firstRun).apply {
            setDailyGoal(30)
            setOnboardingDone()
            setRatingHintShown()
            setReminder(enabled = false, time = LocalTime.of(8, 30))
        }
        firstRun.cancel()

        val secondRun = newScope()
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
