package com.example.komikita.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.komikita.domain.model.Komik
import com.example.komikita.domain.repository.HomeFeedState
import com.example.komikita.domain.usecase.GetHomeFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel untuk Home Screen.
 * Mengambil feed gabungan (Scraper + Custom) melalui use case.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeFeedUseCase: GetHomeFeedUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeFeedState(isLoading = true))
    val state: StateFlow<HomeFeedState> = _state.asStateFlow()

    init {
        loadHomeFeed()
    }

    /**
     * Muat feed home dari repository hybrid.
     * Flow akan otomatis emit state loading -> data -> error.
     */
    fun loadHomeFeed() {
        viewModelScope.launch {
            getHomeFeedUseCase().collect { feedState ->
                _state.value = feedState
            }
        }
    }

    /**
     * Refresh: panggil ulang loadHomeFeed.
     * Bisa dipicu oleh pull-to-refresh di UI.
     */
    fun refresh() {
        loadHomeFeed()
    }
}
