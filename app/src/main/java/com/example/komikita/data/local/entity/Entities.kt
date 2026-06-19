package com.example.komikita.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// ============================================================
// Room Entities - Tabel lokal untuk cache/offline.
// Data utama ada di PostgreSQL (via Backend API).
// Room hanya untuk: cache, offline reading, history, favorites.
// ============================================================

/**
 * Entity User lokal.
 * Menyimpan info user yang sedang login + role dari backend.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,       // Firebase UID
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val role: String = "user",            // "admin", "translator", "user" (dari backend)
    val isEmailVerified: Boolean = false,
    val authToken: String? = null,        // JWT token dari backend
    val loginType: String = "google"      // "google" atau "local"
)

/**
 * Entity Favorite (bookmark komik).
 * Disimpan lokal agar bisa diakses offline.
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val slug: String,
    val title: String,
    val poster: String?,
    val type: String?,
    val userId: String,
    val source: String = "scraper",       // "scraper" atau "custom"
    val addedAt: Long = System.currentTimeMillis()
)

/**
 * Entity Download (chapter yang diunduh untuk offline reading).
 */
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val komikSlug: String,
    val komikTitle: String,
    val chapterId: String,
    val chapterTitle: String,
    val userId: String,
    val downloadedAt: Long = System.currentTimeMillis(),
    val localPath: String?,
    val status: String = "completed"      // "downloading", "completed", "failed"
)

/**
 * Entity History (riwayat baca).
 * Disimpan di Room DB lokal, bisa disinkronkan ke backend secara berkala.
 */
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val komikSlug: String,
    val komikTitle: String,
    val komikPoster: String?,
    val lastChapterId: String,
    val lastChapterLabel: String,
    val userId: String,
    val source: String = "scraper",       // "scraper" atau "custom"
    val readAt: Long = System.currentTimeMillis()
)
