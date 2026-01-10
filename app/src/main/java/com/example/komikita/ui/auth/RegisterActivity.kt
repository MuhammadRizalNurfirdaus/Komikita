package com.example.komikita.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.komikita.databinding.ActivityRegisterBinding
import com.example.komikita.data.local.AppDatabase
import com.example.komikita.data.local.entity.UserEntity
import com.example.komikita.ui.dashboard.DashboardActivity
import com.example.komikita.util.PasswordUtils
import com.example.komikita.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sessionManager: SessionManager
    private var photoUri: Uri? = null
    private val PICK_IMAGE = 1001
    private var isGoogleSignUp = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        
        loadIntentData()
        setupListeners()
    }
    
    private fun loadIntentData() {
        val email = intent.getStringExtra("EMAIL")
        val name = intent.getStringExtra("NAME")
        val photoUrl = intent.getStringExtra("PHOTO_URL")
        isGoogleSignUp = intent.getBooleanExtra("IS_GOOGLE", false)
        val fromLogin = intent.getBooleanExtra("FROM_LOGIN", false)
        
        // Pre-fill fields if coming from Google Sign-In
        if (!email.isNullOrEmpty()) {
            binding.etEmail.setText(email)
            if (isGoogleSignUp) {
                binding.etEmail.isEnabled = false // Disable email editing for Google
            }
        }
        
        if (!name.isNullOrEmpty()) {
            binding.etDisplayName.setText(name)
        }
        
        // Handle Google Sign-Up vs Local registration
        if (isGoogleSignUp) {
            binding.tvTitle.text = "Lengkapi Profil"
            binding.tilPassword.visibility = View.GONE
            binding.tilConfirmPassword.visibility = View.GONE
            binding.tvEmailInfo.visibility = View.VISIBLE
            binding.btnComplete.text = "Selesaikan Registrasi"
        } else {
            binding.tvTitle.text = "Daftar Akun Baru"
            binding.tilPassword.visibility = View.VISIBLE
            binding.tilConfirmPassword.visibility = View.VISIBLE
            binding.tvEmailInfo.visibility = View.GONE
            binding.btnComplete.text = "Daftar Sekarang"
        }
        
        // Load Google profile photo
        photoUrl?.let {
            Glide.with(this)
                .load(it)
                .into(binding.ivProfilePhoto)
            photoUri = Uri.parse(it)
        }
    }
    
    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.btnSelectPhoto.setOnClickListener {
            selectPhoto()
        }
        
        binding.btnComplete.setOnClickListener {
            completeRegistration()
        }
        
        binding.tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
    
    private fun selectPhoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            photoUri = data?.data
            photoUri?.let {
                Glide.with(this)
                    .load(it)
                    .into(binding.ivProfilePhoto)
            }
        }
    }
    
    private fun completeRegistration() {
        val email = binding.etEmail.text.toString().trim()
        val displayName = binding.etDisplayName.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        
        // Validate form
        if (displayName.isBlank()) {
            binding.tilDisplayName.error = "Nama tidak boleh kosong"
            return
        }
        if (displayName.length < 3) {
            binding.tilDisplayName.error = "Nama minimal 3 karakter"
            return
        }
        binding.tilDisplayName.error = null
        
        if (email.isBlank()) {
            binding.tilEmail.error = "Email tidak boleh kosong"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Format email tidak valid"
            return
        }
        binding.tilEmail.error = null
        
        // Password validation only for local registration
        if (!isGoogleSignUp) {
            if (password.isBlank()) {
                binding.tilPassword.error = "Password tidak boleh kosong"
                return
            }
            if (!PasswordUtils.isValidPassword(password)) {
                binding.tilPassword.error = "Password minimal 6 karakter"
                return
            }
            binding.tilPassword.error = null
            
            if (confirmPassword != password) {
                binding.tilConfirmPassword.error = "Password tidak cocok"
                return
            }
            binding.tilConfirmPassword.error = null
        }
        
        // Disable button to prevent double submission
        binding.btnComplete.isEnabled = false
        
        // Check if email already exists and save user
        CoroutineScope(Dispatchers.IO).launch {
            val userDao = AppDatabase.getDatabase(this@RegisterActivity).userDao()
            
            // Check existing email
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                withContext(Dispatchers.Main) {
                    binding.btnComplete.isEnabled = true
                    binding.tilEmail.error = "Email sudah terdaftar. Silakan login."
                }
                return@launch
            }
            
            // Create new user
            val user = UserEntity(
                userId = email,
                email = email,
                displayName = displayName,
                photoUrl = photoUri?.toString(),
                isEmailVerified = isGoogleSignUp, // True for Google, false for local
                passwordHash = if (isGoogleSignUp) null else PasswordUtils.hashPassword(password),
                loginType = if (isGoogleSignUp) "google" else "local"
            )
            
            userDao.insertUser(user)
            
            withContext(Dispatchers.Main) {
                // Save session
                sessionManager.saveSession(
                    userId = user.userId,
                    email = user.email,
                    name = user.displayName,
                    photoUrl = user.photoUrl,
                    loginType = user.loginType
                )
                
                Toast.makeText(
                    this@RegisterActivity,
                    "Registrasi berhasil! Selamat datang, $displayName 🎉",
                    Toast.LENGTH_SHORT
                ).show()
                
                // Navigate to Dashboard
                startActivity(Intent(this@RegisterActivity, DashboardActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
        }
    }
}
