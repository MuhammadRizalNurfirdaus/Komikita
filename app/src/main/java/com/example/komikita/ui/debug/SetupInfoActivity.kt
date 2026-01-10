package com.example.komikita.ui.debug

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.komikita.databinding.ActivitySetupInfoBinding

class SetupInfoActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySetupInfoBinding
    
    private val packageName = "com.example.komikita"
    private val sha1 = "89:D7:9E:42:B8:B9:40:57:58:37:5A:34:B0:D3:72:CD:31:97:B0:49"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Google Sign-In Setup"
        }
        
        setupContent()
        setupListeners()
    }
    
    private fun setupContent() {
        binding.tvPackageName.text = packageName
        binding.tvSha1.text = sha1
        
        binding.tvInstructions.text = """
            To enable Google Sign-In:
            
            1. Go to Google Cloud Console:
               https://console.cloud.google.com/
            
            2. Create or select a project
            
            3. Enable Google Sign-In API
            
            4. Create OAuth 2.0 Client ID (Android):
               - Package name: $packageName
               - SHA-1: $sha1
            
            5. OR use Firebase Console:
               https://console.firebase.google.com/
               - Add Android app with package name above
               - Add SHA-1 fingerprint above
               - Enable Authentication > Google
               - Download google-services.json
               - Replace in app/ folder
            
            6. Rebuild app and try again!
        """.trimIndent()
    }
    
    private fun setupListeners() {
        binding.btnCopyPackage.setOnClickListener {
            copyToClipboard("Package Name", packageName)
        }
        
        binding.btnCopySha1.setOnClickListener {
            copyToClipboard("SHA-1", sha1)
        }
        
        binding.btnOpenGuide.setOnClickListener {
            Toast.makeText(this, "Check GOOGLE_SIGNIN_SETUP.md in project root", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun copyToClipboard(label: String, text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
