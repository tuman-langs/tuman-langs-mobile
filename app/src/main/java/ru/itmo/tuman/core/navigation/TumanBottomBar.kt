package ru.itmo.tuman.core.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import ru.itmo.tuman.R
import ru.itmo.tuman.core.uikit.theme.TumanTheme

private class TumanTab(val route: Any, @StringRes val label: Int, val icon: ImageVector)

private val tabs = listOf(
    TumanTab(HomeRoute, R.string.tab_home, Icons.Filled.Home),
    TumanTab(DecksRoute, R.string.tab_decks, Icons.AutoMirrored.Filled.List),
    TumanTab(CatalogRoute, R.string.tab_catalog, Icons.Filled.Search),
    TumanTab(ProgressRoute, R.string.tab_progress, Icons.Filled.Star),
)

/** true, если этот экран — одна из четырёх вкладок: только на них видна нижняя панель. */
fun NavDestination?.isTab(): Boolean = tabs.any { isTabSelected(it) }

private fun NavDestination?.isTabSelected(tab: TumanTab): Boolean =
    this?.hasRoute(tab.route::class) == true

/** Нижняя панель. Высота 66 dp из макета, системная полоса жестов идёт отдельным отступом. */
@Composable
fun TumanBottomBar(currentDestination: NavDestination?, onTabClick: (route: Any) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(color = colors.surface) {
        NavigationBar(
            modifier = Modifier
                .navigationBarsPadding()
                .height(66.dp),
            containerColor = colors.surface,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0),
        ) {
            tabs.forEach { tab ->
                val label = stringResource(tab.label)
                NavigationBarItem(
                    selected = currentDestination.isTabSelected(tab),
                    onClick = { onTabClick(tab.route) },
                    icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                    label = { Text(text = label, style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.primary,
                        selectedTextColor = colors.primary,
                        indicatorColor = colors.secondaryContainer,
                        unselectedIconColor = colors.onSurfaceVariant,
                        unselectedTextColor = colors.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TumanBottomBarPreview() {
    TumanTheme {
        TumanBottomBar(currentDestination = null, onTabClick = {})
    }
}
