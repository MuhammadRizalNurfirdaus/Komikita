package com.example.komikita.data.model

import com.google.gson.annotations.SerializedName

// For Manga/Search responses
data class KomikItemManga(
    @SerializedName("chapter") val chapter: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("poster") val poster: String?,
    @SerializedName("score") val score: String?,
    @SerializedName("slug") val slug: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("type") val type: String?
)

// For Manhwa/Manhua responses
data class KomikItemList(
    @SerializedName("chapter") val chapter: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("poster") val poster: String?,
    @SerializedName("reader_count") val readerCount: String?,
    @SerializedName("slug") val slug: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("type") val type: String?
)
