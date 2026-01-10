package com.example.komikita.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.komikita.R
import com.example.komikita.ui.auth.LoginActivity
import com.example.komikita.ui.dashboard.DashboardActivity
import com.example.komikita.util.SessionManager

class SplashActivity : AppCompatActivity() {
    
    private val splashDelay = 2500L // 2.5 seconds
    private lateinit var sessionManager: SessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        sessionManager = SessionManager(this)
        
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, splashDelay)
    }
    
    private fun checkLoginStatus() {
        if (sessionManager.isLoggedIn()) {
            // User is logged in, go directly to dashboard
            startActivity(Intent(this, DashboardActivity::class.java))
        } else {
            // User not logged in, go to login
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
