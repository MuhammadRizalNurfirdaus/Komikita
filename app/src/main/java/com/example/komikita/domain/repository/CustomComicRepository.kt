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
