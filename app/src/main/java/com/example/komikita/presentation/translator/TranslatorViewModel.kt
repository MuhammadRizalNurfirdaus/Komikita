package com.example.komikita.presentation.translator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.komikita.domain.usecase.UploadComicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel untuk Translator Dashboard.
 * Menangani form input (Judul, Slug) dan fitur Bulk Paste URL gambar.
 */
@HiltViewModel
class TranslatorViewModel @Inject constructor(
    private val uploadComicUseCase: UploadComicUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TranslatorState())
    val state: StateFlow<TranslatorState> = _state.asStateFlow()

    /** Update judul komik */
    fun updateTitle(title: String) {
        _state.value = _state.value.copy(title = title)
    }

    /** Update slug komik */
    fun updateSlug(slug: String) {
        _state.value = _state.value.copy(slug = slug)
    }

    /** Update tipe komik */
    fun updateType(type: String) {
        _state.value = _state.value.copy(type = type)
    }

    /** Update URL cover */
    fun updateCoverUrl(coverUrl: String) {
        _state.value = _state.value.copy(coverUrl = coverUrl)
    }

    /**
     * FITUR BULK PASTE:
     * User bisa paste 50+ URL gambar sekaligus.
     * Fungsi ini otomatis memisahkan URL berdasarkan newline/spasi/koma.
     *
     * Contoh input:
     * ```
     * https://img1.com/page1.jpg
     * https://img1.com/page2.jpg
     * https://img1.com/page3.jpg
     * ```
     * Atau dipisahkan koma:
     * "https://img1.com/1.jpg, https://img1.com/2.jpg, https://img1.com/3.jpg"
     */
    fun bulkPasteUrls(rawText: String) {
        val urls = rawText
            .split(Regex("[\\n,;\\s]+"))  // Pisahkan berdasarkan newline, koma, semicolon, atau spasi
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .filter { isValidUrl(it) }     // Hanya ambil yang format URL valid

        _state.value = _state.value.copy(
            imageUrls = urls,
            bulkPasteText = rawText
        )
    }

    /**
     * Hapus satu URL dari list.
     */
    fun removeUrl(index: Int) {
        val updated = _state.value.imageUrls.toMutableList().apply {
            if (index in indices) removeAt(index)
        }
        _state.value = _state.value.copy(imageUrls = updated)
    }

    /**
     * Pindahkan URL ke atas/bawah (reorder).
     */
    fun moveUrl(fromIndex: Int, toIndex: Int) {
        val updated = _state.value.imageUrls.toMutableList().apply {
            val item = removeAt(fromIndex)
            add(toIndex, item)
        }
        _state.value = _state.value.copy(imageUrls = updated)
    }

    /**
     * Submit komik ke backend.
     * Validasi: judul, slug, dan minimal 1 URL gambar harus terisi.
     */
    fun submitComic() {
        val current = _state.value

        // Validasi input
        if (current.title.isBlank()) {
            _state.value = current.copy(error = "Judul komik wajib diisi")
            return
        }
        if (current.slug.isBlank()) {
            _state.value = current.copy(error = "Slug wajib diisi")
            return
        }
        if (current.imageUrls.isEmpty()) {
            _state.value = current.copy(error = "Minimal 1 URL gambar harus ada")
            return
        }

        viewModelScope.launch {
            _state.value = current.copy(isSubmitting = true, error = null)

            val result = uploadComicUseCase(
                title = current.title.trim(),
                slug = current.slug.trim(),
                imageUrls = current.imageUrls,
                coverUrl = current.coverUrl.ifBlank { null },
                type = current.type.ifBlank { null }
            )

            result.fold(
                onSuccess = { comic ->
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        isSuccess = true,
                        successMessage = "Komik '${comic.title}' berhasil diupload!"
                    )
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        error = error.message ?: "Gagal upload komik"
                    )
                }
            )
        }
    }

    /**
     * Auto-generate slug dari judul (jika user belum isi slug).
     */
    fun autoGenerateSlug() {
        if (_state.value.slug.isBlank() && _state.value.title.isNotBlank()) {
            val slug = _state.value.title
                .lowercase()
                .replace(Regex("[^a-z0-9\\s-]"), "")  // Hapus karakter non-alfanumerik
                .replace(Regex("\\s+"), "-")            // Spasi jadi dash
                .take(50)                                // Batasi panjang
            _state.value = _state.value.copy(slug = slug)
        }
    }

    /** Reset form setelah berhasil submit */
    fun resetForm() {
        _state.value = TranslatorState()
    }

    /** Clear error */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    /**
     * Validasi sederhana apakah string adalah URL.
     */
    private fun isValidUrl(text: String): Boolean {
        return text.startsWith("http://") || text.startsWith("https://")
    }
}

/**
 * State untuk Translator Dashboard.
 */
data class TranslatorState(
    val title: String = "",
    val slug: String = "",
    val type: String = "Manga",
    val coverUrl: String = "",
    val imageUrls: List<String> = emptyList(),
    val bulkPasteText: String = "",
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val successMessage: String = "",
    val error: String? = null
)
