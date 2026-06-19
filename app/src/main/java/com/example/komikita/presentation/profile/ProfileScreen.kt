package com.example.komikita.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
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
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Profile Screen - Menampilkan info user, role badge, dan aksi (settings, logout).
 *
 * Fitur:
 * - Foto profil dari Google/Firebase
 * - Role badge berwarna (Admin=Merah, Translator=Oranye, User=Biru)
 * - Tombol Translator Dashboard (muncul hanya untuk Translator/Admin)
 * - Tombol Settings & Logout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onTranslatorDashboardClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Saya") }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val user = state.user
        if (user == null) {
            // Belum login
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
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
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // === HEADER: Foto + Nama + Email ===
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
        trailingContent = { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null) },
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
        viewModelScope.launch {
            userRepository.observeCurrentUser().collect { user ->
                _state.value = ProfileState(isLoading = false, user = user)
            }
        }
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            userRepository.logout()
            onLogout()
        }
    }
}

data class ProfileState(
    val isLoading: Boolean = false,
    val user: User? = null
)
