package com.example.komikita.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.komikita.data.local.dao.*
import com.example.komikita.data.local.entity.*

/**
 * Room Database untuk KOMIKITA.
 *
 * Tabel lokal:
 * - users: Cache user yang sedang login + JWT token
 * - favorites: Bookmark komik (offline)
 * - downloads: Chapter yang diunduh untuk offline reading
 * - history: Riwayat baca
 *
 * PENTING: Data utama ada di PostgreSQL (via Backend API).
 * Room hanya untuk cache, offline reading, dan data sementara.
 *
 * Versi 4: Menambahkan tabel history + kolom role & authToken di users.
 */
@Database(
    entities = [
        UserEntity::class,
        FavoriteEntity::class,
        DownloadEntity::class,
        HistoryEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun downloadDao(): DownloadDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "komikita_database"
                )
                .fallbackToDestructiveMigration() // Auto-recreate DB saat schema berubah
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
