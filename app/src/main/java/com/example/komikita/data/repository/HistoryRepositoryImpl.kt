package com.example.komikita.data.repository

import com.example.komikita.data.local.dao.HistoryDao
import com.example.komikita.data.local.entity.HistoryEntity
import com.example.komikita.domain.model.KomikSource
import com.example.komikita.domain.model.ReadHistory
import com.example.komikita.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementasi HistoryRepository.
 * Menyimpan riwayat baca di Room DB lokal.
 * Bisa disinkronkan ke backend secara berkala (future enhancement).
 */
@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao
) : HistoryRepository {

    override fun observeHistory(userId: String): Flow<List<ReadHistory>> {
        return historyDao.getHistoryByUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveHistory(history: ReadHistory) {
        // Cek apakah sudah ada history untuk komik yang sama
        val existing = historyDao.getHistoryBySlugAndUser(history.komikSlug, history.userId)
        if (existing != null) {
            // Update chapter terakhir dan waktu baca
            historyDao.insertHistory(
                existing.copy(
                    lastChapterId = history.lastChapterId,
                    lastChapterLabel = history.lastChapterLabel,
                    readAt = System.currentTimeMillis()
                )
            )
        } else {
            historyDao.insertHistory(history.toEntity())
        }
    }

    override suspend fun deleteHistory(historyId: Int) {
        historyDao.deleteHistory(historyId)
    }

    override suspend fun clearHistory(userId: String) {
        historyDao.clearHistory(userId)
    }

    // --- Mapper Entity <-> Domain ---

    private fun HistoryEntity.toDomain(): ReadHistory {
        return ReadHistory(
            id = id,
            komikSlug = komikSlug,
            komikTitle = komikTitle,
            komikPoster = komikPoster,
            lastChapterId = lastChapterId,
            lastChapterLabel = lastChapterLabel,
            readAt = readAt,
            userId = userId,
            source = if (source == "custom") KomikSource.CUSTOM else KomikSource.SCRAPER
        )
    }

    private fun ReadHistory.toEntity(): HistoryEntity {
        return HistoryEntity(
            id = id,
            komikSlug = komikSlug,
            komikTitle = komikTitle,
            komikPoster = komikPoster,
            lastChapterId = lastChapterId,
            lastChapterLabel = lastChapterLabel,
            userId = userId,
            source = if (source == KomikSource.CUSTOM) "custom" else "scraper",
            readAt = readAt
        )
    }
}
