package com.example.komikita.ui.profile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.komikita.databinding.ActivityProfileBinding
import com.example.komikita.data.local.AppDatabase
import com.example.komikita.ui.auth.LoginActivity
import com.example.komikita.util.SessionManager
import com.example.komikita.R 
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityProfileBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sessionManager: SessionManager
    private lateinit var googleSignInClient: GoogleSignInClient
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        sessionManager = SessionManager(this)
        configureGoogleSignIn()
        
        setupToolbar()
        
        loadUserData()
        setupDarkMode()
        setupListeners()
        setupBottomNavigation()
    }
    
    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_profile
        loadUserData()
    }
    
    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }
    
    private fun loadUserData() {
        if (sessionManager.isLoggedIn()) {
            // Load from session
            binding.tvDisplayName.text = sessionManager.getUserName() ?: "User"
            binding.tvEmail.text = sessionManager.getUserEmail() ?: ""
            
            sessionManager.getUserPhoto()?.let { photoUrl ->
                Glide.with(this)
                    .load(photoUrl)
                    .into(binding.ivProfilePhoto)
            }
            
            // Show logout button for logged in users
            binding.btnLogout.visibility = View.VISIBLE
            binding.btnEditProfile.visibility = View.VISIBLE
            
            // Show login type
            val loginType = sessionManager.getLoginType()
            binding.tvLoginType.visibility = View.VISIBLE
            binding.tvLoginType.text = when (loginType) {
                "google" -> "🔗 Terhubung dengan Google"
                "local" -> "📧 Login dengan Email"
                else -> ""
            }
        } else {
            // Guest user
            binding.tvDisplayName.text = "Guest User"
            binding.tvEmail.text = "Tidak login"
            binding.btnEditProfile.visibility = View.GONE
            binding.tvLoginType.visibility = View.GONE
            
            // Show login prompt
            binding.btnLogout.visibility = View.VISIBLE
            binding.btnLogout.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
    
    private fun setupDarkMode() {
        val sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        
        if (!sharedPreferences.contains("dark_mode")) {
            // First time launch or preference not set, check system
            val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
            val isSystemDark = currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
            
            // Save system preference as default
            sharedPreferences.edit().putBoolean("dark_mode", isSystemDark).apply()
            
            // Apply it
            val mode = if (isSystemDark) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            AppCompatDelegate.setDefaultNightMode(mode)
            binding.switchDarkMode.isChecked = isSystemDark
        } else {
            // Use existing preference
            val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
            binding.switchDarkMode.isChecked = isDarkMode
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun setDarkMode(isDarkMode: Boolean) {
        val sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("dark_mode", isDarkMode).apply()
        
        val mode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        
        AppCompatDelegate.setDefaultNightMode(mode)
        
        // Recreate to apply theme needs to be handled carefully or just let the delegate handle it
        // Usually setDefaultNightMode causes recreation automatically for visible activities
    }
    
    private fun setupListeners() {
        // Back listener removed
        
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, com.example.komikita.ui.profile.EditProfileActivity::class.java))
        }
        
        if (sessionManager.isLoggedIn()) {
            binding.btnLogout.setOnClickListener {
                logout()
            }
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            setDarkMode(isChecked)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_profile

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this@ProfileActivity, com.example.komikita.ui.dashboard.DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_search -> {
                    val intent = Intent(this@ProfileActivity, com.example.komikita.ui.search.SearchActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_favorites -> {
                    val intent = Intent(this@ProfileActivity, com.example.komikita.ui.favorites.FavoritesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_downloads -> {
                    val intent = Intent(this@ProfileActivity, com.example.komikita.ui.downloads.DownloadsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }
    
    private fun logout() { // Renamed from performLogout
        val loginType = sessionManager.getLoginType()
        
        // Sign out from Google if it was Google login
        if (loginType == "google") {
            googleSignInClient.signOut()
        }
        
        // Clear session
        sessionManager.logout()
        
        // Clear local database
        lifecycleScope.launch(Dispatchers.IO) {
            val userDao = AppDatabase.getDatabase(this@ProfileActivity).userDao()
            userDao.deleteAllUsers()
            
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ProfileActivity, "Logout berhasil", Toast.LENGTH_SHORT).show()
                
                // Navigate to login
                val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}
