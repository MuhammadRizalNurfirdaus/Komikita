package com.komikita.app.domain.model

data class UserSession(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val role: UserRole = UserRole.GUEST,
    val isLoggedIn: Boolean = false,
    val isGuest: Boolean = (role == UserRole.GUEST)
)
