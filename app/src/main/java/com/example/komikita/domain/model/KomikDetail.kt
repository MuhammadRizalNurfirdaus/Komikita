package com.example.komikita.domain.model

/**
 * Domain model untuk detail lengkap sebuah komik.
 * Berisi metadata + daftar chapter.
 */
data class KomikDetail(
    val slug: String,
    val title: String,
    val poster: String?,
    val author: String?,
    val description: String?,
    val genres: List<String>,
    val status: String?,         // "Ongoing", "Completed", dll
    val type: String?,           // "Manga", "Manhwa", "Manhua"
    val releaseDate: String?,
    val updatedOn: String?,
    val totalChapter: String?,
    val chapters: List<ChapterItem>,
    val source: KomikSource
)

/**
 * Item chapter individual (daftar chapter di halaman detail).
 */
data class ChapterItem(
    val chapterId: String,       // ID unik chapter (untuk navigasi ke reader)
    val chapterNumber: String,   // Label "Chapter 1", "Chapter 2", dll
    val date: String?            // Tanggal rilis chapter
)
