package com.komikita.app.di

import android.content.Context
import androidx.room.Room
import com.komikita.app.data.local.KomikitaDatabase
import com.komikita.app.data.local.dao.DownloadDao
import com.komikita.app.data.local.dao.FavoriteDao
import com.komikita.app.data.local.dao.HistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideKomikitaDatabase(
        @ApplicationContext context: Context
    ): KomikitaDatabase {
        return Room.databaseBuilder(
            context,
            KomikitaDatabase::class.java,
            "komikita.db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideFavoriteDao(database: KomikitaDatabase): FavoriteDao {
        return database.favoriteDao()
    }

    @Provides
    @Singleton
    fun provideHistoryDao(database: KomikitaDatabase): HistoryDao {
        return database.historyDao()
    }

    @Provides
    @Singleton
    fun provideDownloadDao(database: KomikitaDatabase): DownloadDao {
        return database.downloadDao()
    }
}
