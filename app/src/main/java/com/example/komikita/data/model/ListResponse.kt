package com.example.komikita.data.model

data class ListItem(
    val chapter: String?,
    val date: String?,
    val poster: String?,
    val reader_count: String?,
    val score: String?,
    val slug: String,
    val title: String,
    val type: String?
)

data class ListResponse(
    val data: List<ListItem>,
    val pagination: Pagination?
)
