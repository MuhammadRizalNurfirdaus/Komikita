package com.example.komikita.data.model

data class Pagination(
    val current_page: Int,
    val has_next_page: Boolean,
    val has_previous_page: Boolean,
    val last_visible_page: Int,
    val next_page: Int?,
    val previous_page: Int?
)
