package ru.itmo.tuman.features.decks.ui.list

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import ru.itmo.tuman.R
import ru.itmo.tuman.features.decks.data.FakeDecksRepository
import ru.itmo.tuman.features.decks.domain.DecksRepository

@OptIn(ExperimentalCoroutinesApi::class)
class DecksViewModelTest {

    private lateinit var viewModel: DecksViewModel

    // viewModelScope работает на главном потоке Android; в тесте его заменяем тестовым
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = DecksViewModel(FakeDecksRepository())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun titles() = viewModel.uiState.value.decks.map { it.title }

    @Test
    fun startsWithEmptyListAfterLoading() {
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.decks.isEmpty())
    }

    @Test
    fun createdDeckAppearsInList() {
        viewModel.onCreate("Мои слова")

        assertEquals(listOf("Мои слова"), titles())
    }

    @Test
    fun spacesAroundNameAreTrimmed() {
        viewModel.onCreate("  Ҡош-ҡорттар  ")

        assertEquals(listOf("Ҡош-ҡорттар"), titles())
    }

    @Test
    fun blankNameIsNotSaved() {
        viewModel.onCreate("")
        viewModel.onCreate("   ")

        assertTrue(titles().isEmpty())
    }

    @Test
    fun renameChangesTitle() {
        viewModel.onCreate("Тест 1")
        val id = viewModel.uiState.value.decks.single().id

        viewModel.onRename(id, "Тест 2")

        assertEquals(listOf("Тест 2"), titles())
    }

    @Test
    fun renameToBlankKeepsOldTitle() {
        viewModel.onCreate("Тест 1")
        val id = viewModel.uiState.value.decks.single().id

        viewModel.onRename(id, "  ")

        assertEquals(listOf("Тест 1"), titles())
    }

    @Test
    fun deleteRemovesOnlyChosenDeck() {
        viewModel.onCreate("Оставить")
        viewModel.onCreate("Удалить меня")
        val id = viewModel.uiState.value.decks.first { it.title == "Удалить меня" }.id

        viewModel.onDelete(id)

        assertEquals(listOf("Оставить"), titles())
    }

    @Test
    fun repositoryFailureShowsErrorAndDoesNotCrash() {
        val failing = object : DecksRepository by FakeDecksRepository() {
            override suspend fun createDeck(title: String): Long = error("база недоступна")
        }
        val failingViewModel = DecksViewModel(failing)

        failingViewModel.onCreate("Мои слова")
        assertEquals(R.string.decks_error_save, failingViewModel.uiState.value.errorRes)

        failingViewModel.onErrorShown()
        assertNull(failingViewModel.uiState.value.errorRes)
    }
}
