package com.komikita.app.domain.repository

import com.komikita.app.domain.model.Chapter
import com.komikita.app.domain.model.Komik

interface KomikRepository {
    suspend fun getMergedPopularComics(page: Int = 1): Result<List<Komik>>
    suspend fun getMergedLatestComics(page: Int = 1): Result<List<Komik>>
    suspend fun getComicDetail(endpoint: String, isCustom: Boolean = false): Result<Komik>
    suspend fun getChapterDetail(endpoint: String, isCustom: Boolean = false): Result<Chapter>
    suspend fun searchComics(query: String): Result<List<Komik>>
    suspend fun publishCustomComic(comic: Komik): Result<Komik>
}
