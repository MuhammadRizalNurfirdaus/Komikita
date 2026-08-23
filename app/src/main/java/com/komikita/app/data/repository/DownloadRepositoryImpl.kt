package com.komikita.app.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import com.komikita.app.data.download.ChapterDownloadWorker
import com.komikita.app.data.local.dao.DownloadDao
import com.komikita.app.data.local.entity.DownloadedChapterEntity
import com.komikita.app.data.local.entity.DownloadedComicEntity
import com.komikita.app.domain.repository.DownloadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadDao: DownloadDao
) : DownloadRepository {

    private val workManager = WorkManager.getInstance(context)

    override fun startDownload(
        comicEndpoint: String,
        comicTitle: String,
        coverUrl: String,
        chapterEndpoint: String,
        chapterTitle: String,
        pages: List<String>
    ) {
        val inputData = Data.Builder()
            .putString(ChapterDownloadWorker.KEY_CHAPTER_ENDPOINT, chapterEndpoint)
            .putString(ChapterDownloadWorker.KEY_COMIC_ENDPOINT, comicEndpoint)
            .putString(ChapterDownloadWorker.KEY_COMIC_TITLE, comicTitle)
            .putString(ChapterDownloadWorker.KEY_COVER_URL, coverUrl)
            .putString(ChapterDownloadWorker.KEY_CHAPTER_TITLE, chapterTitle)
            .putString(ChapterDownloadWorker.KEY_PAGES_JSON, Gson().toJson(pages))
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val downloadWork = OneTimeWorkRequestBuilder<ChapterDownloadWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .addTag(chapterEndpoint)
            .build()

        workManager.enqueueUniqueWork(
            "download_$chapterEndpoint",
            ExistingWorkPolicy.REPLACE,
            downloadWork
        )
    }

    override fun cancelDownload(chapterEndpoint: String) {
        workManager.cancelUniqueWork("download_$chapterEndpoint")
    }

    override suspend fun deleteDownloadedChapter(chapterEndpoint: String) {
        cancelDownload(chapterEndpoint)
        val chapter = downloadDao.getDownloadedChapter(chapterEndpoint)
        chapter?.localImagePaths?.forEach { path ->
            try {
                val file = File(path)
                if (file.exists()) file.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        downloadDao.deleteDownloadedChapter(chapterEndpoint)
    }

    override fun getAllDownloadedComics(): Flow<List<DownloadedComicEntity>> {
        return downloadDao.getAllDownloadedComics()
    }

    override fun getDownloadedChaptersForComic(comicEndpoint: String): Flow<List<DownloadedChapterEntity>> {
        return downloadDao.getDownloadedChaptersForComic(comicEndpoint)
    }

    override fun getAllDownloadedChapters(): Flow<List<DownloadedChapterEntity>> {
        return downloadDao.getAllDownloadedChapters()
    }

    override suspend fun getDownloadedPages(chapterEndpoint: String): List<String>? {
        return downloadDao.getDownloadedPages(chapterEndpoint)
    }

    override fun observeDownloadedPages(chapterEndpoint: String): Flow<List<String>?> {
        return downloadDao.observeDownloadedPages(chapterEndpoint)
    }
}
