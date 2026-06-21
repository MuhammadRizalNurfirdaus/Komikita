package com.example.komikita.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Skema warna Light Mode (Material 3).
 */
private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    error = ErrorLight,
    onError = OnErrorLight
)

/**
 * Skema warna Dark Mode (Material 3).
 */
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    error = ErrorDark,
    onError = OnErrorDark
)

/**
 * Tema KOMIKITA menggunakan Material 3.
 *
 * Alur Dark/Light:
 * 1. Baca preferensi user dari ThemeManager (SharedPreferences)
 * 2. Mode bisa: SYSTEM (ikuti OS), LIGHT (paksa terang), DARK (paksa gelap)
 * 3. Saat user ubah mode di Settings → AppCompatDelegate diterapkan →
 *    seluruh UI (status bar, navigation bar, Compose) ikut berubah
 *
 * @param themeManager Singleton yang mengelola preferensi tema
 */
@Composable
fun KomikitaTheme(
    themeManager: ThemeManager,
    content: @Composable () -> Unit
) {
    // Observasi perubahan mode tema secara real-time
    val currentMode by themeManager.themeMode.collectAsState()
    val systemDark = isSystemInDarkTheme()

    // Tentukan apakah harus menggunakan dark mode
    val isDark = when (currentMode) {
        ThemeMode.SYSTEM -> systemDark  // Ikuti pengaturan OS
        ThemeMode.LIGHT -> false         // Paksa mode terang
        ThemeMode.DARK -> true           // Paksa mode gelap
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
