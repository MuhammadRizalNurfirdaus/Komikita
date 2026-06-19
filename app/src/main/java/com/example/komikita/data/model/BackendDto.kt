package com.example.komikita.data.model

import com.google.gson.annotations.SerializedName

// ============================================================
// DTO untuk Backend API (PostgreSQL via REST API)
// Backend ini adalah perantara antara Android dan PostgreSQL Aiven.
// JANGAN PERNAH koneksi langsung dari Android ke PostgreSQL!
// ============================================================

// --- Auth ---

/**
 * Request body untuk login/register ke backend.
 * Backend akan memverifikasi Firebase ID Token, lalu mengembalikan JWT.
 */
data class AuthRequest(
    @SerializedName("firebase_uid") val firebaseUid: String,
    @SerializedName("email") val email: String,
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("photo_url") val photoUrl: String?,
    @SerializedName("firebase_token") val firebaseToken: String  // ID Token dari Firebase
)

/**
 * Response dari endpoint login/register backend.
 */
data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("token") val token: String?,           // JWT token
    @SerializedName("user") val user: BackendUserDto?,
    @SerializedName("message") val message: String?
)

// --- User ---

data class BackendUserDto(
    @SerializedName("id") val id: String?,
    @SerializedName("firebase_uid") val firebaseUid: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("photo_url") val photoUrl: String?,
    @SerializedName("role") val role: String?,             // "admin", "translator", "user"
    @SerializedName("created_at") val createdAt: String?
)

// --- Custom Comics (dari PostgreSQL) ---

data class CustomComicDto(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("slug") val slug: String?,
    @SerializedName("cover_url") val coverUrl: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("is_hidden") val isHidden: Boolean?,
    @SerializedName("author_uid") val authorUid: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("chapters") val chapters: List<CustomChapterDto>?
)

data class CustomChapterDto(
    @SerializedName("id") val id: String?,
    @SerializedName("comic_id") val comicId: String?,
    @SerializedName("chapter_number") val chapterNumber: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("pages") val pages: List<CustomPageDto>?,
    @SerializedName("created_at") val createdAt: String?
)

data class CustomPageDto(
    @SerializedName("id") val id: String?,
    @SerializedName("chapter_id") val chapterId: String?,
    @SerializedName("page_number") val pageNumber: Int?,
    @SerializedName("image_url") val imageUrl: String?
)

// --- Request bodies untuk Translator upload ---

/**
 * Request body untuk upload komik baru oleh Translator.
 */
data class UploadComicRequest(
    @SerializedName("title") val title: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("cover_url") val coverUrl: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("image_urls") val imageUrls: List<String>   // Bulk URL gambar
)

/**
 * Request body untuk menambah chapter ke komik yang sudah ada.
 */
data class AddChapterRequest(
    @SerializedName("chapter_number") val chapterNumber: String,
    @SerializedName("title") val title: String?,
    @SerializedName("image_urls") val imageUrls: List<String>
)

/**
 * Request untuk hide/unhide komik scraper.
 */
data class HideComicRequest(
    @SerializedName("slug") val slug: String,
    @SerializedName("hide") val hide: Boolean
)

// --- Generic Backend Response ---

data class BackendResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: T?,
    @SerializedName("message") val message: String?
)

// --- List response wrapper ---

data class CustomComicListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<CustomComicDto>?,
    @SerializedName("message") val message: String?
)

data class HiddenSlugsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<String>?,
    @SerializedName("message") val message: String?
)
