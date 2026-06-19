package com.example.komikita.presentation.navigation

/**
 * Definisi rute navigasi untuk Compose Navigation.
 * Menggunakan sealed class agar type-safe dan mudah di-maintain.
 *
 * Setiap route bisa memiliki parameter (contoh: "detail/{slug}").
 */
sealed class Screen(val route: String) {
    // Autentikasi
    object Login : Screen("login")

    // Halaman utama
    object Home : Screen("home")

    // Detail komik (parameter: slug)
    object Detail : Screen("detail/{slug}") {
        fun createRoute(slug: String) = "detail/$slug"
    }

    // Reader (parameter: chapterId)
    object Reader : Screen("reader/{chapterId}") {
        fun createRoute(chapterId: String) = "reader/$chapterId"
    }

    // Pencarian
    object Search : Screen("search")

    // Translator Dashboard (hanya muncul untuk role Translator/Admin)
    object TranslatorDashboard : Screen("translator_dashboard")

    // Profil user
    object Profile : Screen("profile")

    // Riwayat baca
    object History : Screen("history")

    // Daftar favorit
    object Favorites : Screen("favorites")

    // Daftar unduhan
    object Downloads : Screen("downloads")

    // Pengaturan
    object Settings : Screen("settings")

    // Genre browsing
    object Genres : Screen("genres")
}
