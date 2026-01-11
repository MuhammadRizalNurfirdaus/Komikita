package com.example.komikita.ui.favorites

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.komikita.data.local.AppDatabase
import com.example.komikita.data.local.entity.FavoriteEntity
import com.example.komikita.databinding.ActivityFavoritesBinding
import com.example.komikita.ui.adapter.FavoriteAdapter
import com.example.komikita.ui.auth.LoginActivity
import com.example.komikita.ui.detail.KomikDetailActivity
import com.example.komikita.util.SessionManager
import com.example.komikita.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoritesActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: FavoriteAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        
        setupToolbar()
        setupBottomNavigation()
        setupRecyclerView()
        setupEmptyState()
        loadFavorites()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_favorites

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this@FavoritesActivity, com.example.komikita.ui.dashboard.DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_search -> {
                    val intent = Intent(this@FavoritesActivity, com.example.komikita.ui.search.SearchActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_favorites -> true
                R.id.nav_downloads -> {
                    val intent = Intent(this@FavoritesActivity, com.example.komikita.ui.downloads.DownloadsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this@FavoritesActivity, com.example.komikita.ui.profile.ProfileActivity::class.java)
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
        adapter = FavoriteAdapter(
            onItemClick = { slug ->
                val intent = Intent(this, KomikDetailActivity::class.java)
                intent.putExtra("KOMIK_ID", slug)
                startActivity(intent)
            },
            onDeleteClick = { favorite ->
                deleteFavorite(favorite)
            }
        )
        
        binding.rvFavorites.layoutManager = LinearLayoutManager(this)
        binding.rvFavorites.adapter = adapter
    }
    
    private fun setupEmptyState() {
        if (!sessionManager.isLoggedIn()) {
            // Guest user
            binding.tvEmptyTitle.text = "Login untuk melihat favorit"
            binding.tvEmptySubtitle.text = "Anda perlu login untuk menyimpan dan melihat komik favorit"
            binding.btnLoginEmpty.visibility = View.VISIBLE
            binding.btnLoginEmpty.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
    
    private fun loadFavorites() {
        if (!sessionManager.isLoggedIn()) {
            // Show empty state with login prompt
            showEmptyState(true)
            return
        }
        
        val userId = sessionManager.getUserId() ?: return
        
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            val favoriteDao = AppDatabase.getDatabase(this@FavoritesActivity).favoriteDao()
            
            favoriteDao.getFavoritesByUser(userId).collectLatest { favorites ->
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    
                    if (favorites.isEmpty()) {
                        showEmptyState(true)
                    } else {
                        showEmptyState(false)
                        adapter.submitList(favorites)
                    }
                }
            }
        }
    }
    
    private fun showEmptyState(show: Boolean) {
        binding.emptyState.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvFavorites.visibility = if (show) View.GONE else View.VISIBLE
    }
    
    private fun deleteFavorite(favorite: FavoriteEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            val favoriteDao = AppDatabase.getDatabase(this@FavoritesActivity).favoriteDao()
            favoriteDao.deleteFavorite(favorite)
            
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@FavoritesActivity,
                    "${favorite.title} dihapus dari favorit",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_favorites
        // Reload when coming back from detail
        if (sessionManager.isLoggedIn()) {
            loadFavorites()
        }
    }
}
