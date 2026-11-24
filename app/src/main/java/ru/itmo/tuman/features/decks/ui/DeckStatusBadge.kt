package ru.itmo.tuman.features.decks.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import ru.itmo.tuman.core.uikit.theme.TumanAccent
import ru.itmo.tuman.core.uikit.theme.TumanAccentSoft
import ru.itmo.tuman.core.uikit.theme.TumanPrimary
import ru.itmo.tuman.core.uikit.theme.TumanPrimarySoft

internal enum class DeckBadgeHighlight { Neutral, Due, Learned }

@Composable
internal fun DeckStatusBadge(
    text: String,
    highlight: DeckBadgeHighlight,
    description: String? = null,
) {
    val (background, foreground) = when (highlight) {
        DeckBadgeHighlight.Neutral ->
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        DeckBadgeHighlight.Due -> TumanAccentSoft to TumanAccent
        DeckBadgeHighlight.Learned -> TumanPrimarySoft to TumanPrimary
    }
    Surface(
        modifier = if (description == null) Modifier else Modifier.clearAndSetSemantics {
            contentDescription = description
        },
        color = background,
        contentColor = foreground,
        shape = CircleShape,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
