# Komikita - Comic Reading App

Komikita adalah aplikasi Android untuk membaca komik (Manga, Manhwa, Manhua) dengan fitur lengkap seperti login dengan Google, favorit, download, dark mode, dan lainnya.

## 📱 Fitur Utama

### Autentikasi
- **Login dengan Google** - Sign in menggunakan akun Google yang reliable
- **Register** - Wajib lengkapi profil dengan nama (minimal 3 karakter) dan foto opsional
- **Mode Guest** - Akses aplikasi tanpa login
- **Auto Login** - Otomatis masuk jika sudah login sebelumnya

### Halaman Utama (16+ Activities)
1. **Splash Screen** - Logo Komikita dengan animasi loading
2. **Dashboard** - Halaman utama dengan daftar komik terbaru  
3. **Search** - Pencarian dengan filter kategori (Manga/Manhwa/Manhua) dan genre
4. **Favorites** - Daftar komik favorit (dengan tombol back)
5. **Downloads** - Manajemen komik yang diunduh (dengan tombol back)
6. **Profile & Settings** - Pengaturan profil, dark mode, dan logout

### Browsing Komik
- **Manga List** - Daftar komik Jepang dengan pagination
- **Manhwa List** - Daftar komik Korea dengan pagination
- **Manhua List** - Daftar komik China dengan pagination
- **Genre Browser** - Filter berdasarkan genre (Action, Romance, Fantasy, Comedy)
- **Search dengan Kategori** - Pilih kategori dan cari berdasarkan genre

### Membaca Komik
- **Detail Page** - Informasi lengkap komik, sinopsis, chapters
- **Chapter Reader** - Baca chapter dengan viewer gambar
- **Add to Favorites** - Tandai komik favorit
- **Download Chapter** - Simpan chapter untuk dibaca offline

### Profile & Settings
- **Dark Mode / Light Mode** - Toggle mode gelap/terang
- **Edit Profile** - Ubah nama dan foto profil
- **Logout** - Sign out dari Google dan hapus data lokal

## 🎨 Logo & Branding

