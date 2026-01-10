package com.example.komikita.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.komikita.databinding.ActivityEditProfileBinding
import com.example.komikita.data.local.AppDatabase
import com.example.komikita.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var sessionManager: SessionManager
    private var selectedImageUri: Uri? = null
    private var currentPhotoPath: String? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Show preview
            Glide.with(this)
                .load(it)
                .into(binding.ivProfilePhoto)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        currentPhotoPath = sessionManager.getUserPhoto()

        setupToolbar()
        loadUserData()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadUserData() {
        binding.etDisplayName.setText(sessionManager.getUserName())
        binding.etEmail.setText(sessionManager.getUserEmail())
        
        currentPhotoPath?.let { photoUrl ->
            Glide.with(this)
                .load(photoUrl)
                .into(binding.ivProfilePhoto)
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            saveProfile()
        }
        
        // Allow clicking the photo or the camera icon or the container
        binding.flProfilePhoto.setOnClickListener { pickImage() }
        binding.ivProfilePhoto.setOnClickListener { pickImage() }
        binding.ivCameraIcon.setOnClickListener { pickImage() }
    }
    
    // Helper to find the camera icon view if not directly accessible, or just rely on wrapper click
    // However, looking at XML, the camera icon is inside a FrameLayout.
    // Let's check XML ID for camera icon. It was just an ImageView. 
    // I will assume I need to update XML to give it an ID if the previous XML didn't have one, 
    // BUT wait, I wrote the XML in step 1388. Let me check the XML content I wrote.
    // It was: <ImageView ... android:src="@android:drawable/ic_menu_camera" ... /> without ID? 
    // Ah, Step 1388 XML: 
    // <com.google.android.material.imageview.ShapeableImageView android:id="@+id/ivProfilePhoto" ... />
    // <ImageView ... android:src="@android:drawable/ic_menu_camera" ... /> 
    // The camera icon indeed has NO ID.
    // I can stick a OnClickListener on the parent FrameLayout? 
    // The FrameLayout matches wrap_content.
    
    private fun pickImage() {
        pickImageLauncher.launch("image/*")
    }

    private fun saveProfile() {
        val newName = binding.etDisplayName.text.toString().trim()
        
        if (newName.isEmpty()) {
            Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = sessionManager.getUserId() ?: return
        
        lifecycleScope.launch(Dispatchers.IO) {
            // Process image if selected
            var finalPhotoPath = currentPhotoPath
            
            selectedImageUri?.let { uri ->
                val savedPath = saveImageToInternalStorage(uri)
                if (savedPath != null) {
                    finalPhotoPath = savedPath
                }
            }
            
            val userDao = AppDatabase.getDatabase(this@EditProfileActivity).userDao()
            
            // Update local DB
            userDao.updateUserName(userId, newName)
            if (finalPhotoPath != null) {
                userDao.updateUserPhoto(userId, finalPhotoPath!!)
            }
            
            // Update session
            sessionManager.saveSession(
                userId,
                sessionManager.getUserEmail() ?: "",
                newName,
                finalPhotoPath,
                sessionManager.getLoginType() ?: "local"
            )

            withContext(Dispatchers.Main) {
                Toast.makeText(this@EditProfileActivity, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val fileName = "profile_${UUID.randomUUID()}.jpg"
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)
            
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
