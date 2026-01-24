package com.example.komikita.data.api

import com.example.komikita.data.model.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface KomikApi {
    
    @GET("api/komik/search")
    suspend fun searchKomik(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): Response<SearchResponse>
    
    @GET("api/komik/latest")
    suspend fun getLatest(
        @Query("page") page: Int = 1
    ): Response<LatestResponse>
    
    @GET("api/komik/popular")
    suspend fun getPopular(
        @Query("page") page: Int = 1,
        @Query("period") period: String? = null
    ): Response<PopularResponse>
    
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
    
    @GET("api/komik/genres")
    suspend fun getGenres(): Response<GenreListResponse>
    
    @GET("api/komik/genre/{genre_slug}/{slug}")
    suspend fun getKomikByGenre(
        @Path("genre_slug") genreSlug: String,
        @Path("slug") slug: String,
        @Query("page") page: Int = 1
    ): Response<GenreFilterResponse>
}
