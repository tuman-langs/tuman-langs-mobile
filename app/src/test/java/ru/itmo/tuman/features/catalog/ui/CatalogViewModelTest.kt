package ru.itmo.tuman.features.catalog.ui

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.itmo.tuman.features.catalog.domain.CatalogFailure
import ru.itmo.tuman.features.catalog.domain.CatalogRepository
import ru.itmo.tuman.features.catalog.domain.CatalogResult
import ru.itmo.tuman.features.catalog.domain.model.CatalogSetSummary

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {

    private lateinit var repository: FakeCatalogRepository
    private lateinit var scheduler: TestCoroutineScheduler

    @Before
    fun setUp() {
        scheduler = TestCoroutineScheduler()
        Dispatchers.setMain(StandardTestDispatcher(scheduler))
        repository = FakeCatalogRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsCatalogSetsAndDownloadedVersions() {
        repository.loadResult = CatalogResult.Success(listOf(sampleSet))
        repository.downloaded.value = mapOf(sampleSet.remoteId to sampleSet.version)

        val viewModel = createViewModel()

        assertEquals(listOf(sampleSet), viewModel.uiState.value.sets)
        assertEquals(mapOf(sampleSet.remoteId to sampleSet.version), viewModel.uiState.value.downloadedVersions)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun reportsNoInternetSeparatelyFromRequestFailure() {
        repository.loadResult = CatalogResult.Failure(CatalogFailure.NoInternet)

        val offlineViewModel = createViewModel()

        assertTrue(offlineViewModel.uiState.value.isOffline)
        assertEquals(CatalogFailure.NoInternet, offlineViewModel.uiState.value.loadFailure)

        repository.loadResult = CatalogResult.Failure(CatalogFailure.RequestFailed)
        val failedViewModel = createViewModel()

        assertFalse(failedViewModel.uiState.value.isOffline)
        assertEquals(CatalogFailure.RequestFailed, failedViewModel.uiState.value.loadFailure)
    }

    @Test
    fun failedDownloadSetsInlineErrorAndCanBeRetried() {
        repository.downloadResult = CatalogResult.Failure(CatalogFailure.NoInternet)
        val viewModel = createViewModel()

        viewModel.onDownloadClick(sampleSet.remoteId)
        scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isOffline)
        assertEquals(sampleSet.remoteId, viewModel.uiState.value.downloadFailureSetId)
        assertTrue(viewModel.uiState.value.downloadingSetIds.isEmpty())

        repository.downloadResult = CatalogResult.Success(Unit)
        viewModel.onDownloadClick(sampleSet.remoteId)
        scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isOffline)
        assertEquals(null, viewModel.uiState.value.downloadFailureSetId)
    }

    @Test
    fun showsServerWakingMessageWhenCatalogRequestTakesSeveralSeconds() {
        repository.pendingLoad = CompletableDeferred()
        val viewModel = CatalogViewModel(repository)

        scheduler.runCurrent()
        scheduler.advanceTimeBy(3_000)
        scheduler.runCurrent()
        assertTrue(viewModel.uiState.value.isWakingServer)

        requireNotNull(repository.pendingLoad).complete(CatalogResult.Success(listOf(sampleSet)))
        scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isWakingServer)
    }

    @Test
    fun showsServerWakingMessageDuringSlowDownload() {
        repository.pendingDownload = CompletableDeferred()
        val viewModel = createViewModel()

        viewModel.onDownloadClick(sampleSet.remoteId)
        scheduler.runCurrent()
        scheduler.advanceTimeBy(3_000)
        scheduler.runCurrent()
        assertTrue(sampleSet.remoteId in viewModel.uiState.value.wakingSetIds)

        requireNotNull(repository.pendingDownload).complete(CatalogResult.Success(Unit))
        scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.wakingSetIds.isEmpty())
    }

    private fun createViewModel(): CatalogViewModel =
        CatalogViewModel(repository).also { scheduler.advanceUntilIdle() }

    private class FakeCatalogRepository : CatalogRepository {
        var loadResult: CatalogResult<List<CatalogSetSummary>> =
            CatalogResult.Success(emptyList())
        var downloadResult: CatalogResult<Unit> = CatalogResult.Success(Unit)
        var pendingLoad: CompletableDeferred<CatalogResult<List<CatalogSetSummary>>>? = null
        var pendingDownload: CompletableDeferred<CatalogResult<Unit>>? = null
        val downloaded = MutableStateFlow<Map<String, Int>>(emptyMap())

        override fun observeDownloadedVersions(): StateFlow<Map<String, Int>> = downloaded

        override suspend fun loadSets(): CatalogResult<List<CatalogSetSummary>> =
            pendingLoad?.await() ?: loadResult

        override suspend fun downloadSet(remoteId: String): CatalogResult<Unit> =
            pendingDownload?.await() ?: downloadResult
    }

    private companion object {
        val sampleSet = CatalogSetSummary(
            remoteId = "test-set",
            title = "Тестовый набор",
            description = "Набор для проверки",
            version = 1,
            cardCount = 2,
        )
    }
}
