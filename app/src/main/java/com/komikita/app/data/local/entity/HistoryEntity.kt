package com.komikita.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val endpoint: String,
    val comicTitle: String,
    val coverUrl: String,
    val lastChapterTitle: String,
    val lastChapterEndpoint: String,
    val timestamp: Long = System.currentTimeMillis()
)
