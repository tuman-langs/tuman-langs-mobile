package ru.itmo.tuman.core.uikit.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Пока файлов Nunito нет в res/font, здесь системный шрифт.
// Когда они появятся, изменится только эта строка: размеры и компоненты останутся прежними.
val TumanFontFamily: FontFamily = FontFamily.Default

private fun tumanStyle(size: Int, weight: FontWeight) = TextStyle(
    fontFamily = TumanFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
)

/** Размеры текста из таблицы «Шрифт» в design/README.md, разложенные по ролям Material 3. */
val Typography = Typography(
    displayLarge = tumanStyle(40, FontWeight.Black),      // слово на карточке
    displayMedium = tumanStyle(32, FontWeight.Black),     // крупное число («18 карточек»)
    displaySmall = tumanStyle(28, FontWeight.Black),      // большой заголовок
    headlineLarge = tumanStyle(26, FontWeight.ExtraBold), // перевод на карточке
    headlineSmall = tumanStyle(20, FontWeight.ExtraBold), // заголовок диалога, пустого состояния
    titleLarge = tumanStyle(21, FontWeight.ExtraBold),    // заголовок экрана
    titleMedium = tumanStyle(18, FontWeight.ExtraBold),   // крупная кнопка
    titleSmall = tumanStyle(16, FontWeight.ExtraBold),    // кнопка, название набора
    bodyLarge = tumanStyle(17, FontWeight.SemiBold),      // текст в поле ввода
    bodyMedium = tumanStyle(15, FontWeight.Normal),       // основной текст
    bodySmall = tumanStyle(13, FontWeight.Normal),        // подпись
    labelLarge = tumanStyle(13, FontWeight.ExtraBold),    // подпись поля, маленькая кнопка
    labelMedium = tumanStyle(12, FontWeight.ExtraBold),   // плашка, текст ошибки
    labelSmall = tumanStyle(11, FontWeight.Bold),         // подпись вкладки
)
