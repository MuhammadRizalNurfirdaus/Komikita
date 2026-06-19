package com.example.komikita.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.komikita.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Login Screen - Halaman masuk dengan Google Sign-In.
 *
 * Flow autentikasi:
 * 1. User tap "Masuk dengan Google"
 * 2. Google Sign-In dialog muncul (Firebase Auth)
 * 3. Setelah berhasil → Firebase memberikan ID Token
 * 4. Token dikirim ke backend → backend verifikasi → JWT
 * 5. JWT disimpan di Room DB → user masuk ke Home
 *
 * Catatan: Integrasi Firebase Auth + Google Sign-In memerlukan konfigurasi
 * google-services.json di Firebase Console. UI ini siap dipakai,
 * tinggal hubungkan Firebase Auth SDK di MainActivity.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Masuk") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // === LOGO ===
            Text(
                "KOMIKITA",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Manga • Manhwa • Manhua",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // === DESKRIPSI ===
            Text(
                "Masuk untuk menyimpan riwayat baca, favorit, dan mendapatkan pengalaman terbaik.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // === TOMBOL GOOGLE SIGN-IN ===
            Button(
                onClick = { viewModel.signInWithGoogle(onLoginSuccess) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !state.isLoading,
                shape = MaterialTheme.shapes.medium
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Masuk dengan Google", style = MaterialTheme.typography.titleMedium)
                }
            }

            // === ERROR MESSAGE ===
            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        state.errorMessage!!,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // === FOOTER ===
            Text(
                "Dengan masuk, Anda menyetujui penggunaan data\nsesuai kebijakan privasi KOMIKITA.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

// --- ViewModel ---

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    /**
     * Trigger Google Sign-In flow.
     *
     * Implementasi sebenarnya memerlukan Activity result launcher
     * untuk Google Sign-In intent. ViewModel ini memanggil UserRepository
     * setelah mendapat Firebase ID Token dari Google.
     *
     * TODO: Hubungkan dengan Google Sign-In SDK di Activity/Fragment level.
     * Setelah Google berhasil, panggil handleGoogleSignInResult() di bawah.
     */
    fun signInWithGoogle(onSuccess: () -> Unit) {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)

        // Placeholder: Di implementasi nyata, ini akan dipanggil setelah Google Sign-In berhasil.
        // Untuk sekarang, tampilkan pesan bahwa Firebase Auth perlu dikonfigurasi.
        _state.value = _state.value.copy(
            isLoading = false,
            errorMessage = "Google Sign-In memerlukan konfigurasi Firebase Auth. " +
                    "Pastikan google-services.json sudah dikonfigurasi di Firebase Console."
        )
    }

    /**
     * Dipanggil dari Activity setelah Google Sign-In berhasil.
     * Menerima Firebase ID Token untuk dikirim ke backend.
     */
    fun handleGoogleSignInResult(
        firebaseUid: String,
        email: String,
        displayName: String?,
        photoUrl: String?,
        firebaseToken: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            userRepository.syncUserToBackend(
                firebaseUid = firebaseUid,
                email = email,
                displayName = displayName,
                photoUrl = photoUrl,
                firebaseToken = firebaseToken
            ).onSuccess {
                _state.value = _state.value.copy(isLoading = false)
                onSuccess()
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal masuk: ${error.message}"
                )
            }
        }
    }
}

data class LoginState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
