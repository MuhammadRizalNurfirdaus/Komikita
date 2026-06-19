package com.example.komikita.di

import com.example.komikita.BuildConfig
import com.example.komikita.data.api.AuthInterceptor
import com.example.komikita.data.api.BackendApi
import com.example.komikita.data.api.ScraperApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt Module untuk konfigurasi Network (Retrofit + OkHttp).
 *
 * Arsitektur 2 OkHttpClient terpisah:
 * 1. SCRAPER client - tanpa AuthInterceptor (API publik, read-only)
 * 2. BACKEND client - dengan AuthInterceptor (otomatis sematkan JWT)
 *
 * Keduanya berbagi konfigurasi dasar (timeout, logging) yang sama.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // ============================================================
    // LOGGING (bersama)
    // ============================================================

    /**
     * Logging interceptor - BODY di debug, NONE di release.
     * Dipasang di kedua client agar developer bisa debug semua request.
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    // ============================================================
    // OKHTTP CLIENTS (2 instance terpisah)
    // ============================================================

    /**
     * OkHttpClient untuk Scraper API (Sumber A).
     * TIDAK menggunakan AuthInterceptor karena scraper bersifat publik.
     */
    @Provides
    @Singleton
    @Named("scraperClient")
    fun provideScraperOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * OkHttpClient untuk Backend API (Sumber B - PostgreSQL via REST).
     * Menggunakan AuthInterceptor untuk otomatis menyematkan JWT token.
     *
     * AuthInterceptor membaca token dari Room DB dan menambahkan header
     * "Authorization: Bearer <jwt>" di setiap request.
     * Jika server merespons 401, interceptor otomatis menghapus token lokal.
     */
    @Provides
    @Singleton
    @Named("backendClient")
    fun provideBackendOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)      // JWT otomatis di setiap request
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // ============================================================
    // RETROFIT INSTANCES
    // ============================================================

    /**
     * Retrofit untuk Scraper API (Sumber A).
     * Base URL: https://scraper.asepharyana.my.id/api/
     * Client: scraperClient (tanpa auth)
     */
    @Provides
    @Singleton
    @Named("scraper")
    fun provideScraperRetrofit(
        @Named("scraperClient") okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.SCRAPER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Retrofit untuk Backend API (Sumber B - PostgreSQL via REST).
     * Base URL: dari local.properties (BACKEND_BASE_URL)
     * Client: backendClient (dengan AuthInterceptor - JWT otomatis)
     */
    @Provides
    @Singleton
    @Named("backend")
    fun provideBackendRetrofit(
        @Named("backendClient") okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ============================================================
    // API INTERFACES
    // ============================================================

    /**
     * Interface ScraperApi - dibuat dari scraper Retrofit (tanpa auth).
     */
    @Provides
    @Singleton
    fun provideScraperApi(@Named("scraper") retrofit: Retrofit): ScraperApi {
        return retrofit.create(ScraperApi::class.java)
    }

    /**
     * Interface BackendApi - dibuat dari backend Retrofit (dengan auth interceptor).
     */
    @Provides
    @Singleton
    fun provideBackendApi(@Named("backend") retrofit: Retrofit): BackendApi {
        return retrofit.create(BackendApi::class.java)
    }
}
