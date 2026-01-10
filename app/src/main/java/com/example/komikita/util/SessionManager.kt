package com.example.komikita.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "komikita_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHOTO = "user_photo"
        private const val KEY_LOGIN_TYPE = "login_type"
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    /**
     * Get current user ID
     */
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }
    
    /**
     * Get current user email
     */
    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }
    
    /**
     * Get current user name
     */
    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }
    
    /**
     * Get user photo URL
     */
    fun getUserPhoto(): String? {
        return prefs.getString(KEY_USER_PHOTO, null)
    }
    
    /**
     * Get login type (google/local)
     */
    fun getLoginType(): String? {
        return prefs.getString(KEY_LOGIN_TYPE, null)
    }
    
    /**
     * Save user session after login
     */
    fun saveSession(
        userId: String,
        email: String,
        name: String?,
        photoUrl: String?,
        loginType: String
    ) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_PHOTO, photoUrl)
            putString(KEY_LOGIN_TYPE, loginType)
            apply()
        }
    }
    
    /**
     * Clear session on logout
     */
    fun logout() {
        prefs.edit().clear().apply()
    }
    
    /**
     * Check if user is guest (not logged in but using app)
     */
    fun isGuest(): Boolean {
        return !isLoggedIn()
    }
}
