package ru.itmo.tuman.features.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.itmo.tuman.core.settings.SettingsRepository
import ru.itmo.tuman.core.settings.UserSettings
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val settingsRepository: SettingsRepository,
    observeToday: () -> Flow<HomeToday>,
    observeProgress: () -> Flow<HomeProgress>,
    observeDecks: () -> Flow<List<HomeDeck>>,
) : ViewModel() {
    private val onboarding = MutableStateFlow(
        HomeUiState.Onboarding(UserSettings.DEFAULT_DAILY_GOAL, saving = false, saveFailed = false),
    )

    val uiState = settingsRepository.settings
        .map { it.onboardingDone }
        .distinctUntilChanged()
        .flatMapLatest { done ->
            if (done) {
                combine(observeToday(), observeProgress(), observeDecks()) { today, progress, decks ->
                    HomeUiState.Dashboard(today, progress, decks)
                }
            } else {
                onboarding
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)

    fun selectGoal(goal: Int) {
        val current = onboarding.value
        if (!current.saving && goal in UserSettings.GOAL_OPTIONS) {
            onboarding.value = current.copy(goal = goal, saveFailed = false)
        }
    }

    fun completeOnboarding() {
        val current = uiState.value as? HomeUiState.Onboarding ?: return
        if (onboarding.value.saving) return
        val goal = onboarding.value.goal
        onboarding.value = current.copy(goal = goal, saving = true, saveFailed = false)
        viewModelScope.launch {
            try {
                settingsRepository.setDailyGoal(goal)
                settingsRepository.setOnboardingDone()
            } catch (_: IOException) {
                onboarding.value = current.copy(goal = goal, saving = false, saveFailed = true)
            }
        }
    }
}
