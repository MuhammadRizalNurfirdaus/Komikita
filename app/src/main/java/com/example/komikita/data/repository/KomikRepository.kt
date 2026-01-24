package com.example.komikita.data.repository

import com.example.komikita.data.api.RetrofitClient
import com.example.komikita.data.model.*

class KomikRepository {
    
    private val api = RetrofitClient.komikApi
    
    suspend fun searchKomik(query: String, page: Int = 1): Result<SearchResponse> {
        return try {
            val response = api.searchKomik(query, page)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getLatest(page: Int = 1): Result<LatestResponse> {
        return try {
            val response = api.getLatest(page)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPopular(page: Int = 1, period: String? = null): Result<PopularResponse> {
        return try {
            val response = api.getPopular(page, period)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getManhwaList(page: Int): Result<ListResponse> {
        return try {
            val response = api.getManhwaList(page)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getManhuaList(page: Int): Result<ListResponse> {
        return try {
            val response = api.getManhuaList(page)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMangaList(page: Int): Result<SearchResponse> {
        return try {
            val response = api.getMangaList(page)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getKomikDetail(komikId: String): Result<DetailResponse> {
        return try {
            val response = api.getKomikDetail(komikId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getChapter(chapterUrl: String): Result<ChapterResponse> {
        return try {
            val response = api.getChapter(chapterUrl)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getGenres(): Result<GenreListResponse> {
        return try {
            val response = api.getGenres()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getKomikByGenre(genreSlug: String, slug: String, page: Int = 1): Result<GenreFilterResponse> {
        return try {
            val response = api.getKomikByGenre(genreSlug, slug, page)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
