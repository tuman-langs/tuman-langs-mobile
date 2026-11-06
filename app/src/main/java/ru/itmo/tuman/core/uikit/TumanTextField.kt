package ru.itmo.tuman.core.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.itmo.tuman.core.uikit.theme.TumanPlaceholder
import ru.itmo.tuman.core.uikit.theme.TumanTheme

/**
 * Поле ввода с подписью сверху. Если передан [error], рамка красная, а текст ошибки стоит под полем.
 * Сделано на BasicTextField: стандартное поле Material выше, чем 50 dp в макете.
 */
@Composable
fun TumanTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    error: String? = null,
    singleLine: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor = when {
        error != null -> colors.error
        isFocused -> colors.primary
        else -> colors.outline
    }
    val shape = RoundedCornerShape(14.dp)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurfaceVariant,
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
            cursorBrush = SolidColor(colors.primary),
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .heightIn(min = 50.dp)
                        .background(colors.surface, shape)
                        .border(1.5.dp, borderColor, shape)
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty() && placeholder != null) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TumanPlaceholder,
                        )
                    }
                    innerTextField()
                }
            },
        )
        if (error != null) {
            Text(text = error, style = MaterialTheme.typography.labelMedium, color = colors.error)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFBF8F3)
@Composable
private fun TumanTextFieldPreview() {
    TumanTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TumanTextField("", {}, "Лицевая сторона", placeholder = "Введите слово")
            TumanTextField("әөүғҡңҙҫһ", {}, "С буквами")
            TumanTextField("", {}, "Обратная сторона", error = "Заполните поле")
        }
    }
}
