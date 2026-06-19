package com.example.komikita.domain.usecase

import com.example.komikita.domain.model.*
import com.example.komikita.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use Case: Ambil feed halaman Home (gabungan Scraper + Custom).
 * Dipanggil oleh HomeViewModel.
 */
class GetHomeFeedUseCase @Inject constructor(
    private val komikRepository: KomikRepository
) {
    operator fun invoke(): Flow<HomeFeedState> {
        return komikRepository.getHomeFeed()
    }
}

/**
 * Use Case: Ambil halaman chapter untuk dibaca di Reader Screen.
 * Otomatis memilih sumber (custom/scrapper) berdasarkan ketersediaan data.
 */
class GetChapterPagesUseCase @Inject constructor(
    private val komikRepository: KomikRepository
) {
    suspend operator fun invoke(chapterId: String): Result<ChapterPages> {
        return komikRepository.getHybridChapterPages(chapterId)
    }
}

/**
 * Use Case: Pencarian hybrid (Scraper + Custom secara paralel).
 */
class SearchKomikUseCase @Inject constructor(
    private val komikRepository: KomikRepository
) {
    suspend operator fun invoke(query: String, page: Int = 1): Result<List<Komik>> {
        return komikRepository.hybridSearch(query, page)
    }
}

/**
 * Use Case: Ambil detail komik (hybrid - custom diutamakan, fallback ke scraper).
 */
class GetKomikDetailUseCase @Inject constructor(
    private val komikRepository: KomikRepository
) {
    suspend operator fun invoke(slug: String): Result<KomikDetail> {
        return komikRepository.getHybridDetail(slug)
    }
}

/**
 * Use Case: Upload komik custom oleh Translator.
 * Hanya boleh dipanggil jika user memiliki role TRANSLATOR atau ADMIN.
 */
class UploadComicUseCase @Inject constructor(
    private val customComicRepository: CustomComicRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        title: String,
        slug: String,
        imageUrls: List<String>,
        coverUrl: String? = null,
        type: String? = null
    ): Result<CustomComic> {
        // Verifikasi role sebelum upload (keamanan di sisi klien)
        val user = userRepository.observeCurrentUser().first()

        if (user == null) {
            return Result.failure(Exception("User belum login"))
        }

        if (user.role != UserRole.TRANSLATOR && user.role != UserRole.ADMIN) {
            return Result.failure(SecurityException("Hanya Translator atau Admin yang bisa upload komik"))
        }

        return customComicRepository.uploadComic(title, slug, imageUrls, coverUrl, type)
    }
}

/**
 * Use Case: Ambil role user saat ini.
 * Digunakan untuk menentukan UI yang ditampilkan ( Translator dashboard, admin panel, dll).
 */
class GetUserRoleUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<UserRole> {
        val token = userRepository.getAuthToken()
            ?: return Result.failure(Exception("Token tidak ditemukan, silakan login ulang"))

        return userRepository.getUserRole("") // UID diambil dari token di backend
    }
}

/**
 * Use Case: Simpan riwayat baca ke lokal.
 */
class SaveHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(history: ReadHistory) {
        historyRepository.saveHistory(history)
    }
}

/**
 * Use Case: Observasi riwayat baca user.
 */
class GetHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    operator fun invoke(userId: String): Flow<List<ReadHistory>> {
        return historyRepository.observeHistory(userId)
    }
}
