package com.example.komikita.data.repository

import com.example.komikita.data.local.dao.FavoriteDao
import com.example.komikita.data.local.entity.FavoriteEntity
import com.example.komikita.domain.model.KomikSource
import com.example.komikita.domain.repository.FavoriteItem
import com.example.komikita.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementasi FavoriteRepository.
 * Menyimpan bookmark komik di Room DB lokal.
 * Data favorit bersifat per-user (berdasarkan userId).
 */
@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao
) : FavoriteRepository {

    override fun observeFavorites(userId: String): Flow<List<FavoriteItem>> {
        return favoriteDao.getFavoritesByUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun isFavorite(slug: String, userId: String): Boolean {
        return favoriteDao.getFavoriteBySlugAndUser(slug, userId) != null
    }

    override suspend fun addFavorite(item: FavoriteItem, userId: String) {
        favoriteDao.insertFavorite(
            FavoriteEntity(
                slug = item.slug,
                title = item.title,
                poster = item.poster,
                type = item.type,
                userId = userId,
                source = if (item.source == KomikSource.CUSTOM) "custom" else "scraper"
            )
        )
    }

    override suspend fun removeFavorite(slug: String, userId: String) {
        favoriteDao.deleteFavoriteBySlugAndUser(slug, userId)
    }

    override suspend fun toggleFavorite(item: FavoriteItem, userId: String): Boolean {
        val existing = favoriteDao.getFavoriteBySlugAndUser(item.slug, userId)
        return if (existing != null) {
            favoriteDao.deleteFavoriteBySlugAndUser(item.slug, userId)
            false // Sekarang tidak lagi favorit
        } else {
            addFavorite(item, userId)
            true // Sekarang jadi favorit
        }
    }

    // --- Mapper Entity -> Domain ---

    private fun FavoriteEntity.toDomain(): FavoriteItem {
        return FavoriteItem(
            slug = slug,
            title = title,
            poster = poster,
            type = type,
            source = if (source == "custom") KomikSource.CUSTOM else KomikSource.SCRAPER,
            addedAt = addedAt
        )
    }
}
