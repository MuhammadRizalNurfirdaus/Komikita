package com.example.komikita.ui.reader

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

import com.example.komikita.ui.base.BaseActivity

class ChapterReaderActivity : BaseActivity() {
    
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
    private var isOfflineMode = false
    
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
        isOfflineMode = intent.getBooleanExtra("IS_OFFLINE", false)
        
        setupToolbar()
        setupRecyclerView()
        setupDownloadFab()
        setupNavigation()
        
        if (isOfflineMode) {
            // Hide download FAB in offline mode
            binding.fabDownload.visibility = View.GONE
            
            // Hide Refresh button and spaces in offline mode (Only Next/Prev)
            findViewById<View>(R.id.btnRefresh)?.visibility = View.GONE
            findViewById<View>(R.id.space1)?.visibility = View.GONE
            findViewById<View>(R.id.space2)?.visibility = View.GONE
            
            // Load from local storage
            val localPath = intent.getStringExtra("LOCAL_PATH")
            if (localPath != null) {
                loadLocalChapter(localPath)
            } else {
                Toast.makeText(this, "Error: Path not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            chapterId?.let { loadChapter(it) }
        }
    }
    
    private fun setupToolbar() {
        binding.toolbar.title = komikTitle ?: "Reading"
        binding.toolbar.subtitle = chapterTitle
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
        if (isOfflineMode) {
            binding.fabDownload.visibility = View.GONE
        } else {
            binding.fabDownload.setOnClickListener {
                handleDownloadClick()
            }
        }
    }
    
    private var isNavHidden = false
    
    private fun setupScrollListener() {
        binding.rvPages.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                if (dy > 0 && !isNavHidden) {
                    // Scroll DOWN - INSTANT HIDE
                    isNavHidden = true
                    binding.layoutNavigation.visibility = View.GONE
                    if (!isOfflineMode) binding.fabDownload.visibility = View.GONE
                } else if (dy < 0 && isNavHidden) {
                    // Scroll UP - INSTANT SHOW
                    isNavHidden = false
                    binding.layoutNavigation.visibility = View.VISIBLE
                    if (!isOfflineMode) binding.fabDownload.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setupNavigation() {
        setupScrollListener()
        // IDs for buttons are now defined in XML
        val btnPrev = findViewById<Button>(R.id.btnPrev)
        val btnNext = findViewById<Button>(R.id.btnNext)
        val btnRefresh = findViewById<View>(R.id.btnRefresh)
        
        btnPrev?.setOnClickListener {
            prevChapterId?.let { id ->
                loadChapter(id)
            }
        }
        
        btnRefresh?.setOnClickListener {
            chapterId?.let { id ->
                Toast.makeText(this, "Reloading chapter...", Toast.LENGTH_SHORT).show()
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
            binding.fabDownload.setImageResource(R.drawable.ic_download_done)
        } else {
            binding.fabDownload.setImageResource(R.drawable.ic_download)
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
                            
                            Log.d("ChapterReader", "===== NAVIGATION DEBUG =====")
                            Log.d("ChapterReader", "nextChapterId from API: '$nextChapterId'")
                            Log.d("ChapterReader", "prevChapterId from API: '$prevChapterId'")
                            
                            // Enable/Disable buttons based on ID availability
                            val hasNext = isValidChapterId(nextChapterId)
                            val hasPrev = isValidChapterId(prevChapterId)
                            
                            Log.d("ChapterReader", "hasNext: $hasNext, hasPrev: $hasPrev")
                            
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
        val btnRefresh = findViewById<View>(R.id.btnRefresh)
        val space1 = findViewById<View>(R.id.space1)
        val space2 = findViewById<View>(R.id.space2)
        
        // Hide buttons completely if chapter not available
        btnPrev?.visibility = if (enablePrev) View.VISIBLE else View.GONE
        btnNext?.visibility = if (enableNext) View.VISIBLE else View.GONE
        
        // Handle spaces visibility based on adjacent button visibility
        // Space1 is between Prev and Refresh - only show if Prev is visible
        space1?.visibility = if (enablePrev && !isOfflineMode) View.VISIBLE else View.GONE
        // Space2 is between Refresh and Next - only show if Next is visible  
        space2?.visibility = if (enableNext && !isOfflineMode) View.VISIBLE else View.GONE
        
        // In offline mode, hide refresh button
        if (isOfflineMode) {
            btnRefresh?.visibility = View.GONE
        }
        
        // Show/hide the entire navigation layout
        // In offline mode: hide if no prev AND no next (single chapter downloaded)
        // In online mode: always show (refresh button is always useful)
        val hasAnyNavButton = enablePrev || enableNext
        if (isOfflineMode) {
            // Offline mode: only show nav if there are navigation buttons
            binding.layoutNavigation.visibility = if (hasAnyNavButton) View.VISIBLE else View.GONE
        } else {
            // Online mode: always show navigation (has refresh button)
            binding.layoutNavigation.visibility = View.VISIBLE
        }
    }
    
    // Helper function to check if chapter ID is valid (not null, empty, "null", "0", "undefined", etc.)
    private fun isValidChapterId(chapterId: String?): Boolean {
        Log.d("ChapterReader", "Checking chapterId: '$chapterId'")
        
        if (chapterId.isNullOrBlank()) {
            Log.d("ChapterReader", "Invalid: null or blank")
            return false
        }
        if (chapterId.equals("null", ignoreCase = true)) {
            Log.d("ChapterReader", "Invalid: 'null' string")
            return false
        }
        if (chapterId.equals("undefined", ignoreCase = true)) {
            Log.d("ChapterReader", "Invalid: 'undefined' string")
            return false
        }
        if (chapterId == "0") {
            Log.d("ChapterReader", "Invalid: '0'")
            return false
        }
        
        Log.d("ChapterReader", "Valid chapterId: '$chapterId'")
        return true
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

    private fun loadLocalChapter(localPath: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvPages.visibility = View.GONE
        binding.layoutNoInternet.root.visibility = View.GONE
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Get list of image paths from local directory
                val images = com.example.komikita.util.ImageDownloader.getChapterImages(localPath)
                
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    
                    if (images.isNotEmpty()) {
                        imageUrls = images
                        adapter.submitList(images)
                        binding.rvPages.visibility = View.VISIBLE
                        
                        // In offline mode with single downloaded chapter, no prev/next available
                        updateNavigationButtons(enablePrev = false, enableNext = false) 
                    } else {
                        binding.layoutNoInternet.root.visibility = View.VISIBLE
                        Toast.makeText(this@ChapterReaderActivity, "No images found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutNoInternet.root.visibility = View.VISIBLE
                    Toast.makeText(this@ChapterReaderActivity, "Error loading chapter: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
