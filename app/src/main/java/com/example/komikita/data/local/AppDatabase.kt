package com.example.komikita.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.komikita.data.local.dao.*
import com.example.komikita.data.local.entity.*

@Database(
    entities = [UserEntity::class, FavoriteEntity::class, DownloadEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun downloadDao(): DownloadDao
    
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
                .fallbackToDestructiveMigration() // Auto-recreate DB on schema change
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
