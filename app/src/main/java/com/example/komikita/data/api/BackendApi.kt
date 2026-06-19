package com.example.komikita.data.api

import com.example.komikita.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Interface Retrofit untuk Backend API (Sumber B - PostgreSQL via REST).
 *
 * PENTING: Android TIDAK BOLEH koneksi langsung ke PostgreSQL Aiven!
 * Semua akses ke database harus melalui backend REST API ini.
 *
 * AUTENTIKASI:
 * JWT token disematkan SECARA OTOMATIS oleh [AuthInterceptor].
 * Tidak perlu menulis @Header("Authorization") manual di setiap endpoint.
 * Interceptor membaca token dari Room DB dan menambahkan header
 * "Authorization: Bearer <jwt>" di setiap request.
 *
 * Alur keamanan:
 * Android App -> AuthInterceptor (JWT) -> Backend REST API -> PostgreSQL Aiven
 */
interface BackendApi {

    // ==============================
    // AUTHENTICATION (publik - tidak perlu JWT)
    // ==============================

    /**
     * Login atau register user.
     * Backend akan:
     * 1. Verifikasi Firebase ID Token yang dikirim di body
     * 2. Cek apakah user sudah ada di PostgreSQL (tabel users)
     * 3. Jika belum, buat record baru dengan role "user"
     * 4. Return JWT token untuk request selanjutnya
     *
     * Endpoint ini TIDAK memerlukan JWT (user belum login saat memanggil ini).
     */
    @POST("auth/login")
    suspend fun loginOrRegister(
        @Body request: AuthRequest
    ): Response<AuthResponse>

    /**
     * Ambil profil user yang sedang login.
     * JWT otomatis disematkan oleh AuthInterceptor.
     */
    @GET("auth/me")
    suspend fun getCurrentUser(): Response<BackendResponse<BackendUserDto>>

    // ==============================
    // USER MANAGEMENT (Admin - JWT + role check di backend)
    // ==============================

    /**
     * Ambil daftar semua user (Admin only - diverifikasi backend via JWT role).
     */
    @GET("users")
    suspend fun getAllUsers(): Response<BackendResponse<List<BackendUserDto>>>

    /**
     * Ubah role user (Admin only).
     * @param body Map berisi {"role": "translator"} atau {"role": "admin"}
     */
    @PUT("users/{uid}/role")
    suspend fun updateUserRole(
        @Path("uid") firebaseUid: String,
        @Body body: Map<String, String>
    ): Response<BackendResponse<BackendUserDto>>

    // ==============================
    // CUSTOM COMICS (PostgreSQL via Backend)
    // ==============================

    /**
     * Ambil semua komik custom dari PostgreSQL.
     * Endpoint ini PUBLIC (interceptor skip jika tidak ada token).
     * Digunakan untuk merge data di Home Screen.
     */
    @GET("comics")
    suspend fun getCustomComics(): Response<CustomComicListResponse>

    /**
     * Ambil detail komik custom berdasarkan slug.
     */
    @GET("comics/{slug}")
    suspend fun getCustomComicDetail(
        @Path("slug") slug: String
    ): Response<BackendResponse<CustomComicDto>>

    /**
     * Ambil halaman chapter custom berdasarkan chapter ID.
     * Response berisi list URL gambar yang disimpan di tabel custom_pages.
     */
    @GET("comics/chapters/{chapterId}")
    suspend fun getCustomChapterPages(
        @Path("chapterId") chapterId: String
    ): Response<BackendResponse<CustomChapterDto>>

    /**
     * Upload komik baru (Translator/Admin).
     * JWT otomatis disematkan oleh AuthInterceptor.
     * Backend akan verifikasi role dari JWT sebelum mengizinkan operasi.
     *
     * Translator mengirim: Judul, Slug, dan List URL Gambar (bulk paste).
     * Gambar TIDAK disimpan di server - hanya URL string-nya di PostgreSQL.
     */
    @POST("comics")
    suspend fun uploadComic(
        @Body request: UploadComicRequest
    ): Response<BackendResponse<CustomComicDto>>

    /**
     * Tambah chapter ke komik custom yang sudah ada (Translator/Admin).
     */
    @POST("comics/{slug}/chapters")
    suspend fun addChapter(
        @Path("slug") comicSlug: String,
        @Body request: AddChapterRequest
    ): Response<BackendResponse<CustomChapterDto>>

    /**
     * Hapus komik custom (Admin/Translator - diverifikasi backend).
     */
    @DELETE("comics/{id}")
    suspend fun deleteComic(
        @Path("id") comicId: String
    ): Response<BackendResponse<Boolean>>

    // ==============================
    // HIDE/UNHIDE SCRAPER COMICS (Translator/Admin)
    // ==============================

    /**
     * Hide atau unhide komik dari scraper yang tidak sesuai.
     * Translator bisa menyembunyikan konten yang tidak diinginkan.
     * Data hide disimpan di PostgreSQL, bukan di scraper.
     */
    @POST("comics/hide")
    suspend fun hideScraperComic(
        @Body request: HideComicRequest
    ): Response<BackendResponse<Boolean>>

    /**
     * Ambil daftar slug komik scraper yang di-hide.
     * Endpoint ini PUBLIC (digunakan saat merge data di Home Screen).
     */
    @GET("comics/hidden-slugs")
    suspend fun getHiddenSlugs(): Response<HiddenSlugsResponse>
}
