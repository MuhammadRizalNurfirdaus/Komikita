package com.example.komikita.ui.reader

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.komikita.R
import com.example.komikita.databinding.ActivityChapterReaderBinding
import com.example.komikita.data.local.AppDatabase
import com.example.komikita.data.local.entity.DownloadEntity
import com.example.komikita.data.repository.KomikRepository
import com.example.komikita.ui.adapter.PageAdapter
import com.example.komikita.ui.auth.LoginActivity
import com.example.komikita.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChapterReaderActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityChapterReaderBinding
    private lateinit var repository: KomikRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: PageAdapter
    private var chapterId: String? = null
    private var komikSlug: String? = null
    private var komikTitle: String? = null
    private var chapterTitle: String? = null
    private var isDownloaded = false
    private var imageUrls: List<String> = emptyList()
    
    // Navigation IDs
    private var nextChapterId: String? = null
    private var prevChapterId: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChapterReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        repository = KomikRepository()
        sessionManager = SessionManager(this)
        
        chapterId = intent.getStringExtra("CHAPTER_ID")
        komikSlug = intent.getStringExtra("KOMIK_SLUG")
        komikTitle = intent.getStringExtra("KOMIK_TITLE")
        val isOffline = intent.getBooleanExtra("IS_OFFLINE", false)
        
        setupToolbar()
        setupRecyclerView()
        setupDownloadFab()
        setupNavigation()
        
        if (isOffline) {
            // Load from local storage (TODO: implement)
            Toast.makeText(this, "Offline mode - coming soon", Toast.LENGTH_SHORT).show()
        } else {
            chapterId?.let { loadChapter(it) }
        }
    }
    
    private fun setupToolbar() {
        binding.toolbar.title = komikTitle ?: "Reading"
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = PageAdapter()
        binding.rvPages.layoutManager = LinearLayoutManager(this)
        binding.rvPages.adapter = adapter
    }
    
    private fun setupDownloadFab() {
        binding.fabDownload.setOnClickListener {
            handleDownloadClick()
        }
    }
    
    private fun setupNavigation() {
        // IDs for buttons are now defined in XML
        val btnPrev = findViewById<Button>(R.id.btnPrev)
        val btnNext = findViewById<Button>(R.id.btnNext)
        
        btnPrev?.setOnClickListener {
            prevChapterId?.let { id ->
                loadChapter(id)
            }
        }
        
        btnNext?.setOnClickListener {
            nextChapterId?.let { id ->
                loadChapter(id)
            }
        }
    }
    
    private fun handleDownloadClick() {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            showLoginRequiredDialog()
            return
        }
        
        if (isDownloaded) {
            Toast.makeText(this, "Chapter sudah didownload", Toast.LENGTH_SHORT).show()
            return
        }
        
        downloadChapter()
    }
    
    private fun showLoginRequiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("Login Diperlukan")
            .setMessage("Anda perlu login untuk mendownload chapter. Login sekarang?")
            .setPositiveButton("Login") { _, _ ->
                startActivity(Intent(this, LoginActivity::class.java))
            }
            .setNegativeButton("Nanti") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun checkDownloadStatus() {
        if (!sessionManager.isLoggedIn()) {
            updateDownloadButton(false)
            return
        }
        
        val userId = sessionManager.getUserId() ?: return
        
        chapterId?.let { id ->
            lifecycleScope.launch(Dispatchers.IO) {
                val downloadDao = AppDatabase.getDatabase(this@ChapterReaderActivity).downloadDao()
                val download = downloadDao.getDownloadByChapterAndUser(id, userId)
                
                withContext(Dispatchers.Main) {
                    isDownloaded = download != null
                    updateDownloadButton(isDownloaded)
                }
            }
        }
    }
    
    private fun updateDownloadButton(downloaded: Boolean) {
        if (downloaded) {
            binding.fabDownload.setImageResource(android.R.drawable.stat_sys_download_done)
        } else {
            binding.fabDownload.setImageResource(android.R.drawable.stat_sys_download)
        }
    }
    
    private fun loadChapter(newChapterId: String) {
        // Update current ID
        this.chapterId = newChapterId
        
        // Reset state
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutNoInternet.root.visibility = View.GONE // Hide no internet layout
        updateNavigationButtons(false, false)
        
        // Scroll to top
        binding.rvPages.scrollToPosition(0)
        
        // Check download status for new chapter
        checkDownloadStatus()
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = repository.getChapter(newChapterId)
                
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    
                    result.onSuccess { response ->
                        response.data?.let { chapterData ->
                            chapterTitle = chapterData.title ?: "Chapter"
                            binding.toolbar.subtitle = chapterTitle
                            
                            chapterData.images?.let { images: List<String> ->
                                imageUrls = images
                                adapter.submitList(images)
                            }
                            
                            // Handle navigation
                            nextChapterId = chapterData.nextChapterId
                            prevChapterId = chapterData.prevChapterId
                            
                            // Enable/Disable buttons based on ID availability
                            val hasNext = !nextChapterId.isNullOrEmpty() && nextChapterId != "null"
                            val hasPrev = !prevChapterId.isNullOrEmpty() && prevChapterId != "null"
                            
                            updateNavigationButtons(hasPrev, hasNext)
                        }
                    }.onFailure {
                        Toast.makeText(this@ChapterReaderActivity, "Failed to load chapter", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutNoInternet.root.visibility = View.VISIBLE
                    Toast.makeText(this@ChapterReaderActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun updateNavigationButtons(enablePrev: Boolean, enableNext: Boolean) {
        val btnPrev = findViewById<Button>(R.id.btnPrev)
        val btnNext = findViewById<Button>(R.id.btnNext)
        
        btnPrev?.isEnabled = enablePrev
        btnNext?.isEnabled = enableNext
        
        // Visual cue (optional, buttons handles enabled state visually usually)
        btnPrev?.alpha = if (enablePrev) 1.0f else 0.5f
        btnNext?.alpha = if (enableNext) 1.0f else 0.5f
    }
    
    private fun downloadChapter() {
        val userId = sessionManager.getUserId() ?: return
        
        if (imageUrls.isEmpty()) {
            Toast.makeText(this, "Tidak ada gambar untuk didownload", Toast.LENGTH_SHORT).show()
            return
        }
        
        Toast.makeText(this, "Memulai download...", Toast.LENGTH_SHORT).show()
        
        lifecycleScope.launch(Dispatchers.IO) {
            val downloadDao = AppDatabase.getDatabase(this@ChapterReaderActivity).downloadDao()
            
            // Save download record
            val download = DownloadEntity(
                komikSlug = komikSlug ?: "",
                komikTitle = komikTitle ?: "Unknown",
                chapterId = chapterId ?: "",
                chapterTitle = chapterTitle ?: "Chapter",
                userId = userId,
                localPath = null, // TODO: implement actual file download
                status = "completed"
            )
            
            downloadDao.insertDownload(download)
            
            withContext(Dispatchers.Main) {
                isDownloaded = true
                updateDownloadButton(true)
                Toast.makeText(
                    this@ChapterReaderActivity,
                    "Chapter berhasil didownload! 📥",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
