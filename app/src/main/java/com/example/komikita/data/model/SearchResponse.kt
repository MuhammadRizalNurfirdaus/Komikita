package com.example.komikita.data.model

import com.google.gson.annotations.SerializedName

data class SearchItem(
    @SerializedName("anime_url") val animeUrl: String?,
    @SerializedName("episode") val episode: String?,
    @SerializedName("genres") val genres: List<String>?,
    @SerializedName("poster") val poster: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("slug") val slug: String,
    @SerializedName("status") val status: String?,
    @SerializedName("title") val title: String,
    // Keep these for compatibility with manga endpoint
    @SerializedName("chapter") val chapter: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("score") val score: String?,
    @SerializedName("type") val type: String?
)

data class SearchResponse(
    val data: List<SearchItem>?,
    val pagination: Pagination?,
    val status: String?
)
