package com.komikita.app.presentation.translator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorDashboardScreen(
    viewModel: TranslatorViewModel,
    onPublishSuccess: () -> Unit
) {
    val rawText by viewModel.rawText.collectAsState()
    val parsedUrls by viewModel.parsedUrls.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var title by remember { mutableStateOf("") }
    var coverUrl by remember { mutableStateOf("") }
    var synopsis by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("MANHWA") }
    var author by remember { mutableStateOf("") }
    var chapterTitle by remember { mutableStateOf("Chapter 1") }
    var overriddenScraperSlug by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Translator Komik", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Upload / Terbitkan Komik Hasil Terjemahan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Komik") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = coverUrl,
                    onValueChange = { coverUrl = it },
                    label = { Text("URL Gambar Cover") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Penulis / Author / Translator") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = overriddenScraperSlug,
                    onValueChange = { overriddenScraperSlug = it },
                    label = { Text("Scraper Slug to Hide/Override (Opsional)") },
                    placeholder = { Text("misal: solo-leveling") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = synopsis,
                    onValueChange = { synopsis = it },
                    label = { Text("Sinopsis Komik") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = chapterTitle,
                    onValueChange = { chapterTitle = it },
                    label = { Text("Judul Chapter Initial") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Bulk Paste URL Halaman Chapter (Pisahkan dengan baris baru atau koma):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { viewModel.updateRawText(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    placeholder = {
                        Text(
                            "https://11.shinigami.asia/image1.jpg\n" +
                                    "https://11.shinigami.asia/image2.jpg,\n" +
                                    "https://11.shinigami.asia/image3.jpg"
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "URL Valid Ditemukan: ${parsedUrls.size} Halaman",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            itemsIndexed(parsedUrls) { index, url ->
                Text(
                    text = "${index + 1}. $url",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                when (val state = uiState) {
                    is TranslatorUiState.Publishing -> {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is TranslatorUiState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    is TranslatorUiState.Success -> {
                        Text(
                            text = "Berhasil Menerbitkan Komik: ${state.publishedComic.title}!",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    else -> {}
                }

                Button(
                    onClick = {
                        viewModel.publishComic(
                            title = title,
                            coverUrl = coverUrl,
                            synopsis = synopsis,
                            type = type,
                            author = author,
                            chapterTitle = chapterTitle,
                            overriddenScraperSlug = overriddenScraperSlug
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is TranslatorUiState.Publishing
                ) {
                    Text("Terbitkan Komik Hasil Terjemahan")
                }
            }
        }
    }
}
