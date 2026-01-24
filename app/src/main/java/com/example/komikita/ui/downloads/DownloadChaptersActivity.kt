package com.example.komikita.ui.downloads

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.komikita.data.local.AppDatabase
import com.example.komikita.data.local.entity.DownloadEntity
import com.example.komikita.databinding.ActivityDownloadChaptersBinding
import com.example.komikita.ui.adapter.DownloadAdapter
import com.example.komikita.ui.reader.ChapterReaderActivity
import com.example.komikita.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadChaptersActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDownloadChaptersBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: DownloadAdapter
    private var komikSlug: String? = null
    private var komikTitle: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadChaptersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        komikSlug = intent.getStringExtra("KOMIK_SLUG")
        komikTitle = intent.getStringExtra("KOMIK_TITLE")
        
        setupToolbar()
        setupRecyclerView()
        loadChapters()
    }
    
    private fun setupToolbar() {
        binding.toolbar.title = komikTitle ?: "Downloads"
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = DownloadAdapter(
            onItemClick = { download ->
                // Open chapter reader in offline mode
                val intent = Intent(this, ChapterReaderActivity::class.java)
                intent.putExtra("CHAPTER_ID", download.chapterId)
                intent.putExtra("KOMIK_SLUG", download.komikSlug)
                intent.putExtra("KOMIK_TITLE", download.komikTitle)
                intent.putExtra("IS_OFFLINE", true)
                intent.putExtra("LOCAL_PATH", download.localPath)
                startActivity(intent)
            },
            onDeleteClick = { download ->
                deleteDownload(download)
            }
        )
        
        binding.rvChapters.layoutManager = LinearLayoutManager(this)
        binding.rvChapters.adapter = adapter
    }
    
    private fun loadChapters() {
        val userId = sessionManager.getUserId() ?: return
        val slug = komikSlug ?: return
        
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            val downloadDao = AppDatabase.getDatabase(this@DownloadChaptersActivity).downloadDao()
            
            downloadDao.getDownloadsByKomik(userId, slug).collectLatest { downloads ->
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    
                    // Sort by chapter number
                    val sortedDownloads = downloads.sortedBy { download ->
                        // Extract number from chapter title
                        download.chapterTitle.filter { it.isDigit() }.toIntOrNull() ?: 0
                    }
                    
                    if (sortedDownloads.isEmpty()) {
                        showEmptyState(true)
                    } else {
                        showEmptyState(false)
                        adapter.submitList(sortedDownloads)
                    }
                }
            }
        }
    }
    
    private fun showEmptyState(show: Boolean) {
        binding.emptyState.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvChapters.visibility = if (show) View.GONE else View.VISIBLE
    }
    
    private fun deleteDownload(download: DownloadEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            val downloadDao = AppDatabase.getDatabase(this@DownloadChaptersActivity).downloadDao()
            downloadDao.deleteDownload(download)
            
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@DownloadChaptersActivity,
                    "${download.chapterTitle} dihapus dari download",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
