package com.example.komikita.domain.model

/**
 * Domain model untuk User (dari Firebase Auth + PostgreSQL).
 */
data class User(
    val uid: String,                  // Firebase UID
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val role: UserRole,               // Role dari PostgreSQL
    val isEmailVerified: Boolean
)

/**
 * Enum role user dalam sistem 3 aktor.
 * - ADMIN: Mengelola user dan sistem
 * - TRANSLATOR: Content manager, upload komik custom
 * - USER: Pembaca biasa
 */
enum class UserRole {
    ADMIN,
    TRANSLATOR,
    USER;

    companion object {
        fun fromString(value: String): UserRole {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: USER
        }
    }
}
