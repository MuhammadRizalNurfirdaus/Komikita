package com.example.komikita.data.local.dao

import androidx.room.*
import com.example.komikita.data.local.entity.*
import kotlinx.coroutines.flow.Flow

// ============================================================
// DAO (Data Access Object) - Interface untuk akses Room Database.
// ============================================================

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUserSync(): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE users SET displayName = :name WHERE userId = :userId")
    suspend fun updateUserName(userId: String, name: String)

    @Query("UPDATE users SET photoUrl = :photoUrl WHERE userId = :userId")
    suspend fun updateUserPhoto(userId: String, photoUrl: String)

    @Query("UPDATE users SET role = :role WHERE userId = :userId")
    suspend fun updateUserRole(userId: String, role: String)

    @Query("UPDATE users SET authToken = :token WHERE userId = :userId")
    suspend fun updateAuthToken(userId: String, token: String)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE userId = :userId ORDER BY addedAt DESC")
    fun getFavoritesByUser(userId: String): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE slug = :slug")
    suspend fun getFavoriteBySlug(slug: String): FavoriteEntity?

    @Query("SELECT * FROM favorites WHERE slug = :slug AND userId = :userId")
    suspend fun getFavoriteBySlugAndUser(slug: String, userId: String): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE slug = :slug AND userId = :userId")
    suspend fun deleteFavoriteBySlugAndUser(slug: String, userId: String)
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY downloadedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE userId = :userId ORDER BY downloadedAt DESC")
    fun getDownloadsByUser(userId: String): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE komikSlug = :slug")
    fun getDownloadsByKomik(slug: String): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE userId = :userId AND komikSlug = :slug ORDER BY chapterTitle ASC")
    fun getDownloadsByKomik(userId: String, slug: String): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE userId = :userId AND komikSlug = :slug ORDER BY chapterTitle ASC")
    suspend fun getDownloadsByKomikSync(userId: String, slug: String): List<DownloadEntity>

    @Query("SELECT * FROM downloads WHERE chapterId = :chapterId AND userId = :userId LIMIT 1")
    suspend fun getDownloadByChapterAndUser(chapterId: String, userId: String): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Delete
    suspend fun deleteDownload(download: DownloadEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownloadById(id: Int)

    @Query("UPDATE downloads SET status = :status WHERE id = :id")
    suspend fun updateDownloadStatus(id: Int, status: String)
}

/**
 * DAO untuk tabel History (riwayat baca).
 */
@Dao
interface HistoryDao {
    @Query("SELECT * FROM history WHERE userId = :userId ORDER BY readAt DESC")
    fun getHistoryByUser(userId: String): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE userId = :userId ORDER BY readAt DESC LIMIT :limit")
    fun getRecentHistory(userId: String, limit: Int = 20): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE komikSlug = :slug AND userId = :userId LIMIT 1")
    suspend fun getHistoryBySlugAndUser(slug: String, userId: String): HistoryEntity?

    /**
     * Insert atau update riwayat baca.
     * Jika komik yang sama sudah ada, update chapter terakhir dan waktu baca.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistory(id: Int)

    @Query("DELETE FROM history WHERE userId = :userId")
    suspend fun clearHistory(userId: String)

    @Query("SELECT COUNT(*) FROM history WHERE userId = :userId")
    suspend fun getHistoryCount(userId: String): Int
}
