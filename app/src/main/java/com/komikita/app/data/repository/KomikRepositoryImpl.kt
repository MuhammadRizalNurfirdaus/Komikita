package com.komikita.app.data.repository

import android.util.Log
import com.komikita.app.data.api.CustomBackendApi
import com.komikita.app.data.api.ScraperApi
import com.komikita.app.data.model.CustomBackendKomikDto
import com.komikita.app.data.model.ScraperKomikDto
import com.komikita.app.domain.model.Chapter
import com.komikita.app.domain.model.ComicSource
import com.komikita.app.domain.model.Komik
import com.komikita.app.domain.repository.KomikRepository
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "KomikRepository"

@Singleton
class KomikRepositoryImpl @Inject constructor(
    private val scraperApi: ScraperApi,
    private val customBackendApi: CustomBackendApi,
    private val shinigamiJsoupParser: com.komikita.app.data.remote.parser.ShinigamiJsoupParser
) : KomikRepository {

    override suspend fun getMergedPopularComics(page: Int): Result<List<Komik>> {
        return runCatching {
            val customComics = if (page == 1) {
                runCatching {
                    customBackendApi.getCustomComics().map { it.toDomain() }
                }.onFailure { e ->
                    Log.e(TAG, "Custom backend popular comics fetch failed: ${e.message}", e)
                }.getOrDefault(emptyList())
            } else {
                emptyList()
            }

            val scraperComics = runCatching {
                val scraperResponse = scraperApi.getPopularComics(page)
                (scraperResponse.data ?: emptyList()).map { it.toDomain() }
            }.onFailure { e ->
                Log.e(TAG, "Scraper API popular comics fetch failed: ${e.message}", e)
            }.getOrDefault(emptyList())

            val merged = mergeComics(customComics, scraperComics)
            if (merged.isEmpty()) {
                Log.e(TAG, "getMergedPopularComics: Resulting comic list is empty")
                throw Exception("Gagal memuat komik dari server. Coba usap ke bawah untuk memuat ulang.")
            }
            merged
        }
    }

    override suspend fun getMergedLatestComics(page: Int): Result<List<Komik>> {
        return runCatching {
            val customComics = if (page == 1) {
                runCatching {
                    customBackendApi.getCustomComics().map { it.toDomain() }
                }.onFailure { e ->
                    Log.e(TAG, "Custom backend latest comics fetch failed: ${e.message}", e)
                }.getOrDefault(emptyList())
            } else {
                emptyList()
            }

            val scraperComics = runCatching {
                val scraperResponse = scraperApi.getLatestComics(page)
                (scraperResponse.data ?: emptyList()).map { it.toDomain() }
            }.onFailure { e ->
                Log.e(TAG, "Scraper API latest comics fetch failed: ${e.message}", e)
            }.getOrDefault(emptyList())

            val merged = mergeComics(customComics, scraperComics)
            if (merged.isEmpty()) {
                Log.e(TAG, "getMergedLatestComics: Resulting comic list is empty")
                throw Exception("Gagal memuat komik dari server. Coba usap ke bawah untuk memuat ulang.")
            }
            merged
        }
    }

    override suspend fun getComicDetail(endpoint: String, isCustom: Boolean): Result<Komik> {
        return runCatching {
            if (isCustom) {
                val customDto = customBackendApi.getCustomComicDetail(endpoint)
                customDto.toDomain()
            } else {
                val dto = scraperApi.getComicDetail(endpoint)
                if (dto.title.isNullOrBlank()) {
                    Log.e(TAG, "getComicDetail: Scraper returned empty/null detail for endpoint: $endpoint")
                    throw Exception("Gagal memuat detail komik dari server.")
                }
                Komik(
                    title = dto.title ?: "",
                    endpoint = dto.endpoint ?: endpoint,
                    coverUrl = dto.cover ?: "",
                    type = dto.type ?: "Manga",
                    status = dto.status ?: "Ongoing",
                    latestChapter = dto.chapters?.firstOrNull()?.title ?: "",
                    rating = dto.rating ?: "0.0",
                    source = ComicSource.SCRAPER,
                    synopsis = dto.synopsis ?: "",
                    author = dto.author ?: "",
                    genres = dto.genres ?: emptyList(),
                    chapters = dto.chapters?.map {
                        Chapter(
                            title = it.title ?: "",
                            endpoint = it.endpoint ?: "",
                            releaseDate = it.releaseDate ?: ""
                        )
                    } ?: emptyList()
                )
            }
        }.onFailure { e ->
            Log.e(TAG, "getComicDetail failed for $endpoint (isCustom=$isCustom): ${e.message}", e)
        }
    }

    override suspend fun getChapterDetail(endpoint: String, isCustom: Boolean): Result<Chapter> {
        return runCatching {
            if (isCustom) {
                Chapter(
                    title = "Chapter Detail",
                    endpoint = endpoint,
                    pages = emptyList()
                )
            } else {
                val dto = scraperApi.getChapterDetail(endpoint)
                Chapter(
                    title = dto.title ?: "",
                    endpoint = dto.endpoint ?: endpoint,
                    pages = dto.pages ?: emptyList()
                )
            }
        }.onFailure { e ->
            Log.e(TAG, "getChapterDetail failed for $endpoint: ${e.message}", e)
        }
    }

    override suspend fun searchComics(query: String): Result<List<Komik>> {
        return runCatching {
            val customComics = runCatching {
                customBackendApi.getCustomComics()
                    .map { it.toDomain() }
                    .filter { it.title.contains(query, ignoreCase = true) }
            }.getOrDefault(emptyList())

            val scraperComics = runCatching {
                val scraperResponse = scraperApi.searchComics(query)
                (scraperResponse.data ?: emptyList()).map { it.toDomain() }
            }.getOrDefault(emptyList())

            val merged = mergeComics(customComics, scraperComics)
            if (merged.isEmpty()) {
                Log.e(TAG, "searchComics: Search returned empty list for query: $query")
                throw Exception("Gagal mencari komik dari server.")
            }
            merged
        }.onFailure { e ->
            Log.e(TAG, "searchComics failed for $query: ${e.message}", e)
        }
    }

    override suspend fun publishCustomComic(comic: Komik): Result<Komik> {
        return runCatching {
            val dto = CustomBackendKomikDto(
                slug = comic.endpoint,
                title = comic.title,
                coverUrl = comic.coverUrl,
                synopsis = comic.synopsis,
                type = comic.type,
                status = comic.status,
                author = comic.author,
                rating = comic.rating,
                overriddenScraperSlug = comic.overriddenScraperSlug,
                chapters = comic.chapters.map { chapter ->
                    com.komikita.app.data.model.CustomChapterDto(
                        chapterTitle = chapter.title,
                        chapterId = chapter.endpoint,
                        imageUrls = chapter.pages
                    )
                }
            )
            val response = customBackendApi.publishCustomComic(dto)
            response.toDomain()
        }.onFailure { e ->
            Log.e(TAG, "publishCustomComic failed: ${e.message}", e)
        }
    }

    private fun mergeComics(customComics: List<Komik>, scraperComics: List<Komik>): List<Komik> {
        val hiddenSlugs = customComics.mapNotNull { it.overriddenScraperSlug }.toSet()
        val customSlugs = customComics.map { it.endpoint }.toSet()

        val filteredScraper = scraperComics.filter { scraper ->
            !hiddenSlugs.contains(scraper.endpoint) && !customSlugs.contains(scraper.endpoint)
        }

        return customComics + filteredScraper
    }

    private fun ScraperKomikDto.toDomain(): Komik {
        return Komik(
            title = title ?: "",
            endpoint = endpoint ?: "",
            coverUrl = cover ?: "",
            type = type ?: "Manga",
            status = status ?: "Ongoing",
            latestChapter = latestChapter ?: "",
            rating = rating ?: "0.0",
            source = ComicSource.SCRAPER
        )
    }

    private fun CustomBackendKomikDto.toDomain(): Komik {
        return Komik(
            title = title,
            endpoint = slug,
            coverUrl = coverUrl,
            type = type,
            status = status,
            latestChapter = chapters.lastOrNull()?.chapterTitle ?: "",
            rating = rating,
            source = ComicSource.CUSTOM_TRANSLATOR,
            synopsis = synopsis,
            author = author,
            overriddenScraperSlug = overriddenScraperSlug,
            chapters = chapters.map {
                Chapter(
                    title = it.chapterTitle,
                    endpoint = it.chapterId,
                    releaseDate = it.releaseDate,
                    pages = it.imageUrls
                )
            }
        )
    }
}
