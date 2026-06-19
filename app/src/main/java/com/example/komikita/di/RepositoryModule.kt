package com.example.komikita.di

import com.example.komikita.data.repository.CustomComicRepositoryImpl
import com.example.komikita.data.repository.HistoryRepositoryImpl
import com.example.komikita.data.repository.KomikRepositoryImpl
import com.example.komikita.data.repository.UserRepositoryImpl
import com.example.komikita.domain.repository.CustomComicRepository
import com.example.komikita.domain.repository.HistoryRepository
import com.example.komikita.domain.repository.KomikRepository
import com.example.komikita.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module untuk binding Repository Interface ke Implementasi.
 *
 * Prinsip Clean Architecture:
 * - Domain layer mendefinisikan interface (kontrak)
 * - Data layer menyediakan implementasi
 * - Hilt menyuntikkan implementasi ke use case/viewmodel
 *
 * @Binds: Lebih efisien daripada @Provides untuk binding interface-impl sederhana.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Bind KomikRepository interface ke KomikRepositoryImpl.
     * KomikRepositoryImpl menggabungkan data dari Scraper API + Backend API (hybrid).
     */
    @Binds
    @Singleton
    abstract fun bindKomikRepository(impl: KomikRepositoryImpl): KomikRepository

    /**
     * Bind UserRepository interface ke UserRepositoryImpl.
     * UserRepositoryImpl menggabungkan Firebase Auth + Backend user data.
     */
    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    /**
     * Bind CustomComicRepository interface ke CustomComicRepositoryImpl.
     * Untuk fitur Translator (upload komik, hide/unhide).
     */
    @Binds
    @Singleton
    abstract fun bindCustomComicRepository(impl: CustomComicRepositoryImpl): CustomComicRepository

    /**
     * Bind HistoryRepository interface ke HistoryRepositoryImpl.
     * Untuk riwayat baca (disimpan di Room DB lokal).
     */
    @Binds
    @Singleton
    abstract fun bindHistoryRepository(impl: HistoryRepositoryImpl): HistoryRepository
}
