# Komikita - Aplikasi Baca Komik Modern

> **Status: Dalam Pengembangan Aktif** — Aplikasi ini masih dalam tahap pengembangan dan belum dirilis secara resmi.

**Developer**: Muhammad Rizal Nurfirdaus

**Komikita** adalah aplikasi Android native untuk membaca komik digital (Manga, Manhwa, Manhua) yang dibangun dengan arsitektur modern **Clean Architecture + Jetpack Compose + Hilt**. Aplikasi ini menggunakan sistem data hybrid yang menggabungkan **Scraper API** (konten publik read-only) dan **PostgreSQL** melalui REST Backend API (komik custom dari Translator), dengan **Firebase** khusus untuk autentikasi Google Sign-In.

🔗 **Repository**: [github.com/MuhammadRizalNurfirdaus/Komikita](https://github.com/MuhammadRizalNurfirdaus/Komikita)

---

## 🏗️ Arsitektur

```
┌─────────────────────────────────────────────────────────┐
│                   Presentation Layer                     │
│  Jetpack Compose + Material 3 + ViewModel (StateFlow)   │
│  HomeScreen, DetailScreen, ReaderScreen, SearchScreen   │
│  ProfileScreen, HistoryScreen, FavoritesScreen, etc.    │
├─────────────────────────────────────────────────────────┤
│                     Domain Layer                         │
│  Use Cases · Repository Interfaces · Domain Models      │
│  GetHomeFeedUseCase · GetChapterPagesUseCase · etc.     │
├─────────────────────────────────────────────────────────┤
│                      Data Layer                          │
│  Retrofit APIs · Room DB · Repositories · DTO Mappers   │
│  ScraperApi (read-only) · BackendApi (PostgreSQL)       │
├─────────────────────────────────────────────────────────┤
│                      DI Layer (Hilt)                     │
│  NetworkModule · DatabaseModule · RepositoryModule      │
└─────────────────────────────────────────────────────────┘
```

### Sistem Data Hybrid

Aplikasi menggabungkan 2 sumber data menjadi satu feed terpadu:

| Sumber | Sifat | Database | Keterangan |
|--------|-------|----------|------------|
| **Scraper API** | Read-only, publik | - | Konten Manga/Manhwa/Manhua dari `scraper.asepharyana.my.id` |
| **Backend API** | Full CRUD, auth | PostgreSQL (Aiven) | Komik custom yang diupload oleh Translator |

Data dari kedua sumber digabungkan secara paralel menggunakan `coroutineScope { async {} }`, kemudian di-merge di `KomikRepositoryImpl` dengan komik custom muncul lebih dulu.

### Sistem 3 Aktor

| Role | Kemampuan |
|------|-----------|
| **Admin** | Kelola user, kelola sistem, kelola konten |
| **Translator** | Upload komik custom (bulk paste 50+ URL), hide/unhide komik scraper |
| **User** | Baca komik, riwayat baca, favorit, download offline |

---

## 🔧 Technology Stack

| Komponen | Teknologi |
|----------|-----------|
| **Language** | Kotlin 100% |
| **Architecture** | Clean Architecture + MVVM |
| **UI Framework** | Jetpack Compose + Material 3 |
| **DI** | Hilt (Dagger) |
| **Image Loading** | Coil 2.x |
| **Networking** | Retrofit 2 + OkHttp 4 (dual client) |
| **Auth** | Firebase Auth (Google Sign-In only) |
| **Local DB** | Room Database v4 |
| **Remote DB** | PostgreSQL via REST Backend API |
| **Async** | Kotlin Coroutines + Flow/StateFlow |
| **Navigation** | Compose Navigation (type-safe routes) |
| **Build** | Gradle Kotlin DSL, KSP |

### Dual OkHttp Client

Aplikasi menggunakan 2 `OkHttpClient` terpisah untuk isolasi keamanan:

- **`scraperClient`** — tanpa Auth Interceptor (endpoint publik, tidak butuh token)
- **`backendClient`** — dengan `AuthInterceptor` (otomatis inject JWT Bearer token dari Room DB, handle 401 centrally)

---

## ✨ Fitur Utama

### Autentikasi & Sesi
- **Splash Screen** — pengecekan sesi asinkron saat app launch
  - Sesi aktif (login/guest) → langsung ke Home
  - Belum ada sesi → Login Screen
- **Google Sign-In** via Firebase Auth — tanpa password lokal
- **Guest Mode** — masuk tanpa akun dengan batasan:
  - ✅ Bisa: Browse, Search, Baca komik
  - ❌ Tidak bisa: Favorit, Riwayat, Download (ditampilkan prompt login)
- **JWT Token** dari backend disimpan di Room DB
- **AuthInterceptor** otomatis inject `Authorization: Bearer <token>` ke semua request backend
- **Penanganan 401** terpusat — token otomatis dihapus jika expired

### Home Feed (Hybrid)
- Grid komik dari Scraper API + Backend API digabung paralel
- Komik custom (Translator) muncul lebih dulu
- Admin/Translator bisa hide komik scraper tertentu
- LazyVerticalGrid dengan Coil image loading

### Reader
- Vertical scroll reading dengan Coil AsyncImage
- Tap-to-toggle kontrol (show/hide navigation overlay)
- Smart chapter navigation (Next/Prev hanya muncul jika tersedia)
- Riwayat baca otomatis tersimpan ke Room DB

### Translator Dashboard
- Bulk upload komik dengan paste 50+ URL gambar sekaligus
- Auto-generate slug dari judul
- Kelola komik custom (tambah chapter, hapus)
- Hide/unhide komik scraper yang tidak sesuai

### Tema & Pengaturan
- **Mode Tema 3-Opsi**: Ikuti Sistem / Mode Terang / Mode Gelap
- Perubahan tema global — status bar, navigation bar, dan seluruh UI berubah bersamaan
- Preferensi tersimpan di SharedPreferences, bertahan setelah app restart
- `ThemeManager` (Hilt Singleton) + `AppCompatDelegate.setDefaultNightMode()`
- Hapus cache aplikasi

### Profil
- Foto profil, nama, email dari Google
- Role badge berwarna (Admin=Merah, Translator=Oranye, User=Biru)
- Menu akses cepat ke Favorit, Riwayat, Pengaturan
- Tombol Translator Dashboard (muncul hanya untuk Translator/Admin)
- **Guest state**: Banner "Mode Tamu" + daftar fitur tersedia/terkunci + tombol login

### Favorit & Riwayat
- Bookmark komik ke favorit (disimpan lokal di Room)
- Riwayat baca otomatis dengan timestamp
- Hapus per-item atau hapus semua

### Bottom Navigation
- 4 tab: Home, Favorit, Riwayat, Profil
- Auto-hide di halaman detail/reader/search
- State restoration saat berpindah tab

---

## 📂 Struktur Package

```
app/src/main/java/com/example/komikita/
├── KomikitaApplication.kt              # @HiltAndroidApp
├── MainActivity.kt                      # @AndroidEntryPoint, Compose entry
│
├── domain/                              # Clean Architecture - Domain Layer
│   ├── model/                           # Komik, KomikDetail, ChapterPages, User, dll.
│   ├── repository/                      # Interface (kontrak) untuk repository
│   └── usecase/                         # GetHomeFeedUseCase, SearchKomikUseCase, dll.
│
├── data/                                # Clean Architecture - Data Layer
│   ├── api/                             # ScraperApi.kt, BackendApi.kt, AuthInterceptor.kt
│   ├── model/                           # DTO (ScraperDto, BackendDto)
│   ├── local/                           # Room DB, Entity, DAO
│   ├── repository/                      # Implementasi repository (KomikRepositoryImpl, dll.)
│   └── mapper/                          # DTO-to-Domain mapper (Mappers.kt)
│
├── di/                                  # Hilt DI Modules
│   ├── NetworkModule.kt                 # Dual OkHttp client + Retrofit
│   ├── DatabaseModule.kt                # Room DB + DAOs
│   └── RepositoryModule.kt             # Interface → Impl bindings
│
├── presentation/                        # Clean Architecture - Presentation Layer
│   ├── theme/                           # Material 3 colors, auto dark/light
│   ├── navigation/                      # Screen routes + NavHost + BottomBar
│   ├── components/                      # Shared UI components
│   ├── home/                            # HomeScreen + HomeViewModel
│   ├── detail/                          # DetailScreen
│   ├── reader/                          # ReaderScreen + ReaderViewModel
│   ├── search/                          # SearchScreen
│   ├── profile/                         # ProfileScreen + ProfileViewModel
│   ├── history/                         # HistoryScreen + HistoryViewModel
│   ├── favorites/                       # FavoritesScreen + FavoritesViewModel
│   ├── settings/                        # SettingsScreen + ThemeManager integration
│   ├── splash/                          # SplashScreen + SplashViewModel (session check)
│   ├── auth/                            # LoginScreen (Google Sign-In + Guest Mode)
│   └── translator/                      # TranslatorDashboardScreen + ViewModel
│
└── util/                                # Legacy helpers (akan dimigrasi)
```

---

## 🔄 Changelog

### v2.1.0 — Session Flow, Guest Mode & Theme System (Juni 2026) `[Dalam Pengembangan]`

**Sistem sesi dan manajemen tema:**

- **Splash Screen**: Pengecekan sesi asinkron saat app launch → redirect otomatis ke Home atau Login
- **SessionState**: Sealed class (`LoggedIn`, `Guest`, `NotLoggedIn`, `Loading`) untuk manajemen state sesi
- **Guest Mode**: Masuk tanpa akun Google — bisa browse & baca, tapi fitur favorit/riwayat/download terkunci dengan prompt login
- **Login Screen**: 2 tombol — "Masuk dengan Google" (Firebase Auth) + "Masuk sebagai Tamu" (Guest Mode)
- **ThemeManager**: Singleton Hilt dengan 3 mode tema (Ikuti Sistem / Mode Terang / Mode Gelap)
- **AppCompatDelegate**: Sinkronisasi tema global — status bar, navigation bar, dan Compose UI berubah bersamaan
- **Guest-aware screens**: ProfileScreen, HistoryScreen, FavoritesScreen menampilkan state berbeda untuk guest
- **Persistent session**: SharedPreferences menyimpan flag guest mode dan preferensi tema

### v2.0.0 — Clean Architecture Rewrite (Juni 2026)

**Perubahan besar-besaran pada arsitektur dan teknologi:**

- **Migrasi UI**: XML Layouts → **Jetpack Compose** + Material 3
- **Migrasi DI**: Manual → **Hilt** (Dagger)
- **Migrasi Image Loader**: Glide → **Coil 2.x**
- **Clean Architecture**: Pisahnya kode menjadi 3 layer (Domain, Data, Presentation)
- **Hybrid Data System**: Penggabungan Scraper API + Backend API (PostgreSQL) secara paralel
- **AuthInterceptor**: JWT token otomatis di-inject ke semua request backend, penanganan 401 terpusat
- **Dual OkHttp Client**: `scraperClient` (tanpa auth) dan `backendClient` (dengan auth)
- **Bottom Navigation**: 4 tab (Home, Favorit, Riwayat, Profil) dengan state restoration
- **Screen Baru**: ProfileScreen, HistoryScreen, FavoritesScreen, SettingsScreen, LoginScreen
- **FavoriteRepository**: Sistem bookmark komik dengan Room DB
- **Role System**: 3 aktor (Admin, Translator, User) dengan UI berbeda
- **Translator Dashboard**: Bulk upload komik dengan paste 50+ URL
- **Room DB v4**: Schema baru (users, favorites, downloads, history)

### v1.1.0 — Bug Fix & UI Improvements (Januari 2026)

- Fix crash SearchActivity (TextInputLayout style compatibility)
- Fix crash Dark Mode toggle
- Konsistensi theme MaterialComponents
- Smart chapter navigation (Next/Prev conditional)

### v1.0.0 — Initial Release

- Aplikasi pembaca komik dengan XML Layout + MVVM
- Login/Register lokal, Dashboard, Detail, Reader
- Download offline, Favorites, History, Profile
- Dark/Light mode support

---

## 🎨 Design Reference

- **Warna Utama**: `#FF6B35` (Brand Orange)
- **Tema**: Material 3, 3-mode (Ikuti Sistem / Mode Terang / Mode Gelap) via `ThemeManager` + `AppCompatDelegate`
- **Status Bar**: Dinamis (adaptasi Light/Dark mode)
- **Navigasi**: Bottom Navigation Bar (tab) + Compose Navigation (push screens)

🔗 **Figma Design**: [Komikita UI Design](https://www.figma.com/design/WhCaIxb9lESuLnuTMszFR8/Komikita?node-id=0-1&p=f&t=ZAtUXDHCXta4IAqz-0)

---

## 🔒 Keamanan

- **PostgreSQL tidak pernah diakses langsung dari Android** — selalu melalui REST Backend API
- **JWT Auth**: Token disimpan di Room DB, di-inject otomatis oleh AuthInterceptor
- **Role Check**: Fitur Translator/Admin diverifikasi di client-side (UseCase) dan server-side (Backend)
- File sensitif tidak di-upload ke repository: `google-services.json`, `local.properties`, `*.jks`

---

## 🚀 Getting Started

```bash
# Clone
git clone https://github.com/MuhammadRizalNurfirdaus/Komikita.git

# Buka di Android Studio (versi terbaru)
# Sync Gradle → Run (Shift+F10)
```

**Requirements**:
- Android Studio Ladybug atau lebih baru
- JDK 17+
- Min SDK 26 (Android 8.0)
- Compile SDK 36

---

*Terakhir diupdate: Juni 2026 — Dalam tahap pengembangan aktif*
