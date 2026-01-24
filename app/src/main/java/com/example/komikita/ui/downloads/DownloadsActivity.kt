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
import com.example.komikita.R
import com.example.komikita.databinding.ActivityDownloadsBinding
import com.example.komikita.ui.adapter.DownloadFolder
import com.example.komikita.ui.adapter.DownloadFolderAdapter
import com.example.komikita.ui.auth.LoginActivity
import com.example.komikita.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDownloadsBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var folderAdapter: DownloadFolderAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        
        setupToolbar()
        setupBottomNavigation()
        setupRecyclerView()
        setupEmptyState()
        loadDownloads()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_downloads

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this@DownloadsActivity, com.example.komikita.ui.dashboard.DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_search -> {
                    val intent = Intent(this@DownloadsActivity, com.example.komikita.ui.search.SearchActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_favorites -> {
                    val intent = Intent(this@DownloadsActivity, com.example.komikita.ui.favorites.FavoritesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_downloads -> true
                R.id.nav_profile -> {
                    val intent = Intent(this@DownloadsActivity, com.example.komikita.ui.profile.ProfileActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupRecyclerView() {
        folderAdapter = DownloadFolderAdapter { folder ->
            // Open chapters inside folder
            val intent = Intent(this, DownloadChaptersActivity::class.java)
            intent.putExtra("KOMIK_SLUG", folder.komikSlug)
            intent.putExtra("KOMIK_TITLE", folder.komikTitle)
            startActivity(intent)
        }
        
        binding.rvDownloads.layoutManager = LinearLayoutManager(this)
        binding.rvDownloads.adapter = folderAdapter
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
                        
                        // Group downloads by komik
                        val folders = downloads
                            .groupBy { it.komikSlug }
                            .map { (slug, items) ->
                                DownloadFolder(
                                    komikSlug = slug,
                                    komikTitle = items.first().komikTitle,
                                    chapterCount = items.size
                                )
                            }
                            .sortedBy { it.komikTitle }
                        
                        folderAdapter.submitList(folders)
                    }
                }
            }
        }
    }
    
    private fun showEmptyState(show: Boolean) {
        binding.emptyState.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvDownloads.visibility = if (show) View.GONE else View.VISIBLE
    }
    
    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_downloads
        if (sessionManager.isLoggedIn()) {
            loadDownloads()
        }
    }
}
