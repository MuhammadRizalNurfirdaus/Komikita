package com.komikita.app.di

import com.komikita.app.data.repository.DownloadRepositoryImpl
import com.komikita.app.data.repository.KomikRepositoryImpl
import com.komikita.app.domain.repository.DownloadRepository
import com.komikita.app.domain.repository.KomikRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindKomikRepository(
        komikRepositoryImpl: KomikRepositoryImpl
    ): KomikRepository

    @Binds
    @Singleton
    abstract fun bindDownloadRepository(
        downloadRepositoryImpl: DownloadRepositoryImpl
    ): DownloadRepository
}
