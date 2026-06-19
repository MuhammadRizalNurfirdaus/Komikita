package com.example.komikita.domain.model

/**
 * Domain model untuk halaman baca chapter.
 * Berisi list URL gambar yang akan ditampilkan di Reader Screen.
 */
data class ChapterPages(
    val title: String?,
    val images: List<String>,         // URL gambar halaman komik
    val prevChapterId: String?,       // ID chapter sebelumnya (null = chapter pertama)
    val nextChapterId: String?,       // ID chapter selanjutnya (null = chapter terakhir)
    val source: KomikSource
)
