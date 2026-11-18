package ru.itmo.tuman.core.reminders

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import ru.itmo.tuman.core.settings.SettingsRepository
import ru.itmo.tuman.core.settings.UserSettings
import java.time.LocalTime

/**
 * Что делает напоминание, когда WorkManager его запускает (#32).
 * Числа и настройки подменяем в Koin на тестовые, после теста возвращаем настоящие.
 */
@RunWith(AndroidJUnit4::class)
class ReminderWorkerTest {

    private lateinit var context: Context
    private val settings = FakeSettingsRepository(UserSettings(reminderEnabled = true))
    private var info = ReminderInfo(cardsToday = 0, streakDays = 0)

    private lateinit var realSettings: SettingsRepository
    private lateinit var realInfoSource: ReminderInfoSource

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .grantRuntimePermission(context.packageName, Manifest.permission.POST_NOTIFICATIONS)
        notificationManager().cancelAll()

        val koin = GlobalContext.get()
        realSettings = koin.get()
        realInfoSource = koin.get()
        loadKoinModules(
            module {
                single<SettingsRepository> { settings }
                single<ReminderInfoSource> { ReminderInfoSource { info } }
            },
        )
    }

    @After
    fun tearDown() {
        notificationManager().cancelAll()
        loadKoinModules(
            module {
                single { realSettings }
                single { realInfoSource }
            },
        )
    }

    private fun notificationManager() = context.getSystemService(NotificationManager::class.java)

    private fun runWorker(): ListenableWorker.Result = runBlocking {
        TestListenableWorkerBuilder<ReminderWorker>(context).build().doWork()
    }

    private fun currentText(): String? = notificationManager().activeNotifications
        .singleOrNull { it.id == ReminderNotifications.NOTIFICATION_ID }
        ?.notification?.extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()

    /** Система добавляет уведомление не мгновенно — ждём до 3 секунд. */
    private fun shownText(): String? {
        repeat(30) {
            currentText()?.let { return it }
            Thread.sleep(100)
        }
        return null
    }

    @Test
    fun cardsWaitingShowsNotificationWithCountAndStreak() {
        info = ReminderInfo(cardsToday = 3, streakDays = 5)

        assertEquals(ListenableWorker.Result.success(), runWorker())

        assertEquals("3 карточки ждут повторения. Не прерывай серию — 5 дней! 🔥", shownText())
    }

    @Test
    fun noStreakYetShowsStartText() {
        info = ReminderInfo(cardsToday = 1, streakDays = 0)

        runWorker()

        assertEquals("1 карточка ждёт повторения. Начни серию сегодня! 🔥", shownText())
    }

    @Test
    fun noCardsMeansNoNotification() {
        info = ReminderInfo(cardsToday = 0, streakDays = 4)

        assertEquals(ListenableWorker.Result.success(), runWorker())

        assertEquals(null, shownText())
    }

    @Test
    fun disabledReminderShowsNothing() = runBlocking {
        settings.setReminder(enabled = false, time = LocalTime.of(19, 0))
        info = ReminderInfo(cardsToday = 10, streakDays = 1)

        runWorker()

        assertTrue(notificationManager().activeNotifications.none { it.id == ReminderNotifications.NOTIFICATION_ID })
    }
}
