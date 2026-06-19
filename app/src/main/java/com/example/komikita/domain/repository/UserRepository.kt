package com.example.komikita.domain.repository

import com.example.komikita.domain.model.User
import com.example.komikita.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

/**
 * Interface Repository untuk manajemen user.
 * Menggabungkan Firebase Auth (Google Sign-In) + data user di PostgreSQL.
 */
interface UserRepository {

    /** Observasi user yang sedang login (dari cache lokal) */
    fun observeCurrentUser(): Flow<User?>

    /** 
     * Sinkronkan data user Firebase ke PostgreSQL setelah login.
     * @param firebaseToken ID Token dari Firebase Auth SDK (diperoleh saat Google Sign-In)
     */
    suspend fun syncUserToBackend(
        firebaseUid: String,
        email: String,
        displayName: String?,
        photoUrl: String?,
        firebaseToken: String
    ): Result<User>

    /** Ambil role user dari backend */
    suspend fun getUserRole(firebaseUid: String): Result<UserRole>

    /** Logout: hapus sesi lokal + token */
    suspend fun logout()

    /** Cek apakah user sedang login */
    suspend fun isLoggedIn(): Boolean

    /** Simpan token JWT dari backend ke local */
    suspend fun saveAuthToken(token: String)

    /** Ambil token JWT yang tersimpan */
    suspend fun getAuthToken(): String?
}
