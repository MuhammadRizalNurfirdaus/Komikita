package com.example.komikita.data.repository

import com.example.komikita.data.api.BackendApi
import com.example.komikita.data.api.ScraperApi
import com.example.komikita.data.mapper.*
import com.example.komikita.domain.model.*
import com.example.komikita.domain.repository.HomeFeedState
import com.example.komikita.domain.repository.KomikRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementasi KomikRepository yang menggabungkan data dari 2 sumber:
 *
 * SUMBER A: Scraper API (Read-only, konten publik)
 * SUMBER B: Backend API / PostgreSQL (Custom comics dari Translator)
 *
 * Logika Hybrid:
 * 1. Home Feed = Scraper data + Custom data - Hidden items
 * 2. Detail = Cek custom dulu, fallback ke scraper
 * 3. Chapter Pages = Cek custom dulu, fallback ke scraper
 * 4. Search = Paralel di kedua sumber, lalu gabungkan
 */
@Singleton
class KomikRepositoryImpl @Inject constructor(
    private val scraperApi: ScraperApi,
    private val backendApi: BackendApi
) : KomikRepository {

    // ============================================================
    // SUMBER A: SCRAPER API
    // ============================================================

    override suspend fun getManhwaList(page: Int): Result<List<Komik>> = safeApiCall {
        val response = scraperApi.getManhwaList(page.toString())
        if (response.isSuccessful) {
            response.body()?.data.toDomainList()
        } else {
            throw Exception("Gagal mengambil daftar manhwa: ${response.code()}")
        }
    }

    override suspend fun getManhuaList(page: Int): Result<List<Komik>> = safeApiCall {
        val response = scraperApi.getManhuaList(page.toString())
        if (response.isSuccessful) {
            response.body()?.data.toDomainList()
        } else {
            throw Exception("Gagal mengambil daftar manhua: ${response.code()}")
        }
    }

    override suspend fun getMangaList(page: Int): Result<List<Komik>> = safeApiCall {
        val response = scraperApi.getMangaList(page.toString())
        if (response.isSuccessful) {
            response.body()?.data.toDomainList()
        } else {
            throw Exception("Gagal mengambil daftar manga: ${response.code()}")
        }
    }

    override suspend fun getPopular(type: String, page: Int): Result<List<Komik>> = safeApiCall {
        val response = scraperApi.getPopular(type)
        if (response.isSuccessful) {
            response.body()?.data.toDomainList()
        } else {
            throw Exception("Gagal mengambil komik populer: ${response.code()}")
        }
    }

    override suspend fun searchKomik(query: String, page: Int): Result<List<Komik>> = safeApiCall {
        val response = scraperApi.searchKomik(query, page)
        if (response.isSuccessful) {
            response.body()?.data.toDomainList()
        } else {
            throw Exception("Gagal mencari komik: ${response.code()}")
        }
    }

    override suspend fun getKomikDetail(slug: String): Result<KomikDetail> = safeApiCall {
        val response = scraperApi.getDetail(slug)
        if (response.isSuccessful && response.body()?.data != null) {
            response.body()!!.toDomain(slug)
        } else {
            throw Exception("Gagal mengambil detail komik: ${response.code()}")
        }
    }

    override suspend fun getChapterPages(chapterSlug: String): Result<ChapterPages> = safeApiCall {
        val response = scraperApi.getChapter(chapterSlug)
        if (response.isSuccessful && response.body()?.data != null) {
            response.body()!!.toDomain()
        } else {
            throw Exception("Gagal mengambil halaman chapter: ${response.code()}")
        }
    }

    override suspend fun getGenreList(): Result<List<Genre>> = safeApiCall {
        val response = scraperApi.getGenreList()
        if (response.isSuccessful) {
            response.body()?.data?.map { it.toDomain() } ?: emptyList()
        } else {
            throw Exception("Gagal mengambil daftar genre: ${response.code()}")
        }
    }

    override suspend fun getKomikByGenre(genreSlug: String, page: Int): Result<List<Komik>> = safeApiCall {
        val response = scraperApi.getByGenre(genreSlug, page)
        if (response.isSuccessful) {
            response.body()?.data.toDomainList()
        } else {
            throw Exception("Gagal mengambil komik berdasarkan genre: ${response.code()}")
        }
    }

    // ============================================================
    // SUMBER B: CUSTOM COMICS (PostgreSQL via Backend API)
    // ============================================================

    override suspend fun getCustomComics(): Result<List<Komik>> = safeApiCall {
        val response = backendApi.getCustomComics()
        if (response.isSuccessful) {
            response.body()?.data.toDomainKomikList()
        } else {
            throw Exception("Gagal mengambil komik custom: ${response.code()}")
        }
    }

    override suspend fun getCustomComicDetail(slug: String): Result<KomikDetail> = safeApiCall {
        val response = backendApi.getCustomComicDetail(slug)
        if (response.isSuccessful && response.body()?.data != null) {
            response.body()!!.data!!.toDomainDetail()
        } else {
            throw Exception("Komik custom tidak ditemukan: ${response.code()}")
        }
    }

    override suspend fun getCustomChapterPages(chapterId: String): Result<ChapterPages> = safeApiCall {
        val response = backendApi.getCustomChapterPages(chapterId)
        if (response.isSuccessful && response.body()?.data != null) {
            response.body()!!.data!!.toDomainChapterPages()
        } else {
            throw Exception("Chapter custom tidak ditemukan: ${response.code()}")
        }
    }

    // ============================================================
    // HYBRID: GABUNGAN KEDUA SUMBER
    // ============================================================

    /**
     * Home Feed: Menggabungkan komik dari Scraper + Custom.
     *
     * Alur:
     * 1. Fetch Scraper (manhwa terbaru) + Custom comics secara PARALEL
     * 2. Ambil daftar slug yang di-hide dari backend
     * 3. Filter: hapus scraper comics yang slug-nya ada di daftar hide
     * 4. Gabungkan: custom comics muncul DULU, lalu scraper comics
     * 5. Emit sebagai Flow agar UI bisa observe secara reaktif
     */
    override fun getHomeFeed(): Flow<HomeFeedState> = flow {
        emit(HomeFeedState(isLoading = true))

        try {
            coroutineScope {
                // Fetch dari kedua sumber secara paralel (lebih cepat!)
                val scraperDeferred = async { getManhwaList(1) }
                val mangaDeferred = async { getMangaList(1) }
                val customDeferred = async { getCustomComics() }
                val hiddenDeferred = async { getHiddenSlugsInternal() }

                val scraperResult = scraperDeferred.await()
                val mangaResult = mangaDeferred.await()
                val customResult = customDeferred.await()
                val hiddenSlugs = hiddenDeferred.await().getOrElse { emptyList() }

                // Gabungkan hasil dari semua sumber
                val scraperKomik = (scraperResult.getOrNull() ?: emptyList()) +
                        (mangaResult.getOrNull() ?: emptyList())
                val customKomik = customResult.getOrNull() ?: emptyList()

                // Filter: hapus scraper komik yang di-hide Translator
                val filteredScraper = scraperKomik.filter { it.slug !in hiddenSlugs }

                // Custom comics muncul pertama (prioritas), lalu scraper
                val mergedList = customKomik + filteredScraper

                emit(
                    HomeFeedState(
                        isLoading = false,
                        komikList = mergedList,
                        error = null
                    )
                )
            }
        } catch (e: Exception) {
            emit(
                HomeFeedState(
                    isLoading = false,
                    komikList = emptyList(),
                    error = e.message ?: "Terjadi kesalahan tidak terduga"
                )
            )
        }
    }

    /**
     * Hybrid Search: Cari di scraper DAN custom secara paralel.
     */
    override suspend fun hybridSearch(query: String, page: Int): Result<List<Komik>> {
        return coroutineScope {
            val scraperDeferred = async { searchKomik(query, page) }
            val customDeferred = async { getCustomComics() }

            val scraperResult = scraperDeferred.await()
            val customResult = customDeferred.await()

            val scraperItems = scraperResult.getOrNull() ?: emptyList()
            // Filter custom comics berdasarkan query (client-side filter)
            val customItems = (customResult.getOrNull() ?: emptyList())
                .filter { it.title.contains(query, ignoreCase = true) }

            val merged = customItems + scraperItems
            Result.success(merged)
        }
    }

    /**
     * Hybrid Detail: Cek custom dulu, kalau tidak ada fallback ke scraper.
     */
    override suspend fun getHybridDetail(slug: String): Result<KomikDetail> {
        // Coba ambil dari custom (PostgreSQL) dulu
        val customResult = getCustomComicDetail(slug)
        if (customResult.isSuccess) {
            return customResult
        }

        // Fallback ke scraper API
        return getKomikDetail(slug)
    }

    /**
     * Hybrid Chapter Pages: Cek custom dulu, kalau tidak ada fallback ke scraper.
     */
    override suspend fun getHybridChapterPages(chapterId: String): Result<ChapterPages> {
        // Coba ambil dari custom dulu
        val customResult = getCustomChapterPages(chapterId)
        if (customResult.isSuccess) {
            return customResult
        }

        // Fallback ke scraper
        return getChapterPages(chapterId)
    }

    // ============================================================
    // HELPER FUNCTIONS
    // ============================================================

    /**
     * Ambil daftar slug yang di-hide dari backend.
     * Digunakan internal untuk filtering home feed.
     */
    private suspend fun getHiddenSlugsInternal(): Result<List<String>> {
        return try {
            val response = backendApi.getHiddenSlugs()
            if (response.isSuccessful) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.success(emptyList()) // Jangan gagal kalau endpoint ini error
            }
        } catch (e: Exception) {
            Result.success(emptyList()) // Silently fail - jangan block home feed
        }
    }

    /**
     * Wrapper aman untuk API call.
     * Menangkap exception dan mengembalikannya sebagai Result.failure.
     */
    private suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
