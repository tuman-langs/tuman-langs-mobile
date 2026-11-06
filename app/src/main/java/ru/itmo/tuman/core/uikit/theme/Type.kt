package ru.itmo.tuman.core.uikit.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ru.itmo.tuman.R

// Nunito лежит в res/font одним вариативным файлом: в нём есть все насыщенности от 200 до 1000.
// Шрифт внутри приложения работает без интернета. Вариативные шрифты Android умеет с API 26.
@OptIn(ExperimentalTextApi::class) // настройки вариативного шрифта в нашей версии Compose ещё экспериментальные
private fun nunito(weight: FontWeight) = Font(
    resId = R.font.nunito_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val TumanFontFamily: FontFamily = FontFamily(
    nunito(FontWeight.Normal),
    nunito(FontWeight.SemiBold),
    nunito(FontWeight.Bold),
    nunito(FontWeight.ExtraBold),
    nunito(FontWeight.Black),
)

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
