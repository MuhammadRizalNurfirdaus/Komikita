package com.example.komikita.data.api

import com.example.komikita.data.model.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface KomikApi {
    
    @GET("api/komik/search")
    suspend fun searchKomik(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): Response<SearchResponse>
    
    @GET("api/komik/manhwa")
    suspend fun getManhwaList(
        @Query("page") page: Int = 1
    ): Response<ListResponse>
    
    @GET("api/komik/manhua")
    suspend fun getManhuaList(
        @Query("page") page: Int = 1
    ): Response<ListResponse>
    
    @GET("api/komik/manga")
    suspend fun getMangaList(
        @Query("page") page: Int = 1
    ): Response<SearchResponse>
    
    @GET("api/komik/detail")
    suspend fun getKomikDetail(
        @Query("komik_id") komikId: String
    ): Response<DetailResponse>
    
    @GET("api/komik/chapter")
    suspend fun getChapter(
        @Query("chapter_url") chapterUrl: String
    ): Response<ChapterResponse>
}
