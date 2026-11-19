package ru.itmo.tuman.features.progress.ui

import ru.itmo.tuman.features.progress.domain.model.Progress

data class ProgressUiState(
    val isLoading: Boolean = true,
    val progress: Progress? = null,
    val errorRes: Int? = null,
)
