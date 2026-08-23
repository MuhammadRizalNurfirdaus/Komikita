package com.komikita.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_comics")
data class DownloadedComicEntity(
    @PrimaryKey val comicEndpoint: String,
    val comicTitle: String,
    val coverUrl: String
)

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    FAILED
}

@Entity(tableName = "downloaded_chapters")
data class DownloadedChapterEntity(
    @PrimaryKey val chapterEndpoint: String,
    val comicEndpoint: String,
    val comicTitle: String,
    val chapterTitle: String,
    val localImagePaths: List<String> = emptyList(),
    val downloadStatus: DownloadStatus = DownloadStatus.PENDING,
    val progressPercentage: Int = 0,
    val totalSizeBytes: Long = 0L,
    val downloadedAt: Long = System.currentTimeMillis()
)
