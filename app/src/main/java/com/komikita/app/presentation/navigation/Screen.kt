package com.komikita.app.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object Favorites : Screen("favorites")
    object History : Screen("history")
    object Profile : Screen("profile")
    object Downloads : Screen("downloads")
    object Detail : Screen("detail/{endpoint}/{isCustom}") {
        fun createRoute(endpoint: String, isCustom: Boolean = false) = "detail/$endpoint/$isCustom"
    }
    object Reader : Screen("reader/{endpoint}/{isCustom}") {
        fun createRoute(endpoint: String, isCustom: Boolean = false) = "reader/$endpoint/$isCustom"
    }
    object TranslatorDashboard : Screen("translator_dashboard")
    object AdminDashboard : Screen("admin_dashboard")
}
