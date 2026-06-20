package com.example.komikita.presentation.auth

import androidx.compose.foundation.layout.*
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
 * Login Screen - Halaman masuk dengan Google Sign-In + Guest Mode.
 *
 * Opsi masuk:
 * 1. "Masuk dengan Google" → Google Sign-In via Firebase Auth
 * 2. "Masuk sebagai Tamu" → Langsung masuk tanpa akun (fitur terbatas)
 *
 * Batasan Guest Mode:
 * - Bisa: Browse, Search, Baca komik
 * - Tidak bisa: Favorit, Download, Riwayat tersimpan
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGuestLogin: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
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

            Spacer(modifier = Modifier.height(12.dp))

            // === TOMBOL GUEST MODE ===
            OutlinedButton(
                onClick = { viewModel.enterAsGuest(onGuestLogin) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !state.isLoading,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    "Masuk sebagai Tamu",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Tamu bisa menjelajah & membaca komik,\ntapi tidak bisa menyimpan favorit atau riwayat.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

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
     * TODO: Hubungkan dengan Google Sign-In SDK di Activity level.
     */
    fun signInWithGoogle(onSuccess: () -> Unit) {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)

        // Placeholder: Firebase Auth perlu dikonfigurasi
        _state.value = _state.value.copy(
            isLoading = false,
            errorMessage = "Google Sign-In memerlukan konfigurasi Firebase Auth. " +
                    "Pastikan google-services.json sudah dikonfigurasi di Firebase Console."
        )
    }

    /**
     * Masuk sebagai tamu (Guest Mode).
     * Tidak memerlukan akun, tapi fitur terbatas.
     * Flag guest disimpan di SharedPreferences.
     */
    fun enterAsGuest(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            userRepository.setGuestMode(true)
            _state.value = _state.value.copy(isLoading = false)
            onSuccess()
        }
    }

    /**
     * Dipanggil dari Activity setelah Google Sign-In berhasil.
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

            // Reset guest mode jika sebelumnya guest, sekarang login
            userRepository.setGuestMode(false)

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
