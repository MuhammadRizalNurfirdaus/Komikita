package com.example.komikita.presentation.settings

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Settings Screen - Pengaturan aplikasi.
 *
 * Fitur:
 * - Toggle Dark/Light mode (auto dari system default)
 * - Info aplikasi (versi, pembuat)
 * - Clear cache
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // === TEMA ===
            ListItem(
                headlineContent = { Text("Tema Gelap") },
                supportingContent = { Text("Mengikuti pengaturan sistem") },
                leadingContent = {
                    Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Switch(
                        checked = state.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                }
            )
            HorizontalDivider()

            // === CACHE ===
            ListItem(
                headlineContent = { Text("Hapus Cache") },
                supportingContent = { Text("Membersihkan data sementara aplikasi") },
                leadingContent = {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    OutlinedButton(
                        onClick = { viewModel.clearCache(context) }
                    ) {
                        Text("Bersihkan")
                    }
                }
            )
            HorizontalDivider()

            // === TENTANG APLIKASI ===
            ListItem(
                headlineContent = { Text("Tentang KOMIKITA") },
                supportingContent = {
                    Column {
                        Text("Versi 1.0")
                        Text(
                            "Aplikasi baca komik (Manga, Manhwa, Manhua)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                leadingContent = {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // === FOOTER ===
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "KOMIKITA v1.0 — Made with Kotlin + Jetpack Compose",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// --- ViewModel ---

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val prefs = context.getSharedPreferences("komikita_settings", Context.MODE_PRIVATE)

    init {
        _state.value = SettingsState(isDarkMode = prefs.getBoolean("dark_mode", false))
    }

    fun toggleDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
        _state.value = _state.value.copy(isDarkMode = enabled)
    }

    fun clearCache(context: Context) {
        viewModelScope.launch {
            try {
                context.cacheDir.deleteRecursively()
                context.cacheDir.mkdirs()
            } catch (_: Exception) {
                // Abaikan error cache
            }
        }
    }
}

data class SettingsState(
    val isDarkMode: Boolean = false
)
