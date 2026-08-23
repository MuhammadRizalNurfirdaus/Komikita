package com.komikita.app.data.api

import com.komikita.app.data.model.CustomBackendKomikDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CustomBackendApi {

    @GET("custom-comics")
    suspend fun getCustomComics(): List<CustomBackendKomikDto>

    @GET("custom-comics/{slug}")
    suspend fun getCustomComicDetail(
        @Path("slug") slug: String
    ): CustomBackendKomikDto

    @POST("custom-comics")
    suspend fun publishCustomComic(
        @Body comicDto: CustomBackendKomikDto
    ): CustomBackendKomikDto
}
