package com.komikita.app.data.download

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.komikita.app.data.local.dao.DownloadDao
import com.komikita.app.data.local.entity.DownloadStatus
import com.komikita.app.data.local.entity.DownloadedChapterEntity
import com.komikita.app.data.local.entity.DownloadedComicEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@HiltWorker
class ChapterDownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val okHttpClient: OkHttpClient,
    private val downloadDao: DownloadDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val chapterEndpoint = inputData.getString(KEY_CHAPTER_ENDPOINT) ?: return Result.failure()
        val comicEndpoint = inputData.getString(KEY_COMIC_ENDPOINT) ?: ""
        val comicTitle = inputData.getString(KEY_COMIC_TITLE) ?: ""
        val coverUrl = inputData.getString(KEY_COVER_URL) ?: ""
        val chapterTitle = inputData.getString(KEY_CHAPTER_TITLE) ?: ""
        val pagesJson = inputData.getString(KEY_PAGES_JSON) ?: return Result.failure()

        val typeToken = object : TypeToken<List<String>>() {}.type
        val pages: List<String> = try {
            Gson().fromJson(pagesJson, typeToken)
        } catch (e: Exception) {
            emptyList()
        }

        if (pages.isEmpty()) {
            return Result.failure()
        }

        // Safe directory names
        val comicSlug = comicEndpoint.ifBlank { comicTitle }.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]+"), "_")
        val chapterSlug = chapterEndpoint.ifBlank { chapterTitle }.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]+"), "_")

        val downloadDir = File(context.filesDir, "downloads/$comicSlug/$chapterSlug").apply {
            mkdirs()
        }

        // Insert initial DB records
        downloadDao.insertComic(
            DownloadedComicEntity(
                comicEndpoint = comicEndpoint,
                comicTitle = comicTitle,
                coverUrl = coverUrl
            )
        )

        downloadDao.insertDownload(
            DownloadedChapterEntity(
                chapterEndpoint = chapterEndpoint,
                comicEndpoint = comicEndpoint,
                comicTitle = comicTitle,
                chapterTitle = chapterTitle,
                localImagePaths = emptyList(),
                downloadStatus = DownloadStatus.DOWNLOADING,
                progressPercentage = 0,
                totalSizeBytes = 0L
            )
        )

        val downloadedPaths = mutableListOf<String>()
        var totalBytes = 0L

        for ((index, pageUrl) in pages.withIndex()) {
            if (isStopped) {
                downloadDao.updateProgress(chapterEndpoint, DownloadStatus.FAILED, 0, emptyList(), 0L)
                return Result.failure()
            }

            val fileExt = when {
                pageUrl.contains(".webp", true) -> ".webp"
                pageUrl.contains(".png", true) -> ".png"
                else -> ".jpg"
            }
            val tempFile = File(downloadDir, "page_${index + 1}.tmp")
            val targetFile = File(downloadDir, "page_${index + 1}$fileExt")

            try {
                val request = Request.Builder().url(pageUrl).build()
                val response = okHttpClient.newCall(request).execute()

                if (!response.isSuccessful || response.body == null) {
                    downloadDao.updateProgress(chapterEndpoint, DownloadStatus.FAILED, 0, emptyList(), 0L)
                    return Result.retry()
                }

                val body = response.body!!
                FileOutputStream(tempFile).use { output ->
                    body.byteStream().use { input ->
                        input.copyTo(output)
                    }
                }

                // Rename temp file once complete
                if (tempFile.exists()) {
                    if (targetFile.exists()) targetFile.delete()
                    tempFile.renameTo(targetFile)
                }

                val fileSize = targetFile.length()
                totalBytes += fileSize
                downloadedPaths.add(targetFile.absolutePath)

                val progress = ((index + 1) * 100) / pages.size
                setProgress(workDataOf("PROGRESS" to progress))
                downloadDao.updateProgress(
                    chapterEndpoint = chapterEndpoint,
                    status = DownloadStatus.DOWNLOADING,
                    progress = progress,
                    paths = downloadedPaths.toList(),
                    sizeBytes = totalBytes
                )
            } catch (e: Exception) {
                e.printStackTrace()
                downloadDao.updateProgress(chapterEndpoint, DownloadStatus.FAILED, 0, emptyList(), 0L)
                return Result.retry()
            }
        }

        downloadDao.markCompleted(
            chapterEndpoint = chapterEndpoint,
            paths = downloadedPaths,
            sizeBytes = totalBytes
        )

        return Result.success()
    }

    companion object {
        const val KEY_CHAPTER_ENDPOINT = "key_chapter_endpoint"
        const val KEY_COMIC_ENDPOINT = "key_comic_endpoint"
        const val KEY_COMIC_TITLE = "key_comic_title"
        const val KEY_COVER_URL = "key_cover_url"
        const val KEY_CHAPTER_TITLE = "key_chapter_title"
        const val KEY_PAGES_JSON = "key_pages_json"
    }
}
