package com.example.komikita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.komikita.presentation.navigation.KomikitaNavHost
import com.example.komikita.presentation.theme.KomikitaTheme
import com.example.komikita.presentation.theme.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * MainActivity - Entry point utama aplikasi KOMIKITA versi Compose.
 *
 * Alur tema:
 * 1. ThemeManager di-inject oleh Hilt (singleton)
 * 2. Saat onCreate, terapkan preferensi tema ke AppCompatDelegate
 *    (agar status bar, nav bar, dan seluruh UI ikut berubah)
 * 3. KomikitaTheme membaca StateFlow dari ThemeManager
 * 4. Saat user ubah mode di Settings → otomatis recompose seluruh UI
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** ThemeManager singleton yang mengelola preferensi Dark/Light/System */
    @Inject lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Terapkan tema SEBELUM super.onCreate agar tidak ada flash
        themeManager.applyToSystem(themeManager.themeMode.value)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KomikitaTheme(themeManager = themeManager) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    KomikitaNavHost()
                }
            }
        }
    }
}
