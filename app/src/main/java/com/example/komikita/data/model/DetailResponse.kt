package com.example.komikita.data.model

import com.google.gson.annotations.SerializedName

data class Chapter(
    val chapter: String?,
    @SerializedName("chapter_id") val id: String,
    val date: String?
)

data class DetailData(
    @SerializedName("author") val author: String?,
    @SerializedName("chapters") val chapters: List<Chapter>?,
    @SerializedName("description") val description: String?,
    @SerializedName("genres") val genres: List<String>?,
    @SerializedName("poster") val poster: String?,
    @SerializedName("release_date") val release_date: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("total_chapter") val total_chapter: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("updated_on") val updated_on: String?
)

data class DetailResponse(
    @SerializedName("data") val data: DetailData?,
    @SerializedName("status") val status: Boolean?,
    @SerializedName("message") val message: String?
)
