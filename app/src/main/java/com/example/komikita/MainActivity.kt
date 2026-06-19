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
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity - Entry point utama aplikasi KOMIKITA versi Compose.
 *
 * Menggunakan:
 * - @AndroidEntryPoint: Hilt inject dependencies ke Activity ini
 * - setContent: Mengatur Compose sebagai UI framework
 * - KomikitaTheme: Tema Material 3 (auto dark/light)
 * - KomikitaNavHost: Navigasi antar screen
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KomikitaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    KomikitaNavHost()
                }
            }
        }
    }
}
