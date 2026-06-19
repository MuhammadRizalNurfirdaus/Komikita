package com.example.komikita.domain.repository

import com.example.komikita.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Interface Repository untuk data komik (Scraper API + Custom/PostgreSQL).
 * Implementasi ada di data layer: KomikRepositoryImpl.
 *
 * Prinsip Clean Architecture: Domain layer hanya mendefinisikan kontrak,
 * tidak tahu detail implementasi jaringan/database.
 */
interface KomikRepository {

    // === SUMBER A: SCRAPER API (Read-only, Public Content) ===

    /** Ambil daftar manhwa dari scraper */
    suspend fun getManhwaList(page: Int = 1): Result<List<Komik>>

    /** Ambil daftar manhua dari scraper */
    suspend fun getManhuaList(page: Int = 1): Result<List<Komik>>

    /** Ambil daftar manga dari scraper */
    suspend fun getMangaList(page: Int = 1): Result<List<Komik>>

    /** Ambil komik populer dari scraper */
    suspend fun getPopular(type: String, page: Int = 1): Result<List<Komik>>

    /** Cari komik di scraper */
    suspend fun searchKomik(query: String, page: Int = 1): Result<List<Komik>>

    /** Detail komik dari scraper */
    suspend fun getKomikDetail(slug: String): Result<KomikDetail>

    /** Halaman chapter dari scraper */
    suspend fun getChapterPages(chapterSlug: String): Result<ChapterPages>

    /** Daftar genre dari scraper */
    suspend fun getGenreList(): Result<List<Genre>>

    /** Komik berdasarkan genre dari scraper */
    suspend fun getKomikByGenre(genreSlug: String, page: Int = 1): Result<List<Komik>>

    // === SUMBER B: CUSTOM COMICS (PostgreSQL via Backend API) ===

    /** Ambil semua komik custom dari backend */
    suspend fun getCustomComics(): Result<List<Komik>>

    /** Detail komik custom dari backend */
    suspend fun getCustomComicDetail(slug: String): Result<KomikDetail>

    /** Halaman chapter custom dari backend */
    suspend fun getCustomChapterPages(chapterId: String): Result<ChapterPages>

    // === HYBRID: GABUNGAN KEDUA SUMBER ===

    /**
     * Ambil feed home: gabungan komik dari Scraper + Custom.
     * Mengembalikan Flow agar UI bisa observe perubahan secara reaktif.
     *
     * Logika merge:
     * 1. Fetch dari Scraper API (latest/popular)
     * 2. Fetch dari Backend (custom comics)
     * 3. Filter komik scraper yang di-hide Translator
     * 4. Gabungkan ke satu list unified
     */
    fun getHomeFeed(): Flow<HomeFeedState>

    /**
     * Hybrid search: cari di scraper DAN custom comics secara paralel.
     */
    suspend fun hybridSearch(query: String, page: Int = 1): Result<List<Komik>>

    /**
     * Hybrid detail: cek custom dulu, kalau tidak ada fallback ke scraper.
     */
    suspend fun getHybridDetail(slug: String): Result<KomikDetail>

    /**
     * Hybrid chapter pages: cek custom dulu, kalau tidak ada fallback ke scraper.
     */
    suspend fun getHybridChapterPages(chapterId: String): Result<ChapterPages>
}

/**
 * State untuk Home Feed (digunakan oleh StateFlow di ViewModel).
 */
data class HomeFeedState(
    val isLoading: Boolean = false,
    val komikList: List<Komik> = emptyList(),
    val error: String? = null
)
