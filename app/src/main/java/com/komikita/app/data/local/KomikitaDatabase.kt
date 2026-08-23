package com.komikita.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.komikita.app.data.local.converter.StringListConverter
import com.komikita.app.data.local.dao.DownloadDao
import com.komikita.app.data.local.dao.FavoriteDao
import com.komikita.app.data.local.dao.HistoryDao
import com.komikita.app.data.local.entity.DownloadedChapterEntity
import com.komikita.app.data.local.entity.DownloadedComicEntity
import com.komikita.app.data.local.entity.FavoriteEntity
import com.komikita.app.data.local.entity.HistoryEntity

@Database(
    entities = [
        FavoriteEntity::class,
        HistoryEntity::class,
        DownloadedComicEntity::class,
        DownloadedChapterEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class KomikitaDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun historyDao(): HistoryDao
    abstract fun downloadDao(): DownloadDao
}
