package com.example.komikita.data.repository

import com.example.komikita.data.api.BackendApi
import com.example.komikita.data.mapper.toDomainCustomComic
import com.example.komikita.data.mapper.toDomainChapterPages
import com.example.komikita.data.model.AddChapterRequest
import com.example.komikita.data.model.HideComicRequest
import com.example.komikita.data.model.UploadComicRequest
import com.example.komikita.data.local.dao.UserDao
import com.example.komikita.domain.model.CustomChapter
import com.example.komikita.domain.model.CustomComic
import com.example.komikita.domain.repository.CustomComicRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementasi CustomComicRepository.
 * Menangani fitur Translator: upload komik, tambah chapter, hide/unhide.
 *
 * AUTENTIKASI:
 * JWT token disematkan SECARA OTOMATIS oleh AuthInterceptor di OkHttp client.
 * Repository TIDAK perlu mengirim token manual — interceptor membaca dari Room DB.
 * getAuthTokenOrThrow() tetap dipanggil sebagai fail-fast check (gagal cepat jika belum login).
 */
@Singleton
class CustomComicRepositoryImpl @Inject constructor(
    private val backendApi: BackendApi,
    private val userDao: UserDao
) : CustomComicRepository {

    /**
     * Upload komik baru ke PostgreSQL via backend API.
     * Translator hanya perlu mengirim: Judul, Slug, dan List URL Gambar.
     *
     * @param imageUrls Bisa 50+ URL gambar (bulk paste dari Translator Dashboard)
     */
    override suspend fun uploadComic(
        title: String,
        slug: String,
        imageUrls: List<String>,
        coverUrl: String?,
        type: String?
    ): Result<CustomComic> {
        return try {
            // Fail-fast: pastikan user sudah login sebelum upload
            getAuthTokenOrThrow()

            val request = UploadComicRequest(
                title = title,
                slug = slug,
                coverUrl = coverUrl,
                type = type,
                imageUrls = imageUrls
            )

            // JWT otomatis disematkan oleh AuthInterceptor
            val response = backendApi.uploadComic(request)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.toDomainCustomComic())
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal upload komik"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Tambah chapter baru ke komik yang sudah ada.
     */
    override suspend fun addChapter(
        comicSlug: String,
        chapterNumber: String,
        imageUrls: List<String>
    ): Result<CustomChapter> {
        return try {
            // Fail-fast: pastikan user sudah login
            getAuthTokenOrThrow()

            val request = AddChapterRequest(
                chapterNumber = chapterNumber,
                title = "Chapter $chapterNumber",
                imageUrls = imageUrls
            )

            // JWT otomatis disematkan oleh AuthInterceptor
            val response = backendApi.addChapter(comicSlug, request)
            if (response.isSuccessful && response.body()?.data != null) {
                val dto = response.body()!!.data!!
                Result.success(
                    CustomChapter(
                        id = dto.id,
                        comicId = dto.comicId,
                        chapterNumber = dto.chapterNumber.orEmpty(),
                        title = dto.title,
                        pages = dto.pages?.map { it.imageUrl.orEmpty() } ?: emptyList()
                    )
                )
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal tambah chapter"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Hide/unhide komik dari scraper.
     * Translator bisa menyembunyikan konten yang tidak sesuai.
     */
    override suspend fun hideScraperComic(slug: String, hide: Boolean): Result<Boolean> {
        return try {
            // Fail-fast: pastikan user sudah login
            getAuthTokenOrThrow()

            // JWT otomatis disematkan oleh AuthInterceptor
            val response = backendApi.hideScraperComic(
                HideComicRequest(slug, hide)
            )

            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Gagal ${if (hide) "hide" else "unhide"} komik"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getHiddenSlugs(): Result<List<String>> {
        return try {
            val response = backendApi.getHiddenSlugs()
            if (response.isSuccessful) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }

    override suspend fun deleteComic(comicId: String): Result<Boolean> {
        return try {
            // Fail-fast: pastikan user sudah login
            getAuthTokenOrThrow()
            // JWT otomatis disematkan oleh AuthInterceptor
            val response = backendApi.deleteComic(comicId)
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Ambil JWT token dari cache lokal.
     * Throw exception jika belum login.
     */
    private suspend fun getAuthTokenOrThrow(): String {
        val user = userDao.getCurrentUserSync()
            ?: throw IllegalStateException("User belum login")
        return user.authToken ?: throw IllegalStateException("Token tidak ditemukan, silakan login ulang")
    }
}
