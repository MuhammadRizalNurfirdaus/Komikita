package com.example.komikita.ui.base

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureSystemBars()
    }
    
    override fun onResume() {
        super.onResume()
        configureSystemBars()
    }

    protected fun configureSystemBars() {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        val isDarkMode = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES

        if (isDarkMode) {
            // DARK MODE: Black Status Bar, White Icons
            window.statusBarColor = Color.BLACK
            window.navigationBarColor = Color.BLACK
            
            insetsController.isAppearanceLightStatusBars = false // White content
            insetsController.isAppearanceLightNavigationBars = false // White content
        } else {
            // LIGHT MODE: White Status Bar, Black Icons
            window.statusBarColor = Color.WHITE
            window.navigationBarColor = Color.WHITE
            
            insetsController.isAppearanceLightStatusBars = true // Black content
            insetsController.isAppearanceLightNavigationBars = true // Black content
        }
        
        // Ensure flags allow drawing
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }
}
