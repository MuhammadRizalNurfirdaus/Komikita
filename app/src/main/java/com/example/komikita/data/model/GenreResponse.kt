package com.example.komikita.data.model

import com.google.gson.annotations.SerializedName

data class Genre(
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("url") val url: String?
)

data class GenreListResponse(
    val data: List<Genre>?,
    val status: String?
)

data class GenreFilterItem(
    @SerializedName("chapter") val chapter: String?,
    @SerializedName("komik_url") val komikUrl: String?,
    @SerializedName("poster") val poster: String?,
    @SerializedName("score") val score: String?,
    @SerializedName("slug") val slug: String,
    @SerializedName("title") val title: String,
    @SerializedName("type") val type: String?
)

data class GenreFilterResponse(
    val data: List<GenreFilterItem>?,
    val genre: String?,
    val pagination: Pagination?,
    val status: String?
)
