package com.example.komikita.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.komikita.data.local.entity.DownloadEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object ImageDownloader {

    suspend fun downloadImage(context: Context, imageUrl: String, savePath: File): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                
                val input = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(input)
                
                if (bitmap != null) {
                    // Create parent directories if they don't exist
                    savePath.parentFile?.mkdirs()
                    
                    val outputStream = FileOutputStream(savePath)
                    // Compress to JPG with 90% quality
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    outputStream.flush()
                    outputStream.close()
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    fun getLocalChapterDir(context: Context, komikTitle: String, chapterTitle: String): File {
        // Sanitize names for file system
        val safeKomikTitle = komikTitle.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
        val safeChapterTitle = chapterTitle.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
        
        // Use external files dir (Android/data/package/files) - no permission needed
        return File(context.getExternalFilesDir(null), "Downloads/$safeKomikTitle/$safeChapterTitle")
    }

    fun getChapterImages(localPath: String): List<String> {
        val dir = File(localPath)
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        
        // Return sorted list of image file paths
        return dir.listFiles()
            ?.filter { it.extension == "jpg" || it.extension == "png" }
            ?.sortedBy { file -> 
                // Sort by number in filename (image_1.jpg, image_2.jpg, etc)
                file.nameWithoutExtension.replace("image_", "").toIntOrNull() ?: 0
            }
            ?.map { it.absolutePath }
            ?: emptyList()
    }
}
