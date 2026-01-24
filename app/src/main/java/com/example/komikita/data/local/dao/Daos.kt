package com.example.komikita.data.local.dao

import androidx.room.*
import com.example.komikita.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>
    
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUserSync(): UserEntity?
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash LIMIT 1")
    suspend fun getUserByEmailAndPassword(email: String, passwordHash: String): UserEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Query("UPDATE users SET displayName = :name WHERE userId = :userId")
    suspend fun updateUserName(userId: String, name: String)
    
    @Query("UPDATE users SET photoUrl = :photoUrl WHERE userId = :userId")
    suspend fun updateUserPhoto(userId: String, photoUrl: String)

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
    
    @Query("DELETE FROM favorites WHERE slug = :slug")
    suspend fun deleteFavoriteBySlug(slug: String)
    
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
