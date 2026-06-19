package com.example.komikita.presentation.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.komikita.domain.model.ChapterPages
import com.example.komikita.domain.usecase.GetChapterPagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel untuk Reader Screen.
 * Mengambil halaman chapter dan mengelola navigasi prev/next chapter.
 */
@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val getChapterPagesUseCase: GetChapterPagesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val chapterId: String = savedStateHandle["chapterId"] ?: ""

    private val _state = MutableStateFlow(ReaderState(isLoading = true))
    val state: StateFlow<ReaderState> = _state.asStateFlow()

    init {
        loadChapter()
    }

    /**
     * Muat halaman chapter dari hybrid repository.
     * Otomatis memilih sumber (custom/scrapper) berdasarkan ketersediaan.
     */
    fun loadChapter() {
        viewModelScope.launch {
            _state.value = ReaderState(isLoading = true)

            val result = getChapterPagesUseCase(chapterId)
            result.fold(
                onSuccess = { pages ->
                    _state.value = ReaderState(
                        isLoading = false,
                        chapterPages = pages,
                        currentImageIndex = 0
                    )
                },
                onFailure = { error ->
                    _state.value = ReaderState(
                        isLoading = false,
                        error = error.message ?: "Gagal memuat chapter"
                    )
                }
            )
        }
    }

    /**
     * Navigasi ke chapter selanjutnya.
     */
    fun loadNextChapter() {
        val nextId = _state.value.chapterPages?.nextChapterId ?: return
        viewModelScope.launch {
            _state.value = ReaderState(isLoading = true)
            val result = getChapterPagesUseCase(nextId)
            result.fold(
                onSuccess = { pages ->
                    _state.value = ReaderState(
                        isLoading = false,
                        chapterPages = pages,
                        currentImageIndex = 0
                    )
                },
                onFailure = { error ->
                    _state.value = ReaderState(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    /**
     * Navigasi ke chapter sebelumnya.
     */
    fun loadPrevChapter() {
        val prevId = _state.value.chapterPages?.prevChapterId ?: return
        viewModelScope.launch {
            _state.value = ReaderState(isLoading = true)
            val result = getChapterPagesUseCase(prevId)
            result.fold(
                onSuccess = { pages ->
                    _state.value = ReaderState(
                        isLoading = false,
                        chapterPages = pages,
                        currentImageIndex = 0
                    )
                },
                onFailure = { error ->
                    _state.value = ReaderState(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
}

/**
 * State untuk Reader Screen.
 */
data class ReaderState(
    val isLoading: Boolean = false,
    val chapterPages: ChapterPages? = null,
    val currentImageIndex: Int = 0,
    val error: String? = null
)
