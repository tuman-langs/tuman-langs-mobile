package ru.itmo.tuman.core.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import ru.itmo.tuman.features.catalog.ui.CatalogScreen
import ru.itmo.tuman.features.decks.ui.deck.DeckScreen
import ru.itmo.tuman.features.decks.ui.edit.CardEditScreen
import ru.itmo.tuman.features.decks.ui.list.DecksScreen
import ru.itmo.tuman.features.home.ui.HomeScreen
import ru.itmo.tuman.features.progress.ui.ProgressScreen
import ru.itmo.tuman.features.review.ui.ReviewScreen
import ru.itmo.tuman.features.settings.ui.SettingsScreen

/**
 * Граф навигации: какой маршрут открывает какой экран.
 * Экраны не знают про navController — они получают коллбэки (onBack, onDecksClick…),
 * а переходы описаны только здесь.
 */
@Composable
fun TumanNavHost(navController: NavHostController = rememberNavController()) {
    val goBack: () -> Unit = { if (navController.canNavigate()) navController.popBackStack() }

    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                onStartReviewClick = { if (navController.canNavigate()) navController.navigate(ReviewRoute) },
                onDecksClick = { if (navController.canNavigate()) navController.navigate(DecksRoute) },
                onCatalogClick = { if (navController.canNavigate()) navController.navigate(CatalogRoute) },
                onProgressClick = { if (navController.canNavigate()) navController.navigate(ProgressRoute) },
                onSettingsClick = { if (navController.canNavigate()) navController.navigate(SettingsRoute) },
            )
        }
        composable<DecksRoute> {
            DecksScreen(
                onDeckClick = { deckId ->
                    if (navController.canNavigate()) navController.navigate(DeckRoute(deckId))
                },
                onBack = goBack,
            )
        }
        composable<DeckRoute> { entry ->
            val route = entry.toRoute<DeckRoute>()
            DeckScreen(
                deckId = route.deckId,
                onAddCardClick = {
                    if (navController.canNavigate()) navController.navigate(CardEditRoute(deckId = route.deckId))
                },
                onBack = goBack,
            )
        }
        composable<CardEditRoute> {
            CardEditScreen(onBack = goBack)
        }
        composable<CatalogRoute> { CatalogScreen(onBack = goBack) }
        composable<ReviewRoute> { ReviewScreen(onBack = goBack) }
        composable<ProgressRoute> { ProgressScreen(onBack = goBack) }
        composable<SettingsRoute> { SettingsScreen(onBack = goBack) }
    }
}

/**
 * true, если текущий экран полностью открыт и не уезжает с анимацией.
 * Пока идёт анимация перехода, кнопки старого экрана ещё нажимаются: без этой проверки
 * двойное нажатие «Назад» убирает и главный экран (остаётся пустой), а двойное нажатие
 * кнопки открывает один экран два раза.
 */
private fun NavHostController.canNavigate() =
    currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED
