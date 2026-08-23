package com.komikita.app.presentation.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komikita.app.data.local.entity.DownloadedChapterEntity
import com.komikita.app.domain.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DownloadsUiState(
    val downloadedChapters: List<DownloadedChapterEntity> = emptyList(),
    val totalSizeBytes: Long = 0L
)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    val uiState: StateFlow<DownloadsUiState> = downloadRepository.getAllDownloadedChapters()
        .map { chapters ->
            val totalSize = chapters.sumOf { it.totalSizeBytes }
            DownloadsUiState(
                downloadedChapters = chapters,
                totalSizeBytes = totalSize
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DownloadsUiState()
        )

    fun deleteChapter(chapterEndpoint: String) {
        viewModelScope.launch {
            downloadRepository.deleteDownloadedChapter(chapterEndpoint)
        }
    }
}
