package com.example.komikita.presentation.theme

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mode tema aplikasi.
 * - SYSTEM: Mengikuti pengaturan dark/light mode dari OS
 * - LIGHT: Paksa mode terang (terlepas dari pengaturan OS)
 * - DARK: Paksa mode gelap (terlepas dari pengaturan OS)
 */
enum class ThemeMode(val label: String) {
    SYSTEM("Ikuti Sistem"),
    LIGHT("Mode Terang"),
    DARK("Mode Gelap");

    companion object {
        /** Konversi dari string yang tersimpan di SharedPreferences */
        fun fromString(value: String): ThemeMode = entries.find { it.name == value } ?: SYSTEM
    }
}

/**
 * ThemeManager - Mengelola preferensi tema (Dark/Light/System) secara global.
 *
 * Alur:
 * 1. Saat app launch, ThemeManager dibaca dari SharedPreferences
 * 2. Mode diterjemahkan ke AppCompatDelegate.setDefaultNightMode() agar
 *    seluruh UI (termasuk status bar & navigation bar) ikut berubah
 * 3. Compose MaterialTheme membaca isDarkTheme() untuk memilih color scheme
 * 4. Saat user mengubah tema di Settings, seluruh app langsung recompose
 *
 * Penting: Menggunakan AppCompatDelegate agar theme berlaku global,
 * bukan hanya di Compose, tapi juga di View XML (jika masih ada).
 */
@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("komikita_theme", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    /** Baca mode tema dari SharedPreferences */
    private fun loadThemeMode(): ThemeMode {
        val saved = prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return ThemeMode.fromString(saved)
    }

    /**
     * Ubah mode tema dan terapkan secara global.
     * AppCompatDelegate akan trigger Activity recreation
     * sehingga seluruh UI (status bar, nav bar, Compose) ikut berubah.
     */
    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _themeMode.value = mode
        applyToSystem(mode)
    }

    /**
     * Terapkan mode ke AppCompatDelegate.
     * Dipanggil saat app startup dan saat user mengubah preferensi.
     */
    fun applyToSystem(mode: ThemeMode) {
        val nightMode = when (mode) {
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    /**
     * Cek apakah tema saat ini gelap.
     * Digunakan oleh KomikitaTheme untuk memilih color scheme.
     */
    fun isDarkTheme(systemDark: Boolean): Boolean {
        return when (_themeMode.value) {
            ThemeMode.SYSTEM -> systemDark
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
    }
}