- **Logo**: Menggunakan gambar Komikita yang menampilkan buku terbuka dengan karakter komik
- **App Icon**: Logo Komikita di semua resolusi (mdpi sampai xxxhdpi)
- **Splash Screen**: Logo besar dengan animasi 2.5 detik
- **Color Scheme**: Orange (#FF6B35) & Blue (#004E89)

## 🔌 API Configuration

**Base URL**: `https://ws.asepharyana.tech/`

### Endpoints
- `GET /api/komik/search?query={query}` - Search komik
- `GET /api/komik/manga?page={page}` - List manga
- `GET /api/komik/manhwa?page={page}` - List manhwa  
- `GET /api/komik/manhua?page={page}` - List manhua
- `GET /api/komik/detail?komik_id={id}` - Detail komik
- `GET /api/komik/chapter?chapter_url={url}` - Baca chapter

## ⚙️ Setup & Configuration

### 1. Clone Repository
```bash
git clone <repository-url>
cd Komikita
```

### 2. Configure API (Already Set)
API sudah dikonfigurasi ke: `https://ws.asepharyana.tech/`

### 3. Google Sign-In (Already Configured)
- ✅ Google Services JSON sudah ada di `app/google-services.json`
- ✅ Client ID: `885636086964-jtlur82q4n9fls3kq28ld865d9le1c3s.apps.googleusercontent.com`
- ✅ Plugin Google Services sudah aktif

### 4. Build & Run
```bash
./gradlew assembleDebug
```

APK Location: `app/build/outputs/apk/debug/app-debug.apk`

## 🎯 Fitur Unggulan yang Sudah Diperbaiki

### ✅ Search yang Sempurna
- Auto load komik manga saat pertama buka
- Filter kategori: Manga, Manhwa, Manhua (single selection)
- Filter genre: Action, Romance, Fantasy, Comedy
- Real-time search dengan debounce
- Pagination support

### ✅ Navigation yang Benar
- Tombol back (←) di pojok kiri atas berfungsi di:
  - Favorites Activity
  - Downloads Activity  
  - Profile Activity
  - Search Activity

### ✅ Logo & Branding Konsisten
- Logo Komikita asli di splash screen (250x250dp)
- App launcher icon menggunakan logo Komikita
- Ukuran responsive untuk semua device

### ✅ Google Sign-In yang Reliable
- Error handling yang lengkap (error code 12501, 12500, dll)
- Validasi email & account data
- User-friendly error messages
- Auto redirect ke register setelah sign in

### ✅ Registrasi yang Ketat
- **Wajib isi email** (validasi not blank)
- **Wajib isi nama** (minimal 3 karakter)
- Foto profil opsional
- Disable button setelah submit (prevent double submission)
- Success message dengan nama user
- Langsung masuk ke dashboard setelah register

### ✅ Profile & Settings Lengkap
- Tampilan user info (nama, email, foto dari Google)
- **Dark Mode Toggle** - Switch antara light & dark mode
- Edit Profile button (coming soon feature)
- **Logout Button** - Sign out dari Google + clear local data
- Redirect ke login setelah logout

### ✅ Auto Login
- Check Google Sign-In status di splash
- Check local database untuk user data
- Auto navigate ke dashboard jika sudah login
- Navigate ke login jika belum login

## 📦 Dependencies

### Networking & Data
- Retrofit 2.11.0
- OkHttp 4.12.0
- Gson 2.10.1

### Database
- Room 2.6.1
- KSP 2.0.21-1.0.25

### UI & Image
- Material Components 1.10.0
- Glide 4.16.0
- RecyclerView 1.3.2

### Authentication
- Google Play Services Auth 21.2.0
- Google Services Plugin 4.4.0

### Async
- Kotlin Coroutines 1.8.1
- Lifecycle ViewModel 2.7.0

## 📝 Recent Updates (December 21, 2025)

### ✅ LOGIN & REGISTER FLOW FIXED!

**Flow Baru yang Benar:**
- Login berhasil → Cek database
  - User sudah terdaftar? → **Langsung ke Dashboard** ✅
  - User baru? → Ke Register screen → Isi form → **Auto ke Dashboard** ✅
- Email verification message ditampilkan di Register screen
- Welcome back message untuk user yang sudah pernah login
- Database auto-check untuk skip register jika sudah terdaftar

### Database Changes
✅ UserEntity tambah field `isEmailVerified`  
✅ UserDao tambah method `getUserByEmail()`  
✅ Database version upgrade ke v2 dengan auto-migration  

### UI Improvements
✅ Email verification info box di Register screen  
✅ Toast message "Registrasi berhasil!" dengan emoji  
✅ Welcome back message untuk returning users  

### Search Improvements
✅ Auto load manga saat pertama buka search  
✅ Kategori filter chips (Manga/Manhwa/Manhua)  
✅ Genre filter chips (Action/Romance/Fantasy/Comedy)  
✅ Fix empty list issue

### Navigation Fixes
✅ Back button di Favorites Activity  
✅ Back button di Downloads Activity  
✅ Proper toolbar setup dengan navigation icon

### Logo & Branding
✅ Replace app icon dengan logo Komikita  
✅ Update splash screen dengan logo besar (250dp)  
✅ Remove duplicate XML vector drawable

### Authentication
✅ Improved Google Sign-In error handling  
✅ Better validation messages  
✅ Auto login check di splash screen

### Registration
✅ Required field validation (email & nama)  
✅ Minimum length validation (3 characters)  
✅ Prevent double submission  
✅ Success message dengan nama user

### Profile & Settings
✅ **NEW**: Dark Mode / Light Mode toggle  
✅ **NEW**: Logout functionality  
✅ Display user info dari Google account  
✅ Proper navigation dan back button

## 🚀 How to Use

1. **First Launch**: Splash screen akan cek login status
2. **Login**: Pilih "Sign in with Google" atau "Continue as Guest"  
3. **Register**: Isi nama (min 3 karakter), foto opsional, tekan Complete Registration
4. **Dashboard**: Browse manga/manhwa/manhua, gunakan bottom navigation
5. **Search**: Pilih kategori (Manga/Manhwa/Manhua), filter by genre, atau search text
6. **Read**: Tap komik → lihat detail → pilih chapter → baca
7. **Settings**: Ke Profile → toggle Dark Mode → Logout jika perlu

## 📄 License

Project ini dibuat untuk keperluan pembelajaran dan portfolio.

## 👨‍💻 Developer

Rizal - Mobile Developer

---

**Komikita** - Baca komik favorit kamu dimana saja! 📚✨

**Last Updated**: December 21, 2025  
**Version**: 1.0  
**Build**: Successful ✅

## 📱 Fitur Utama

### Autentikasi
- **Login dengan Google** - Sign in menggunakan akun Google
- **Register** - Lengkapi profil dengan nama dan foto (opsional)
- **Mode Guest** - Akses aplikasi tanpa login

### Halaman Utama
1. **Dashboard** - Halaman utama dengan daftar komik terbaru
2. **Search** - Pencarian komik dengan filter genre/kategori
3. **Favorites** - Daftar komik favorit
4. **Downloads** - Manajemen komik yang diunduh
5. **Profile** - Pengaturan profil pengguna

### Browsing Komik
- **Manga List** - Daftar komik Jepang
- **Manhwa List** - Daftar komik Korea
- **Manhua List** - Daftar komik China
- **Genre Browser** - Telusuri berdasarkan genre
- **Reading History** - Riwayat bacaan

### Membaca Komik
- **Detail Page** - Informasi lengkap komik, sinopsis, chapters
- **Chapter Reader** - Baca chapter dengan viewer gambar
- **Add to Favorites** - Tandai komik favorit
- **Download Chapter** - Simpan chapter untuk dibaca offline

## 🏗️ Arsitektur

### Technology Stack
- **Language**: Kotlin
- **Architecture**: Repository Pattern
- **Networking**: Retrofit + OkHttp
- **Database**: Room
- **Image Loading**: Glide
- **Authentication**: Google Play Services Auth
- **Async**: Kotlin Coroutines
- **UI**: Material Design 3, ViewBinding

### Project Structure
```
app/
├── data/
│   ├── api/           # Retrofit API interfaces
│   ├── local/         # Room database (entities, DAOs)
│   ├── model/         # Data models
│   └── repository/    # Repository layer
├── ui/
│   ├── adapter/       # RecyclerView adapters
│   ├── auth/          # Login, Register
│   ├── dashboard/     # Main dashboard
│   ├── search/        # Search functionality
│   ├── detail/        # Comic detail
│   ├── reader/        # Chapter reader
│   ├── favorites/     # Favorites management
│   ├── downloads/     # Downloads management
│   ├── profile/       # User profile
│   ├── manga/         # Manga list
│   ├── manhwa/        # Manhwa list
│   ├── manhua/        # Manhua list
│   ├── genres/        # Genre browsing
│   ├── history/       # Reading history
│   ├── settings/      # App settings
│   └── splash/        # Splash screen
└── res/
    ├── layout/        # XML layouts
    ├── drawable/      # Images, vectors
    ├── menu/          # Navigation menus
    └── values/        # Strings, colors, themes
```

## 🔌 API Endpoints

Aplikasi menggunakan API berikut:

### Search
- `GET /api/komik/search?query={query}`

### List by Type
- `GET /api/komik/manga?page={page}`
- `GET /api/komik/manhwa?page={page}`
- `GET /api/komik/manhua?page={page}`

### Detail & Chapter
- `GET /api/komik/detail?komik_id={id}`
- `GET /api/komik/chapter?chapter_url={url}`

## ⚙️ Setup & Configuration

### 1. Clone Repository
```bash
git clone <repository-url>
cd Komikita
```

### 2. Configure API Base URL
Edit `local.properties`:
```properties
API_BASE_URL=https://your-api-url.com
```

### 3. Google Sign-In Setup
1. Buat project di [Google Cloud Console](https://console.cloud.google.com/)
2. Enable Google Sign-In API
3. Download `google-services.json`
4. Tempatkan di folder `app/`

### 4. Build & Run
```bash
./gradlew assembleDebug
```

Atau gunakan Android Studio:
1. Open Project
2. Sync Gradle
3. Run on emulator/device

## 📦 Dependencies

### Networking & Data
- Retrofit 2.11.0
- OkHttp 4.12.0
- Gson 2.10.1

### Database
- Room 2.6.1
- KSP 2.0.21-1.0.25

### UI & Image
- Material Components 1.10.0
- Glide 4.16.0
- RecyclerView 1.3.2

### Authentication
- Google Play Services Auth 21.2.0

### Async
- Kotlin Coroutines 1.8.1
- Lifecycle ViewModel 2.7.0

## 🎨 Screens (14+ Activities)

1. **SplashActivity** - Logo dan loading
2. **LoginActivity** - Google Sign-In
3. **RegisterActivity** - Lengkapi profil
4. **DashboardActivity** - Halaman utama dengan bottom nav
5. **SearchActivity** - Pencarian dengan filter
6. **MangaListActivity** - List manga
7. **ManhwaListActivity** - List manhwa
8. **ManhuaListActivity** - List manhua
9. **KomikDetailActivity** - Detail komik
10. **ChapterReaderActivity** - Baca chapter
11. **FavoritesActivity** - Daftar favorit
12. **DownloadsActivity** - Daftar download
13. **ProfileActivity** - Profil user
14. **GenresActivity** - Browse by genre
15. **HistoryActivity** - Riwayat baca
16. **SettingsActivity** - Pengaturan

## 🎯 Fitur Unggulan

### Search dengan Filter
- Pencarian teks real-time dengan debounce
- Filter berdasarkan genre (Action, Romance, Fantasy, Comedy, dll)
- Hasil pencarian dengan pagination

### Bottom Navigation
- Home - Komik terbaru
- Search - Pencarian
- Favorites - Favorit
- Downloads - Unduhan
- Profile - Profil

### Database Lokal
- User profiles
- Favorite comics
- Download records
- Reading history

## 📝 Notes

### To-Do / Future Improvements
- [ ] Implementasi download chapter yang lengkap
- [ ] Offline reading mode
- [ ] Push notifications untuk chapter baru
- [ ] Bookmark posisi baca
- [ ] Dark mode/Night mode
- [ ] Customize reading settings (brightness, orientation)
- [ ] Share komik ke social media
- [ ] Rating & review sistem

### Known Issues
- Google Sign-In menggunakan API deprecated (perlu update ke Credential Manager)
- `adapterPosition` deprecated di ChapterAdapter (gunakan `bindingAdapterPosition`)

## 📄 License

Project ini dibuat untuk keperluan pembelajaran dan portfolio.

## 👨‍💻 Developer

Rizal - Mobile Developer

---

**Komikita** - Baca komik favorit kamu dimana saja! 📚✨
