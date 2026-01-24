package com.example.komikita.data.model

import com.google.gson.annotations.SerializedName

// API returns: {"status": true, "data": {...}}
data class DetailResponse(
    @SerializedName("status") val status: Boolean?,
    @SerializedName("data") val data: DetailData?
)

data class DetailData(
    @SerializedName("author") val author: String?,
    @SerializedName("chapters") val chapters: List<Chapter>?,
    @SerializedName("description") val description: String?,
    @SerializedName("genres") val genres: List<String>?,
    @SerializedName("poster") val poster: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("total_chapter") val totalChapter: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("updated_on") val updatedOn: String?
)

data class Chapter(
    @SerializedName("chapter") val chapter: String?,
    @SerializedName("chapter_id") val id: String,
    @SerializedName("date") val date: String?
)
