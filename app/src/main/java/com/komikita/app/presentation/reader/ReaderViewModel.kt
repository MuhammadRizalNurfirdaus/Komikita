package com.komikita.app.presentation.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komikita.app.domain.model.Chapter
import com.komikita.app.domain.repository.DownloadRepository
import com.komikita.app.domain.repository.KomikRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ReaderUiState {
    object Loading : ReaderUiState()
    data class Success(val chapter: Chapter, val isOffline: Boolean = false) : ReaderUiState()
    data class Error(val message: String) : ReaderUiState()
}

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val komikRepository: KomikRepository,
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.Loading)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    fun loadChapter(endpoint: String, isCustom: Boolean) {
        viewModelScope.launch {
            _uiState.value = ReaderUiState.Loading

            // 1. Check Offline Storage in Room DB first
            val offlinePages = downloadRepository.getDownloadedPages(endpoint)
            if (!offlinePages.isNullOrEmpty()) {
                _uiState.value = ReaderUiState.Success(
                    chapter = Chapter(
                        title = "Offline Chapter",
                        endpoint = endpoint,
                        pages = offlinePages
                    ),
                    isOffline = true
                )
                return@launch
            }

            // 2. Fallback to Online Fetch via API
            komikRepository.getChapterDetail(endpoint, isCustom)
                .onSuccess { chapter ->
                    _uiState.value = ReaderUiState.Success(chapter, isOffline = false)
                }
                .onFailure { error ->
                    _uiState.value = ReaderUiState.Error(error.localizedMessage ?: "Gagal memuat chapter")
                }
        }
    }
}
