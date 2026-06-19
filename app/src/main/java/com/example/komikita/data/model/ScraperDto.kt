package com.example.komikita.data.model

import com.google.gson.annotations.SerializedName

// ============================================================
// DTO (Data Transfer Object) untuk Scraper API
// Base URL: https://scraper.asepharyana.my.id/api/
// Semua endpoint menggunakan path parameter, bukan query parameter.
// ============================================================

// --- Response wrapper umum (semua endpoint dibungkus dalam objek ini) ---

/**
 * Response umum untuk list komik dari scraper.
 * Contoh: /komik/manga/{slug}, /komik/manhwa/{slug}, /komik/popular/{slug}
 */
data class ScraperListResponse(
    @SerializedName("status") val status: Boolean?,
    @SerializedName("data") val data: List<ScraperKomikItem>?,
    @SerializedName("pagination") val pagination: ScraperPagination?
)

/**
 * Item komik individual dari scraper.
 * Struktur fleksibel yang menampung berbagai endpoint (manga, manhwa, manhua, popular, search).
 */
data class ScraperKomikItem(
    @SerializedName("title") val title: String?,
    @SerializedName("slug") val slug: String?,
    @SerializedName("poster") val poster: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("chapter") val chapter: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("score") val score: String?,
    @SerializedName("reader_count") val readerCount: String?,
    @SerializedName("rank") val rank: Int?,
    @SerializedName("komik_url") val komikUrl: String?,
    @SerializedName("genres") val genres: List<String>?,
    @SerializedName("status") val status: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("latest_chapter") val latestChapter: String?,
    @SerializedName("update_time") val updateTime: String?,
    @SerializedName("chapter_url") val chapterUrl: String?
)

/**
 * Pagination info dari scraper.
 */
data class ScraperPagination(
    @SerializedName("current_page") val currentPage: Int?,
    @SerializedName("total_pages") val totalPages: Int?,
    @SerializedName("has_next") val hasNext: Boolean?,
    @SerializedName("has_prev") val hasPrev: Boolean?
)

// --- Detail Response ---

data class ScraperDetailResponse(
    @SerializedName("status") val status: Boolean?,
    @SerializedName("data") val data: ScraperDetailData?
)

data class ScraperDetailData(
    @SerializedName("title") val title: String?,
    @SerializedName("author") val author: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("genres") val genres: List<String>?,
    @SerializedName("poster") val poster: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("total_chapter") val totalChapter: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("updated_on") val updatedOn: String?,
    @SerializedName("chapters") val chapters: List<ScraperChapter>?
)

data class ScraperChapter(
    @SerializedName("chapter") val chapter: String?,
    @SerializedName("chapter_id") val chapterId: String?,
    @SerializedName("date") val date: String?
)

// --- Chapter (Reader) Response ---

data class ScraperChapterResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: ScraperChapterData?
)

data class ScraperChapterData(
    @SerializedName("title") val title: String?,
    @SerializedName("images") val images: List<String>?,
    @SerializedName("list_chapter") val listChapter: String?,
    @SerializedName("prev_chapter_id") val prevChapterId: String?,
    @SerializedName("next_chapter_id") val nextChapterId: String?
)

// --- Genre List Response ---

data class ScraperGenreListResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("data") val data: List<ScraperGenre>?
)

data class ScraperGenre(
    @SerializedName("name") val name: String?,
    @SerializedName("slug") val slug: String?,
    @SerializedName("url") val url: String?
)

// --- Genre Filter Response ---

data class ScraperGenreFilterResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("genre") val genre: String?,
    @SerializedName("data") val data: List<ScraperKomikItem>?,
    @SerializedName("pagination") val pagination: ScraperPagination?
)

// --- Search Response ---

data class ScraperSearchResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("data") val data: List<ScraperKomikItem>?,
    @SerializedName("pagination") val pagination: ScraperPagination?
)
