package com.example.komikita.domain.repository

import com.example.komikita.domain.model.CustomComic
import com.example.komikita.domain.model.CustomChapter
import com.example.komikita.domain.model.ReadHistory
import kotlinx.coroutines.flow.Flow

/**
 * Interface Repository untuk fitur Translator (upload komik custom)
 * dan manajemen History baca.
 */
interface CustomComicRepository {

    /** Upload komik baru ke backend (Translator only) */
    suspend fun uploadComic(title: String, slug: String, imageUrls: List<String>, coverUrl: String?, type: String?): Result<CustomComic>

    /** Tambah chapter ke komik yang sudah ada */
    suspend fun addChapter(comicSlug: String, chapterNumber: String, imageUrls: List<String>): Result<CustomChapter>

    /** Hide/unhide komik scraper (Translator bisa hide konten yang tidak sesuai) */
    suspend fun hideScraperComic(slug: String, hide: Boolean): Result<Boolean>

    /** Ambil daftar slug komik yang di-hide */
    suspend fun getHiddenSlugs(): Result<List<String>>

    /** Hapus komik custom (admin/translator) */
    suspend fun deleteComic(comicId: String): Result<Boolean>
}

/**
 * Interface Repository untuk riwayat baca (History).
 */
interface HistoryRepository {

    /** Observasi riwayat baca user tertentu */
    fun observeHistory(userId: String): Flow<List<ReadHistory>>

    /** Simpan/update riwayat baca */
    suspend fun saveHistory(history: ReadHistory)

    /** Hapus riwayat baca tertentu */
    suspend fun deleteHistory(historyId: Int)

    /** Hapus semua riwayat user */
    suspend fun clearHistory(userId: String)
}

/**
 * Domain model untuk item Favorit (bookmark komik).
 * Disimpan di Room DB lokal untuk akses cepat.
 */
data class FavoriteItem(
    val slug: String,
    val title: String,
    val poster: String?,
    val type: String?,
    val source: com.example.komikita.domain.model.KomikSource,
    val addedAt: Long
)

/**
 * Interface Repository untuk manajemen Favorit (bookmark komik).
 * Data favorit disimpan di Room DB lokal.
 */
interface FavoriteRepository {

    /** Observasi semua favorit user tertentu */
    fun observeFavorites(userId: String): Flow<List<FavoriteItem>>

    /** Cek apakah komik tertentu ada di favorit */
    suspend fun isFavorite(slug: String, userId: String): Boolean

    /** Tambahkan komik ke favorit */
    suspend fun addFavorite(item: FavoriteItem, userId: String)

    /** Hapus komik dari favorit */
    suspend fun removeFavorite(slug: String, userId: String)

    /** Toggle favorit (tambah jika belum, hapus jika sudah) */
    suspend fun toggleFavorite(item: FavoriteItem, userId: String): Boolean
}
