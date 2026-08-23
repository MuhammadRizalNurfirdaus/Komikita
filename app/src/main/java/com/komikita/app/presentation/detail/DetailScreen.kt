package com.komikita.app.presentation.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.komikita.app.data.local.entity.DownloadStatus
import com.komikita.app.domain.model.ComicSource
import com.komikita.app.presentation.components.LoginRequiredDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    endpoint: String,
    isCustom: Boolean,
    isGuest: Boolean = false,
    viewModel: DetailViewModel,
    onBackClick: () -> Unit,
    onChapterClick: (chapterEndpoint: String) -> Unit,
    onLoginClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isFavorite by viewModel.isFavorite(endpoint).collectAsState(initial = false)
    val historyState by viewModel.getHistoryForComic(endpoint).collectAsState(initial = null)
    val downloadedChapters by viewModel.getDownloadedChapters(endpoint).collectAsState(initial = emptyList())
    var showGuestDownloadDialog by remember { mutableStateOf(false) }

    LaunchedEffect(endpoint, isCustom) {
        viewModel.loadComicDetail(endpoint, isCustom)
    }

    if (showGuestDownloadDialog) {
        LoginRequiredDialog(
            title = "Akses Terbatas",
            message = "Fitur unduh komik hanya untuk pengguna terdaftar. Silakan login terlebih dahulu.",
            onDismiss = { showGuestDownloadDialog = false },
            onLoginClick = {
                showGuestDownloadDialog = false
                onLoginClick()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Komik") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (uiState is DetailUiState.Success) {
                        val komik = (uiState as DetailUiState.Success).komik
                        IconButton(onClick = { viewModel.toggleFavorite(komik, isFavorite) }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorit",
                                tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is DetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DetailUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.loadComicDetail(endpoint, isCustom) }) {
                            Text("Coba Lagi")
                        }
                    }
                }
                is DetailUiState.Success -> {
                    val comic = state.komik
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                AsyncImage(
                                    model = comic.coverUrl,
                                    contentDescription = comic.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(120.dp)
                                        .aspectRatio(2f / 3f)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = comic.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    val badgeText = if (comic.source == ComicSource.CUSTOM_TRANSLATOR) "ORIGINAL" else "SCRAPER"
                                    val badgeColor = if (comic.source == ComicSource.CUSTOM_TRANSLATOR) Color(0xFF673AB7) else Color(0xFF00897B)
                                    Surface(
                                        color = badgeColor,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = badgeText,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (comic.rating.isNotBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFD700))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = comic.rating, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    if (comic.status.isNotBlank()) {
                                        Text(
                                            text = "Status: ${comic.status}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            if (comic.synopsis.isNotBlank()) {
                                Text(text = "Sinopsis", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = comic.synopsis, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            Text(
                                text = "Daftar Chapter (${comic.chapters.size})",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(comic.chapters) { chapter ->
                            val downloadEntity = downloadedChapters.find { it.chapterEndpoint == chapter.endpoint }
                            val isRead = historyState?.lastChapterEndpoint == chapter.endpoint

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        viewModel.saveHistory(comic, chapter.title, chapter.endpoint)
                                        onChapterClick(chapter.endpoint)
                                    },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = chapter.title,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isRead) {
                                            Text(
                                                text = "Terakhir Dibaca",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    // Download Button per Chapter
                                    when (downloadEntity?.downloadStatus) {
                                        DownloadStatus.COMPLETED -> {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Downloaded",
                                                tint = Color(0xFF4CAF50),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        DownloadStatus.DOWNLOADING -> {
                                            CircularProgressIndicator(
                                                progress = { (downloadEntity.progressPercentage / 100f) },
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        else -> {
                                            IconButton(
                                                onClick = {
                                                    if (isGuest) {
                                                        showGuestDownloadDialog = true
                                                    } else {
                                                        viewModel.startDownload(comic, chapter.endpoint, chapter.title)
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Download,
                                                    contentDescription = "Download Chapter"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
