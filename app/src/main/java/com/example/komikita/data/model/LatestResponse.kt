package com.example.komikita.data.model

import com.google.gson.annotations.SerializedName

data class LatestItem(
    @SerializedName("chapter_url") val chapterUrl: String?,
    @SerializedName("latest_chapter") val latestChapter: String?,
    @SerializedName("poster") val poster: String?,
    @SerializedName("slug") val slug: String,
    @SerializedName("title") val title: String,
    @SerializedName("type") val type: String?,
    @SerializedName("update_time") val updateTime: String?
)

data class LatestResponse(
    val data: List<LatestItem>?,
    val pagination: Pagination?,
    val status: String?
)
