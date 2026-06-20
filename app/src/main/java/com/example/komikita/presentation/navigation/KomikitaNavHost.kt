package com.example.komikita.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import com.example.komikita.presentation.splash.SplashScreen
import com.example.komikita.presentation.translator.TranslatorDashboardScreen

/**
 * NavHost utama aplikasi KOMIKITA.
 *
 * Alur navigasi sesi:
 * 1. Splash Screen → Cek sesi (logged in / guest / belum masuk)
 * 2. Jika sudah punya sesi → Home (tab utama)
 * 3. Jika belum → Login Screen (Google Sign-In / Guest Mode)
 *
 * Bottom Bar: Home, Favorit, Riwayat, Profil (4 tab utama)
 * Push screens: Detail, Reader, Search, Settings, Translator Dashboard
 */
@Composable
fun KomikitaNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Bottom bar hanya muncul di 4 tab utama (BUKAN splash/login)
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
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(padding)
        ) {
            // ═══════════════════════════════════════
            // SPLASH SCREEN - Cek sesi user
            // ═══════════════════════════════════════
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToHome = {
                        // User sudah punya sesi (login/guest) → langsung Home
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        // Belum ada sesi → Login Screen
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // ═══════════════════════════════════════
            // LOGIN SCREEN - Google Sign-In / Guest
            // ═══════════════════════════════════════
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onGuestLogin = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // ═══════════════════════════════════════
            // TAB: HOME - Feed komik hybrid
            // ═══════════════════════════════════════
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

            // ═══════════════════════════════════════
            // TAB: FAVORIT - Bookmark komik
            // ═══════════════════════════════════════
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onBackClick = { navController.popBackStack() },
                    onKomikClick = { slug ->
                        navController.navigate(Screen.Detail.createRoute(slug))
                    },
                    onLoginClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // ═══════════════════════════════════════
            // TAB: RIWAYAT - History baca
            // ═══════════════════════════════════════
            composable(Screen.History.route) {
                HistoryScreen(
                    onBackClick = { navController.popBackStack() },
                    onHistoryClick = { chapterId ->
                        navController.navigate(Screen.Reader.createRoute(chapterId))
                    },
                    onLoginClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // ═══════════════════════════════════════
            // TAB: PROFIL - Info user / Guest state
            // ═══════════════════════════════════════
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
                        // Kembali ke splash setelah logout (cek ulang sesi)
                        navController.navigate(Screen.Splash.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onLoginClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // ═══════════════════════════════════════
            // PUSH: DETAIL KOMIK
            // ═══════════════════════════════════════
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

            // ═══════════════════════════════════════
            // PUSH: READER - Baca chapter
            // ═══════════════════════════════════════
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

            // ═══════════════════════════════════════
            // PUSH: SEARCH - Pencarian
            // ═══════════════════════════════════════
            composable(Screen.Search.route) {
                SearchScreen(
                    onKomikClick = { slug ->
                        navController.navigate(Screen.Detail.createRoute(slug))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            // ═══════════════════════════════════════
            // PUSH: SETTINGS
            // ═══════════════════════════════════════
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // ═══════════════════════════════════════
            // PUSH: TRANSLATOR DASHBOARD
            // ═══════════════════════════════════════
            composable(Screen.TranslatorDashboard.route) {
                TranslatorDashboardScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * ProfileScreen wrapper.
 * Menyambungkan ProfileScreen dari presentation/profile/ ke NavHost.
 */
@Composable
private fun ProfileScreen(
    onTranslatorDashboardClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onLogout: () -> Unit,
    onLoginClick: () -> Unit
) {
    com.example.komikita.presentation.profile.ProfileScreen(
        onTranslatorDashboardClick = onTranslatorDashboardClick,
        onSettingsClick = onSettingsClick,
        onFavoritesClick = onFavoritesClick,
        onHistoryClick = onHistoryClick,
        onLogout = onLogout,
        onLoginClick = onLoginClick
    )
}
