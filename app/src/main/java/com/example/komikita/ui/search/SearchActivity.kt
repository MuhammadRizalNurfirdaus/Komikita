package com.example.komikita.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.komikita.databinding.ActivitySearchBinding
import com.example.komikita.data.repository.KomikRepository
import com.example.komikita.ui.adapter.KomikAdapter
import com.example.komikita.ui.adapter.KomikListAdapter
import com.example.komikita.ui.detail.KomikDetailActivity
import com.example.komikita.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.example.komikita.ui.base.BaseActivity

class SearchActivity : BaseActivity() {
    
    private lateinit var binding: ActivitySearchBinding
    private lateinit var repository: KomikRepository
    private lateinit var adapter: KomikAdapter
    private lateinit var listAdapter: KomikListAdapter
    private var searchJob: Job? = null
    private var selectedCategory = "manga" // default
    private var selectedGenre: String? = null
    private var currentPage = 1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        repository = KomikRepository()
        setupToolbar()
        setupRecyclerView()
        setupSearchField()
        setupCategoryChips()
        setupGenreChips()
        setupBottomNavigation()
        
        // Load initial data (manga)
        // Load initial data (manga)
        loadByCategory()
    }
    
    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_search
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_search

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this@SearchActivity, com.example.komikita.ui.dashboard.DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_search -> true
                R.id.nav_favorites -> {
                    val intent = Intent(this@SearchActivity, com.example.komikita.ui.favorites.FavoritesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_downloads -> {
                    val intent = Intent(this@SearchActivity, com.example.komikita.ui.downloads.DownloadsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this@SearchActivity, com.example.komikita.ui.profile.ProfileActivity::class.java)
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
        adapter = KomikAdapter { komikSlug ->
            val intent = Intent(this, KomikDetailActivity::class.java)
            intent.putExtra("KOMIK_ID", komikSlug)
            startActivity(intent)
        }
        
        listAdapter = KomikListAdapter { komikSlug ->
            val intent = Intent(this, KomikDetailActivity::class.java)
            intent.putExtra("KOMIK_ID", komikSlug)
            startActivity(intent)
        }
        
        binding.rvSearchResults.layoutManager = LinearLayoutManager(this)
        binding.rvSearchResults.adapter = adapter
    }
    
    private fun setupSearchField() {
        binding.etSearch.addTextChangedListener { text ->
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(500) // Debounce
                text?.toString()?.let { query ->
                    if (query.length >= 3) {
                        performSearch(query)
                    } else if (query.isEmpty()) {
                        // Reset to category view
                        clearGenreSelection()
                        loadByCategory()
                    }
                }
            }
        }
    }
    
    private fun setupCategoryChips() {
        binding.chipManga.setOnClickListener {
            selectedCategory = "manga"
            binding.rvSearchResults.adapter = adapter
            binding.etSearch.text?.clear()
            clearGenreSelection()
            loadByCategory()
        }
        
        binding.chipManhwa.setOnClickListener {
            selectedCategory = "manhwa"
            binding.rvSearchResults.adapter = listAdapter
            binding.etSearch.text?.clear()
            clearGenreSelection()
            loadByCategory()
        }
        
        binding.chipManhua.setOnClickListener {
            selectedCategory = "manhua"
            binding.rvSearchResults.adapter = listAdapter
            binding.etSearch.text?.clear()
            clearGenreSelection()
            loadByCategory()
        }
    }
    
    private fun setupGenreChips() {
        binding.chipAction.setOnClickListener { 
            if (binding.chipAction.isChecked) {
                selectedGenre = "action"
                performSearch("action")
            } else {
                selectedGenre = null
                loadByCategory()
            }
        }
        binding.chipRomance.setOnClickListener { 
            if (binding.chipRomance.isChecked) {
                selectedGenre = "romance"
                performSearch("romance")
            } else {
                selectedGenre = null
                loadByCategory()
            }
        }
        binding.chipFantasy.setOnClickListener { 
            if (binding.chipFantasy.isChecked) {
                selectedGenre = "fantasy"
                performSearch("fantasy")
            } else {
                selectedGenre = null
                loadByCategory()
            }
        }
        binding.chipComedy.setOnClickListener { 
            if (binding.chipComedy.isChecked) {
                selectedGenre = "comedy"
                performSearch("comedy")
            } else {
                selectedGenre = null
                loadByCategory()
            }
        }
    }
    
    private fun clearGenreSelection() {
        selectedGenre = null
        binding.chipAction.isChecked = false
        binding.chipRomance.isChecked = false
        binding.chipFantasy.isChecked = false
        binding.chipComedy.isChecked = false
    }
    
    private fun loadByCategory() {
        binding.progressBar.visibility = View.VISIBLE
        currentPage = 1
        
        lifecycleScope.launch {
            try {
                when (selectedCategory) {
                    "manga" -> {
                        binding.rvSearchResults.adapter = adapter
                        val result = repository.getMangaList(currentPage)
                        result.onSuccess { response ->
                            response.data?.let { comics ->
                                adapter.submitList(comics)
                            }
                        }.onFailure { error ->
                            showError(error.message)
                        }
                    }
                    "manhwa" -> {
                        binding.rvSearchResults.adapter = listAdapter
                        val result = repository.getManhwaList(currentPage)
                        result.onSuccess { response ->
                            response.data?.let { comics ->
                                listAdapter.submitList(comics)
                            }
                        }.onFailure { error ->
                            showError(error.message)
                        }
                    }
                    "manhua" -> {
                        binding.rvSearchResults.adapter = listAdapter
                        val result = repository.getManhuaList(currentPage)
                        result.onSuccess { response ->
                            response.data?.let { comics ->
                                listAdapter.submitList(comics)
                            }
                        }.onFailure { error ->
                            showError(error.message)
                        }
                    }
                }
            } catch (e: Exception) {
                showError(e.message)
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun performSearch(query: String) {
        binding.progressBar.visibility = View.VISIBLE
        
        // Use the adapter for search results
        binding.rvSearchResults.adapter = adapter
        
        lifecycleScope.launch {
            try {
                val result = repository.searchKomik(query, currentPage)
                
                result.onSuccess { response ->
                    response.data?.let { comics ->
                        if (comics.isEmpty()) {
                            Toast.makeText(
                                this@SearchActivity,
                                "Tidak ditemukan hasil untuk \"$query\"",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        adapter.submitList(comics)
                    } ?: run {
                        Toast.makeText(
                            this@SearchActivity,
                            "Tidak ditemukan hasil untuk \"$query\"",
                            Toast.LENGTH_SHORT
                        ).show()
                        adapter.submitList(emptyList())
                    }
                }.onFailure { error ->
                    showError(error.message)
                }
            } catch (e: Exception) {
                showError(e.message)
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun showError(message: String?) {
        Toast.makeText(
            this,
            "Error: ${message ?: "Unknown error"}",
            Toast.LENGTH_SHORT
        ).show()
    }
}
