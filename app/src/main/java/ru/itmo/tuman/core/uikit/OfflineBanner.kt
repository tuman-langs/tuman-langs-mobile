package ru.itmo.tuman.core.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.itmo.tuman.R
import ru.itmo.tuman.core.uikit.theme.TumanErrorSoft
import ru.itmo.tuman.core.uikit.theme.TumanTheme

/** Плашка «Нет интернета». Значок Warning: значка без Wi-Fi нет в базовом наборе иконок. */
@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.error
    val title = stringResource(R.string.offline_title)
    val text = stringResource(R.string.offline_text)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TumanErrorSoft, RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(imageVector = Icons.Filled.Warning, contentDescription = null, tint = color)
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) { append(title) }
                append(" ")
                append(text)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = color,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFBF8F3)
@Composable
private fun OfflineBannerPreview() {
    TumanTheme {
        OfflineBanner(Modifier.padding(16.dp))
    }
}
