package com.komikita.app.data.model

import com.google.gson.annotations.SerializedName

data class ScraperListResponse(
    @SerializedName("status") val status: Boolean? = true,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<ScraperKomikDto>? = emptyList()
)

data class ScraperKomikDto(
    @SerializedName("title") val title: String? = "",
    @SerializedName("endpoint") val endpoint: String? = "",
    @SerializedName("cover") val cover: String? = "",
    @SerializedName("type") val type: String? = "Manga",
    @SerializedName("status") val status: String? = "Ongoing",
    @SerializedName("latest_chapter") val latestChapter: String? = "",
    @SerializedName("rating") val rating: String? = "0.0"
)

data class ScraperDetailDto(
    @SerializedName("title") val title: String? = "",
    @SerializedName("endpoint") val endpoint: String? = "",
    @SerializedName("cover") val cover: String? = "",
    @SerializedName("synopsis") val synopsis: String? = "",
    @SerializedName("type") val type: String? = "",
    @SerializedName("status") val status: String? = "",
    @SerializedName("author") val author: String? = "",
    @SerializedName("rating") val rating: String? = "",
    @SerializedName("genres") val genres: List<String>? = emptyList(),
    @SerializedName("chapters") val chapters: List<ScraperChapterItemDto>? = emptyList()
)

data class ScraperChapterItemDto(
    @SerializedName("title") val title: String? = "",
    @SerializedName("endpoint") val endpoint: String? = "",
    @SerializedName("release_date") val releaseDate: String? = ""
)

data class ScraperChapterDto(
    @SerializedName("title") val title: String? = "",
    @SerializedName("endpoint") val endpoint: String? = "",
    @SerializedName("pages") val pages: List<String>? = emptyList()
)
