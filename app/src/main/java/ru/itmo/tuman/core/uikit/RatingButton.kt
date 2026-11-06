package ru.itmo.tuman.core.uikit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.itmo.tuman.core.uikit.theme.TumanAccent
import ru.itmo.tuman.core.uikit.theme.TumanAccentSoft
import ru.itmo.tuman.core.uikit.theme.TumanEasy
import ru.itmo.tuman.core.uikit.theme.TumanEasySoft
import ru.itmo.tuman.core.uikit.theme.TumanHardSoft
import ru.itmo.tuman.core.uikit.theme.TumanPrimary
import ru.itmo.tuman.core.uikit.theme.TumanPrimarySoft
import ru.itmo.tuman.core.uikit.theme.TumanTheme
import ru.itmo.tuman.core.uikit.theme.TumanXp

/**
 * Оформление четырёх кнопок оценки. Здесь только цвета: связь с Rating из core-srs
 * остаётся на экране повторения, поэтому UI-kit ничего не знает про алгоритм.
 */
enum class RatingTone(val color: Color, val background: Color) {
    Forgot(TumanAccent, TumanAccentSoft),
    Hard(TumanXp, TumanHardSoft),
    Good(TumanPrimary, TumanPrimarySoft),
    Easy(TumanEasy, TumanEasySoft),
}

/** Кнопка оценки ответа: слева название, справа интервал до следующего показа. */
@Composable
fun RatingButton(
    label: String,
    interval: String,
    tone: RatingTone,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 50.dp),
        shape = RoundedCornerShape(14.dp),
        color = tone.background,
        contentColor = tone.color,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label, style = MaterialTheme.typography.titleSmall, maxLines = 1)
            Text(
                text = interval,
                modifier = Modifier.alpha(0.8f),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFBF8F3)
@Composable
private fun RatingButtonPreview() {
    TumanTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RatingButton("не помню", "<1 мин", RatingTone.Forgot, {}, Modifier.weight(1f))
                RatingButton("трудно", "1 д", RatingTone.Hard, {}, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RatingButton("хорошо", "3 д", RatingTone.Good, {}, Modifier.weight(1f))
                RatingButton("легко", "7 д", RatingTone.Easy, {}, Modifier.weight(1f))
            }
        }
    }
}
