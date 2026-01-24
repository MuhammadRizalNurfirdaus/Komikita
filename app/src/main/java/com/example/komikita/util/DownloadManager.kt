package com.example.komikita.util

import android.content.Context
import com.example.komikita.data.local.AppDatabase
import com.example.komikita.data.local.entity.DownloadEntity
import com.example.komikita.data.repository.KomikRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DownloadStatus(
    val isDownloading: Boolean = false,
    val totalChapters: Int = 0,
    val currentChapterIndex: Int = 0,
    val currentChapterName: String = "",
    val progress: Int = 0, // 0-100
    val message: String = ""
)

object DownloadManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _status = MutableStateFlow(DownloadStatus())
    val status: StateFlow<DownloadStatus> = _status.asStateFlow()
    
    fun startDownload(context: Context, userId: String, komikId: String, komikTitle: String, chapters: List<com.example.komikita.data.model.Chapter>) {
        if (_status.value.isDownloading) return // Already downloading
        
        scope.launch {
            _status.value = DownloadStatus(isDownloading = true, totalChapters = chapters.size, message = "Memulai download...")
            
            val downloadDao = AppDatabase.getDatabase(context).downloadDao()
            val repository = KomikRepository()
            var successCount = 0
            val total = chapters.size
            
            try {
                for ((index, chapter) in chapters.withIndex()) {
                    val chapterTitle = chapter.chapter ?: "Chapter"
                    _status.value = _status.value.copy(
                        currentChapterIndex = index + 1,
                        currentChapterName = chapterTitle,
                        progress = ((index.toFloat() / total) * 100).toInt(),
                        message = "Downloading $chapterTitle..."
                    )
                    
                    // Notification
                    withContext(Dispatchers.Main) {
                        DownloadNotificationHelper.showDownloadProgressNotification(
                            context, total, index + 1, chapterTitle
                        )
                    }
                    
                    try {
                        // 1. Fetch chapter details
                        val chapterResult = repository.getChapter(chapter.id)
                        val chapterData = chapterResult.getOrNull()?.data
                        val imageUrls = chapterData?.images
                        
                        if (!imageUrls.isNullOrEmpty()) {
                            // 2. Prepare directory
                            val saveDir = ImageDownloader.getLocalChapterDir(context, komikTitle, chapterTitle)
                            
                            // 3. Download images
                            var imagesDownloaded = 0
                            val totalImages = imageUrls.size
                            
                            for ((imgIndex, imageUrl) in imageUrls.withIndex()) {
                                // Detailed progress for UI
                                val chapterBaseProgress = (index.toFloat() / total)
                                val imageProgress = (imgIndex.toFloat() / totalImages) / total
                                val totalDetailedProgress = ((chapterBaseProgress + imageProgress) * 100).toInt()
                                
                                _status.value = _status.value.copy(progress = totalDetailedProgress)
                                
                                val imageFile = java.io.File(saveDir, "image_$imgIndex.jpg")
                                if (ImageDownloader.downloadImage(context, imageUrl, imageFile)) {
                                    imagesDownloaded++
                                }
                            }
                            
                            // 4. Save to DB
                            if (imagesDownloaded > 0) {
                                val download = DownloadEntity(
                                    komikSlug = komikId,
                                    komikTitle = komikTitle,
                                    chapterId = chapter.id,
                                    chapterTitle = chapterTitle,
                                    userId = userId,
                                    localPath = saveDir.absolutePath,
                                    status = "completed"
                                )
                                downloadDao.insertDownload(download)
                                successCount++
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    kotlinx.coroutines.delay(100) // Small delay
                }
            } finally {
                withContext(Dispatchers.Main) {
                    DownloadNotificationHelper.showDownloadCompleteNotification(context, successCount)
                }
                _status.value = DownloadStatus(isDownloading = false, message = "Selesai! $successCount chapter berhasil.")
            }
        }
    }
}
