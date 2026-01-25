# Penjelasan Teknis Project Komikita
### Panduan Lengkap Konsep, Logika, dan Implementasi

Dokumen ini disusun untuk menjelaskan secara mendetail bagaimana aplikasi **Komikita** dibangun, konsep dasar, teknologi yang digunakan, serta solusi atas tantangan teknis yang dihadapi selama pengembangan.

---

## 1. Pendahuluan

**Komikita** adalah aplikasi Android Native untuk membaca komik (Manga, Manhwa, Manhua) secara online maupun offline. Project ini dibangun dengan standar industri modern untuk memenuhi Tugas Besar Pemrograman Mobile.

### Informasi Project
- **Versi Terbaru**: v1.1.0 (25 Januari 2026)
- **Platform**: Android (Min SDK 24 / Android 7.0)
- **Bahasa**: Kotlin 100%
- **Arsitektur**: MVVM + Repository Pattern

---

## 2. Bahasa Pemrograman: Kotlin 🤖

Aplikasi ini 100% ditulis menggunakan **Kotlin**. Keunggulan utama yang dimanfaatkan:

### A. Modern & Concise
- Menggunakan *ViewBinding* untuk menggantikan `findViewById` yang repot.
- Data classes untuk model response API.
- Extension functions untuk kode yang lebih bersih.

### B. Null Safety
- Mencegah *Force Close* akibat `NullPointerException` dengan manajemen tipe data nullable (`?`).
- Safe call operator (`?.`) dan Elvis operator (`?:`) untuk handling null.

### C. Coroutines & Flow
- Menangani proses *asynchronous* (seperti download file besar atau request API) dengan efisien.
- Tidak memblokir UI Thread.
- `lifecycleScope` untuk operasi yang terikat lifecycle Activity.

---

## 3. Arsitektur Aplikasi: MVVM (Model-View-ViewModel) 🏗️

Pemisahan tanggung jawab kode (Separation of Concerns) diterapkan dengan ketat:

### A. View (Activities/Fragments)
Hanya bertanggung jawab menampilkan data dan menangani interaksi UI:
- `ChapterReaderActivity` - Menampilkan gambar chapter
- `SearchActivity` - Menampilkan hasil pencarian
- `KomikDetailActivity` - Detail komik dan daftar chapter
- `ProfileActivity` - Pengaturan user dan Dark Mode

### B. ViewModel
- Menyimpan state UI agar data tidak hilang saat rotasi layar.
- Menggunakan `LiveData`/`StateFlow` untuk memberi tahu View jika ada data baru.
- Menangani business logic.

### C. Repository
- **Single Source of Truth** - Memutuskan apakah akan mengambil data dari Server (Online) atau Room Database (Offline).
- Abstraksi data source dari ViewModel.

### Alur Data
```
API/DB ➡ Repository ➡ ViewModel ➡ Activity ➡ User Interface
```

---

## 4. Teknologi & Stack Teknis 🛠️

### A. Networking (API) - Retrofit
- Menggunakan **Retrofit 2** untuk komunikasi dengan REST API.
- **Gson** sebagai converter JSON ke Object Kotlin.
- **OkHttp Interceptors** untuk logging dan header handling.
- Base URL: `https://ws.asepharyana.tech/api/komik/`

### B. Local Database - Room
Menyimpan data secara lokal untuk fitur offline:

| Tabel | Fungsi |
|-------|--------|
| `UserEntity` | Data user yang terdaftar |
| `FavoriteEntity` | Daftar komik favorit user |
| `DownloadEntity` | Metadata chapter yang didownload |
| `ReadHistoryEntity` | Riwayat baca user |

**DAO (Data Access Object)** menangani query SQL yang kompleks secara aman dengan suspend functions untuk Coroutines.

### C. Image Loading - Glide
- Menangani caching gambar secara agresif untuk performa scrolling yang mulus.
- Placeholder & Error handling otomatis.
- Mendukung loading dari URL (online) dan File (offline).

