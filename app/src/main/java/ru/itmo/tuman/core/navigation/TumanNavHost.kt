package ru.itmo.tuman.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.activity.compose.ReportDrawnWhen
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.compose.runtime.collectAsState
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import ru.itmo.tuman.features.catalog.ui.CatalogScreen
import ru.itmo.tuman.features.decks.ui.deck.DeckScreen
import ru.itmo.tuman.features.decks.ui.edit.CardEditScreen
import ru.itmo.tuman.features.decks.ui.list.DecksScreen
import ru.itmo.tuman.features.home.ui.HomeScreen
import ru.itmo.tuman.features.home.ui.HomeUiState
import ru.itmo.tuman.features.home.ui.HomeViewModel
import ru.itmo.tuman.features.progress.ui.ProgressScreen
import ru.itmo.tuman.features.progress.ui.AchievementsScreen
import ru.itmo.tuman.features.review.ui.ReviewScreen
import ru.itmo.tuman.features.settings.ui.SettingsScreen

/**
 * Граф навигации: какой маршрут открывает какой экран, и нижняя панель вкладок.
 * Экраны не знают про navController — они получают коллбэки (onBack, onDeckClick…),
 * а переходы описаны только здесь.
 */
@Composable
fun TumanNavHost(navController: NavHostController = rememberNavController()) {
    val goBack: () -> Unit = { if (navController.canNavigate()) navController.popBackStack() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val homeViewModel: HomeViewModel = koinViewModel()
    val homeState by homeViewModel.uiState.collectAsState()
    ReportDrawnWhen { homeState != HomeUiState.Loading || currentDestination?.hasRoute<HomeRoute>() == false }

    Scaffold(
        bottomBar = {
            if (currentDestination.isTab() &&
                (currentDestination?.hasRoute<HomeRoute>() != true || homeState is HomeUiState.Dashboard)
            ) {
                TumanBottomBar(currentDestination) { route -> navController.openTab(route) }
            }
        },
    ) { innerPadding ->
        // Берём только нижний отступ: верхний учтёт шапка самого экрана. consumeWindowInsets
        // говорит вложенному Scaffold, что низ уже занят, иначе отступ посчитается дважды.
        val bottomPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding())
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier
                .padding(bottomPadding)
                .consumeWindowInsets(bottomPadding),
        ) {
            composable<HomeRoute> {
                HomeScreen(
                    state = homeState,
                    onSelectGoal = homeViewModel::selectGoal,
                    onCompleteOnboarding = homeViewModel::completeOnboarding,
                    onCatalogClick = { if (navController.canNavigate()) navController.openTab(CatalogRoute) },
                    onCreateDeckClick = {
                        if (navController.canNavigate()) navController.openTab(DecksRoute(createOnOpen = true))
                    },
                    onDecksClick = { if (navController.canNavigate()) navController.openTab(DecksRoute()) },
                    onDeckClick = { deckId ->
                        if (navController.canNavigate()) navController.navigate(DeckRoute(deckId))
                    },
                    onProgressClick = { if (navController.canNavigate()) navController.openTab(ProgressRoute) },
                    onStartReviewClick = { if (navController.canNavigate()) navController.navigate(ReviewRoute()) },
                    onSettingsClick = { if (navController.canNavigate()) navController.navigate(SettingsRoute) },
                )
            }
            composable<DecksRoute> { entry ->
                DecksScreen(
                    createOnOpen = entry.toRoute<DecksRoute>().createOnOpen,
                    onCatalogClick = { if (navController.canNavigate()) navController.openTab(CatalogRoute) },
                    onReviewClick = { deckId ->
                        if (navController.canNavigate()) navController.navigate(ReviewRoute(deckId))
                    },
                    onDeckClick = { deckId ->
                        if (navController.canNavigate()) navController.navigate(DeckRoute(deckId))
                    },
                )
            }
            composable<DeckRoute> { entry ->
                val route = entry.toRoute<DeckRoute>()
                DeckScreen(
                    deckId = route.deckId,
                    onCardClick = { cardId ->
                        if (navController.canNavigate()) {
                            navController.navigate(CardEditRoute(deckId = route.deckId, cardId = cardId))
                        }
                    },
                    onAddCardClick = {
                        if (navController.canNavigate()) navController.navigate(CardEditRoute(deckId = route.deckId))
                    },
                    // «Учить» запускает повторение только этого набора (#29)
                    onStudyClick = {
                        if (navController.canNavigate()) navController.navigate(ReviewRoute(deckId = route.deckId))
                    },
                    onBack = goBack,
                )
            }
            composable<CardEditRoute> { entry ->
                val route = entry.toRoute<CardEditRoute>()
                CardEditScreen(deckId = route.deckId, cardId = route.cardId, onBack = goBack)
            }
            composable<CatalogRoute> {
                CatalogScreen(
                    onMyDecksClick = {
                        if (navController.canNavigate()) navController.openTab(DecksRoute())
                    },
                )
            }
            composable<ReviewRoute> { entry ->
                val route = entry.toRoute<ReviewRoute>()
                ReviewScreen(
                    deckId = route.deckId,
                    onBack = goBack,
                    onHome = {
                        if (navController.canNavigate()) {
                            navController.navigate(HomeRoute) {
                                popUpTo<HomeRoute> { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    },
                    onProgress = {
                        if (navController.canNavigate()) navController.navigate(ProgressRoute)
                    },
                )
            }
            composable<ProgressRoute> {
                ProgressScreen(
                    onAchievementsClick = {
                        if (navController.canNavigate()) navController.navigate(AchievementsRoute)
                    },
                    onStartReviewClick = {
                        if (navController.canNavigate()) navController.navigate(ReviewRoute())
                    },
                )
            }
            composable<AchievementsRoute> {
                AchievementsScreen(onBack = goBack)
            }
            composable<SettingsRoute> { SettingsScreen(onBack = goBack) }
        }
    }
}

/**
 * Открывает вкладку. popUpTo оставляет под ней только главный экран, поэтому «Назад» с любой
 * вкладки ведёт на главную. launchSingleTop не даёт открыть ту же вкладку второй раз.
 */
private fun NavHostController.openTab(route: Any) {
    navigate(route) {
        popUpTo<HomeRoute>()
        launchSingleTop = true
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
