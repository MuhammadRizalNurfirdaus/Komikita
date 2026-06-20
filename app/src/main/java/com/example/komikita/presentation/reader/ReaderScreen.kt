package com.example.komikita.presentation.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Reader Screen (Jetpack Compose).
 * Tampilan baca komik secara vertical scroll menggunakan LazyColumn + Coil.
 *
 * Fitur:
 * - Vertical scroll untuk membaca (LazyColumn)
 * - Image loading dengan Coil (caching otomatis)
 * - Navigasi chapter (prev/next) di toolbar
 * - Loading per-image dengan placeholder
 * - Immersive mode (toolbar bisa di-hide)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    chapterId: String,
    onBackClick: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Toggle toolbar visibility saat scroll
    var showToolbar by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            if (showToolbar) {
                TopAppBar(
                    title = {
                        Text(
                            text = state.chapterPages?.title ?: "Membaca...",
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali"
                            )
                        }
                    },
                    actions = {
                        // Tombol chapter sebelumnya
                        if (state.chapterPages?.prevChapterId != null) {
                            IconButton(onClick = { viewModel.loadPrevChapter() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Chapter Sebelumnya"
                                )
                            }
                        }

                        // Tombol chapter selanjutnya
                        if (state.chapterPages?.nextChapterId != null) {
                            IconButton(onClick = { viewModel.loadNextChapter() }) {
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = "Chapter Selanjutnya"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.7f),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        },
        modifier = Modifier.background(Color.Black),
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            when {
                // === LOADING ===
                state.isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Memuat halaman...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // === ERROR ===
                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Gagal Memuat",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.error!!,
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadChapter() }) {
                            Text("Coba Lagi")
                        }
                    }
                }

                // === READER: LAZY COLUMN DENGAN COIL ===
                state.chapterPages != null -> {
                    val images = state.chapterPages!!.images

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(images) { index, imageUrl ->
                            // Toggle toolbar saat user scroll ke atas
                            ComicPageImage(
                                imageUrl = imageUrl,
                                pageNumber = index + 1,
                                totalPages = images.size,
                                onTap = { showToolbar = !showToolbar }
                            )
                        }

                        // Navigasi chapter di akhir halaman
                        item {
                            ChapterNavigation(
                                hasPrev = state.chapterPages?.prevChapterId != null,
                                hasNext = state.chapterPages?.nextChapterId != null,
                                onPrev = { viewModel.loadPrevChapter() },
                                onNext = { viewModel.loadNextChapter() }
                            )
                        }
                    }

                    // Progress indicator: halaman ke-X dari total
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        val currentPage = if (listState.firstVisibleItemIndex >= 0) {
                            listState.firstVisibleItemIndex + 1
                        } else 1

                        Text(
                            text = "$currentPage / ${images.size}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Satu halaman komik (full-width image).
 * Menggunakan Coil AsyncImage untuk loading yang mulus.
 */
@Composable
fun ComicPageImage(
    imageUrl: String,
    pageNumber: Int,
    totalPages: Int,
    onTap: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Halaman $pageNumber dari $totalPages",
            modifier = Modifier
                .fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )

        // Overlay transparan untuk toggle toolbar saat tap
        // Menggunakan clickable tanpa ripple (indication = null) agar tidak mengganggu tampilan
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.TopCenter)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onTap
                )
        )
    }
}

/**
 * Navigasi prev/next chapter di bagian bawah reader.
 */
@Composable
fun ChapterNavigation(
    hasPrev: Boolean,
    hasNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (hasPrev) {
            OutlinedButton(
                onClick = onPrev,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("< Sebelumnya")
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        if (hasNext) {
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Selanjutnya >")
            }
        }
    }
}
