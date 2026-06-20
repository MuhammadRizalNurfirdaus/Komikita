package com.example.komikita.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.komikita.domain.model.Komik
import com.example.komikita.domain.model.KomikSource
import com.example.komikita.presentation.theme.CustomBadgeColor
import com.example.komikita.presentation.theme.ScraperBadgeColor

/**
 * Home Screen (Jetpack Compose).
 * Menampilkan gabungan komik dari Scraper API + PostgreSQL dalam LazyVerticalGrid.
 *
 * Fitur:
 * - Grid 2 kolom modern (adaptive)
 * - Badge untuk membedakan sumber (Scraper = hijau, Custom = oranye)
 * - Loading indicator
 * - Error state dengan tombol retry
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onKomikClick: (slug: String) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KOMIKITA") },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Cari Komik")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        // Cegah double padding: outer Scaffold (NavHost) sudah handle insets
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // === STATE: LOADING ===
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // === STATE: ERROR ===
                state.error != null && state.komikList.isEmpty() -> {
                    ErrorContent(
                        message = state.error!!,
                        onRetry = { viewModel.refresh() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // === STATE: DATA ===
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.komikList, key = { "${it.source}_${it.slug}" }) { komik ->
                            KomikCard(
                                komik = komik,
                                onClick = { onKomikClick(komik.slug) }
                            )
                        }
                    }

                    // Tampilkan error snackbar jika ada error tapi data tetap ada
                    if (state.error != null) {
                        Snackbar(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        ) {
                            Text("Gagal memuat beberapa data: ${state.error}")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card komik individual untuk grid.
 * Menampilkan poster, judul, chapter terbaru, dan badge sumber.
 */
@Composable
fun KomikCard(
    komik: Komik,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Poster komik dengan aspect ratio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.7f) // Rasio poster komik standar
            ) {
                AsyncImage(
                    model = komik.poster,
                    contentDescription = komik.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Badge sumber data (Scraper vs Custom)
                val badgeColor = if (komik.source == KomikSource.CUSTOM) {
                    CustomBadgeColor
                } else {
                    ScraperBadgeColor
                }
                val badgeText = if (komik.source == KomikSource.CUSTOM) "Custom" else "Scraper"

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    color = badgeColor,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                // Chapter terbaru di pojok bawah
                komik.chapter?.let { chapter ->
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(4.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            text = chapter,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            // Judul dan info tambahan
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = komik.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (komik.type != null) {
                    Text(
                        text = komik.type,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Tampilan error dengan tombol retry.
 */
@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Terjadi Kesalahan",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Coba Lagi")
        }
    }
}
