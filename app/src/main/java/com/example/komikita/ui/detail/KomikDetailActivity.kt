package com.example.komikita.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.komikita.R
import com.example.komikita.databinding.ActivityKomikDetailBinding
import com.example.komikita.data.local.AppDatabase
import com.example.komikita.data.local.entity.FavoriteEntity
import com.example.komikita.data.model.DetailResponse
import com.example.komikita.data.model.DetailData
import com.example.komikita.data.repository.KomikRepository
import com.example.komikita.ui.adapter.ChapterAdapter
import com.example.komikita.ui.auth.LoginActivity
import com.example.komikita.ui.reader.ChapterReaderActivity
import com.example.komikita.util.SessionManager
import com.example.komikita.data.local.entity.DownloadEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KomikDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityKomikDetailBinding
    private lateinit var repository: KomikRepository
    private lateinit var sessionManager: SessionManager
    private var komikId: String? = null
    private var isFavorite = false
    private var currentDetail: DetailData? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKomikDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        repository = KomikRepository()
        sessionManager = SessionManager(this)
        komikId = intent.getStringExtra("KOMIK_ID")
        
        setupToolbar()
        setupFab()
        setupRetry()
        loadDetail()
        checkFavoriteStatus()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        binding.toolbar.inflateMenu(R.menu.menu_detail)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_download -> {
                    showDownloadSheet()
                    true
                }
                else -> false
            }
        }
    }

    private fun showDownloadSheet() {
        if (!sessionManager.isLoggedIn()) {
            showLoginRequiredDialog()
            return
        }
        
        currentDetail?.chapters?.let { chapters ->
            val bottomSheet = DownloadSelectionBottomSheet.newInstance(chapters)
            bottomSheet.setOnDownloadClickListener { chapter ->
                // Start download for this chapter
                downloadChapter(chapter)
            }
            bottomSheet.show(supportFragmentManager, "DownloadSelectionBottomSheet")
        }
    }

    private fun downloadChapter(chapter: com.example.komikita.data.model.Chapter) {
        val userId = sessionManager.getUserId() ?: return
        
        Toast.makeText(this, "Mulai download ${chapter.chapter}...", Toast.LENGTH_SHORT).show()
        
        lifecycleScope.launch(Dispatchers.IO) {
             val downloadDao = AppDatabase.getDatabase(this@KomikDetailActivity).downloadDao()

             // Basic mock download entry creation
             val download = DownloadEntity(
                komikSlug = komikId ?: "",
                komikTitle = currentDetail?.title ?: "Unknown",
                chapterId = chapter.id,
                chapterTitle = chapter.chapter ?: "Chapter",
                userId = userId,
                localPath = null,
                status = "completed"
             )
             downloadDao.insertDownload(download)
             
             withContext(Dispatchers.Main) {
                 Toast.makeText(this@KomikDetailActivity, "Download selesai! ✅", Toast.LENGTH_SHORT).show()
             }
        }
    }
    
    private fun setupFab() {
        binding.fabFavorite.setOnClickListener {
            handleFavoriteClick()
        }
    }

    private fun setupRetry() {
        binding.layoutNoInternet.btnRetry.setOnClickListener {
            loadDetail()
        }
        binding.layoutNoInternet.ivRetry.setOnClickListener {
            loadDetail()
        }
    }
    
    private fun handleFavoriteClick() {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            showLoginRequiredDialog()
            return
        }
        
        toggleFavorite()
    }
    
    private fun showLoginRequiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("Login Diperlukan")
            .setMessage("Anda perlu login untuk menambahkan komik ke favorit. Login sekarang?")
            .setPositiveButton("Login") { _, _ ->
                startActivity(Intent(this, LoginActivity::class.java))
            }
            .setNegativeButton("Nanti") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun checkFavoriteStatus() {
        if (!sessionManager.isLoggedIn()) {
            updateFavoriteIcon(false)
            return
        }
        
        val userId = sessionManager.getUserId() ?: return
        
        komikId?.let { id ->
            lifecycleScope.launch(Dispatchers.IO) {
                val favoriteDao = AppDatabase.getDatabase(this@KomikDetailActivity).favoriteDao()
                val favorite = favoriteDao.getFavoriteBySlugAndUser(id, userId)
                
                withContext(Dispatchers.Main) {
                    isFavorite = favorite != null
                    updateFavoriteIcon(isFavorite)
                }
            }
        }
    }
    
    private fun updateFavoriteIcon(isFav: Boolean) {
        if (isFav) {
            binding.fabFavorite.setImageResource(android.R.drawable.btn_star_big_on)
        } else {
            binding.fabFavorite.setImageResource(android.R.drawable.btn_star_big_off)
        }
    }
    
    private fun loadDetail() {
        binding.layoutNoInternet.root.visibility = View.GONE
        komikId?.let { id ->
            lifecycleScope.launch(Dispatchers.IO) {
                val result = repository.getKomikDetail(id)
                
                withContext(Dispatchers.Main) {
                    result.onSuccess { response ->
                        val detail = response.data
                        if (detail != null) {
                            currentDetail = detail
                            binding.tvTitle.text = detail.title
                            binding.tvAuthor.text = "By: ${detail.author ?: "Unknown"}"
                            binding.tvStatus.text = detail.status ?: "Unknown"
                            binding.tvDescription.text = detail.description ?: "No description"
                            
                            // Set status background color
                            val statusColor = when (detail.status?.lowercase()) {
                                "ongoing" -> "#4CAF50"
                                "completed" -> "#2196F3"
                                else -> "#9E9E9E"
                            }
                            binding.tvStatus.setBackgroundColor(android.graphics.Color.parseColor(statusColor))
                            
                            Glide.with(this@KomikDetailActivity)
                                .load(detail.poster)
                                .into(binding.ivPoster)
                            
                            // Setup chapters
                            detail.chapters?.let { chapters ->
                                val adapter = ChapterAdapter { chapterId ->
                                    val intent = Intent(this@KomikDetailActivity, ChapterReaderActivity::class.java)
                                    intent.putExtra("CHAPTER_ID", chapterId)
                                    intent.putExtra("KOMIK_SLUG", komikId)
                                    intent.putExtra("KOMIK_TITLE", detail.title ?: "Unknown")
                                    startActivity(intent)
                                }
                                binding.rvChapters.layoutManager = LinearLayoutManager(this@KomikDetailActivity)
                                binding.rvChapters.adapter = adapter
                                
                                // Show only first 5 chapters
                                val displayChapters = chapters.take(5)
                                adapter.submitList(displayChapters)
                                
                                // Setup See All button
                                binding.btnSeeAllChapters.setOnClickListener {
                                    val bottomSheet = ChapterListBottomSheet.newInstance(
                                        chapters, 
                                        komikId ?: "", 
                                        detail.title ?: ""
                                    )
                                    bottomSheet.show(supportFragmentManager, "ChapterListBottomSheet")
                                }
                            }
                        } else {
                            Toast.makeText(this@KomikDetailActivity, "Data komik tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                    }.onFailure {
                        Toast.makeText(this@KomikDetailActivity, "Failed to load details", Toast.LENGTH_SHORT).show()
                        binding.layoutNoInternet.root.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
    
    private fun toggleFavorite() {
        val userId = sessionManager.getUserId() ?: return
        val detail = currentDetail ?: return
        
        lifecycleScope.launch(Dispatchers.IO) {
            val favoriteDao = AppDatabase.getDatabase(this@KomikDetailActivity).favoriteDao()
            
            if (isFavorite) {
                // Remove from favorites
                favoriteDao.deleteFavoriteBySlugAndUser(komikId!!, userId)
                
                withContext(Dispatchers.Main) {
                    isFavorite = false
                    updateFavoriteIcon(false)
                    Toast.makeText(
                        this@KomikDetailActivity,
                        "${detail.title} dihapus dari favorit",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Add to favorites
                val favorite = FavoriteEntity(
                    slug = komikId!!,
                    title = detail.title ?: "Unknown",
                    poster = detail.poster,
                    type = detail.type,
                    userId = userId
                )
                favoriteDao.insertFavorite(favorite)
                
                withContext(Dispatchers.Main) {
                    isFavorite = true
                    updateFavoriteIcon(true)
                    Toast.makeText(
                        this@KomikDetailActivity,
                        "${detail.title} ditambahkan ke favorit ⭐",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Recheck favorite status when returning
        checkFavoriteStatus()
    }
}
