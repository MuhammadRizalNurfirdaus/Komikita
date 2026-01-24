package com.example.komikita.data.model

import com.google.gson.annotations.SerializedName

data class PopularItem(
    @SerializedName("chapter") val chapter: String?,
    @SerializedName("komik_url") val komikUrl: String?,
    @SerializedName("poster") val poster: String?,
    @SerializedName("rank") val rank: Int?,
    @SerializedName("score") val score: String?,
    @SerializedName("slug") val slug: String,
    @SerializedName("title") val title: String,
    @SerializedName("type") val type: String?
)

data class PopularResponse(
    val data: List<PopularItem>?,
    val pagination: Pagination?,
    val period: String?,
    val status: String?
)
