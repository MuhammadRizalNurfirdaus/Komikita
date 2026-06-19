package com.example.komikita.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.example.komikita.domain.model.Komik
import com.example.komikita.domain.model.KomikSource
import com.example.komikita.domain.usecase.SearchKomikUseCase
import com.example.komikita.presentation.theme.CustomBadgeColor
import com.example.komikita.presentation.theme.ScraperBadgeColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Search Screen - Pencarian hybrid (Scraper + Custom secara paralel).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onKomikClick: (slug: String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = query,
                        onValueChange = {
                            query = it
                            viewModel.search(it)
                        },
                        placeholder = { Text("Cari komik...") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.results.isEmpty() && query.isNotBlank() && !state.isLoading -> {
                    Text(
                        "Tidak ditemukan hasil untuk \"$query\"",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.results, key = { "${it.source}_${it.slug}" }) { komik ->
                            SearchResultItem(
                                komik = komik,
                                onClick = { onKomikClick(komik.slug) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(komik: Komik, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(komik.title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Row {
                komik.type?.let { Text("$it - ") }
                komik.chapter?.let { Text(it) }
            }
        },
        leadingContent = {
            AsyncImage(
                model = komik.poster,
                contentDescription = komik.title,
                modifier = Modifier.size(56.dp, 80.dp),
                contentScale = ContentScale.Crop
            )
        },
        trailingContent = {
            val color = if (komik.source == KomikSource.CUSTOM) CustomBadgeColor else ScraperBadgeColor
            val label = if (komik.source == KomikSource.CUSTOM) "Custom" else "Scraper"
            Surface(color = color, shape = MaterialTheme.shapes.extraSmall) {
                Text(label, modifier = Modifier.padding(4.dp), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
    HorizontalDivider()
}

// ViewModel
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchKomikUseCase: SearchKomikUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun search(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _state.value = SearchState()
            return
        }
        searchJob = viewModelScope.launch {
            delay(500) // Debounce: tunggu 500ms setelah user berhenti mengetik
            _state.value = _state.value.copy(isLoading = true)
            val result = searchKomikUseCase(query)
            result.fold(
                onSuccess = { _state.value = SearchState(results = it) },
                onFailure = { _state.value = SearchState(error = it.message) }
            )
        }
    }
}

data class SearchState(
    val isLoading: Boolean = false,
    val results: List<Komik> = emptyList(),
    val error: String? = null
)
