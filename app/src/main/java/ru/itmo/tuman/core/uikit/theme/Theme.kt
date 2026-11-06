package ru.itmo.tuman.core.uikit.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Только светлая тема: тёмную в MVP не делаем. Динамические цвета Android 12+ тоже
// отключены, иначе на новых телефонах приложение перекрасится под обои и уйдёт от макета.
private val TumanColorScheme = lightColorScheme(
    primary = TumanPrimary,
    onPrimary = TumanSurface,
    secondary = TumanAccent,
    onSecondary = TumanSurface,
    // Индикатор выбранной вкладки в NavigationBar берёт этот цвет.
    secondaryContainer = TumanPrimarySoft,
    onSecondaryContainer = TumanPrimary,
    background = TumanBackground,
    onBackground = TumanText,
    surface = TumanSurface,
    onSurface = TumanText,
    surfaceVariant = TumanSubstrate,
    onSurfaceVariant = TumanTextSecondary,
    // Material 3 подмешивает в эти поверхности свой сиреневый оттенок, поэтому задаём их явно.
    surfaceTint = TumanPrimary,
    surfaceContainerLowest = TumanSurface,
    surfaceContainerLow = TumanSurface,
    surfaceContainer = TumanSurface,
    surfaceContainerHigh = TumanSurface,
    surfaceContainerHighest = TumanSubstrate,
    outline = TumanBorder,
    outlineVariant = TumanBorder,
    error = TumanError,
    onError = TumanSurface,
)

@Composable
fun TumanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TumanColorScheme,
        typography = Typography,
        content = content,
    )
}
