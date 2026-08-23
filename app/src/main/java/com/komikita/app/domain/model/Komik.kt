package com.komikita.app.domain.model

enum class ComicSource {
    SCRAPER,
    CUSTOM_TRANSLATOR
}

data class Komik(
    val title: String,
    val endpoint: String,
    val coverUrl: String,
    val type: String,
    val status: String,
    val latestChapter: String,
    val rating: String,
    val source: ComicSource,
    val synopsis: String = "",
    val author: String = "",
    val genres: List<String> = emptyList(),
    val chapters: List<Chapter> = emptyList(),
    val overriddenScraperSlug: String? = null
)
