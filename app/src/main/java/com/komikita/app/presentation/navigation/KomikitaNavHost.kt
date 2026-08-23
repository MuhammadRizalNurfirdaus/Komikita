package com.komikita.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.komikita.app.domain.model.UserRole
import com.komikita.app.presentation.admin.AdminDashboardScreen
import com.komikita.app.presentation.auth.AuthViewModel
import com.komikita.app.presentation.auth.LoginScreen
import com.komikita.app.presentation.components.BottomNavBar
import com.komikita.app.presentation.detail.DetailScreen
import com.komikita.app.presentation.detail.DetailViewModel
import com.komikita.app.presentation.downloads.DownloadsScreen
import com.komikita.app.presentation.downloads.DownloadsViewModel
import com.komikita.app.presentation.favorites.FavoritesScreen
import com.komikita.app.presentation.favorites.FavoritesViewModel
import com.komikita.app.presentation.history.HistoryScreen
import com.komikita.app.presentation.history.HistoryViewModel
import com.komikita.app.presentation.home.HomeScreen
import com.komikita.app.presentation.home.HomeViewModel
import com.komikita.app.presentation.profile.ProfileScreen
import com.komikita.app.presentation.reader.ReaderScreen
import com.komikita.app.presentation.reader.ReaderViewModel
import com.komikita.app.presentation.splash.SplashScreen
import com.komikita.app.presentation.translator.TranslatorDashboardScreen
import com.komikita.app.presentation.translator.TranslatorViewModel

@Composable
fun KomikitaNavHost(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val sessionState by authViewModel.sessionState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = when (sessionState.role) {
        UserRole.ADMIN -> listOf(
            Screen.Home.route,
            Screen.Favorites.route,
            Screen.History.route,
            Screen.AdminDashboard.route,
            Screen.Profile.route
        )
        UserRole.TRANSLATOR -> listOf(
            Screen.Home.route,
            Screen.Favorites.route,
            Screen.History.route,
            Screen.TranslatorDashboard.route,
            Screen.Profile.route
        )
        UserRole.USER, UserRole.GUEST -> listOf(
            Screen.Home.route,
            Screen.Favorites.route,
            Screen.History.route,
            Screen.Downloads.route,
            Screen.Profile.route
        )
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                BottomNavBar(
                    userRole = sessionState.role,
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    isLoggedIn = sessionState.isLoggedIn,
                    onNavigateNext = { isLoggedIn ->
                        val destination = if (isLoggedIn) Screen.Home.route else Screen.Login.route
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = hiltViewModel()
                HomeScreen(
                    viewModel = homeViewModel,
                    onComicClick = { endpoint, isCustom ->
                        navController.navigate(Screen.Detail.createRoute(endpoint, isCustom))
                    }
                )
            }

            composable(Screen.Favorites.route) {
                val favoritesViewModel: FavoritesViewModel = hiltViewModel()
                FavoritesScreen(
                    isGuest = sessionState.isGuest,
                    viewModel = favoritesViewModel,
                    onLoginClick = {
                        navController.navigate(Screen.Login.route)
                    },
                    onComicClick = { endpoint, isCustom ->
                        navController.navigate(Screen.Detail.createRoute(endpoint, isCustom))
                    }
                )
            }

            composable(Screen.History.route) {
                val historyViewModel: HistoryViewModel = hiltViewModel()
                HistoryScreen(
                    isGuest = sessionState.isGuest,
                    viewModel = historyViewModel,
                    onLoginClick = {
                        navController.navigate(Screen.Login.route)
                    },
                    onReadChapterClick = { chapterEndpoint ->
                        navController.navigate(Screen.Reader.createRoute(chapterEndpoint, false))
                    }
                )
            }

            composable(Screen.Profile.route) {
                val profileViewModel: com.komikita.app.presentation.profile.ProfileViewModel = hiltViewModel()
                ProfileScreen(
                    userSession = sessionState,
                    profileViewModel = profileViewModel,
                    onLoginClick = { navController.navigate(Screen.Login.route) },
                    onManageDownloadsClick = { navController.navigate(Screen.Downloads.route) },
                    onLogoutClick = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Downloads.route) {
                val downloadsViewModel: DownloadsViewModel = hiltViewModel()
                DownloadsScreen(
                    viewModel = downloadsViewModel,
                    onBackClick = { navController.popBackStack() },
                    onReadChapterClick = { chapterEndpoint ->
                        navController.navigate(Screen.Reader.createRoute(chapterEndpoint, false))
                    }
                )
            }

            composable(
                route = Screen.Detail.route,
                arguments = listOf(
                    navArgument("endpoint") { type = NavType.StringType },
                    navArgument("isCustom") { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val endpoint = backStackEntry.arguments?.getString("endpoint") ?: ""
                val isCustom = backStackEntry.arguments?.getBoolean("isCustom") ?: false
                val detailViewModel: DetailViewModel = hiltViewModel()
                DetailScreen(
                    endpoint = endpoint,
                    isCustom = isCustom,
                    isGuest = sessionState.isGuest,
                    viewModel = detailViewModel,
                    onBackClick = { navController.popBackStack() },
                    onChapterClick = { chapterEndpoint ->
                        navController.navigate(Screen.Reader.createRoute(chapterEndpoint, isCustom))
                    },
                    onLoginClick = {
                        navController.navigate(Screen.Login.route)
                    }
                )
            }

            composable(
                route = Screen.Reader.route,
                arguments = listOf(
                    navArgument("endpoint") { type = NavType.StringType },
                    navArgument("isCustom") { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val endpoint = backStackEntry.arguments?.getString("endpoint") ?: ""
                val isCustom = backStackEntry.arguments?.getBoolean("isCustom") ?: false
                val readerViewModel: ReaderViewModel = hiltViewModel()
                ReaderScreen(
                    endpoint = endpoint,
                    isCustom = isCustom,
                    viewModel = readerViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.TranslatorDashboard.route) {
                val translatorViewModel: TranslatorViewModel = hiltViewModel()
                TranslatorDashboardScreen(
                    viewModel = translatorViewModel,
                    onPublishSuccess = {
                        navController.navigate(Screen.Home.route)
                    }
                )
            }

            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen()
            }
        }
    }
}
