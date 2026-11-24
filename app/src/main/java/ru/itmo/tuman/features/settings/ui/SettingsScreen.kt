package ru.itmo.tuman.features.settings.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import org.koin.androidx.compose.koinViewModel
import ru.itmo.tuman.R
import ru.itmo.tuman.core.uikit.TumanSecondaryButton
import ru.itmo.tuman.core.uikit.TumanTopBar
import ru.itmo.tuman.core.uikit.theme.TumanErrorSoft
import ru.itmo.tuman.core.uikit.theme.TumanPrimarySoft
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ReminderTimeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT)
private val ReminderTimePresets = listOf(8, 12, 19, 21).map { LocalTime.of(it, 0) }

// Контурные иконки из раздела «Напоминания» в design/index.html.
private const val BELL_PATH =
    "M6 8a6 6 0 1 1 12 0c0 7 3 9 3 9H3s3-2 3-9M10.3 21a1.9 1.9 0 0 0 3.4 0"
private const val BELL_OFF_PATH =
    "M2 2l20 20M8.7 3.7A6 6 0 0 1 18 8c0 3 .6 5 1.3 6.5M17 17H3s3-2 3-9c0-.6 0-1.1.2-1.6M10.3 21a1.9 1.9 0 0 0 3.4 0"
private const val CLOCK_PATH = "M3 12a9 9 0 1 0 18 0a9 9 0 1 0-18 0M12 7v5l3 2"
private const val ALERT_PATH = "M3 12a9 9 0 1 0 18 0a9 9 0 1 0-18 0M12 8v4m0 4h.01"

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showTimeDialog by rememberSaveable { mutableStateOf(value = false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.onPermissionResult(granted)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshPermission()
    }

    fun openAppNotificationSettings() {
        context.startActivity(
            Intent(
                Settings.ACTION_APP_NOTIFICATION_SETTINGS,
                Uri.parse("package:${context.packageName}"),
            ),
        )
    }

    SettingsContent(
        state = state,
        onBack = onBack,
        onEnabledChanged = { enabled ->
            if (enabled && !state.hasPermission && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.setEnabled(enabled)
            }
        },
        onShowTimeDialog = { showTimeDialog = true },
        onOpenPhoneSettings = ::openAppNotificationSettings,
    )

    if (showTimeDialog && state.enabled && state.hasPermission) {
        ReminderTimeDialog(
            initialTime = state.time,
            onDismiss = { showTimeDialog = false },
            onConfirm = { time ->
                viewModel.setTime(time)
                showTimeDialog = false
            },
        )
    }
}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    onBack: () -> Unit,
    onEnabledChanged: (Boolean) -> Unit,
    onShowTimeDialog: () -> Unit,
    onOpenPhoneSettings: () -> Unit,
) {
    val effectiveEnabled = state.enabled && state.hasPermission
    val colors = MaterialTheme.colorScheme
    val timeColor = if (effectiveEnabled) colors.primary else colors.onSurfaceVariant

    Scaffold(
        topBar = {
            TumanTopBar(
                title = stringResource(R.string.settings_title),
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!state.hasPermission) {
                ReminderBanner(
                    path = BELL_OFF_PATH,
                    text = stringResource(R.string.settings_notifications_denied),
                    containerColor = TumanErrorSoft,
                    contentColor = colors.error,
                )
                TumanSecondaryButton(
                    text = stringResource(R.string.settings_open_phone_settings),
                    onClick = onOpenPhoneSettings,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.outlineVariant),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ReminderIcon(if (effectiveEnabled) BELL_PATH else BELL_OFF_PATH, timeColor)
                    ReminderSettingLabel(
                        title = stringResource(R.string.settings_reminder_enabled),
                        description = stringResource(
                            if (effectiveEnabled) R.string.settings_reminder_description
                            else R.string.settings_reminder_disabled,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = effectiveEnabled,
                        onCheckedChange = onEnabledChanged,
                        colors = SwitchDefaults.colors(
                            uncheckedThumbColor = colors.surface,
                            uncheckedTrackColor = colors.outlineVariant,
                            uncheckedBorderColor = colors.outlineVariant,
                        ),
                    )
                }

                HorizontalDivider(color = colors.outlineVariant)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = effectiveEnabled,
                            role = Role.Button,
                            onClick = onShowTimeDialog,
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ReminderIcon(CLOCK_PATH, timeColor)
                    ReminderSettingLabel(
                        title = stringResource(R.string.settings_reminder_time),
                        description = stringResource(R.string.settings_reminder_every_day),
                        modifier = Modifier.weight(1f),
                        titleColor = if (effectiveEnabled) colors.onSurface else colors.onSurfaceVariant,
                    )
                    Text(
                        state.time.format(ReminderTimeFormat),
                        style = MaterialTheme.typography.titleLarge,
                        color = timeColor,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (effectiveEnabled) {
                NotificationPreview()
            } else if (state.hasPermission) {
                ReminderBanner(
                    path = ALERT_PATH,
                    text = stringResource(R.string.settings_disabled_warning),
                    containerColor = TumanPrimarySoft,
                    contentColor = colors.primary,
                )
            }
        }
    }
}

@Composable
private fun ReminderSettingLabel(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Column(modifier) {
        Text(title, color = titleColor, style = MaterialTheme.typography.titleSmall)
        Text(
            description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun NotificationPreview() {
    Text(
        stringResource(R.string.settings_notification_preview),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelLarge,
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.home_owl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(36.dp).clip(CircleShape),
            )
            ReminderSettingLabel(
                title = stringResource(R.string.settings_notification_preview_title),
                description = stringResource(R.string.settings_notification_preview_text),
            )
        }
    }
}

@Composable
private fun ReminderBanner(path: String, text: String, containerColor: Color, contentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReminderIcon(path, contentColor)
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ReminderIcon(path: String, color: Color) {
    Box(
        Modifier.size(24.dp).drawWithCache {
            val outline = PathParser().parsePathString(path).toPath()
            val stroke = Stroke(width = 1.8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            onDrawBehind {
                scale(size.width / 24f, size.height / 24f, pivot = Offset.Zero) {
                    drawPath(outline, color, style = stroke)
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ReminderTimeDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    val pickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_time_dialog_title)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TimePicker(state = pickerState)
                FlowRow(
                    modifier = Modifier.fillMaxWidth().selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                ) {
                    ReminderTimePresets.forEach { time ->
                        FilterChip(
                            selected = pickerState.hour == time.hour && pickerState.minute == time.minute,
                            onClick = {
                                pickerState.hour = time.hour
                                pickerState.minute = time.minute
                            },
                            label = { Text(time.format(ReminderTimeFormat)) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                selectedContainerColor = TumanPrimarySoft,
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(pickerState.hour, pickerState.minute)) }) {
                Text(stringResource(R.string.settings_time_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
