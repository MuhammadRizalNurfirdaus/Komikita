package com.example.komikita.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.komikita.presentation.auth.LoginScreen
import com.example.komikita.presentation.components.KomikitaBottomBar
import com.example.komikita.presentation.detail.DetailScreen
import com.example.komikita.presentation.favorites.FavoritesScreen
import com.example.komikita.presentation.history.HistoryScreen
import com.example.komikita.presentation.home.HomeScreen
import com.example.komikita.presentation.reader.ReaderScreen
import com.example.komikita.presentation.search.SearchScreen
import com.example.komikita.presentation.settings.SettingsScreen
import com.example.komikita.presentation.translator.TranslatorDashboardScreen

/**
 * NavHost utama aplikasi KOMIKITA.
 *
 * Struktur navigasi:
 * - Bottom Bar: Home, Favorit, Riwayat, Profil (4 tab utama)
 * - Push screens: Detail, Reader, Search, Login, Settings, Translator Dashboard
 *
 * @param startRoute Rute awal (default: Home)
 */
@Composable
fun KomikitaNavHost(startRoute: String = Screen.Home.route) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Bottom bar hanya muncul di 4 tab utama
    val bottomBarRoutes = listOf(
        Screen.Home.route,
        Screen.Favorites.route,
        Screen.History.route,
        Screen.Profile.route
    )
    val showBottomBar = currentRoute in bottomBarRoutes
    val selectedTab = bottomBarRoutes.indexOf(currentRoute).coerceAtLeast(0)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                KomikitaBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { index ->
                        val route = bottomBarRoutes[index]
                        if (route != currentRoute) {
                            // Navigasi ke tab tanpa menambah back stack
                            navController.navigate(route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier.padding(padding)
        ) {
            // === TAB: HOME ===
            composable(Screen.Home.route) {
                HomeScreen(
                    onKomikClick = { slug ->
                        navController.navigate(Screen.Detail.createRoute(slug))
                    },
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                    }
                )
            }

            // === TAB: FAVORIT ===
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onBackClick = { navController.popBackStack() },
                    onKomikClick = { slug ->
                        navController.navigate(Screen.Detail.createRoute(slug))
                    }
                )
            }

            // === TAB: RIWAYAT ===
            composable(Screen.History.route) {
                HistoryScreen(
                    onBackClick = { navController.popBackStack() },
                    onHistoryClick = { chapterId ->
                        navController.navigate(Screen.Reader.createRoute(chapterId))
                    }
                )
            }

            // === TAB: PROFIL ===
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onTranslatorDashboardClick = {
                        navController.navigate(Screen.TranslatorDashboard.route)
                    },
                    onSettingsClick = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onFavoritesClick = {
                        navController.navigate(Screen.Favorites.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onHistoryClick = {
                        navController.navigate(Screen.History.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onLogout = {
                        // Kembali ke home setelah logout
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // === PUSH: DETAIL ===
            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("slug") { type = NavType.StringType })
            ) { backStackEntry ->
                val slug = backStackEntry.arguments?.getString("slug").orEmpty()
                DetailScreen(
                    slug = slug,
                    onBackClick = { navController.popBackStack() },
                    onChapterClick = { chapterId ->
                        navController.navigate(Screen.Reader.createRoute(chapterId))
                    }
                )
            }

            // === PUSH: READER ===
            composable(
                route = Screen.Reader.route,
                arguments = listOf(navArgument("chapterId") { type = NavType.StringType })
            ) { backStackEntry ->
                val chapterId = backStackEntry.arguments?.getString("chapterId").orEmpty()
                ReaderScreen(
                    chapterId = chapterId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // === PUSH: SEARCH ===
            composable(Screen.Search.route) {
                SearchScreen(
                    onKomikClick = { slug ->
                        navController.navigate(Screen.Detail.createRoute(slug))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            // === PUSH: LOGIN ===
            composable(Screen.Login.route) {
                LoginScreen(
                    onBackClick = { navController.popBackStack() },
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // === PUSH: SETTINGS ===
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // === PUSH: TRANSLATOR DASHBOARD ===
            composable(Screen.TranslatorDashboard.route) {
                TranslatorDashboardScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * ProfileScreen wrapper - perlu import karena didefinisikan di file ProfileScreen.kt
 * dengan fungsi composable top-level.
 */
@Composable
private fun ProfileScreen(
    onTranslatorDashboardClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onLogout: () -> Unit
) {
    com.example.komikita.presentation.profile.ProfileScreen(
        onTranslatorDashboardClick = onTranslatorDashboardClick,
        onSettingsClick = onSettingsClick,
        onFavoritesClick = onFavoritesClick,
        onHistoryClick = onHistoryClick,
        onLogout = onLogout
    )
}
