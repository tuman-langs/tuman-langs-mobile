package ru.itmo.tuman.core.uikit.theme

import androidx.compose.ui.graphics.Color

// Цвета из раздела «UI-kit» макета (design/README.md). HEX-коды живут только здесь:
// экраны берут цвет из MaterialTheme.colorScheme или по имени из этого файла.

val TumanPrimary = Color(0xFF2D5A46)
val TumanPrimaryDark = Color(0xFF1F4334)
val TumanAccent = Color(0xFFD95338)
val TumanXp = Color(0xFFB45309)
val TumanEasy = Color(0xFF0F766E)
val TumanError = Color(0xFF9F1239)

val TumanText = Color(0xFF221D1A)
val TumanTextSecondary = Color(0xFF7D6A53)
val TumanPlaceholder = Color(0xFFB8A992)

val TumanBackground = Color(0xFFFBF8F3)
val TumanSubstrate = Color(0xFFF4EFE6)
val TumanBorder = Color(0xFFE9DFC9)
val TumanSurface = Color(0xFFFFFFFF)

// Мягкие фоны плашек и кнопок оценки: тот же цвет с прозрачностью.
val TumanPrimarySoft = TumanPrimary.copy(alpha = 0.10f)
val TumanAccentSoft = TumanAccent.copy(alpha = 0.10f)
val TumanHardSoft = Color(0xFFD97706).copy(alpha = 0.12f)
val TumanEasySoft = TumanEasy.copy(alpha = 0.10f)
val TumanErrorSoft = TumanError.copy(alpha = 0.08f)
