package com.komikita.app.data.model

import com.google.gson.annotations.SerializedName

data class CustomBackendKomikDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("slug") val slug: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("cover_url") val coverUrl: String = "",
    @SerializedName("synopsis") val synopsis: String = "",
    @SerializedName("type") val type: String = "MANHWA",
    @SerializedName("status") val status: String = "ONGOING",
    @SerializedName("author") val author: String = "",
    @SerializedName("rating") val rating: String = "5.0",
    @SerializedName("overridden_scraper_slug") val overriddenScraperSlug: String? = null,
    @SerializedName("chapters") val chapters: List<CustomChapterDto> = emptyList()
)

data class CustomChapterDto(
    @SerializedName("chapter_id") val chapterId: String = "",
    @SerializedName("chapter_title") val chapterTitle: String = "",
    @SerializedName("release_date") val releaseDate: String = "",
    @SerializedName("image_urls") val imageUrls: List<String> = emptyList()
)
