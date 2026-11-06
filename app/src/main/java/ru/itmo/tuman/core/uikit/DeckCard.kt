package ru.itmo.tuman.core.uikit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.itmo.tuman.core.uikit.theme.TumanHardSoft
import ru.itmo.tuman.core.uikit.theme.TumanPrimary
import ru.itmo.tuman.core.uikit.theme.TumanPrimarySoft
import ru.itmo.tuman.core.uikit.theme.TumanTheme
import ru.itmo.tuman.core.uikit.theme.TumanXp

/**
 * Карточка набора: значок, название и подпись.
 * В [trailing] кладут то, что стоит справа: число карточек или кнопку «Скачать».
 */
@Composable
fun DeckCard(
    title: String,
    subtitle: String,
    badge: String,
    modifier: Modifier = Modifier,
    badgeColor: Color = TumanPrimary,
    badgeBackground: Color = TumanPrimarySoft,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(badgeBackground, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.headlineSmall,
                    color = badgeColor,
                    maxLines = 1,
                    softWrap = false,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            trailing?.invoke()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFBF8F3)
@Composable
private fun DeckCardPreview() {
    TumanTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DeckCard(title = "Мои слова", subtitle = "24 карточки", badge = "М", onClick = {}) {
                Text("24", style = MaterialTheme.typography.titleSmall)
            }
            DeckCard(
                title = "Аффиксы",
                subtitle = "30 карточек · из каталога",
                badge = "-лар",
                badgeColor = TumanXp,
                badgeBackground = TumanHardSoft,
            ) {
                TumanAccentButton("Скачать", {}, size = TumanButtonSize.Small)
            }
        }
    }
}
