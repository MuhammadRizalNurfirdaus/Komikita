package com.komikita.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.komikita.app.domain.model.UserRole
import com.komikita.app.domain.model.UserSession
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("komikita_prefs", Context.MODE_PRIVATE)

    private val _sessionState = MutableStateFlow(getCurrentSession())
    val sessionState: StateFlow<UserSession> = _sessionState.asStateFlow()

    fun getCurrentSession(): UserSession {
        val isLoggedIn = prefs.getBoolean("KEY_IS_LOGGED_IN", false)
        val isGuest = prefs.getBoolean("KEY_IS_GUEST", !isLoggedIn)
        val uid = prefs.getString("KEY_UID", "") ?: ""
        val email = prefs.getString("KEY_EMAIL", "") ?: ""
        val name = prefs.getString("KEY_NAME", "") ?: ""
        val photo = prefs.getString("KEY_PHOTO", "") ?: ""
        val roleStr = prefs.getString("KEY_ROLE", null)

        val role = when {
            roleStr != null -> runCatching { UserRole.valueOf(roleStr) }.getOrDefault(determineRole(email, isGuest))
            else -> determineRole(email, isGuest)
        }

        return UserSession(
            uid = uid,
            email = email,
            displayName = name,
            photoUrl = photo,
            role = role,
            isLoggedIn = isLoggedIn,
            isGuest = (role == UserRole.GUEST)
        )
    }

    private fun determineRole(email: String, isGuest: Boolean): UserRole {
        if (isGuest) return UserRole.GUEST
        return when (email.lowercase().trim()) {
            "admin@gmail.com" -> UserRole.ADMIN
            "translator@gmail.com" -> UserRole.TRANSLATOR
            else -> UserRole.USER
        }
    }

    fun setGuestSession() {
        prefs.edit()
            .putBoolean("KEY_IS_LOGGED_IN", true)
            .putBoolean("KEY_IS_GUEST", true)
            .putString("KEY_ROLE", UserRole.GUEST.name)
            .putString("KEY_UID", "guest_user")
            .putString("KEY_EMAIL", "")
            .putString("KEY_NAME", "Tamu (Guest)")
            .putString("KEY_PHOTO", "")
            .apply()
        _sessionState.value = getCurrentSession()
    }

    fun setUserSession(uid: String, email: String, name: String, photo: String = "", explicitRole: UserRole? = null) {
        val role = explicitRole ?: determineRole(email, false)
        prefs.edit()
            .putBoolean("KEY_IS_LOGGED_IN", true)
            .putBoolean("KEY_IS_GUEST", role == UserRole.GUEST)
            .putString("KEY_ROLE", role.name)
            .putString("KEY_UID", uid)
            .putString("KEY_EMAIL", email)
            .putString("KEY_NAME", name)
            .putString("KEY_PHOTO", photo)
            .apply()
        _sessionState.value = getCurrentSession()
    }

    fun logout() {
        prefs.edit().clear().apply()
        _sessionState.value = UserSession(isLoggedIn = false, role = UserRole.GUEST, isGuest = true)
    }
}
