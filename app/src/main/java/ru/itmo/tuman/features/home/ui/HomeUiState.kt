package ru.itmo.tuman.features.home.ui

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Onboarding(val goal: Int, val saving: Boolean, val saveFailed: Boolean) : HomeUiState

    data class Dashboard(
        val today: HomeToday,
        val progress: HomeProgress,
        val decks: List<HomeDeck>,
    ) : HomeUiState {
        val section: Section
            get() = when {
                decks.isEmpty() -> Section.NoDecks
                today.total == 0 -> Section.Done
                else -> Section.Today
            }
    }

    enum class Section { Today, Done, NoDecks }
}

data class HomeToday(val due: Int, val new: Int) {
    val total: Int get() = due + new
    val minutes: Int get() = (total + 2) / 3
}

data class HomeProgress(val answers: Int, val goal: Int, val streak: Int, val xp: Int) {
    val remaining: Int get() = (goal - answers).coerceAtLeast(0)
}

data class HomeDeck(val id: Long, val title: String, val cardCount: Int)
