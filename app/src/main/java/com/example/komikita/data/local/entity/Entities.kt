package com.example.komikita.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean = false,
    val passwordHash: String? = null, // For local login
    val loginType: String = "google" // "google" or "local"
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val slug: String,
    val title: String,
    val poster: String?,
    val type: String?,
    val userId: String, // Associate with user
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val komikSlug: String,
    val komikTitle: String,
    val chapterId: String,
    val chapterTitle: String,
    val userId: String, // Associate with user
    val downloadedAt: Long = System.currentTimeMillis(),
    val localPath: String?,
    val status: String = "completed" // "downloading", "completed", "failed"
)
