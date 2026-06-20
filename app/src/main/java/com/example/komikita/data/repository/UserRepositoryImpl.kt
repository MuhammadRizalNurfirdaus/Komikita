package com.example.komikita.data.repository

import com.example.komikita.data.api.BackendApi
import com.example.komikita.data.local.dao.UserDao
import com.example.komikita.data.local.entity.UserEntity
import com.example.komikita.data.mapper.toDomainUser
import com.example.komikita.data.model.AuthRequest
import com.example.komikita.domain.model.User
import com.example.komikita.domain.model.UserRole
import android.content.Context
import com.example.komikita.domain.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementasi UserRepository.
 * Menggabungkan Firebase Auth (Google Sign-In) + data user di PostgreSQL.
 *
 * Alur login:
 * 1. User login via Google Sign-In -> dapat Firebase ID Token
 * 2. Kirim Firebase ID Token ke backend -> backend verifikasi & return JWT
 * 3. Simpan JWT + user data di Room DB lokal
 * 4. Request selanjutnya ke backend pakai JWT (bukan Firebase token)
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val backendApi: BackendApi,
    private val userDao: UserDao,
    @ApplicationContext private val context: Context
) : UserRepository {

    /** SharedPreferences untuk menyimpan flag guest mode */
    private val prefs = context.getSharedPreferences("komikita_session", Context.MODE_PRIVATE)

    override fun observeCurrentUser(): Flow<User?> {
        return userDao.getCurrentUser().map { entity ->
            entity?.let {
                User(
                    uid = it.userId,
                    email = it.email,
                    displayName = it.displayName,
                    photoUrl = it.photoUrl,
                    role = UserRole.fromString(it.role),
                    isEmailVerified = it.isEmailVerified
                )
            }
        }
    }

    /**
     * Sinkronkan user Firebase ke backend PostgreSQL.
     * Backend akan:
     * 1. Verifikasi Firebase ID Token
     * 2. Cek/buat record di tabel users
     * 3. Return JWT + data user (termasuk role)
     */
    override suspend fun syncUserToBackend(
        firebaseUid: String,
        email: String,
        displayName: String?,
        photoUrl: String?,
        firebaseToken: String
    ): Result<User> {
        return try {
            // Kirim Firebase ID Token ke backend untuk diverifikasi
            val request = AuthRequest(
                firebaseUid = firebaseUid,
                email = email,
                displayName = displayName,
                photoUrl = photoUrl,
                firebaseToken = firebaseToken // ID Token dari Firebase Auth SDK
            )

            val response = backendApi.loginOrRegister(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val authResponse = response.body()!!
                val token = authResponse.token.orEmpty()
                val backendUser = authResponse.user

                // Simpan ke Room DB lokal
                val userEntity = UserEntity(
                    userId = firebaseUid,
                    email = email,
                    displayName = displayName,
                    photoUrl = photoUrl,
                    role = backendUser?.role ?: "user",
                    authToken = token
                )
                userDao.insertUser(userEntity)

                Result.success(backendUser?.toDomainUser() ?: User(
                    uid = firebaseUid,
                    email = email,
                    displayName = displayName,
                    photoUrl = photoUrl,
                    role = UserRole.USER,
                    isEmailVerified = false
                ))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal sinkronisasi user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserRole(firebaseUid: String): Result<UserRole> {
        // Cek dari cache lokal dulu
        val localUser = userDao.getCurrentUserSync()
        if (localUser != null) {
            return Result.success(UserRole.fromString(localUser.role))
        }
        return Result.failure(Exception("User tidak ditemukan di cache"))
    }

    override suspend fun logout() {
        userDao.deleteAllUsers()
        setGuestMode(false) // Reset guest mode saat logout
    }

    override suspend fun isLoggedIn(): Boolean {
        return userDao.getCurrentUserSync() != null
    }

    override suspend fun saveAuthToken(token: String) {
        val user = userDao.getCurrentUserSync()
        user?.let { userDao.updateAuthToken(it.userId, token) }
    }

    override suspend fun getAuthToken(): String? {
        return userDao.getCurrentUserSync()?.authToken
    }

    override fun isGuestMode(): Boolean {
        return prefs.getBoolean("is_guest", false)
    }

    override fun setGuestMode(isGuest: Boolean) {
        prefs.edit().putBoolean("is_guest", isGuest).apply()
    }

    /**
     * Cek apakah user punya sesi aktif.
     * Sesi aktif = sudah login (ada user di Room) ATAU masuk sebagai guest.
     */
    override fun hasActiveSession(): Boolean {
        return prefs.getBoolean("is_guest", false) ||
               kotlinx.coroutines.runBlocking { userDao.getCurrentUserSync() != null }
    }
}
