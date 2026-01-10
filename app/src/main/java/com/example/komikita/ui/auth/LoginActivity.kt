package com.example.komikita.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.komikita.R
import com.example.komikita.data.local.AppDatabase
import com.example.komikita.databinding.ActivityLoginBinding
import com.example.komikita.ui.dashboard.DashboardActivity
import com.example.komikita.ui.debug.SetupInfoActivity
import com.example.komikita.util.PasswordUtils
import com.example.komikita.util.SessionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sessionManager: SessionManager
    private val RC_SIGN_IN = 9001
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        
        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToDashboard()
            return
        }
        
        configureGoogleSignIn()
        setupListeners()
    }
    
    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }
    
    private fun setupListeners() {
        // Login with email/password
        binding.btnLogin.setOnClickListener {
            loginWithEmailPassword()
        }
        
        // Register link
        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java).apply {
                putExtra("FROM_LOGIN", true)
            })
        }
        
        // Google Sign In
        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }
        
        // Skip login (Guest mode)
        binding.btnSkipLogin.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
        
        // Long press on logo to view setup info (for debugging)
        binding.ivLogo.setOnLongClickListener {
            startActivity(Intent(this, SetupInfoActivity::class.java))
            true
        }
    }
    
    private fun loginWithEmailPassword() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        
        // Validate
        if (email.isBlank()) {
            binding.tilEmail.error = "Email tidak boleh kosong"
            return
        }
        binding.tilEmail.error = null
        
        if (password.isBlank()) {
            binding.tilPassword.error = "Password tidak boleh kosong"
            return
        }
        binding.tilPassword.error = null
        
        // Disable button during login
        binding.btnLogin.isEnabled = false
        
        lifecycleScope.launch(Dispatchers.IO) {
            val userDao = AppDatabase.getDatabase(this@LoginActivity).userDao()
            val passwordHash = PasswordUtils.hashPassword(password)
            val user = userDao.getUserByEmailAndPassword(email, passwordHash)
            
            withContext(Dispatchers.Main) {
                binding.btnLogin.isEnabled = true
                
                if (user != null) {
                    // Login successful
                    sessionManager.saveSession(
                        userId = user.userId,
                        email = user.email,
                        name = user.displayName,
                        photoUrl = user.photoUrl,
                        loginType = "local"
                    )
                    Toast.makeText(this@LoginActivity, "Login berhasil! Selamat datang, ${user.displayName} 🎉", Toast.LENGTH_SHORT).show()
                    navigateToDashboard()
                } else {
                    Toast.makeText(this@LoginActivity, "Email atau password salah", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }
    
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            
            if (account == null) {
                Toast.makeText(this, "Sign in failed: No account returned", Toast.LENGTH_LONG).show()
                return
            }
            
            if (account.email.isNullOrEmpty()) {
                Toast.makeText(this, "Sign in failed: No email found", Toast.LENGTH_LONG).show()
                return
            }
            
            // Check if user exists in database
            lifecycleScope.launch(Dispatchers.IO) {
                val userDao = AppDatabase.getDatabase(this@LoginActivity).userDao()
                val existingUser = userDao.getUserByEmail(account.email!!)
                
                withContext(Dispatchers.Main) {
                    if (existingUser != null) {
                        // User already registered, login directly
                        sessionManager.saveSession(
                            userId = existingUser.userId,
                            email = existingUser.email,
                            name = existingUser.displayName,
                            photoUrl = existingUser.photoUrl,
                            loginType = "google"
                        )
                        Toast.makeText(this@LoginActivity, "Welcome back, ${existingUser.displayName}! 🎉", Toast.LENGTH_SHORT).show()
                        navigateToDashboard()
                    } else {
                        // New user, go to register
                        Toast.makeText(this@LoginActivity, "Sign in successful! Please complete registration.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, RegisterActivity::class.java).apply {
                            putExtra("EMAIL", account.email)
                            putExtra("NAME", account.displayName ?: account.email?.substringBefore("@"))
                            putExtra("PHOTO_URL", account.photoUrl?.toString())
                            putExtra("IS_GOOGLE", true)
                        }
                        startActivity(intent)
                        finish()
                    }
                }
            }
            
        } catch (e: ApiException) {
            val errorMessage = when (e.statusCode) {
                12501 -> "Sign in cancelled by user"
                12500 -> """
                    Google Sign-In failed!
                    
                    Error Code: 12500
                    
                    Possible causes:
                    1. SHA-1 fingerprint not registered in Google Console
                    2. Package name mismatch
                    3. OAuth Client ID not configured
                    
                    Please check GOOGLE_SIGNIN_SETUP.md for instructions.
                """.trimIndent()
                10 -> """
                    Developer Error (Code 10)
                    
                    google-services.json might be invalid or missing.
                    
                    Please download correct google-services.json from:
                    https://console.firebase.google.com/
                """.trimIndent()
                else -> """
                    Sign in error!
                    Code: ${e.statusCode}
                    Message: ${e.message}
                    
                    Check GOOGLE_SIGNIN_SETUP.md for setup instructions.
                """.trimIndent()
            }
            
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            android.util.Log.e("LoginActivity", "Google Sign-In Error: Code=${e.statusCode}, Message=${e.message}")
        } catch (e: Exception) {
            val errorMsg = "Unexpected error: ${e.message}\n${e.javaClass.simpleName}"
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            android.util.Log.e("LoginActivity", "Unexpected error during sign in", e)
        }
    }
    
    private fun navigateToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}
