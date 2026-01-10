package com.example.komikita.ui.manhwa

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.komikita.databinding.ActivityDashboardBinding
import com.example.komikita.data.repository.KomikRepository
import com.example.komikita.ui.adapter.KomikListAdapter
import com.example.komikita.ui.detail.KomikDetailActivity

class ManhwaListActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var repository: KomikRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.toolbar.title = "Manhwa"
        repository = KomikRepository()
    }
}
