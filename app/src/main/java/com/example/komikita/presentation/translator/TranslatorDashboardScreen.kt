package com.example.komikita.presentation.translator

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Translator Dashboard Screen (Jetpack Compose).
 *
 * Form untuk Translator mengunggah komik baru ke PostgreSQL:
 * - Input Judul
 * - Input Slug (auto-generate dari judul)
 * - Pilih Tipe (Manga/Manhwa/Manhua)
 * - URL Cover (opsional)
 * - **Bulk Paste**: Paste 50+ URL gambar sekaligus, otomatis terpisah
 * - Preview list URL yang sudah di-paste
 * - Tombol Submit
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorDashboardScreen(
    onBackClick: () -> Unit,
    viewModel: TranslatorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Translator Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        // Jika berhasil submit, tampilkan success screen
        if (state.isSuccess) {
            SuccessContent(
                message = state.successMessage,
                onNewComic = { viewModel.resetForm() },
                onBack = onBackClick,
                modifier = Modifier.padding(paddingValues)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // === JUDUL ===
            item {
                Text(
                    text = "Upload Komik Baru",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Isi data komik dan paste URL gambar di bawah ini.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Input Judul
            item {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = { Text("Judul Komik *") },
                    placeholder = { Text("Contoh: One Piece") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                )
            }

            // Input Slug + tombol auto-generate
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.slug,
                        onValueChange = { viewModel.updateSlug(it) },
                        label = { Text("Slug *") },
                        placeholder = { Text("one-piece") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.autoGenerateSlug() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Auto-generate Slug")
                    }
                }
            }

            // Pilih Tipe
            item {
                val types = listOf("Manga", "Manhwa", "Manhua")
                Text("Tipe Komik:", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    types.forEach { type ->
                        FilterChip(
                            selected = state.type == type,
                            onClick = { viewModel.updateType(type) },
                            label = { Text(type) }
                        )
                    }
                }
            }

            // Cover URL (opsional)
            item {
                OutlinedTextField(
                    value = state.coverUrl,
                    onValueChange = { viewModel.updateCoverUrl(it) },
                    label = { Text("URL Cover (opsional)") },
                    placeholder = { Text("https://...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // === BULK PASTE SECTION ===
            item {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Bulk Paste URL Gambar",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Paste semua URL gambar sekaligus (dipisahkan newline, koma, atau spasi). Mendukung 50+ URL.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Text area untuk bulk paste
            item {
                OutlinedTextField(
                    value = state.bulkPasteText,
                    onValueChange = { viewModel.bulkPasteUrls(it) },
                    label = { Text("Paste URL gambar di sini...") },
                    placeholder = {
                        Text("https://img1.com/page1.jpg\nhttps://img1.com/page2.jpg\n...")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp),
                    maxLines = 10
                )
            }

            // Info jumlah URL yang terdeteksi
            item {
                if (state.imageUrls.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${state.imageUrls.size} URL gambar terdeteksi",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Preview list URL
            if (state.imageUrls.isNotEmpty()) {
                itemsIndexed(state.imageUrls) { index, url ->
                    ImageUrlItem(
                        index = index + 1,
                        url = url,
                        onRemove = { viewModel.removeUrl(index) }
                    )
                }
            }

            // === ERROR MESSAGE ===
            if (state.error != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.error!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // === TOMBOL SUBMIT ===
            item {
                Button(
                    onClick = { viewModel.submitComic() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !state.isSubmitting
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload Komik")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Item URL gambar individual di preview list.
 */
@Composable
fun ImageUrlItem(
    index: Int,
    url: String,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$index.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp)
        )
        Text(
            text = url,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Hapus URL",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Tampilan sukses setelah upload berhasil.
 */
@Composable
fun SuccessContent(
    message: String,
    onNewComic: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Berhasil!",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onNewComic, modifier = Modifier.fillMaxWidth()) {
            Text("Upload Komik Lain")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Kembali ke Home")
        }
    }
}
