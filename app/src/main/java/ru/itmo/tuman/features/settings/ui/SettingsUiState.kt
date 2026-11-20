package ru.itmo.tuman.features.settings.ui

import java.time.LocalTime

data class SettingsUiState(
    val enabled: Boolean = true,
    val time: LocalTime = LocalTime.of(19, 0),
    val hasPermission: Boolean = true,
)
