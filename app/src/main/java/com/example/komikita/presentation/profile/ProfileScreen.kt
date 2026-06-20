package com.example.komikita.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.example.komikita.domain.model.User
import com.example.komikita.domain.model.UserRole
import com.example.komikita.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Profile Screen - Menampilkan info user dengan 3 kondisi:
 *
 * 1. LOGGED IN: Foto profil, nama, email, role badge, menu lengkap
 * 2. GUEST MODE: Banner "Belum Login" + tombol Google Sign-In
 * 3. NOT LOGGED IN: (seharusnya tidak tercapai karena redirect ke Login)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onTranslatorDashboardClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onLogout: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") }
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        // ═══════════════════════════════════════
        // GUEST MODE: Belum login, tampilkan banner + tombol login
        // ═══════════════════════════════════════
        if (state.isGuest) {
            GuestProfileContent(
                modifier = Modifier.fillMaxSize().padding(padding),
                onLoginClick = onLoginClick,
                onSettingsClick = onSettingsClick
            )
            return@Scaffold
        }

        val user = state.user
        // ═══════════════════════════════════════
        // NOT LOGGED IN: User null tapi bukan guest (edge case)
        // ═══════════════════════════════════════
        if (user == null) {
            NotLoggedInContent(
                modifier = Modifier.fillMaxSize().padding(padding),
                onLoginClick = onLoginClick
            )
            return@Scaffold
        }

        // ═══════════════════════════════════════
        // LOGGED IN: Profil lengkap dengan semua fitur
        // ═══════════════════════════════════════
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // === HEADER: Foto + Nama + Email + Role ===
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = user.displayName,
                        modifier = Modifier.size(64.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            user.displayName ?: "User",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        RoleBadge(user.role)
                    }
                }
            }

            // === MENU ITEMS ===
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        NavigationMenuItem(
                            icon = Icons.Default.Favorite,
                            label = "Favorit Saya",
                            onClick = onFavoritesClick
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        NavigationMenuItem(
                            icon = Icons.AutoMirrored.Filled.List,
                            label = "Riwayat Baca",
                            onClick = onHistoryClick
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        NavigationMenuItem(
                            icon = Icons.Default.Settings,
                            label = "Pengaturan",
                            onClick = onSettingsClick
                        )
                    }
                }
            }

            // === TRANSLATOR DASHBOARD (hanya untuk Translator/Admin) ===
            if (user.role == UserRole.TRANSLATOR || user.role == UserRole.ADMIN) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Content Manager",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Upload komik custom dan kelola konten",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onTranslatorDashboardClick,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Translator Dashboard")
                            }
                        }
                    }
                }
            }

            // === LOGOUT ===
            item {
                OutlinedButton(
                    onClick = { viewModel.logout(onLogout) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Keluar")
                }
            }
        }
    }
}

/**
 * Konten profil untuk mode Guest.
 * Menampilkan banner "Belum Login" + tombol Google Sign-In.
 */
@Composable
private fun GuestProfileContent(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // === BANNER GUEST ===
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Mode Tamu",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Anda sedang menjelajah sebagai tamu.\nLogin untuk mengakses semua fitur.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // === TOMBOL LOGIN ===
        item {
            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Masuk dengan Google", style = MaterialTheme.typography.titleMedium)
            }
        }

        // === INFO FITUR TERKUNCI ===
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Fitur yang tersedia:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FeatureRow(Icons.Default.CheckCircle, "Jelajahi semua komik", true)
                    FeatureRow(Icons.Default.CheckCircle, "Baca & cari komik", true)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Login untuk:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FeatureRow(Icons.Default.Close, "Simpan favorit", false)
                    FeatureRow(Icons.Default.Close, "Riwayat bacaan", false)
                    FeatureRow(Icons.Default.Close, "Download offline", false)
                }
            }
        }

        // === SETTINGS (tetap bisa diakses guest) ===
        item {
            OutlinedButton(
                onClick = onSettingsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pengaturan")
            }
        }
    }
}

/**
 * Baris fitur: ikon centang/silang + teks.
 */
@Composable
private fun FeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    available: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (available) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (available) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

/**
 * Konten jika user null tapi bukan guest (edge case, redirect ke login).
 */
@Composable
private fun NotLoggedInContent(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Belum login", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Login untuk menyimpan riwayat dan favorit",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onLoginClick) {
                Text("Masuk Sekarang")
            }
        }
    }
}

/**
 * Badge role user dengan warna berbeda.
 */
@Composable
private fun RoleBadge(role: UserRole) {
    val (color, label) = when (role) {
        UserRole.ADMIN -> MaterialTheme.colorScheme.error to "Admin"
        UserRole.TRANSLATOR -> MaterialTheme.colorScheme.tertiary to "Translator"
        UserRole.USER -> MaterialTheme.colorScheme.primary to "User"
    }
    Surface(color = color, shape = MaterialTheme.shapes.extraSmall) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

/**
 * Item menu navigasi di profil (ikon + label + arrow).
 */
@Composable
private fun NavigationMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary) },
        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

// --- ViewModel ---

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState(isLoading = true))
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    /**
     * Load profil user:
     * - Jika guest mode → set isGuest = true
     * - Jika ada user → set user data
     */
    private fun loadProfile() {
        viewModelScope.launch {
            // Cek guest mode
            if (userRepository.isGuestMode()) {
                _state.value = ProfileState(isLoading = false, isGuest = true)
                return@launch
            }

            // Observasi user yang login
            userRepository.observeCurrentUser().collect { user ->
                _state.value = ProfileState(isLoading = false, user = user, isGuest = false)
            }
        }
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            userRepository.logout() // Hapus user dari Room + reset guest flag
            onLogout()
        }
    }
}

data class ProfileState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val isGuest: Boolean = false
)
