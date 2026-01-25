package com.example.komikita.ui.manga

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.komikita.databinding.ActivityDashboardBinding
import com.example.komikita.data.repository.KomikRepository
import com.example.komikita.ui.adapter.KomikAdapter
import com.example.komikita.ui.detail.KomikDetailActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.example.komikita.ui.base.BaseActivity

class MangaListActivity : BaseActivity() {
    
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var repository: KomikRepository
    private lateinit var adapter: KomikAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        repository = KomikRepository()
        binding.toolbar.title = "Manga"
        setupRecyclerView()
        loadManga()
    }
    
    private fun setupRecyclerView() {
        adapter = KomikAdapter { slug ->
            val intent = Intent(this, KomikDetailActivity::class.java)
            intent.putExtra("KOMIK_ID", slug)
            startActivity(intent)
        }
        binding.rvComics.layoutManager = LinearLayoutManager(this)
        binding.rvComics.adapter = adapter
    }
    
    private fun loadManga() {
        binding.progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            val result = repository.getMangaList(1)
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                result.onSuccess { response ->
                    adapter.submitList(response.data)
                }.onFailure {
                    Toast.makeText(this@MangaListActivity, "Error loading manga", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
