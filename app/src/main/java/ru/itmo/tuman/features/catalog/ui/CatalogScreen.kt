package ru.itmo.tuman.features.catalog.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import ru.itmo.tuman.R
import ru.itmo.tuman.core.uikit.DeckCard
import ru.itmo.tuman.core.uikit.DeckCardSkeleton
import ru.itmo.tuman.core.uikit.EmptyState
import ru.itmo.tuman.core.uikit.OfflineBanner
import ru.itmo.tuman.core.uikit.TumanAccentButton
import ru.itmo.tuman.core.uikit.TumanButtonSize
import ru.itmo.tuman.core.uikit.TumanPrimaryButton
import ru.itmo.tuman.core.uikit.TumanSecondaryButton
import ru.itmo.tuman.core.uikit.TumanTopBar
import ru.itmo.tuman.core.uikit.theme.TumanTheme
import ru.itmo.tuman.features.catalog.domain.CatalogFailure
import ru.itmo.tuman.features.catalog.domain.model.CatalogSetSummary

@Composable
fun CatalogScreen(
    onMyDecksClick: () -> Unit,
    viewModel: CatalogViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    CatalogContent(
        state = state,
        onAction = { action ->
            when (action) {
                CatalogAction.RetryLoad -> viewModel.onRetryLoad()
                is CatalogAction.Download -> viewModel.onDownloadClick(action.remoteId)
            }
        },
        onMyDecksClick = onMyDecksClick,
    )
}

private sealed interface CatalogAction {
    data object RetryLoad : CatalogAction
    data class Download(val remoteId: String) : CatalogAction
}

@Composable
private fun CatalogContent(
    state: CatalogUiState,
    onAction: (CatalogAction) -> Unit,
    onMyDecksClick: () -> Unit,
) {
    Scaffold(topBar = { TumanTopBar(title = stringResource(R.string.catalog_title)) }) { padding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(padding)
        when {
            state.isLoading -> CatalogLoading(contentModifier, state.isWakingServer)
            state.sets.isEmpty() && state.loadFailure == CatalogFailure.NoInternet ->
                CatalogOffline(contentModifier, onAction, onMyDecksClick)
            state.sets.isEmpty() && state.loadFailure != null ->
                CatalogLoadError(contentModifier, onAction)
            state.sets.isEmpty() -> CatalogEmpty(contentModifier, onAction)
            else -> CatalogList(state, contentModifier, onAction)
        }
    }
}

@Composable
private fun CatalogLoading(modifier: Modifier, isWakingServer: Boolean) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (isWakingServer) {
            Text(
                text = stringResource(R.string.catalog_server_waking),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        repeat(3) { DeckCardSkeleton() }
    }
}

@Composable
private fun CatalogOffline(
    modifier: Modifier,
    onAction: (CatalogAction) -> Unit,
    onMyDecksClick: () -> Unit,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            EmptyState(
                title = stringResource(R.string.catalog_offline_title),
                text = stringResource(R.string.catalog_offline_text),
                illustration = R.drawable.home_owl,
            )
            TumanPrimaryButton(
                text = stringResource(R.string.catalog_retry),
                onClick = { onAction(CatalogAction.RetryLoad) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Filled.Refresh,
            )
            TextButton(onClick = onMyDecksClick) {
                Text(stringResource(R.string.catalog_my_decks))
            }
        }
    }
}

@Composable
private fun CatalogLoadError(modifier: Modifier, onAction: (CatalogAction) -> Unit) {
    Box(modifier, contentAlignment = Alignment.Center) {
        EmptyState(
            title = stringResource(R.string.catalog_load_error_title),
            text = stringResource(R.string.catalog_load_error_text),
            actionText = stringResource(R.string.catalog_retry),
            onAction = { onAction(CatalogAction.RetryLoad) },
        )
    }
}

