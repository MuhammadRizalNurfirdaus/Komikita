package com.example.komikita.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.komikita.domain.model.SessionState
import com.example.komikita.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Splash Screen - Menampilkan logo KOMIKITA saat mengecek status sesi.
 *
 * Alur:
 * 1. Tampilkan logo + animasi fade
 * 2. Cek sesi: sudah login? / guest? / belum pernah masuk?
 * 3. Navigasi otomatis berdasarkan hasil pengecekan:
 *    - Sudah login → Home
 *    - Guest (session masih aktif) → Home
 *    - Belum ada sesi → Login
 */
@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Animasi opacity logo
    var logoOpacity by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        logoOpacity = 1f
    }

    // Navigasi otomatis saat pengecekan sesi selesai
    LaunchedEffect(state) {
        when (state) {
            is SessionState.LoggedIn, is SessionState.Guest -> {
                delay(800) // Delay agar splash terlihat
                onNavigateToHome()
            }
            is SessionState.NotLoggedIn -> {
                delay(800)
                onNavigateToLogin()
            }
            is SessionState.Loading -> { /* tunggu */ }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "KOMIKITA",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Manga • Manhwa • Manhua",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
    }
}



// --- ViewModel ---

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    init {
        checkSession()
    }

    /**
     * Cek status sesi user:
     * 1. Jika ada user di Room DB → LoggedIn
     * 2. Jika flag guest aktif → Guest
     * 3. Jika keduanya tidak ada → NotLoggedIn
     */
    private fun checkSession() {
        viewModelScope.launch {
            delay(500) // Minimum splash duration

            // Cek apakah ada user yang login di Room DB
            val currentUser = userRepository.observeCurrentUser().first()
            if (currentUser != null) {
                _state.value = SessionState.LoggedIn(currentUser)
                return@launch
            }

            // Cek apakah mode guest masih aktif
            if (userRepository.isGuestMode()) {
                _state.value = SessionState.Guest
                return@launch
            }

            // Belum ada sesi
            _state.value = SessionState.NotLoggedIn
        }
    }
}
