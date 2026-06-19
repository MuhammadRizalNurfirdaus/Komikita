package com.example.komikita.data.api

import com.example.komikita.data.model.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Interface Retrofit untuk Scraper API (Sumber A).
 * Base URL: https://scraper.asepharyana.my.id/api/
 *
 * API ini bersifat READ-ONLY dan menyediakan konten publik.
 * Semua endpoint menggunakan path parameter.
 *
 * Endpoint yang tersedia:
 * - GET /komik/chapter/{slug}         -> Halaman chapter (reader)
 * - GET /komik/detail/{slug}          -> Detail komik
 * - GET /komik/genre/{slug}/{page}    -> Komik per genre (paginated)
 * - GET /komik/genre_list             -> Daftar semua genre
 * - GET /komik/manga/{slug}           -> Daftar manga (paginated)
 * - GET /komik/manhua/{slug}          -> Daftar manhua (paginated)
 * - GET /komik/manhwa/{slug}          -> Daftar manhwa (paginated)
 * - GET /komik/popular/{slug}         -> Komik populer per tipe
 * - GET /komik/search/{slug}/{page}   -> Pencarian komik (paginated)
 */
interface ScraperApi {

    /**
     * Ambil halaman gambar chapter untuk dibaca di Reader.
     * @param slug Slug/ID chapter (contoh: "one-piece-chapter-1050")
     */
    @GET("komik/chapter/{slug}")
    suspend fun getChapter(
        @Path("slug") slug: String
    ): Response<ScraperChapterResponse>

    /**
     * Ambil detail lengkap komik (metadata + daftar chapter).
     * @param slug Slug komik (contoh: "one-piece")
     */
    @GET("komik/detail/{slug}")
    suspend fun getDetail(
        @Path("slug") slug: String
    ): Response<ScraperDetailResponse>

    /**
     * Ambil daftar komik berdasarkan genre.
     * @param slug Slug genre (contoh: "action")
     * @param page Nomor halaman
     */
    @GET("komik/genre/{slug}/{page}")
    suspend fun getByGenre(
        @Path("slug") slug: String,
        @Path("page") page: Int
    ): Response<ScraperGenreFilterResponse>

    /**
     * Ambil daftar semua genre yang tersedia.
     */
    @GET("komik/genre_list")
    suspend fun getGenreList(): Response<ScraperGenreListResponse>

    /**
     * Ambil daftar manga.
     * @param slug Halaman atau filter (contoh: "1" untuk halaman pertama)
     */
    @GET("komik/manga/{slug}")
    suspend fun getMangaList(
        @Path("slug") slug: String
    ): Response<ScraperListResponse>

    /**
     * Ambil daftar manhua.
     * @param slug Halaman atau filter
     */
    @GET("komik/manhua/{slug}")
    suspend fun getManhuaList(
        @Path("slug") slug: String
    ): Response<ScraperListResponse>

    /**
     * Ambil daftar manhwa.
     * @param slug Halaman atau filter
     */
    @GET("komik/manhwa/{slug}")
    suspend fun getManhwaList(
        @Path("slug") slug: String
    ): Response<ScraperListResponse>

    /**
     * Ambil komik populer berdasarkan tipe.
     * @param slug Tipe komik (contoh: "manga", "manhwa", "manhua")
     */
    @GET("komik/popular/{slug}")
    suspend fun getPopular(
        @Path("slug") slug: String
    ): Response<ScraperListResponse>

    /**
     * Pencarian komik.
     * @param slug Query pencarian (contoh: "one-piece")
     * @param page Nomor halaman
     */
    @GET("komik/search/{slug}/{page}")
    suspend fun searchKomik(
        @Path("slug") slug: String,
        @Path("page") page: Int
    ): Response<ScraperSearchResponse>
}
