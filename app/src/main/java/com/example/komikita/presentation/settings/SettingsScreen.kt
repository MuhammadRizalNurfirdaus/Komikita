package com.example.komikita.presentation.settings

import android.content.Context
import androidx.compose.foundation.clickable
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
import com.example.komikita.presentation.theme.ThemeManager
import com.example.komikita.presentation.theme.ThemeMode
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
 * - Mode tema: Ikuti Sistem / Mode Terang / Mode Gelap
 * - Hapus cache aplikasi
 * - Info aplikasi
 *
 * Alur tema:
 * 1. User pilih salah satu dari 3 mode → ThemeManager.setThemeMode()
 * 2. ThemeManager simpan ke SharedPreferences + terapkan AppCompatDelegate
 * 3. Seluruh UI (Compose + status bar + nav bar) langsung berubah
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
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
        },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // ═══════════════════════════════════════
            // TEMA: Pilihan 3 mode (Sistem / Terang / Gelap)
            // ═══════════════════════════════════════
            Text(
                "Tampilan",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            // Opsi 1: Ikuti Sistem
            ThemeOptionItem(
                icon = Icons.Default.SettingsBrightness,
                title = "Ikuti Sistem",
                subtitle = "Otomatis mengikuti mode gelap/terang dari HP",
                selected = themeMode == ThemeMode.SYSTEM,
                onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) }
            )

            // Opsi 2: Mode Terang
            ThemeOptionItem(
                icon = Icons.Default.LightMode,
                title = "Mode Terang",
                subtitle = "Selalu gunakan tampilan terang",
                selected = themeMode == ThemeMode.LIGHT,
                onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) }
            )

            // Opsi 3: Mode Gelap
            ThemeOptionItem(
                icon = Icons.Default.DarkMode,
                title = "Mode Gelap",
                subtitle = "Selalu gunakan tampilan gelap",
                selected = themeMode == ThemeMode.DARK,
                onClick = { viewModel.setThemeMode(ThemeMode.DARK) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ═══════════════════════════════════════
            // CACHE
            // ═══════════════════════════════════════
            Text(
                "Penyimpanan",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            )

            ListItem(
                headlineContent = { Text("Hapus Cache") },
                supportingContent = {
                    Text(if (state.cacheCleared) "Cache berhasil dibersihkan" else "Membersihkan data sementara aplikasi")
                },
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ═══════════════════════════════════════
            // TENTANG APLIKASI
            // ═══════════════════════════════════════
            Text(
                "Tentang",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            )

            ListItem(
                headlineContent = { Text("KOMIKITA") },
                supportingContent = {
                    Column {
                        Text("Versi 2.0", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Platform baca komik hybrid (Manga, Manhwa, Manhua)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    "KOMIKITA v2.0 — Made with Kotlin + Jetpack Compose",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Item opsi tema: ikon + judul + subtitle + radio button.
 * Diklik untuk memilih mode tema.
 */
@Composable
private fun ThemeOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                title,
                color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall) },
        leadingContent = {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            RadioButton(
                selected = selected,
                onClick = onClick
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

// --- ViewModel ---

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeManager: ThemeManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    /** Observasi mode tema dari ThemeManager secara real-time */
    val themeMode: StateFlow<ThemeMode> = themeManager.themeMode

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    /** Ubah mode tema (tersimpan di SharedPreferences + terapkan AppCompatDelegate) */
    fun setThemeMode(mode: ThemeMode) {
        themeManager.setThemeMode(mode)
    }

    fun clearCache(context: Context) {
        viewModelScope.launch {
            try {
                context.cacheDir.deleteRecursively()
                context.cacheDir.mkdirs()
                _state.value = _state.value.copy(cacheCleared = true)
            } catch (_: Exception) {
                // Abaikan error cache
            }
        }
    }
}

data class SettingsState(
    val cacheCleared: Boolean = false
)
