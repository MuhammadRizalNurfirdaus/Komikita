package com.example.komikita.data.model

import com.google.gson.annotations.SerializedName

data class ChapterData(
    @SerializedName("images") val images: List<String>?,
    @SerializedName("list_chapter") val listChapter: String?,
    @SerializedName("next_chapter_id") val nextChapterId: String?,
    @SerializedName("prev_chapter_id") val prevChapterId: String?,
    @SerializedName("title") val title: String?
)

data class ChapterResponse(
    @SerializedName("data") val data: ChapterData?,
    @SerializedName("message") val message: String?
)
