package ru.itmo.tuman.core.reminders

import android.content.Context
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.itmo.tuman.core.reminders.WorkManagerReminderScheduler.Companion.WORK_NAME
import ru.itmo.tuman.core.settings.UserSettings
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/** Планирование напоминания (#32) на тестовом WorkManager. Сейчас — 18.11.2026 18:00 по Москве. */
@RunWith(AndroidJUnit4::class)
class ReminderSchedulerTest {

    private lateinit var context: Context
    private lateinit var workManager: WorkManager
    private val settings = FakeSettingsRepository()
    private lateinit var scheduler: WorkManagerReminderScheduler

    private val now = ZonedDateTime.of(2026, 11, 18, 18, 0, 0, 0, ZoneId.of("Europe/Moscow"))

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(context)
        scheduler = WorkManagerReminderScheduler(context, workManager, settings, now = { now })
    }

    private fun works(): List<WorkInfo> = workManager.getWorkInfosForUniqueWork(WORK_NAME).get()

    @Test
    fun enableSavesSettingsAndSchedulesDailyWork() = runBlocking {
        scheduler.enable(LocalTime.of(19, 0))

        assertEquals(UserSettings(reminderEnabled = true, reminderTime = LocalTime.of(19, 0)), settings.state.value)
        val work = works().single()
        assertEquals(WorkInfo.State.ENQUEUED, work.state)
        assertEquals(Duration.ofHours(1).toMillis(), work.initialDelayMillis)
        assertEquals(Duration.ofDays(1).toMillis(), work.periodicityInfo!!.repeatIntervalMillis)
    }

    @Test
    fun changingTimeReplacesWork() = runBlocking {
        scheduler.enable(LocalTime.of(19, 0))
        val first = works().single().id

        scheduler.enable(LocalTime.of(20, 30))

        val active = works().filter { it.state == WorkInfo.State.ENQUEUED }
        assertEquals(1, active.size)
        assertTrue("работа должна быть новой", active.single().id != first)
        assertEquals(Duration.ofMinutes(150).toMillis(), active.single().initialDelayMillis)
    }

    @Test
    fun disableCancelsWorkAndKeepsTime() = runBlocking {
        scheduler.enable(LocalTime.of(8, 15))

        scheduler.disable()

        assertTrue(works().all { it.state == WorkInfo.State.CANCELLED })
        assertFalse(settings.state.value.reminderEnabled)
        assertEquals(LocalTime.of(8, 15), settings.state.value.reminderTime)
    }

    @Test
    fun syncSchedulesWhenEnabledAndKeepsExisting() = runBlocking {
        scheduler.sync() // по умолчанию напоминание включено на 19:00
        val first = works().single()
        assertEquals(WorkInfo.State.ENQUEUED, first.state)

        scheduler.sync() // повторный запуск приложения не сдвигает расписание

        assertEquals(first.id, works().single().id)
    }

    @Test
    fun syncDoesNothingWhenDisabled() = runBlocking {
        settings.setReminder(enabled = false, time = LocalTime.of(19, 0))

        scheduler.sync()

        assertTrue(works().none { it.state == WorkInfo.State.ENQUEUED })
    }
}
