package ru.itmo.tuman.core.uikit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.itmo.tuman.core.uikit.theme.TumanTheme

/** Размеры кнопок из макета: высота, скругление и отступ текста от краёв. */
enum class TumanButtonSize(val height: Dp, val radius: Dp, val horizontalPadding: Dp) {
    Small(36.dp, 11.dp, 14.dp),
    Regular(48.dp, 14.dp, 22.dp),
    Large(58.dp, 18.dp, 22.dp),
}

/** Основная кнопка: главное действие экрана. При enabled = false становится неактивной. */
@Composable
fun TumanPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: TumanButtonSize = TumanButtonSize.Regular,
) {
    FilledButton(text, onClick, MaterialTheme.colorScheme.primary, modifier, enabled, size)
}

/** Кнопка акцентного цвета, например «Скачать». */
@Composable
fun TumanAccentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: TumanButtonSize = TumanButtonSize.Regular,
) {
    FilledButton(text, onClick, MaterialTheme.colorScheme.secondary, modifier, enabled, size)
}

/** Второстепенная кнопка с контуром. */
@Composable
fun TumanSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: TumanButtonSize = TumanButtonSize.Regular,
) {
    val colors = MaterialTheme.colorScheme
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(size.height),
        enabled = enabled,
        shape = RoundedCornerShape(size.radius),
        border = BorderStroke(1.5.dp, if (enabled) colors.primary else colors.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colors.primary,
            disabledContentColor = colors.onSurfaceVariant,
        ),
        contentPadding = PaddingValues(horizontal = size.horizontalPadding),
    ) {
        ButtonLabel(text, size.textStyle())
    }
}

/** Опасная кнопка: действие, которое что-то удаляет. */
@Composable
fun TumanDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: TumanButtonSize = TumanButtonSize.Regular,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(size.height),
        enabled = enabled,
        shape = RoundedCornerShape(size.radius),
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.error,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        contentPadding = PaddingValues(horizontal = size.horizontalPadding),
    ) {
        ButtonLabel(text, size.textStyle())
    }
}

@Composable
private fun FilledButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    modifier: Modifier,
    enabled: Boolean,
    size: TumanButtonSize,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(size.height),
        enabled = enabled,
        shape = RoundedCornerShape(size.radius),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.outline,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        contentPadding = PaddingValues(horizontal = size.horizontalPadding),
    ) {
        ButtonLabel(text, size.textStyle())
    }
}

@Composable
private fun ButtonLabel(text: String, style: TextStyle) {
    Text(text = text, style = style, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun TumanButtonSize.textStyle(): TextStyle = when (this) {
    TumanButtonSize.Small -> MaterialTheme.typography.labelLarge
    TumanButtonSize.Regular -> MaterialTheme.typography.titleSmall
    TumanButtonSize.Large -> MaterialTheme.typography.titleMedium
}

@Preview(showBackground = true, backgroundColor = 0xFFFBF8F3)
@Composable
private fun TumanButtonsPreview() {
    TumanTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TumanPrimaryButton("Начать повторение", {}, size = TumanButtonSize.Large)
            TumanPrimaryButton("Сохранить", {})
            TumanAccentButton("Скачать", {}, size = TumanButtonSize.Small)
            TumanSecondaryButton("Создать свой набор", {})
            TumanDangerButton("Удалить", {})
            TumanPrimaryButton("Неактивная", {}, enabled = false)
        }
    }
}
