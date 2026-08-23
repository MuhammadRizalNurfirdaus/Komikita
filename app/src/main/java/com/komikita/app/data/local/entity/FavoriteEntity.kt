package com.komikita.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val endpoint: String,
    val title: String,
    val coverUrl: String,
    val type: String,
    val rating: String,
    val isCustom: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)
