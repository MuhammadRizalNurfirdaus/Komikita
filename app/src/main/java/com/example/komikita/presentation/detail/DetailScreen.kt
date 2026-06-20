package com.example.komikita.presentation.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.example.komikita.domain.model.KomikDetail
import com.example.komikita.domain.usecase.GetKomikDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Detail Screen - Menampilkan informasi lengkap komik + daftar chapter.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    slug: String,
    onBackClick: () -> Unit,
    onChapterClick: (chapterId: String) -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.detail?.title ?: "Detail Komik") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error!!, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadDetail() }) { Text("Coba Lagi") }
                    }
                }
            }
            state.detail != null -> {
                val detail = state.detail!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Header: Poster + Info
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            AsyncImage(
                                model = detail.poster,
                                contentDescription = detail.title,
                                modifier = Modifier
                                    .width(120.dp)
                                    .aspectRatio(0.7f),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(detail.title, style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(4.dp))
                                detail.author?.let {
                                    Text("Oleh: $it", style = MaterialTheme.typography.bodyMedium)
                                }
                                detail.type?.let {
                                    Text(it, style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                                detail.status?.let {
                                    Text(it, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }

                    // Deskripsi
                    item {
                        detail.description?.let { desc ->
                            Text(
                                text = desc,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Genre chips
                    if (detail.genres.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                detail.genres.take(4).forEach { genre ->
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(genre, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Daftar Chapter
                    item {
                        Text(
                            text = "Daftar Chapter (${detail.chapters.size})",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    items(detail.chapters) { chapter ->
                        ListItem(
                            headlineContent = { Text(chapter.chapterNumber) },
                            supportingContent = { chapter.date?.let { Text(it) } },
                            modifier = Modifier.clickable {
                                onChapterClick(chapter.chapterId)
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

// ViewModel
@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getKomikDetailUseCase: GetKomikDetailUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val slug: String = savedStateHandle["slug"] ?: ""

    private val _state = MutableStateFlow(DetailState(isLoading = true))
    val state: StateFlow<DetailState> = _state.asStateFlow()

    init { loadDetail() }

    fun loadDetail() {
        viewModelScope.launch {
            _state.value = DetailState(isLoading = true)
            val result = getKomikDetailUseCase(slug)
            result.fold(
                onSuccess = { _state.value = DetailState(detail = it) },
                onFailure = { _state.value = DetailState(error = it.message) }
            )
        }
    }
}

data class DetailState(
    val isLoading: Boolean = false,
    val detail: KomikDetail? = null,
    val error: String? = null
)

