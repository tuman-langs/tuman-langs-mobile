package ru.itmo.tuman.core.uikit

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.itmo.tuman.core.uikit.theme.TumanTheme

/** Пустое состояние с иллюстрацией или значком, текстом и необязательным действием. */
@Composable
fun EmptyState(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Info,
    actionText: String? = null,
    @DrawableRes illustration: Int? = null,
    onAction: () -> Unit = {},
    secondaryAction: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (illustration == null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        } else {
            Image(
                painter = painterResource(illustration),
                contentDescription = null,
                modifier = Modifier.size(120.dp).clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionText != null) {
            Spacer(Modifier.height(18.dp))
            TumanPrimaryButton(actionText, onAction)
        }
        secondaryAction?.invoke()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFBF8F3)
@Composable
private fun EmptyStatePreview() {
    TumanTheme {
        EmptyState(
            title = "Наборов пока нет",
            text = "Создайте свой набор или скачайте готовый из каталога",
            actionText = "Создать набор",
        )
    }
}