### D. Background Services
- **DownloadManager**: Menangani pengunduhan file chapter di latar belakang.
- **BroadcastReceiver**: Mendengarkan event ketika download selesai.
- **ImageDownloader**: Custom utility untuk download batch gambar.

---

## 5. Sistem Tema (Dark Mode / Light Mode) 🎨

### A. Konfigurasi Tema
Aplikasi menggunakan `Theme.MaterialComponents.DayNight.NoActionBar` yang mendukung pergantian tema otomatis:

```xml
<!-- values/themes.xml (Light Mode) -->
<style name="Base.Theme.Komikita" parent="Theme.MaterialComponents.DayNight.NoActionBar">

<!-- values-night/themes.xml (Dark Mode) -->
<style name="Base.Theme.Komikita" parent="Theme.MaterialComponents.DayNight.NoActionBar">
```

### B. Konsistensi Style
Semua komponen Material menggunakan style `MaterialComponents`, bukan `Material3`:
- `Widget.MaterialComponents.TextInputLayout.OutlinedBox`
- `Widget.MaterialComponents.Button.OutlinedButton`
- `Widget.MaterialComponents.Chip.Filter`
- `Widget.MaterialComponents.Button.TextButton`

### C. Dark Mode Toggle
Toggle di ProfileActivity menggunakan deteksi tema aktual:
```kotlin
val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
val isCurrentlyDark = currentNightMode == Configuration.UI_MODE_NIGHT_YES
binding.switchDarkMode.isChecked = isCurrentlyDark
```

### D. BaseActivity (Theme Management)
Semua Activity mewarisi `BaseActivity` yang mengatur Status Bar dan Navigation Bar:
```kotlin
if (isDarkMode) {
    // DARK MODE: Black Status Bar, White Icons
    window.statusBarColor = Color.BLACK
    WindowInsetsControllerCompat(window, decorView).isAppearanceLightStatusBars = false
} else {
    // LIGHT MODE: White Status Bar, Black Icons
    window.statusBarColor = Color.WHITE
    WindowInsetsControllerCompat(window, decorView).isAppearanceLightStatusBars = true
}
```

---

## 6. Logika Sistem & Fitur Unggulan (Deep Dive) 🧠

### A. Logika Login & Register (Secure)

1. **Validasi Input**: Email regex & password length check.
2. **Hashing**: Password di-hash (SHA-256) + Salt sebelum disimpan.
3. **Session Persistence**: Login statis disimpan aman di SharedPreferences/Room.

```kotlin
fun hashPassword(password: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}
```

### B. Logika Real Offline Download

Sistem ini memungkinkan user membaca tanpa kuota:

#### Download Flow
```
User klik Download 
    ➡ API Get Image List 
    ➡ ImageDownloader save ke Storage 
    ➡ Update DownloadEntity di Room
```

#### Offline Reader Flow
```
User buka chapter offline
    ➡ Sistem cek isOfflineMode = true
    ➡ Load gambar dari localPath (Storage HP)
    ➡ Zero Loading Time (file lokal)
```

### C. Logika Navigasi Chapter (Smart Navigation)

#### Mode Online
Navigasi berdasarkan `chapterIds` list yang dikirim dari `KomikDetailActivity`:
```kotlin
private fun calculateNavigation() {
    val currentIndex = chapterIds.indexOf(currentChapterId)
    if (currentIndex != -1) {
        hasPrev = currentIndex < chapterIds.size - 1  // Ada chapter sebelumnya
        hasNext = currentIndex > 0                      // Ada chapter selanjutnya
        prevChapterId = if (hasPrev) chapterIds[currentIndex + 1] else null
        nextChapterId = if (hasNext) chapterIds[currentIndex - 1] else null
    }
}
```

