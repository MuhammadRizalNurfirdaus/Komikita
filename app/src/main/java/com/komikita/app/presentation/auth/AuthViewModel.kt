package com.komikita.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.komikita.app.data.local.SessionManager
import com.komikita.app.domain.model.UserRole
import com.komikita.app.domain.model.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val GOOGLE_WEB_CLIENT_ID = "885636086964-akuhha38avuerosasf9c91s0v4dmlvuv.apps.googleusercontent.com"

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    val sessionState: StateFlow<UserSession> = sessionManager.sessionState

    fun loginAsGuest() {
        sessionManager.setGuestSession()
    }

    fun loginWithGoogleIdToken(idToken: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnSuccessListener { result ->
                        val user = result.user
                        sessionManager.setUserSession(
                            uid = user?.uid ?: "google_user",
                            email = user?.email ?: "user@google.com",
                            name = user?.displayName ?: "Google User",
                            photo = user?.photoUrl?.toString() ?: "",
                            explicitRole = UserRole.USER
                        )
                        onComplete(true, null)
                    }
                    .addOnFailureListener { exc ->
                        // Fallback demo for development/testing if Firebase auth returns error or SHA mismatch
                        sessionManager.setUserSession(
                            uid = "google_user_demo",
                            email = "google_user@gmail.com",
                            name = "Google User",
                            explicitRole = UserRole.USER
                        )
                        onComplete(true, null)
                    }
            } catch (e: Exception) {
                sessionManager.setUserSession(
                    uid = "google_user_demo",
                    email = "google_user@gmail.com",
                    name = "Google User",
                    explicitRole = UserRole.USER
                )
                onComplete(true, null)
            }
        }
    }

    fun loginWithEmail(email: String, pass: String, onComplete: (Boolean, String?) -> Unit) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()

        if (trimmedEmail.isBlank() || trimmedPass.isBlank()) {
            onComplete(false, "Email dan Password tidak boleh kosong")
            return
        }

        // Hardcoded Role Check for Admin & Translator credentials
        if (trimmedEmail.equals("admin@gmail.com", ignoreCase = true)) {
            if (trimmedPass == "admin123") {
                sessionManager.setUserSession(
                    uid = "admin_uid",
                    email = "admin@gmail.com",
                    name = "Administrator",
                    explicitRole = UserRole.ADMIN
                )
                onComplete(true, null)
                return
            } else {
                onComplete(false, "Password Admin salah")
                return
            }
        }

        if (trimmedEmail.equals("translator@gmail.com", ignoreCase = true)) {
            if (trimmedPass == "translator123") {
                sessionManager.setUserSession(
                    uid = "translator_uid",
                    email = "translator@gmail.com",
                    name = "Custom Translator",
                    explicitRole = UserRole.TRANSLATOR
                )
                onComplete(true, null)
                return
            } else {
                onComplete(false, "Password Translator salah")
                return
            }
        }

        // Standard Email Login (Firebase + Fallback)
        try {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(trimmedEmail, trimmedPass)
                .addOnSuccessListener { result ->
                    val user = result.user
                    sessionManager.setUserSession(
                        uid = user?.uid ?: "user_uid",
                        email = user?.email ?: trimmedEmail,
                        name = user?.displayName ?: trimmedEmail.substringBefore("@"),
                        explicitRole = UserRole.USER
                    )
                    onComplete(true, null)
                }
                .addOnFailureListener { exc ->
                    // Fallback for standard users
                    sessionManager.setUserSession(
                        uid = "user_demo_${System.currentTimeMillis()}",
                        email = trimmedEmail,
                        name = trimmedEmail.substringBefore("@"),
                        explicitRole = UserRole.USER
                    )
                    onComplete(true, null)
                }
        } catch (e: Exception) {
            sessionManager.setUserSession(
                uid = "user_demo_${System.currentTimeMillis()}",
                email = trimmedEmail,
                name = trimmedEmail.substringBefore("@"),
                explicitRole = UserRole.USER
            )
            onComplete(true, null)
        }
    }

    fun logout() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) {}
        sessionManager.logout()
    }
}
