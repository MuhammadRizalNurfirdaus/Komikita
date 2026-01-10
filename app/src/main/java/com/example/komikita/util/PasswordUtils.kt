package com.example.komikita.util

import java.security.MessageDigest

object PasswordUtils {
    
    /**
     * Hash password using SHA-256
     */
    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Verify password by comparing hashes
     */
    fun verifyPassword(password: String, hash: String): Boolean {
        return hashPassword(password) == hash
    }
    
    /**
     * Validate password strength
     * - Minimum 6 characters
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
}