#### Mode Offline
Navigasi berdasarkan data yang dikirim dari `DownloadChaptersActivity`:
```kotlin
// Intent extras dari DownloadChaptersActivity
offlinePrevChapterId = intent.getStringExtra("OFFLINE_PREV_CHAPTER_ID")
offlinePrevLocalPath = intent.getStringExtra("OFFLINE_PREV_LOCAL_PATH")
offlineNextChapterId = intent.getStringExtra("OFFLINE_NEXT_CHAPTER_ID")
offlineNextLocalPath = intent.getStringExtra("OFFLINE_NEXT_LOCAL_PATH")
```

#### Visibility Logic
```kotlin
private fun updateNavigationButtons(enablePrev: Boolean, enableNext: Boolean) {
    binding.btnPrev.visibility = if (enablePrev) View.VISIBLE else View.GONE
    binding.btnNext.visibility = if (enableNext) View.VISIBLE else View.GONE
    
    // Mode offline: sembunyikan refresh button
    binding.btnRefresh.visibility = if (isOfflineMode) View.GONE else View.VISIBLE
    
    // Jika offline dan hanya 1 chapter, sembunyikan navigation bar
    if (isOfflineMode && !enablePrev && !enableNext) {
        binding.navigationContainer.visibility = View.GONE
    }
}
```

---

## 7. Tantangan Pengembangan & Solusi 💡

### A. Crash "InflateException" pada TextInputLayout

**Masalah**: Aplikasi crash saat membuka SearchActivity dengan error:
```
Error inflating class com.google.android.material.textfield.TextInputLayout
Caused by: java.lang.IllegalArgumentException: This component requires that you 
specify a valid TextAppearance attribute. Update your app theme to inherit from 
Theme.MaterialComponents
```

**Penyebab**: Menggunakan style `Widget.Material3.*` dengan tema `Theme.MaterialComponents`.

**Solusi**: Mengganti semua style Material3 ke MaterialComponents:
- `Widget.Material3.TextInputLayout.OutlinedBox` ➡ `Widget.MaterialComponents.TextInputLayout.OutlinedBox`
- `Widget.Material3.Chip.Filter` ➡ `Widget.MaterialComponents.Chip.Filter`
- `Widget.Material3.Button.OutlinedButton` ➡ `Widget.MaterialComponents.Button.OutlinedButton`
- `Widget.Material3.Button.TextButton` ➡ `Widget.MaterialComponents.Button.TextButton`

**File yang diperbaiki**:
- `activity_search.xml` (8 perubahan)
- `activity_login.xml` (3 perubahan)
- `activity_register.xml` (5 perubahan)
- `activity_setup_info.xml` (2 perubahan)

### B. Dark Mode Toggle Tidak Sesuai

**Masalah**: Toggle Dark Mode tidak menunjukkan posisi yang benar saat app dibuka. Misalnya sistem dalam dark mode tapi toggle menunjukkan OFF.

**Penyebab**: Logika lama hanya mengecek preference yang tersimpan, bukan kondisi tema aktual.

**Solusi**: Menggunakan deteksi tema aktual dari `Configuration`:
```kotlin
private fun setupDarkMode() {
    // Cek kondisi tema saat ini (bukan preference, tapi kondisi aktual)
    val currentNightMode = resources.configuration.uiMode and 
        android.content.res.Configuration.UI_MODE_NIGHT_MASK
    val isCurrentlyDark = currentNightMode == 
        android.content.res.Configuration.UI_MODE_NIGHT_YES
    
    // Set switch sesuai kondisi tema aktual saat ini
    binding.switchDarkMode.isChecked = isCurrentlyDark
}
```

### C. Navigasi Chapter Tidak Akurat (Online Mode)

**Masalah**: Tombol Next/Prev muncul padahal tidak ada chapter selanjutnya. API mengembalikan `next_chapter_id` yang tidak valid.

**Penyebab**: API response tidak reliable - terkadang mengembalikan ID chapter yang sama untuk `next_chapter_id` dan `prev_chapter_id`.

