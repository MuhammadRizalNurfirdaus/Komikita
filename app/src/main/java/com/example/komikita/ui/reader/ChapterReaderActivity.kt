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
    private var localPath: String? = null
    
    // Navigation IDs (from API)
    private var nextChapterId: String? = null
    private var prevChapterId: String? = null
    
    // Chapter list for reliable navigation (from intent)
    private var chapterIds: Array<String>? = null
    
    // Offline Navigation Info
    private var offlinePrevChapterId: String? = null
    private var offlinePrevLocalPath: String? = null
    private var offlinePrevChapterTitle: String? = null
    private var offlineNextChapterId: String? = null
    private var offlineNextLocalPath: String? = null
    private var offlineNextChapterTitle: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChapterReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        repository = KomikRepository()
        sessionManager = SessionManager(this)
        
        chapterId = intent.getStringExtra("CHAPTER_ID")
        komikSlug = intent.getStringExtra("KOMIK_SLUG")
        komikTitle = intent.getStringExtra("KOMIK_TITLE")
        chapterTitle = intent.getStringExtra("CHAPTER_TITLE")
        isOfflineMode = intent.getBooleanExtra("IS_OFFLINE", false)
        localPath = intent.getStringExtra("LOCAL_PATH")
        
        // Get chapter list for reliable navigation (online mode)
        chapterIds = intent.getStringArrayExtra("CHAPTER_IDS")
        Log.d("ChapterReader", "Received chapterIds: ${chapterIds?.size ?: 0} chapters")
        
        // Extract offline navigation info
        if (isOfflineMode) {
            offlinePrevChapterId = intent.getStringExtra("OFFLINE_PREV_CHAPTER_ID")
            offlinePrevLocalPath = intent.getStringExtra("OFFLINE_PREV_LOCAL_PATH")
            offlinePrevChapterTitle = intent.getStringExtra("OFFLINE_PREV_CHAPTER_TITLE")
            offlineNextChapterId = intent.getStringExtra("OFFLINE_NEXT_CHAPTER_ID")
            offlineNextLocalPath = intent.getStringExtra("OFFLINE_NEXT_LOCAL_PATH")
            offlineNextChapterTitle = intent.getStringExtra("OFFLINE_NEXT_CHAPTER_TITLE")
        }
        
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
            if (localPath != null) {
                loadLocalChapter(localPath!!)
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
    private var shouldShowNavigation = true // Track if navigation should be visible at all
    
    private fun setupScrollListener() {
        binding.rvPages.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                // Only handle scroll hide/show if navigation should be visible
                if (!shouldShowNavigation) return
                
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
            Log.d("ChapterReader", "Prev button clicked! isOfflineMode=$isOfflineMode, prevChapterId=$prevChapterId")
            if (isOfflineMode) {
                // Offline mode: navigate to previous downloaded chapter
                offlinePrevLocalPath?.let { path ->
                    navigateToOfflineChapter(
                        path = path,
                        chapterId = offlinePrevChapterId,
                        chapterTitle = offlinePrevChapterTitle
                    )
                }
            } else {
                // Online mode: load previous chapter from API
                if (isValidChapterId(prevChapterId)) {
                    prevChapterId?.let { id ->
                        Log.d("ChapterReader", "Loading prev chapter: $id")
                        loadChapter(id)
                    }
                } else {
                    Log.d("ChapterReader", "Invalid prevChapterId, not navigating")
                }
            }
        }
        
        btnRefresh?.setOnClickListener {
            chapterId?.let { id ->
                Toast.makeText(this, "Reloading chapter...", Toast.LENGTH_SHORT).show()
                loadChapter(id)
            }
        }
        
        btnNext?.setOnClickListener {
            Log.d("ChapterReader", "Next button clicked! isOfflineMode=$isOfflineMode, nextChapterId=$nextChapterId")
            if (isOfflineMode) {
                // Offline mode: navigate to next downloaded chapter
                offlineNextLocalPath?.let { path ->
                    navigateToOfflineChapter(
                        path = path,
                        chapterId = offlineNextChapterId,
                        chapterTitle = offlineNextChapterTitle
                    )
                }
            } else {
                // Online mode: load next chapter from API
                if (isValidChapterId(nextChapterId)) {
                    nextChapterId?.let { id ->
                        Log.d("ChapterReader", "Loading next chapter: $id")
                        loadChapter(id)
                    }
                } else {
                    Log.d("ChapterReader", "Invalid nextChapterId, not navigating")
                }
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
                Log.d("ChapterReader", "Loading chapter from API: $newChapterId")
                val result = repository.getChapter(newChapterId)
                
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    
                    result.onSuccess { response ->
                        Log.d("ChapterReader", "API Response received")
                        
                        response.data?.let { chapterData ->
                            chapterTitle = chapterData.title ?: "Chapter"
                            binding.toolbar.subtitle = chapterTitle
                            
                            chapterData.images?.let { images: List<String> ->
                                imageUrls = images
                                adapter.submitList(images)
                            }
                            
                            // Determine navigation based on chapter list (priority) or API response (fallback)
                            val (hasPrev, hasNext) = calculateNavigation(newChapterId, chapterData.prevChapterId, chapterData.nextChapterId)
                            
                            updateNavigationButtons(hasPrev, hasNext)
                        }
                    }.onFailure { error ->
                        Log.e("ChapterReader", "Failed to load chapter", error)
                        Toast.makeText(this@ChapterReaderActivity, "Failed to load chapter", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ChapterReader", "Exception loading chapter", e)
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutNoInternet.root.visibility = View.VISIBLE
                    Toast.makeText(this@ChapterReaderActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    /**
     * Calculate navigation availability.
     * Priority: Use chapterIds list if available (reliable).
     * Fallback: Use API response prev/next IDs.
     */
    private fun calculateNavigation(currentChapterId: String, apiPrevId: String?, apiNextId: String?): Pair<Boolean, Boolean> {
        Log.d("ChapterReader", "===== NAVIGATION CALCULATION =====")
        Log.d("ChapterReader", "Current chapterId: $currentChapterId")
        Log.d("ChapterReader", "chapterIds list available: ${chapterIds != null}, size: ${chapterIds?.size ?: 0}")
        
        // If we have the chapter list, use it for reliable navigation
        if (chapterIds != null && chapterIds!!.isNotEmpty()) {
            val currentIndex = chapterIds!!.indexOf(currentChapterId)
            Log.d("ChapterReader", "Current chapter index in list: $currentIndex")
            
            if (currentIndex != -1) {
                // Chapter list is ordered from newest (index 0) to oldest (last index)
                // So: Prev = index - 1 (newer chapter), Next = index + 1 (older chapter)
                // But typically users expect: Prev = older chapter, Next = newer chapter
                // Let's check the order: chapters[0] = chapter 84 (newest), chapters[last] = chapter 1 (oldest)
                
                // For reading order (chapter 1 -> 2 -> 3...):
                // Prev should go to lower chapter number (older in list = higher index)
                // Next should go to higher chapter number (newer in list = lower index)
                
                // Actually, looking at the list: index 0 = newest chapter, so:
                // - hasPrev = there's a chapter at index+1 (older/lower chapter number)
                // - hasNext = there's a chapter at index-1 (newer/higher chapter number)
                
                val hasPrev = currentIndex < chapterIds!!.size - 1  // There's older chapter
                val hasNext = currentIndex > 0  // There's newer chapter
                
                // Set the actual IDs for navigation
                prevChapterId = if (hasPrev) chapterIds!![currentIndex + 1] else null
                nextChapterId = if (hasNext) chapterIds!![currentIndex - 1] else null
                
                Log.d("ChapterReader", "Using chapterIds list - hasPrev: $hasPrev (id: $prevChapterId), hasNext: $hasNext (id: $nextChapterId)")
                return Pair(hasPrev, hasNext)
            }
        }
        
        // Fallback to API response
        Log.d("ChapterReader", "Fallback to API response - prevId: '$apiPrevId', nextId: '$apiNextId'")
        prevChapterId = apiPrevId
        nextChapterId = apiNextId
        
        val hasPrev = isValidChapterId(apiPrevId)
        val hasNext = isValidChapterId(apiNextId)
        
        Log.d("ChapterReader", "API validation result - hasPrev: $hasPrev, hasNext: $hasNext")
        return Pair(hasPrev, hasNext)
    }
    
    private fun updateNavigationButtons(enablePrev: Boolean, enableNext: Boolean) {
        val btnPrev = findViewById<Button>(R.id.btnPrev)
        val btnNext = findViewById<Button>(R.id.btnNext)
        val btnRefresh = findViewById<View>(R.id.btnRefresh)
        val space1 = findViewById<View>(R.id.space1)
        val space2 = findViewById<View>(R.id.space2)
        
        Log.d("ChapterReader", "updateNavigationButtons: enablePrev=$enablePrev, enableNext=$enableNext, isOfflineMode=$isOfflineMode")
        
        // Hide buttons completely if chapter not available
        btnPrev?.visibility = if (enablePrev) View.VISIBLE else View.GONE
        btnNext?.visibility = if (enableNext) View.VISIBLE else View.GONE
        
        if (isOfflineMode) {
            // Offline mode: hide refresh button and spaces
            btnRefresh?.visibility = View.GONE
            space1?.visibility = View.GONE
            space2?.visibility = View.GONE
        } else {
            // Online mode: always show refresh button
            btnRefresh?.visibility = View.VISIBLE
            // Show spaces based on adjacent button visibility
            space1?.visibility = if (enablePrev) View.VISIBLE else View.GONE
            space2?.visibility = if (enableNext) View.VISIBLE else View.GONE
        }
        
        // Show/hide the entire navigation layout
        // In offline mode: hide if no prev AND no next (single chapter downloaded)
        // In online mode: always show (refresh button is always useful)
        val hasAnyNavButton = enablePrev || enableNext
        if (isOfflineMode) {
            // Offline mode: only show nav if there are navigation buttons
            shouldShowNavigation = hasAnyNavButton
            binding.layoutNavigation.visibility = if (hasAnyNavButton) View.VISIBLE else View.GONE
        } else {
            // Online mode: always show navigation (has refresh button)
            shouldShowNavigation = true
            binding.layoutNavigation.visibility = View.VISIBLE
        }
        
        Log.d("ChapterReader", "Navigation updated: btnPrev=${btnPrev?.visibility}, btnNext=${btnNext?.visibility}, btnRefresh=${btnRefresh?.visibility}")
    }
    
    // Helper function to check if chapter ID is valid (not null, empty, "null", "0", "undefined", etc.)
    private fun isValidChapterId(chapterId: String?): Boolean {
        Log.d("ChapterReader", "Checking chapterId: '$chapterId'")
        
        // Check null or blank
        if (chapterId.isNullOrBlank()) {
            Log.d("ChapterReader", "Invalid: null or blank")
            return false
        }
        
        val trimmed = chapterId.trim().lowercase()
        
        // List of invalid values that APIs commonly return for "no chapter"
        val invalidValues = listOf(
            "null", 
            "undefined", 
            "0", 
            "false", 
            "none", 
            "-", 
            "n/a",
            "#",
            ""
        )
        
        if (trimmed in invalidValues) {
            Log.d("ChapterReader", "Invalid: matches invalid value '$trimmed'")
            return false
        }
        
        // Check if it's just whitespace or special characters
        if (trimmed.matches(Regex("^[\\s\\-_#]+$"))) {
            Log.d("ChapterReader", "Invalid: only special characters")
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
        
        // Store current local path
        this.localPath = localPath
        
        // Scroll to top
        binding.rvPages.scrollToPosition(0)
        
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
                        
                        // Update toolbar subtitle with chapter title
                        binding.toolbar.subtitle = chapterTitle
                        
                        // In offline mode: check if prev/next chapters are available
                        val hasPrev = offlinePrevLocalPath != null
                        val hasNext = offlineNextLocalPath != null
                        
                        Log.d("ChapterReader", "===== OFFLINE NAVIGATION DEBUG =====")
                        Log.d("ChapterReader", "offlinePrevLocalPath: '$offlinePrevLocalPath'")
                        Log.d("ChapterReader", "offlineNextLocalPath: '$offlineNextLocalPath'")
                        Log.d("ChapterReader", "hasPrev: $hasPrev, hasNext: $hasNext")
                        
                        updateNavigationButtons(enablePrev = hasPrev, enableNext = hasNext)
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
    
    private fun navigateToOfflineChapter(path: String, chapterId: String?, chapterTitle: String?) {
        // Update current chapter info
        this.chapterId = chapterId
        this.chapterTitle = chapterTitle
        this.localPath = path
        
        // We need to fetch the new prev/next info from database
        // For now, start a new activity with the new chapter info
        val intent = Intent(this, ChapterReaderActivity::class.java)
        intent.putExtra("CHAPTER_ID", chapterId)
        intent.putExtra("KOMIK_SLUG", komikSlug)
        intent.putExtra("KOMIK_TITLE", komikTitle)
        intent.putExtra("CHAPTER_TITLE", chapterTitle)
        intent.putExtra("IS_OFFLINE", true)
        intent.putExtra("LOCAL_PATH", path)
        
        // We need to get the prev/next chapter info from database
        lifecycleScope.launch(Dispatchers.IO) {
            val userId = sessionManager.getUserId() ?: return@launch
            val slug = komikSlug ?: return@launch
            
            val downloadDao = AppDatabase.getDatabase(this@ChapterReaderActivity).downloadDao()
            val downloads = downloadDao.getDownloadsByKomikSync(userId, slug)
            
            // Sort by chapter number
            val sortedDownloads = downloads.sortedBy { download ->
                download.chapterTitle.filter { it.isDigit() }.toIntOrNull() ?: 0
            }
            
            // Find current position
            val currentIndex = sortedDownloads.indexOfFirst { it.localPath == path }
            
            withContext(Dispatchers.Main) {
                if (currentIndex != -1) {
                    // Set prev chapter info
                    if (currentIndex > 0) {
                        val prevDownload = sortedDownloads[currentIndex - 1]
                        intent.putExtra("OFFLINE_PREV_CHAPTER_ID", prevDownload.chapterId)
                        intent.putExtra("OFFLINE_PREV_LOCAL_PATH", prevDownload.localPath)
                        intent.putExtra("OFFLINE_PREV_CHAPTER_TITLE", prevDownload.chapterTitle)
                    }
                    
                    // Set next chapter info
                    if (currentIndex < sortedDownloads.size - 1) {
                        val nextDownload = sortedDownloads[currentIndex + 1]
                        intent.putExtra("OFFLINE_NEXT_CHAPTER_ID", nextDownload.chapterId)
                        intent.putExtra("OFFLINE_NEXT_LOCAL_PATH", nextDownload.localPath)
                        intent.putExtra("OFFLINE_NEXT_CHAPTER_TITLE", nextDownload.chapterTitle)
                    }
                }
                
                // Start new activity and finish current
                startActivity(intent)
                finish()
            }
        }
    }
}
