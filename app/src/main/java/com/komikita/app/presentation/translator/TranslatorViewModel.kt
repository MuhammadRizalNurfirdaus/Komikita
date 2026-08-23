package com.komikita.app.presentation.translator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komikita.app.domain.model.Chapter
import com.komikita.app.domain.model.ComicSource
import com.komikita.app.domain.model.Komik
import com.komikita.app.domain.repository.KomikRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

sealed class TranslatorUiState {
    object Idle : TranslatorUiState()
    object Publishing : TranslatorUiState()
    data class Success(val publishedComic: Komik) : TranslatorUiState()
    data class Error(val message: String) : TranslatorUiState()
}

@HiltViewModel
class TranslatorViewModel @Inject constructor(
    private val komikRepository: KomikRepository
) : ViewModel() {

    private val _rawText = MutableStateFlow("")
    val rawText: StateFlow<String> = _rawText.asStateFlow()

    private val _parsedUrls = MutableStateFlow<List<String>>(emptyList())
    val parsedUrls: StateFlow<List<String>> = _parsedUrls.asStateFlow()

    private val _uiState = MutableStateFlow<TranslatorUiState>(TranslatorUiState.Idle)
    val uiState: StateFlow<TranslatorUiState> = _uiState.asStateFlow()

    fun updateRawText(text: String) {
        _rawText.value = text
        _parsedUrls.value = parseBulkUrls(text)
    }

    /**
     * Multi-line textfield bulk paste parser that splits text by newlines and commas,
     * extracting valid HTTP/HTTPS image URLs into List<String>.
     */
    fun parseBulkUrls(input: String): List<String> {
        if (input.isBlank()) return emptyList()

        return input.split("\n", ",")
            .map { it.trim() }
            .filter { token ->
                token.isNotBlank() &&
                        (token.startsWith("http://", ignoreCase = true) || token.startsWith("https://", ignoreCase = true)) &&
                        (token.lowercase(Locale.ROOT).contains(".png") ||
                                token.lowercase(Locale.ROOT).contains(".jpg") ||
                                token.lowercase(Locale.ROOT).contains(".jpeg") ||
                                token.lowercase(Locale.ROOT).contains(".webp") ||
                                token.lowercase(Locale.ROOT).contains(".gif") ||
                                token.lowercase(Locale.ROOT).contains("shinigami") ||
                                token.lowercase(Locale.ROOT).contains("cdn") ||
                                token.contains("/"))
            }
    }

    fun publishComic(
        title: String,
        coverUrl: String,
        synopsis: String,
        type: String,
        author: String,
        chapterTitle: String,
        overriddenScraperSlug: String?
    ) {
        if (title.isBlank() || coverUrl.isBlank() || _parsedUrls.value.isEmpty()) {
            _uiState.value = TranslatorUiState.Error("Judul, Cover URL, dan Minimal 1 URL Halaman harus diisi!")
            return
        }

        viewModelScope.launch {
            _uiState.value = TranslatorUiState.Publishing

            val slug = title.lowercase(Locale.ROOT)
                .replace(Regex("[^a-z0-9]+"), "-")
                .trim('-')

            val chapterSlug = "$slug-ch-1"

            val customComic = Komik(
                title = title,
                endpoint = slug,
                coverUrl = coverUrl,
                type = type.ifBlank { "MANHWA" },
                status = "ONGOING",
                latestChapter = chapterTitle.ifBlank { "Chapter 1" },
                rating = "5.0",
                source = ComicSource.CUSTOM_TRANSLATOR,
                synopsis = synopsis,
                author = author,
                overriddenScraperSlug = overriddenScraperSlug?.ifBlank { null },
                chapters = listOf(
                    Chapter(
                        title = chapterTitle.ifBlank { "Chapter 1" },
                        endpoint = chapterSlug,
                        pages = _parsedUrls.value
                    )
                )
            )

            komikRepository.publishCustomComic(customComic)
                .onSuccess { published ->
                    _uiState.value = TranslatorUiState.Success(published)
                }
                .onFailure { error ->
                    _uiState.value = TranslatorUiState.Error(error.localizedMessage ?: "Gagal mempublikasi komik")
                }
        }
    }
}