**Solusi**: 
1. Mengirim `CHAPTER_IDS` array dari `KomikDetailActivity` ke `ChapterReaderActivity`.
2. Menghitung posisi current chapter dalam array.
3. Menentukan `hasPrev`/`hasNext` berdasarkan index, bukan API response.

```kotlin
// Di KomikDetailActivity - kirim chapter IDs
val chapterIds = chapterListData.map { it.slug }.toTypedArray()
intent.putExtra("CHAPTER_IDS", chapterIds)

// Di ChapterReaderActivity - gunakan untuk navigasi
chapterIds = intent.getStringArrayExtra("CHAPTER_IDS")?.toList() ?: emptyList()
calculateNavigation()
```

### D. Status Bar Overlap (UI "Bentrok")

**Masalah**: Tampilan toolbar menabrak status bar di beberapa halaman.

**Solusi**: 
- Mengimplementasikan `android:fitsSystemWindows="true"` pada root layout.
- Mengimplementasikan **Dynamic Theme Detection** di `BaseActivity`.
- Status bar selalu kontras dan terbaca (jam/baterai tidak pernah hilang).

---

## 8. Struktur File Penting 📁

### Activities
| File | Fungsi |
|------|--------|
| `DashboardActivity.kt` | Halaman utama dengan daftar komik |
| `KomikDetailActivity.kt` | Detail komik dan daftar chapter |
| `ChapterReaderActivity.kt` | Membaca chapter (online/offline) |
| `SearchActivity.kt` | Pencarian dan filter komik |
| `ProfileActivity.kt` | Pengaturan user dan Dark Mode |
| `DownloadsActivity.kt` | Daftar komik yang didownload |
| `DownloadChaptersActivity.kt` | Daftar chapter yang didownload per komik |
| `LoginActivity.kt` | Halaman login |
| `RegisterActivity.kt` | Halaman registrasi |
| `BaseActivity.kt` | Parent class untuk theme management |

### Data Layer
| File | Fungsi |
|------|--------|
| `AppDatabase.kt` | Room Database configuration |
| `Daos.kt` | Data Access Objects (query functions) |
| `Entities.kt` | Room Entity classes |
| `ApiService.kt` | Retrofit API interface |
| `KomikRepository.kt` | Repository pattern implementation |

### Layouts
| File | Fungsi |
|------|--------|
| `activity_chapter_reader.xml` | Layout reader dengan navigasi |
| `activity_search.xml` | Layout search dengan filter chips |
| `activity_login.xml` | Form login |
| `activity_register.xml` | Form registrasi |
| `activity_profile.xml` | Layout profil dengan Dark Mode toggle |

---

## 9. Langkah Instalasi & Build 📝

### Menggunakan APK (Recommended)
1. Download [Komikita.apk](releases/Komikita.apk)
2. Enable "Install from Unknown Sources" di pengaturan Android
3. Install dan jalankan

### Build dari Source
1. Clone repository
2. Buka dengan Android Studio (Ladybug/Jellyfish atau lebih baru)
3. Sync Gradle
4. Pastikan device/emulator memiliki koneksi internet untuk first run
5. Build & Run (`Shift+F10`)

---

## 10. Kesimpulan

Aplikasi **Komikita** adalah implementasi nyata dari konsep **Full Stack Mobile Development** (sisi klien). Project ini mengajarkan:

1. **Arsitektur Bersih**: MVVM + Repository Pattern untuk kode yang maintainable.
2. **Offline First**: Strategi caching dan local storage untuk UX yang baik.
3. **Theme Management**: Implementasi Dark Mode yang konsisten.
4. **Error Handling**: Graceful degradation saat tidak ada internet.
5. **Material Design**: UI/UX yang modern dan familiar bagi pengguna Android.

---

## 11. Referensi

- [Android Developers Documentation](https://developer.android.com/)
- [Material Design Guidelines](https://material.io/design)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [Retrofit Documentation](https://square.github.io/retrofit/)

---

*Dokumen diperbarui terakhir: 25 Januari 2026*  
*Versi Aplikasi: v1.1.0*
