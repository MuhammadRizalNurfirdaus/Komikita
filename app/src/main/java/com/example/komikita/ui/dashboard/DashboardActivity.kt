package com.example.komikita.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.komikita.R
import com.example.komikita.databinding.ActivityDashboardBinding
import com.example.komikita.data.repository.KomikRepository
import com.example.komikita.ui.adapter.KomikAdapter
import com.example.komikita.ui.search.SearchActivity
import com.example.komikita.ui.favorites.FavoritesActivity
import com.example.komikita.ui.downloads.DownloadsActivity
import com.example.komikita.ui.profile.ProfileActivity
import com.example.komikita.ui.detail.KomikDetailActivity
import com.example.komikita.ui.manga.MangaListActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var repository: KomikRepository
    private lateinit var adapter: KomikAdapter
    private var currentPage = 1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        repository = KomikRepository()
        setupRecyclerView()
        setupBottomNavigation()
        loadComics()
    }
    
    private fun setupRecyclerView() {
        adapter = KomikAdapter { komikSlug ->
            val intent = Intent(this, KomikDetailActivity::class.java)
            intent.putExtra("KOMIK_ID", komikSlug)
            startActivity(intent)
        }
        
        binding.rvComics.layoutManager = GridLayoutManager(this, 2)
        binding.rvComics.adapter = adapter
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home
                    true
                }
                R.id.nav_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    true
                }
                R.id.nav_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    true
                }
                R.id.nav_downloads -> {
                    startActivity(Intent(this, DownloadsActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
    
    private fun loadComics() {
        binding.progressBar.visibility = View.VISIBLE
        
        CoroutineScope(Dispatchers.IO).launch {
            val result = repository.getMangaList(currentPage)
            
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                
                result.onSuccess { response ->
                    response.data?.let { comics ->
                        adapter.submitList(comics)
                    }
                }.onFailure { error ->
                    Toast.makeText(
                        this@DashboardActivity,
                        "Error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
