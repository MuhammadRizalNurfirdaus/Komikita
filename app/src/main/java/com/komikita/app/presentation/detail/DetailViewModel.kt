package com.komikita.app.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komikita.app.data.local.dao.DownloadDao
import com.komikita.app.data.local.dao.FavoriteDao
import com.komikita.app.data.local.dao.HistoryDao
import com.komikita.app.data.local.entity.DownloadStatus
import com.komikita.app.data.local.entity.DownloadedChapterEntity
import com.komikita.app.data.local.entity.FavoriteEntity
import com.komikita.app.data.local.entity.HistoryEntity
import com.komikita.app.domain.model.ComicSource
import com.komikita.app.domain.model.Komik
import com.komikita.app.domain.repository.DownloadRepository
import com.komikita.app.domain.repository.KomikRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val komik: Komik) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val komikRepository: KomikRepository,
    private val favoriteDao: FavoriteDao,
    private val historyDao: HistoryDao,
    private val downloadDao: DownloadDao,
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadComicDetail(endpoint: String, isCustom: Boolean) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            komikRepository.getComicDetail(endpoint, isCustom)
                .onSuccess { komik ->
                    _uiState.value = DetailUiState.Success(komik)
                }
                .onFailure { error ->
                    _uiState.value = DetailUiState.Error(error.localizedMessage ?: "Gagal memuat detail komik")
                }
        }
    }

    fun isFavorite(endpoint: String): Flow<Boolean> {
        return favoriteDao.isFavorite(endpoint)
    }

    fun toggleFavorite(komik: Komik, isFavorite: Boolean) {
        viewModelScope.launch {
            if (isFavorite) {
                favoriteDao.deleteFavoriteByEndpoint(komik.endpoint)
            } else {
                favoriteDao.insertFavorite(
                    FavoriteEntity(
                        endpoint = komik.endpoint,
                        title = komik.title,
                        coverUrl = komik.coverUrl,
                        type = komik.type,
                        rating = komik.rating,
                        isCustom = komik.source == ComicSource.CUSTOM_TRANSLATOR
                    )
                )
            }
        }
    }

    fun saveHistory(komik: Komik, chapterTitle: String, chapterEndpoint: String) {
        viewModelScope.launch {
            historyDao.upsertHistory(
                HistoryEntity(
                    endpoint = komik.endpoint,
                    comicTitle = komik.title,
                    coverUrl = komik.coverUrl,
                    lastChapterTitle = chapterTitle,
                    lastChapterEndpoint = chapterEndpoint
                )
            )
        }
    }

    fun getHistoryForComic(comicEndpoint: String): Flow<HistoryEntity?> {
        return historyDao.getHistoryForComic(comicEndpoint)
    }

    fun getDownloadedChapters(comicEndpoint: String): Flow<List<DownloadedChapterEntity>> {
        return downloadDao.getDownloadedChaptersForComic(comicEndpoint)
    }

    fun startDownload(komik: Komik, chapterEndpoint: String, chapterTitle: String) {
        viewModelScope.launch {
            komikRepository.getChapterDetail(chapterEndpoint, komik.source == ComicSource.CUSTOM_TRANSLATOR)
                .onSuccess { chapter ->
                    if (chapter.pages.isNotEmpty()) {
                        downloadRepository.startDownload(
                            comicEndpoint = komik.endpoint,
                            comicTitle = komik.title,
                            coverUrl = komik.coverUrl,
                            chapterEndpoint = chapterEndpoint,
                            chapterTitle = chapterTitle,
                            pages = chapter.pages
                        )
                    }
                }
        }
    }
}
