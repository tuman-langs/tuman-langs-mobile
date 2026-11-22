package ru.itmo.tuman

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import ru.itmo.tuman.core.database.TumanDatabase
import ru.itmo.tuman.core.settings.SettingsRepository
import ru.itmo.tuman.features.decks.domain.DecksRepository
import ru.itmo.tuman.features.decks.domain.model.Card
import ru.itmo.tuman.features.progress.domain.ProgressRepository
import ru.itmo.tuman.features.review.domain.ReviewRepository
import java.io.File
import java.time.LocalTime

/** Приёмочные проверки настоящих экранов и Room. Запускать на отдельном QA-эмуляторе:
 * перед каждым тестом очищается база установленной тестовой сборки.
 * Поэтому класс включается только явно: `-e qaAcceptance true` (без него все тесты пропускаются),
 * чтобы обычный connectedDebugAndroidTest не стёр наборы и прогресс на рабочем или демонстрационном устройстве. */
@RunWith(AndroidJUnit4::class)
class AcceptanceSmokeTest {
    @get:Rule val ui = createEmptyComposeRule()
    private val koin get() = GlobalContext.get()
    private val decks get() = koin.get<DecksRepository>()
    private val settings get() = koin.get<SettingsRepository>()
    private var activity: ActivityScenario<MainActivity>? = null
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val evidence get() = File(context.filesDir, "qa-evidence").apply { mkdirs() }

