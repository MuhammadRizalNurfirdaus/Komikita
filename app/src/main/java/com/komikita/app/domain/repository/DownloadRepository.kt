package com.komikita.app.domain.repository

import com.komikita.app.data.local.entity.DownloadedChapterEntity
import com.komikita.app.data.local.entity.DownloadedComicEntity
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    fun startDownload(
        comicEndpoint: String,
        comicTitle: String,
        coverUrl: String,
        chapterEndpoint: String,
        chapterTitle: String,
        pages: List<String>
    )

    fun cancelDownload(chapterEndpoint: String)

    suspend fun deleteDownloadedChapter(chapterEndpoint: String)

    fun getAllDownloadedComics(): Flow<List<DownloadedComicEntity>>

    fun getDownloadedChaptersForComic(comicEndpoint: String): Flow<List<DownloadedChapterEntity>>

    fun getAllDownloadedChapters(): Flow<List<DownloadedChapterEntity>>

    suspend fun getDownloadedPages(chapterEndpoint: String): List<String>?

    fun observeDownloadedPages(chapterEndpoint: String): Flow<List<String>?>
}
