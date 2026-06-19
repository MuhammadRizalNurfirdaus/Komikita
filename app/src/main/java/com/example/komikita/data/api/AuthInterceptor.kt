package com.example.komikita.data.api

import com.example.komikita.data.local.dao.UserDao
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp Interceptor untuk otomatis menyematkan JWT token di setiap request ke Backend API.
 *
 * CARA KERJA:
 * 1. Interceptor ini dipasang HANYA pada OkHttpClient untuk Backend API (Sumber B).
 *    Scraper API (Sumber A) TIDAK menggunakan interceptor ini karena bersifat publik.
 * 2. Sebelum setiap request dikirim, interceptor membaca JWT dari Room DB (UserDao).
 * 3. Jika token ada, tambahkan header "Authorization: Bearer <token>".
 * 4. Jika request ditolak (HTTP 401 Unauthorized), hapus token lokal (force logout).
 *
 * KEUNTUNGAN vs manual header di setiap endpoint:
 * - Tidak perlu menulis @Header("Authorization") di setiap fungsi BackendApi
 * - Token selalu ter-sinkron (dibaca dari DB setiap request)
 * - Penanganan 401 terpusat di satu tempat
 *
 * CATATAN KEAMANAN:
 * - JWT disimpan di Room DB lokal (bukan SharedPreferences) untuk keamanan lebih baik
 * - runBlocking digunakan karena Interceptor.synchronous() tidak mendukung suspend
 *   Ini aman karena Room query sederhana (satu row) dan sudah dioptimasi
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val userDao: UserDao
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Ambil JWT token dari Room DB secara synchronous
        // (OkHttp Interceptor bersifat synchronous, tidak bisa suspend)
        val token = runBlocking {
            userDao.getCurrentUserSync()?.authToken
        }

        // Jika tidak ada token, kirim request apa adanya (endpoint publik)
        if (token.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        // Sematkan JWT token di header Authorization
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            // Tambahkan header tambahan untuk keamanan
            .header("Accept", "application/json")
            .build()

        val response = chain.proceed(authenticatedRequest)

        // Penanganan HTTP 401 Unauthorized secara terpusat:
        // Jika backend menolak token (expired/invalid), hapus token dari DB lokal
        // agar user dipaksa login ulang
        if (response.code == 401) {
            runBlocking {
                // Hapus token yang sudah invalid
                userDao.getCurrentUserSync()?.let { user ->
                    userDao.updateAuthToken(user.userId, "")
                }
            }
        }

        return response
    }
}
