package ru.itmo.tuman.features.catalog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.itmo.tuman.features.catalog.domain.CatalogFailure
import ru.itmo.tuman.features.catalog.domain.CatalogRepository
import ru.itmo.tuman.features.catalog.domain.CatalogResult

class CatalogViewModel(
    private val repository: CatalogRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        repository.observeDownloadedVersions()
            .onEach { versions ->
                _uiState.update {
                    it.copy(
                        downloadedVersions = versions,
                        downloadedVersionsUnavailable = false,
                    )
                }
            }
            .catch {
                _uiState.update { it.copy(downloadedVersionsUnavailable = true) }
            }
            .launchIn(viewModelScope)
        loadSets()
    }

    fun onRetryLoad() {
        loadSets()
    }

    fun onDownloadClick(remoteId: String) {
        if (remoteId in _uiState.value.downloadingSetIds) return
        _uiState.update {
            it.copy(
                downloadingSetIds = it.downloadingSetIds + remoteId,
                downloadFailureSetId = null,
                isOffline = false,
            )
        }
        viewModelScope.launch {
            val wakingIndicator = launch {
                delay(SERVER_WAKE_UP_INDICATOR_DELAY)
                _uiState.update { it.copy(wakingSetIds = it.wakingSetIds + remoteId) }
            }
            val result = try {
                repository.downloadSet(remoteId)
            } finally {
                wakingIndicator.cancel()
                _uiState.update { it.copy(wakingSetIds = it.wakingSetIds - remoteId) }
            }
            when (result) {
                is CatalogResult.Success -> _uiState.update {
                    it.copy(downloadingSetIds = it.downloadingSetIds - remoteId)
                }
                is CatalogResult.Failure -> _uiState.update {
                    it.copy(
                        downloadingSetIds = it.downloadingSetIds - remoteId,
                        downloadFailureSetId = remoteId,
                        isOffline = result.reason == CatalogFailure.NoInternet,
                    )
                }
            }
        }
    }

    private fun loadSets() {
        if (_uiState.value.isLoading) return
        _uiState.update {
            it.copy(
                isLoading = true,
                isWakingServer = false,
                isOffline = false,
                loadFailure = null,
            )
        }
        viewModelScope.launch {
            val wakingIndicator = launch {
                delay(SERVER_WAKE_UP_INDICATOR_DELAY)
                _uiState.update { state ->
                    if (state.isLoading) state.copy(isWakingServer = true) else state
                }
            }
            val result = try {
                repository.loadSets()
            } finally {
                wakingIndicator.cancel()
            }
            when (result) {
                is CatalogResult.Success -> _uiState.update {
                    it.copy(
                        sets = result.value,
                        isLoading = false,
                        isWakingServer = false,
                        isOffline = false,
                        loadFailure = null,
                    )
                }
                is CatalogResult.Failure -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        isWakingServer = false,
                        isOffline = result.reason == CatalogFailure.NoInternet,
                        loadFailure = result.reason,
                    )
                }
            }
        }
    }

    private companion object {
        const val SERVER_WAKE_UP_INDICATOR_DELAY = 3_000L
    }
}
