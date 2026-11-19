package ru.itmo.tuman.features.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.itmo.tuman.core.reminders.ReminderScheduler
import ru.itmo.tuman.core.settings.SettingsRepository
import java.time.LocalTime

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(hasPermission = reminderScheduler.hasPermission()),
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        settingsRepository.settings
            .onEach { settings ->
                _uiState.update {
                    it.copy(
                        enabled = settings.reminderEnabled,
                        time = settings.reminderTime,
                        hasPermission = reminderScheduler.hasPermission(),
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun setEnabled(enabled: Boolean) {
        _uiState.update { it.copy(enabled = enabled) }
        viewModelScope.launch {
            val time = _uiState.value.time
            if (enabled) reminderScheduler.enable(time) else reminderScheduler.disable()
            refreshPermission()
        }
    }

    fun setTime(time: LocalTime) {
        _uiState.update { it.copy(time = time) }
        viewModelScope.launch {
            if (_uiState.value.enabled) reminderScheduler.enable(time)
        }
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) setEnabled(true)
        refreshPermission()
    }

    fun refreshPermission() {
        _uiState.update { it.copy(hasPermission = reminderScheduler.hasPermission()) }
    }
}
