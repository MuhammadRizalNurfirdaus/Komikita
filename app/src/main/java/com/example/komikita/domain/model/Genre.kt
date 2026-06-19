package com.example.komikita.domain.model

/**
 * Domain model untuk Genre.
 */
data class Genre(
    val name: String,
    val slug: String,
    val url: String? = null
)

/**
 * Domain model untuk riwayat baca (history).
 * Disimpan di Room DB lokal dan bisa disinkronkan ke backend.
 */
data class ReadHistory(
    val id: Int = 0,
    val komikSlug: String,
    val komikTitle: String,
    val komikPoster: String?,
    val lastChapterId: String,
    val lastChapterLabel: String,
    val readAt: Long = System.currentTimeMillis(),
    val userId: String,
    val source: KomikSource = KomikSource.SCRAPER
)
