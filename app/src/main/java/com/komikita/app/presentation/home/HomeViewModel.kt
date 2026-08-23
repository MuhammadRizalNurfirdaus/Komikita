package com.komikita.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komikita.app.domain.model.ComicSource
import com.komikita.app.domain.model.Komik
import com.komikita.app.domain.repository.KomikRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CategoryFilter {
    ALL,
    SCRAPER,
    CUSTOM_TRANSLATOR
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val comics: List<Komik>,
        val filteredComics: List<Komik>,
        val currentCategory: CategoryFilter = CategoryFilter.ALL,
        val isRefreshing: Boolean = false
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val komikRepository: KomikRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var allComics: List<Komik> = emptyList()
    private var selectedCategory: CategoryFilter = CategoryFilter.ALL
    private var currentQuery: String = ""

    init {
        loadComics()
    }

    fun loadComics(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh) {
                _uiState.value = HomeUiState.Loading
            }
            komikRepository.getMergedPopularComics(1)
                .onSuccess { list ->
                    allComics = list
                    applyFilters()
                }
                .onFailure { error ->
                    _uiState.value = HomeUiState.Error(error.localizedMessage ?: "Gagal memuat komik")
                }
        }
    }

    fun setCategory(category: CategoryFilter) {
        selectedCategory = category
        applyFilters()
    }

    fun searchComics(query: String) {
        currentQuery = query
        if (query.isBlank()) {
            applyFilters()
            return
        }
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            komikRepository.searchComics(query)
                .onSuccess { list ->
                    allComics = list
                    applyFilters()
                }
                .onFailure { error ->
                    _uiState.value = HomeUiState.Error(error.localizedMessage ?: "Gagal mencari komik")
                }
        }
    }

    private fun applyFilters() {
        val filtered = allComics.filter { comic ->
            when (selectedCategory) {
                CategoryFilter.ALL -> true
                CategoryFilter.SCRAPER -> comic.source == ComicSource.SCRAPER
                CategoryFilter.CUSTOM_TRANSLATOR -> comic.source == ComicSource.CUSTOM_TRANSLATOR
            }
        }
        _uiState.value = HomeUiState.Success(
            comics = allComics,
            filteredComics = filtered,
            currentCategory = selectedCategory,
            isRefreshing = false
        )
    }
}
