package ru.itmo.tuman.features.progress.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.itmo.tuman.R
import ru.itmo.tuman.core.settings.SettingsRepository
import ru.itmo.tuman.features.progress.domain.ProgressRepository

class ProgressViewModel(
    progressRepository: ProgressRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        progressRepository.observeProgress()
            .onEach { progress ->
                _uiState.update {
                    it.copy(isLoading = false, progress = progress, errorRes = null)
                }
            }
            .catch {
                _uiState.update {
                    it.copy(isLoading = false, errorRes = R.string.progress_error_load)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onDailyGoalChanged(goal: Int) {
        viewModelScope.launch {
            try {
                settingsRepository.setDailyGoal(goal)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(errorRes = R.string.progress_error_goal) }
            }
        }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorRes = null) }
    }
}
