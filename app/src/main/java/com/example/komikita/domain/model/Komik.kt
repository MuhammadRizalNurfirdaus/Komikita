package com.example.komikita.domain.model

/**
 * Domain model utama untuk Komik.
 * Menampung data dari Scraper API DAN PostgreSQL (custom comics) dalam satu format unified.
 * Field [source] membedakan asal data: "scraper" atau "custom".
 */
data class Komik(
    val slug: String,
    val title: String,
    val poster: String?,
    val type: String?,           // "Manga", "Manhwa", "Manhua"
    val chapter: String?,        // Chapter terbaru (jika ada)
    val date: String?,           // Tanggal update terakhir
    val score: String?,          // Rating/skor
    val source: KomikSource      // Asal data: scraper atau custom (PostgreSQL)
)

/**
 * Enum untuk menandai asal data komik.
 * Penting agar UI bisa menampilkan badge atau perlakuan berbeda.
 */
enum class KomikSource {
    SCRAPER,    // Dari API Scraper (konten publik)
    CUSTOM      // Dari PostgreSQL (komik yang diunggah Translator)
}
