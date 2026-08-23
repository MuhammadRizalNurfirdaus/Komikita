package com.komikita.app.domain.model

data class Chapter(
    val title: String,
    val endpoint: String,
    val releaseDate: String = "",
    val pages: List<String> = emptyList()
)
