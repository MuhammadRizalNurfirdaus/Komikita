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
import com.example.komikita.databinding.ActivityDownloadsBinding
import com.example.komikita.ui.adapter.DownloadAdapter
import com.example.komikita.ui.auth.LoginActivity
import com.example.komikita.ui.reader.ChapterReaderActivity
import com.example.komikita.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDownloadsBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: DownloadAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        
        setupToolbar()
        setupRecyclerView()
        setupEmptyState()
        loadDownloads()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
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
        
        binding.rvDownloads.layoutManager = LinearLayoutManager(this)
        binding.rvDownloads.adapter = adapter
    }
    
    private fun setupEmptyState() {
        if (!sessionManager.isLoggedIn()) {
            // Guest user
            binding.tvEmptyTitle.text = "Login untuk download"
            binding.tvEmptySubtitle.text = "Anda perlu login untuk mendownload dan membaca komik secara offline"
            binding.btnLoginEmpty.visibility = View.VISIBLE
            binding.btnLoginEmpty.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
    
    private fun loadDownloads() {
        if (!sessionManager.isLoggedIn()) {
            // Show empty state with login prompt
            showEmptyState(true)
            return
        }
        
        val userId = sessionManager.getUserId() ?: return
        
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            val downloadDao = AppDatabase.getDatabase(this@DownloadsActivity).downloadDao()
            
            downloadDao.getDownloadsByUser(userId).collectLatest { downloads ->
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    
                    if (downloads.isEmpty()) {
                        showEmptyState(true)
                    } else {
                        showEmptyState(false)
                        adapter.submitList(downloads)
                    }
                }
            }
        }
    }
    
    private fun showEmptyState(show: Boolean) {
        binding.emptyState.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvDownloads.visibility = if (show) View.GONE else View.VISIBLE
    }
    
    private fun deleteDownload(download: DownloadEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            val downloadDao = AppDatabase.getDatabase(this@DownloadsActivity).downloadDao()
            downloadDao.deleteDownload(download)
            
            // TODO: Also delete local files
            
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@DownloadsActivity,
                    "${download.chapterTitle} dihapus dari download",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (sessionManager.isLoggedIn()) {
            loadDownloads()
        }
    }
}
