package com.komikita.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.komikita.app.data.local.entity.DownloadStatus
import com.komikita.app.data.local.entity.DownloadedChapterEntity
import com.komikita.app.data.local.entity.DownloadedComicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComic(comic: DownloadedComicEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(chapter: DownloadedChapterEntity)

    @Query("UPDATE downloaded_chapters SET downloadStatus = :status, progressPercentage = :progress, localImagePaths = :paths, totalSizeBytes = :sizeBytes WHERE chapterEndpoint = :chapterEndpoint")
    suspend fun updateProgress(
        chapterEndpoint: String,
        status: DownloadStatus,
        progress: Int,
        paths: List<String>,
        sizeBytes: Long
    )

    @Query("UPDATE downloaded_chapters SET downloadStatus = 'COMPLETED', progressPercentage = 100, localImagePaths = :paths, totalSizeBytes = :sizeBytes WHERE chapterEndpoint = :chapterEndpoint")
    suspend fun markCompleted(
        chapterEndpoint: String,
        paths: List<String>,
        sizeBytes: Long
    )

    @Query("DELETE FROM downloaded_chapters WHERE chapterEndpoint = :chapterEndpoint")
    suspend fun deleteDownloadedChapter(chapterEndpoint: String)

    @Query("SELECT * FROM downloaded_comics")
    fun getAllDownloadedComics(): Flow<List<DownloadedComicEntity>>

    @Query("SELECT * FROM downloaded_chapters WHERE comicEndpoint = :comicEndpoint")
    fun getDownloadedChaptersForComic(comicEndpoint: String): Flow<List<DownloadedChapterEntity>>

    @Query("SELECT * FROM downloaded_chapters ORDER BY downloadedAt DESC")
    fun getAllDownloadedChapters(): Flow<List<DownloadedChapterEntity>>

    @Query("SELECT * FROM downloaded_chapters WHERE chapterEndpoint = :chapterEndpoint LIMIT 1")
    suspend fun getDownloadedChapter(chapterEndpoint: String): DownloadedChapterEntity?

    @Query("SELECT localImagePaths FROM downloaded_chapters WHERE chapterEndpoint = :chapterEndpoint AND downloadStatus = 'COMPLETED' LIMIT 1")
    suspend fun getDownloadedPages(chapterEndpoint: String): List<String>?

    @Query("SELECT localImagePaths FROM downloaded_chapters WHERE chapterEndpoint = :chapterEndpoint AND downloadStatus = 'COMPLETED' LIMIT 1")
    fun observeDownloadedPages(chapterEndpoint: String): Flow<List<String>?>
}
