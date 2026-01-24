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
        setupToolbar()
        setupRetry()
        loadComics()
    }
    
    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
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
    
    private fun setupRetry() {
        binding.layoutNoInternet.btnRetry.setOnClickListener {
            loadComics()
        }
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_home

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_search -> {
                    val intent = Intent(this@DashboardActivity, SearchActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_favorites -> {
                    val intent = Intent(this@DashboardActivity, FavoritesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_downloads -> {
                    val intent = Intent(this@DashboardActivity, DownloadsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this@DashboardActivity, ProfileActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }
    
    private fun loadComics() {
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutNoInternet.root.visibility = View.GONE
        binding.rvComics.visibility = View.GONE
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = repository.getMangaList(currentPage)
                
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    
                    result.onSuccess { response ->
                        response.data?.let { comics ->
                            binding.rvComics.visibility = View.VISIBLE
                            adapter.submitList(comics)
                        } ?: run {
                            showNoInternetState()
                        }
                    }.onFailure { error ->
                        showNoInternetState()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    showNoInternetState()
                }
            }
        }
    }
    
    private fun showNoInternetState() {
        binding.rvComics.visibility = View.GONE
        binding.layoutNoInternet.root.visibility = View.VISIBLE
    }
}
