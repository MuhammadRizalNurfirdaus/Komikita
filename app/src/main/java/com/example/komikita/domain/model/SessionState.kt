package com.example.komikita.domain.model

/**
 * Status sesi user saat ini.
 * Digunakan oleh SplashViewModel untuk menentukan navigasi awal:
 * - LOGGED_IN → langsung ke Home
 * - GUEST → langsung ke Home (dengan batasan fitur)
 * - NOT_LOGGED_IN → ke halaman Login
 * - LOADING → masih mengecek sesi
 */
sealed class SessionState {

    /** User sudah login via Google Sign-In, sesi aktif */
    data class LoggedIn(val user: User) : SessionState()

    /** User memilih masuk sebagai tamu (tanpa akun) */
    data object Guest : SessionState()

    /** Belum login dan belum memilih guest */
    data object NotLoggedIn : SessionState()

    /** Sedang mengecek status sesi (splash loading) */
    data object Loading : SessionState()
}
