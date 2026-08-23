package com.komikita.app.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.komikita.app.data.api.CustomBackendApi
import com.komikita.app.data.api.ScraperApi
import com.komikita.app.data.api.ShinigamiCdnInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideShinigamiCdnInterceptor(): ShinigamiCdnInterceptor {
        return ShinigamiCdnInterceptor()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        shinigamiCdnInterceptor: ShinigamiCdnInterceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(shinigamiCdnInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("ScraperRetrofit")
    fun provideScraperRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://11.shinigami.asia/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideScraperApi(@Named("ScraperRetrofit") retrofit: Retrofit): ScraperApi {
        return retrofit.create(ScraperApi::class.java)
    }

    @Provides
    @Singleton
    @Named("CustomRetrofit")
    fun provideCustomRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.komikita.example.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideCustomBackendApi(@Named("CustomRetrofit") retrofit: Retrofit): CustomBackendApi {
        return retrofit.create(CustomBackendApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCoilImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(context.cacheDir, "image_cache_shinigami"))
                    .maxSizeBytes(512 * 1024 * 1024) // 512MB
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