@Composable
private fun CatalogEmpty(modifier: Modifier, onAction: (CatalogAction) -> Unit) {
    Box(modifier, contentAlignment = Alignment.Center) {
        EmptyState(
            title = stringResource(R.string.catalog_empty_title),
            text = stringResource(R.string.catalog_empty_text),
            actionText = stringResource(R.string.catalog_retry),
            onAction = { onAction(CatalogAction.RetryLoad) },
        )
    }
}

@Composable
private fun CatalogList(
    state: CatalogUiState,
    modifier: Modifier,
    onAction: (CatalogAction) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "catalog-heading") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.catalog_language_name),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.catalog_sets_count,
                        state.sets.size,
                        state.sets.size,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        if (state.isOffline) {
            item(key = "offline") { OfflineBanner() }
        }
        if (state.isWakingServer || state.wakingSetIds.isNotEmpty()) {
            item(key = "waking") {
                Text(
                    text = stringResource(R.string.catalog_server_waking),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        state.loadFailure?.let { failure ->
            item(key = "load-error") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (failure == CatalogFailure.NoInternet) OfflineBanner()
                    Text(
                        text = stringResource(
                            if (failure == CatalogFailure.NoInternet) {
                                R.string.catalog_offline_retry_text
                            } else {
                                R.string.catalog_load_error_text
                            },
                        ),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    TumanSecondaryButton(
                        text = stringResource(R.string.catalog_retry),
                        onClick = { onAction(CatalogAction.RetryLoad) },
                        size = TumanButtonSize.Small,
                    )
                }
            }
        }
        if (state.downloadedVersionsUnavailable) {
            item(key = "versions-error") {
                Text(
                    text = stringResource(R.string.catalog_versions_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        state.downloadFailureSetId?.let { remoteId ->
            val title = state.sets.firstOrNull { it.remoteId == remoteId }?.title ?: remoteId
            item(key = "download-error") {
                CatalogErrorBanner(
                    message = stringResource(R.string.catalog_download_error, title),
                    onRetry = { onAction(CatalogAction.Download(remoteId)) },
                )
            }
        }
        items(state.sets, key = { it.remoteId }) { set ->
            CatalogSetCard(
                set = set,
                downloadedVersion = state.downloadedVersions[set.remoteId],
                isDownloading = set.remoteId in state.downloadingSetIds,
                onDownload = { onAction(CatalogAction.Download(set.remoteId)) },
            )
        }
    }
}

@Composable
private fun CatalogErrorBanner(message: String, onRetry: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
            TumanSecondaryButton(
                text = stringResource(R.string.catalog_retry),
                onClick = onRetry,
                size = TumanButtonSize.Small,
            )
        }
    }
}

@Composable
private fun CatalogSetCard(
    set: CatalogSetSummary,
    downloadedVersion: Int?,
    isDownloading: Boolean,
    onDownload: () -> Unit,
) {
    val cardCount = pluralStringResource(R.plurals.decks_card_count, set.cardCount, set.cardCount)
    DeckCard(
        title = set.title,
        subtitle = if (set.description.isBlank()) cardCount else "$cardCount · ${set.description}",
        badge = set.title.firstOrNull()?.uppercase() ?: "?",
    ) {
        when {
            isDownloading -> CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
            )
            downloadedVersion != null && downloadedVersion >= set.version -> Text(
                text = stringResource(R.string.catalog_downloaded),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
            )
            else -> TumanAccentButton(
                text = stringResource(
                    if (downloadedVersion == null) R.string.catalog_download else R.string.catalog_update,
                ),
                onClick = onDownload,
                size = TumanButtonSize.Small,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CatalogContentPreview() {
    TumanTheme {
        CatalogContent(
            state = CatalogUiState(
                isLoading = false,
                sets = listOf(
                    CatalogSetSummary(
                        remoteId = "sample",
                        title = "Базовый набор",
                        description = "Начальный уровень",
                        version = 1,
                        cardCount = 24,
                    ),
                ),
            ),
            onAction = {},
            onMyDecksClick = {},
        )
    }
}
