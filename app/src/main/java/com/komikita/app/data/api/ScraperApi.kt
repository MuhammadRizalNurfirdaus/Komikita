package com.komikita.app.data.api

import com.komikita.app.data.model.ScraperKomikDto
import com.komikita.app.data.model.ScraperDetailDto
import com.komikita.app.data.model.ScraperChapterDto
import com.komikita.app.data.model.ScraperListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ScraperApi {

    @GET("popular")
    suspend fun getPopularComics(
        @Query("page") page: Int = 1
    ): ScraperListResponse

    @GET("latest")
    suspend fun getLatestComics(
        @Query("page") page: Int = 1
    ): ScraperListResponse

    @GET("detail/{endpoint}")
    suspend fun getComicDetail(
        @Path("endpoint") endpoint: String
    ): ScraperDetailDto

    @GET("chapter/{endpoint}")
    suspend fun getChapterDetail(
        @Path("endpoint") endpoint: String
    ): ScraperChapterDto

    @GET("search")
    suspend fun searchComics(
        @Query("query") query: String
    ): ScraperListResponse
}