    @Before fun reset() = runBlocking {
        org.junit.Assume.assumeTrue(
            "Очищает базу приложения; включается аргументом qaAcceptance=true",
            InstrumentationRegistry.getArguments().getString("qaAcceptance") == "true",
        )
        koin.get<TumanDatabase>().clearAllTables()
        settings.setDailyGoal(10)
        settings.setOnboardingDone() // иначе после #33 откроется выбор цели, а не главный экран
        settings.setReminder(false, LocalTime.of(19, 0))
    }
    @After fun close() { activity?.close() }
    private fun launch() {
        activity = ActivityScenario.launch(MainActivity::class.java)
        waitText("Начать повторение")
    }
    private fun waitText(text: String) {
        ui.waitUntil(10_000) { ui.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
        ui.waitForIdle()
    }
    private fun click(text: String) {
        waitText(text)
        ui.onAllNodesWithText(text).onLast().performClick()
        ui.waitForIdle()
    }
    private fun desc(text: String) {
        ui.onNodeWithContentDescription(text).performClick()
        ui.waitForIdle()
    }
    private fun field(index: Int, text: String) {
        ui.onAllNodes(hasSetTextAction())[index].performTextReplacement(text)
    }
    private fun photo(name: String) {
        ui.waitForIdle()
        InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().also { bitmap ->
            File(evidence, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }

    @Test fun deckAndCardCrudWithUnicodeAndValidation() = runBlocking {
        launch()
        click("Наборы")
        waitText("Наборов пока нет")
        photo("empty-decks")
        click("Создать набор")
        field(0, "Тест 1")
        click("Сохранить")
        waitText("Тест 1")
        desc("Меню набора")
        click("Переименовать")
        field(0, "Тест 2")
        click("Сохранить")
        waitText("Тест 2")
        ui.onNodeWithText("Тест 1").assertDoesNotExist()
        click("Тест 2")
        waitText("В наборе нет карточек")
        click("Добавить карточку")
        click("Сохранить")
        ui.onAllNodesWithText("Заполните поле").assertCountEquals(2)
        photo("required-fields")
        field(0, "әөүғҡңҙҫһ")
        field(1, "ӘӨҮҒҠҢҘҪҺ")
        field(2, "")
        photo("unicode-editor")
        click("Сохранить")
        ui.waitUntil(5_000) { runBlocking { decks.observeDecks().first().single().cardCount == 1 } }
        field(0, "тест")
        field(1, "test")
        field(2, "пример 1")
        click("Сохранить")
        ui.waitUntil(5_000) { runBlocking { decks.observeDecks().first().single().cardCount == 2 } }
        desc("Назад")
        click("тест")
        field(1, "test 2")
        click("Сохранить")
        waitText("test 2")
        click("тест")
        desc("Удалить карточку")
        click("Удалить")
        waitText("әөүғҡңҙҫһ")
        ui.onNodeWithText("тест").assertDoesNotExist()
        val deck = decks.observeDecks().first().single()
        val saved = decks.observeCards(deck.id).first().single()
        assertNull(saved.example)
        click("әөүғҡңҙҫһ")
        field(2, "әөүғҡңҙҫһ ӘӨҮҒҠҢҘҪҺ")
        photo("unicode-editor")
        click("Сохранить")
        val card = decks.observeCards(deck.id).first().single()
        assertEquals("ӘӨҮҒҠҢҘҪҺ", card.back)
        assertEquals("әөүғҡңҙҫһ ӘӨҮҒҠҢҘҪҺ", card.example)
        photo("unicode-deck")
        desc("Назад")
        desc("Меню набора")
        click("Удалить набор")
        click("Отмена")
        waitText("Тест 2")
        desc("Меню набора")
        click("Удалить набор")
        click("Удалить")
        waitText("Наборов пока нет")
        assertTrue(decks.observeDecks().first().isEmpty())
    }

    @Test fun reviewRatingsPersistAndProgressMatchesHistory() = runBlocking {
        val deck = decks.createDeck("QA повторение")
        val ids = (1..10).map { decks.saveCard(Card(deckId = deck, front = "QA $it", back = "Ответ $it")) }
        launch()
        click("Начать повторение")
        waitText("QA 1")
        val timings = mutableListOf<Long>()
        for (index in 1..10) {
            click("Показать ответ")
            if (ui.onAllNodesWithText("Понятно").fetchSemanticsNodes().isNotEmpty()) click("Понятно")
            listOf("не помню", "трудно", "хорошо", "легко").forEach { ui.onNodeWithText(it).assertIsDisplayed() }
            val rating = if (index == 1) "не помню" else listOf("трудно", "хорошо", "легко")[(index - 2) % 3]
            val start = SystemClock.elapsedRealtime()
            click(rating)
            waitText(if (index < 10) "QA ${index + 1}" else "QA 1")
            timings += SystemClock.elapsedRealtime() - start
        }
        click("Показать ответ")
        click("хорошо")
        waitText("Сессия завершена")
        photo("review-summary")
        click("К прогрессу")
        waitText("77 XP")
        photo("progress")
        val progress = koin.get<ProgressRepository>().observeProgress().first()
        assertEquals(10, progress.totalAnswers)
        assertEquals(77, progress.xp)
        assertTrue(progress.isGoalDone)
        assertEquals(1, progress.currentStreak)
        assertEquals(6, progress.achievements.size)
        assertTrue(progress.achievements.count { it.isUnlocked } >= 2)
        val db = koin.get<TumanDatabase>()
        ids.forEach { assertEquals(1, db.reviewDao().getByCard(it).size) }
        val hard = decks.getCard(ids[1])!!
        val easy = decks.getCard(ids[3])!!
        assertTrue(hard.due!! < easy.due!!)
        assertTrue(koin.get<ReviewRepository>().observeTodayCards(deck).first().isEmpty())
        File(evidence, "review-timings-ms.txt").writeText(timings.joinToString(","))
        click("Изменить цель")
        click("20 карточек в день")
        assertEquals(20, settings.settings.first().dailyGoal)
        waitText("из 20")
        click("Все")
        waitText("Достижения")
        photo("achievements")
        activity!!.recreate()
        waitText("Достижения")
        assertEquals(77, koin.get<ProgressRepository>().observeProgress().first().xp)
    }

    @Test fun emptyProgressAndReminderToggle() = runBlocking {
        launch()
        click("Прогресс")
        waitText("Прогресс появится после первого повторения")
        ui.onNodeWithText("0 XP").assertIsDisplayed()
        photo("empty-progress")
        click("Главная")
        desc("Настройки")
        ui.onNode(isToggleable()).assertIsOff().performClick()
        ui.waitUntil(5_000) { runBlocking { settings.settings.first().reminderEnabled } }
        photo("reminder-on")
        ui.onNode(isToggleable()).assertIsOn().performClick()
        ui.waitUntil(5_000) { runBlocking { !settings.settings.first().reminderEnabled } }
        photo("reminder-off")
    }

    @Test fun activityLaunchReadyWithinFourSeconds() {
        val values = (1..3).map {
            val start = SystemClock.elapsedRealtime()
            launch()
            val elapsed = SystemClock.elapsedRealtime() - start
            activity!!.close()
            activity = null
            elapsed
        }
        File(evidence, "launch-ready-ms.txt").writeText(values.joinToString(","))
        assertTrue("Activity launch → UI ready (ms): $values", values.all { it <= 4_000 })
    }
    /** Сетевые/долгие проверки включаются явно аргументами runner, без внешней сети в обычном прогоне. */
    @Test fun publishedCatalogDownloadAndOfflineReview() = runBlocking {
        org.junit.Assume.assumeTrue(InstrumentationRegistry.getArguments().getString("qaNetwork") == "true")
        val catalog = koin.get<ru.itmo.tuman.features.catalog.domain.CatalogRepository>()
        val result = catalog.loadSets()
        assertTrue(result is ru.itmo.tuman.features.catalog.domain.CatalogResult.Success)
        val sets = (result as ru.itmo.tuman.features.catalog.domain.CatalogResult.Success).value
        assertTrue(sets.isNotEmpty())
        launch()
        click("Каталог")
        waitText(sets.first().title)
        photo("catalog-online")
        for (set in sets) {
            assertTrue(catalog.downloadSet(set.remoteId) is ru.itmo.tuman.features.catalog.domain.CatalogResult.Success)
        }
        val downloaded = decks.observeDecks().first()
        assertEquals(sets.size, downloaded.size)
        File(evidence, "published-catalog.txt").writeText(downloaded.joinToString("\n") { "${it.title}: ${it.cardCount}" })
        val inst = InstrumentationRegistry.getInstrumentation()
        fun shell(command: String) { android.os.ParcelFileDescriptor.AutoCloseInputStream(inst.uiAutomation.executeShellCommand(command)).use { it.readBytes() } }
        try {
            shell("svc wifi disable")
            shell("svc data disable")
            val internet = koin.get<ru.itmo.tuman.core.network.InternetAvailability>()
            val deadline = SystemClock.elapsedRealtime() + 5_000
            while (internet.hasInternet() && SystemClock.elapsedRealtime() < deadline) Thread.sleep(100)
            File(evidence, "offline-fixture.txt").writeText("validatedNetworkAfterDisable=${internet.hasInternet()}")
            org.junit.Assume.assumeFalse("Emulator retains a validated network after svc disable; offline fixture unavailable", internet.hasInternet())
            click("Наборы")
            click(sets.first().title)
            waitText("Учить")
            photo("downloaded-offline")
            click("Учить")
            waitText("Показать ответ")
            click("Показать ответ")
            if (ui.onAllNodesWithText("Понятно").fetchSemanticsNodes().isNotEmpty()) click("Понятно")
            photo("review-offline")
            click("легко")
            desc("Закрыть повторение")
            desc("Назад")
            click("Прогресс")
            waitText("10 XP")
            photo("progress-offline")
            click("Каталог")
            photo("catalog-offline-observed")
            File(evidence, "catalog-offline-tree.txt").writeText(ui.onRoot().printToString())
            waitText("Нет интернета")
            photo("catalog-offline")
            assertEquals(sets.size, decks.observeDecks().first().size)
        } finally {
            shell("svc wifi enable")
            shell("svc data enable")
            ui.waitUntil(15_000) { koin.get<ru.itmo.tuman.core.network.InternetAvailability>().hasInternet() }
        }
    }

    @Test fun realReminderArrivesThenIsCancelled() = runBlocking {
        org.junit.Assume.assumeTrue(InstrumentationRegistry.getArguments().getString("qaReminder") == "true")
        val manager = context.getSystemService(android.app.NotificationManager::class.java)
        manager.cancelAll()
        val deck = decks.createDeck("QA напоминание")
        decks.saveCard(Card(deckId = deck, front = "QA", back = "QA"))
        val next = LocalTime.now().plusMinutes(2).withSecond(0).withNano(0)
        settings.setReminder(false, next)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                context.packageName, android.Manifest.permission.POST_NOTIFICATIONS)
        }
        val scheduledAt = System.currentTimeMillis()
        launch()
        desc("Настройки")
        ui.onNode(isToggleable()).performClick()
        ui.waitUntil(5_000) { runBlocking { settings.settings.first().reminderEnabled } }
        photo("reminder-scheduled")
        activity!!.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
        val start = System.currentTimeMillis()
        var notification: android.service.notification.StatusBarNotification? = null
        val deadline = SystemClock.elapsedRealtime() + 150_000
        while (SystemClock.elapsedRealtime() < deadline && notification == null) {
            notification = manager.activeNotifications.firstOrNull { it.id == ru.itmo.tuman.core.reminders.ReminderNotifications.NOTIFICATION_ID }
            if (notification == null) Thread.sleep(500)
        }
        assertNotNull("Real WorkManager notification did not arrive within 150 seconds", notification)
        File(evidence, "reminder-arrival.txt").writeText("selected=$next scheduledAt=$scheduledAt backgroundAt=$start posted=${notification!!.postTime}")
        notification!!.notification.contentIntent.send()
        activity!!.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
        ui.waitForIdle()
        manager.cancelAll()
        koin.get<ru.itmo.tuman.core.reminders.ReminderScheduler>().disable()
        val works = androidx.work.WorkManager.getInstance(context).getWorkInfosForUniqueWork("daily_reminder").get()
        assertTrue(works.all { it.state.isFinished })
        assertFalse(settings.settings.first().reminderEnabled)
        File(evidence, "reminder-cancelled.txt").writeText(works.joinToString { it.state.name })
    }

}
