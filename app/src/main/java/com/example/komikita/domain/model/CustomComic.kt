package com.example.komikita.domain.model

/**
 * Domain model untuk komik custom yang diunggah Translator ke PostgreSQL.
 */
data class CustomComic(
    val id: String?,                   // ID dari PostgreSQL (null saat create baru)
    val title: String,
    val slug: String,
    val coverUrl: String?,             // URL cover image
    val type: String?,                 // "Manga", "Manhwa", "Manhua"
    val chapters: List<CustomChapter>,
    val isHidden: Boolean = false,     // Translator bisa hide komik
    val createdAt: Long = System.currentTimeMillis(),
    val authorUid: String?             // UID Translator yang upload
)

/**
 * Chapter dari komik custom (PostgreSQL).
 * Satu chapter berisi list URL gambar.
 */
data class CustomChapter(
    val id: String?,
    val comicId: String?,
    val chapterNumber: String,         // "1", "2", dst
    val title: String?,
    val pages: List<String>,           // List URL gambar halaman
    val createdAt: Long = System.currentTimeMillis()
)
