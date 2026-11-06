package ru.itmo.tuman.core.uikit

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.itmo.tuman.core.uikit.theme.TumanTheme

/** Скелетон строки набора: показывается, пока данные загружаются. Плавно мерцает. */
@Composable
fun DeckCardSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 700), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(pulse),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SkeletonBlock(Modifier.size(46.dp), radius = 14.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonBlock(Modifier.fillMaxWidth(0.7f).height(14.dp))
            SkeletonBlock(Modifier.fillMaxWidth(0.45f).height(12.dp))
        }
    }
}

@Composable
private fun SkeletonBlock(modifier: Modifier, radius: Dp = 8.dp) {
    Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(radius)))
}

@Preview(showBackground = true, backgroundColor = 0xFFFBF8F3)
@Composable
private fun DeckCardSkeletonPreview() {
    TumanTheme {
        DeckCardSkeleton(Modifier.padding(16.dp))
    }
}
