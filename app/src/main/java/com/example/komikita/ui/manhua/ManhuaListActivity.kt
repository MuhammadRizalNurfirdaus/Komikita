package com.example.komikita.ui.manhua

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.komikita.databinding.ActivityDashboardBinding
import com.example.komikita.data.repository.KomikRepository

class ManhuaListActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var repository: KomikRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.toolbar.title = "Manhua"
        repository = KomikRepository()
    }
}
