package com.example.komikita.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.komikita.presentation.home.HomeScreen
import com.example.komikita.presentation.detail.DetailScreen
import com.example.komikita.presentation.reader.ReaderScreen
import com.example.komikita.presentation.translator.TranslatorDashboardScreen
import com.example.komikita.presentation.search.SearchScreen

/**
 * NavHost utama aplikasi KOMIKITA.
 * Mengatur semua navigasi antar screen menggunakan Compose Navigation.
 *
 * @param navController Controller navigasi dari Compose
 */
@Composable
fun KomikitaNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // === HOME SCREEN ===
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

        // === DETAIL SCREEN ===
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

        // === READER SCREEN ===
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

        // === SEARCH SCREEN ===
        composable(Screen.Search.route) {
            SearchScreen(
                onKomikClick = { slug ->
                    navController.navigate(Screen.Detail.createRoute(slug))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // === TRANSLATOR DASHBOARD ===
        composable(Screen.TranslatorDashboard.route) {
            TranslatorDashboardScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
