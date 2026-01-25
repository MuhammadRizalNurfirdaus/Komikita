# Penjelasan Teknis Project Komikita
### Panduan Lengkap dari Dasar: Konsep, Arsitektur, API, Database, dan Implementasi

Dokumen ini menjelaskan secara **mendetail dari dasar** bagaimana aplikasi **Komikita** dibangun, mulai dari konsep programming, arsitektur, sumber data API, struktur database, hingga implementasi fitur-fitur utama.

---

## Daftar Isi
1. [Pendahuluan](#1-pendahuluan)
2. [Konsep Dasar Android Development](#2-konsep-dasar-android-development)
3. [Bahasa Pemrograman Kotlin](#3-bahasa-pemrograman-kotlin)
4. [Arsitektur MVVM](#4-arsitektur-mvvm-model-view-viewmodel)
5. [Sumber Data API (REST API)](#5-sumber-data-api-rest-api)
6. [Local Database (Room)](#6-local-database-room)
7. [Networking dengan Retrofit](#7-networking-dengan-retrofit)
8. [Sistem Tema (Dark/Light Mode)](#8-sistem-tema-darklight-mode)
9. [Implementasi Fitur Utama](#9-implementasi-fitur-utama)
10. [Tantangan & Solusi](#10-tantangan--solusi)
11. [Struktur Project](#11-struktur-project)
12. [Kesimpulan](#12-kesimpulan)

---

## 1. Pendahuluan

### Apa itu Komikita?
**Komikita** adalah aplikasi Android Native untuk membaca komik (Manga, Manhwa, Manhua) secara online maupun offline.

### Informasi Project
| Item | Detail |
|------|--------|
| Versi | v1.1.0 (25 Januari 2026) |
| Platform | Android (Min SDK 24 / Android 7.0) |
| Target SDK | 34 (Android 14) |
| Bahasa | Kotlin 100% |
| Arsitektur | MVVM + Repository Pattern |
| Database | Room (SQLite) |
| Networking | Retrofit 2 + OkHttp |
| Image Loading | Glide 4.x |

### Apa yang Akan Dipelajari?
1. Cara membuat aplikasi Android dari nol
2. Cara mengambil data dari API (internet)
3. Cara menyimpan data secara lokal (offline)
4. Cara membuat UI yang responsif dan modern
5. Cara handle dark mode dan light mode

---

## 2. Konsep Dasar Android Development

### A. Activity
**Activity** adalah satu "halaman" atau "layar" di aplikasi Android.

```kotlin
class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inisialisasi halaman
    }
}
```

**Contoh Activity di Komikita:**
| Activity | Fungsi |
|----------|--------|
| `DashboardActivity` | Halaman utama (Home) |
| `KomikDetailActivity` | Detail komik |
| `ChapterReaderActivity` | Membaca chapter |
| `SearchActivity` | Pencarian komik |
| `ProfileActivity` | Profil & pengaturan |
| `LoginActivity` | Halaman login |
| `RegisterActivity` | Halaman registrasi |

### B. Layout (XML)
Layout adalah file XML yang menentukan tampilan UI.

```xml
<!-- activity_dashboard.xml -->
<LinearLayout 
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <TextView android:text="Selamat Datang" />
    <RecyclerView android:id="@+id/rvKomik" />
    
</LinearLayout>
```

### C. ViewBinding
ViewBinding menghubungkan kode Kotlin dengan elemen di Layout tanpa `findViewById`.

```kotlin
// CARA LAMA (tanpa ViewBinding)
val textView = findViewById<TextView>(R.id.tvTitle)

// CARA BARU (dengan ViewBinding) - lebih aman & cepat
private lateinit var binding: ActivityDashboardBinding

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityDashboardBinding.inflate(layoutInflater)
    setContentView(binding.root)
    
    // Akses langsung
    binding.tvTitle.text = "Judul Komik"
}
```

### D. RecyclerView
RecyclerView adalah komponen untuk menampilkan daftar item yang bisa di-scroll.

```kotlin
// Setup RecyclerView
binding.rvKomik.apply {
    layoutManager = GridLayoutManager(this@DashboardActivity, 2)
    adapter = komikAdapter
}
```

### E. Intent
Intent digunakan untuk berpindah antar Activity atau mengirim data.

```kotlin
// Pindah ke halaman detail
val intent = Intent(this, KomikDetailActivity::class.java)
intent.putExtra("KOMIK_SLUG", "one-piece")
intent.putExtra("KOMIK_TITLE", "One Piece")
startActivity(intent)

// Di Activity tujuan, ambil data:
val slug = intent.getStringExtra("KOMIK_SLUG")
```

---

## 3. Bahasa Pemrograman Kotlin

### A. Mengapa Kotlin?
- **Official**: Bahasa resmi untuk Android development
- **Null Safety**: Mencegah crash karena null
- **Concise**: Kode lebih pendek dan bersih
- **Interoperable**: Bisa berjalan bersama Java

### B. Fitur Kotlin yang Digunakan

#### 1. Data Class
```kotlin
// Otomatis generate equals(), hashCode(), toString(), copy()
data class KomikItem(
    val slug: String,
    val title: String,
    val poster: String?,  // ? = boleh null
    val chapter: String?
)
```

#### 2. Null Safety
```kotlin
// Mencegah NullPointerException
val title: String? = null

// Safe call - tidak crash jika null
val length = title?.length  // hasil: null

// Elvis operator - nilai default jika null
val safeTitle = title ?: "Tidak ada judul"  // hasil: "Tidak ada judul"
```

#### 3. Coroutines (Async Programming)
```kotlin
// Menjalankan operasi di background thread
lifecycleScope.launch {
    // Ini berjalan di background
    val komikList = repository.getKomikFromAPI()
    
    // Update UI di main thread
    withContext(Dispatchers.Main) {
        adapter.submitList(komikList)
    }
}
```

#### 4. Extension Functions
```kotlin
// Menambah fungsi ke class yang sudah ada
fun String.toSlug(): String {
    return this.lowercase().replace(" ", "-")
}

// Penggunaan
val slug = "One Piece".toSlug()  // hasil: "one-piece"
```

#### 5. Higher-Order Functions
```kotlin
// Fungsi sebagai parameter
val numbers = listOf(1, 2, 3, 4, 5)
val doubled = numbers.map { it * 2 }  // [2, 4, 6, 8, 10]
val evens = numbers.filter { it % 2 == 0 }  // [2, 4]
```

---

## 4. Arsitektur MVVM (Model-View-ViewModel)

### A. Apa itu MVVM?
MVVM adalah pola arsitektur yang memisahkan kode menjadi 3 bagian:

```
┌─────────────────────────────────────────────────────────────┐
│                         USER                                │
│                          ↓↑                                 │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                     VIEW                              │  │
│  │           (Activity / Fragment)                       │  │
│  │    - Menampilkan data ke user                        │  │
│  │    - Menangkap input dari user                       │  │
│  └──────────────────────────────────────────────────────┘  │
│                          ↓↑                                 │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                   VIEWMODEL                           │  │
│  │    - Menyimpan state/data UI                         │  │
│  │    - Menangani business logic                        │  │
│  │    - Berkomunikasi dengan Repository                 │  │
│  └──────────────────────────────────────────────────────┘  │
│                          ↓↑                                 │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                   REPOSITORY                          │  │
│  │    - Single Source of Truth                          │  │
│  │    - Memutuskan: ambil dari API atau Database?       │  │
│  └──────────────────────────────────────────────────────┘  │
│                    ↓↑           ↓↑                          │
│  ┌────────────────────┐   ┌─────────────────────┐          │
│  │   REMOTE (API)     │   │   LOCAL (Room DB)   │          │
│  │   - Retrofit       │   │   - SQLite          │          │
│  │   - Internet       │   │   - Offline         │          │
│  └────────────────────┘   └─────────────────────┘          │
└─────────────────────────────────────────────────────────────┘
```

### B. Alur Data
```
User klik "Cari Komik"
    ↓
VIEW: SearchActivity menangkap input
    ↓
VIEWMODEL: Memanggil repository.searchKomik("naruto")
    ↓
REPOSITORY: Memutuskan fetch dari API (ada internet)
    ↓
API: GET https://ws.asepharyana.tech/api/komik/search?query=naruto
    ↓
REPOSITORY: Terima response, convert ke data class
    ↓
VIEWMODEL: Update LiveData dengan hasil pencarian
    ↓
VIEW: Observe LiveData, tampilkan di RecyclerView
    ↓
User melihat hasil pencarian
```

### C. Contoh Implementasi

#### View (Activity)
```kotlin
class SearchActivity : AppCompatActivity() {
    private val viewModel: SearchViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Observe data dari ViewModel
        viewModel.searchResults.observe(this) { results ->
            adapter.submitList(results)
        }
        
        // Kirim input ke ViewModel
        binding.btnSearch.setOnClickListener {
            val query = binding.etSearch.text.toString()
            viewModel.searchKomik(query)
        }
    }
}
```

#### ViewModel
```kotlin
class SearchViewModel(private val repository: KomikRepository) : ViewModel() {
    
    private val _searchResults = MutableLiveData<List<KomikItem>>()
    val searchResults: LiveData<List<KomikItem>> = _searchResults
    
    fun searchKomik(query: String) {
        viewModelScope.launch {
            val results = repository.searchKomik(query)
            _searchResults.postValue(results)
        }
    }
}
```

#### Repository
```kotlin
class KomikRepository(
    private val api: KomikApi,
    private val database: AppDatabase
) {
    suspend fun searchKomik(query: String): List<KomikItem> {
        return try {
            // Coba ambil dari API
            val response = api.searchKomik(query)
            response.body()?.data ?: emptyList()
        } catch (e: Exception) {
            // Jika gagal, return empty
            emptyList()
        }
    }
}
```

---

## 5. Sumber Data API (REST API)

### A. Apa itu REST API?
REST API adalah cara aplikasi berkomunikasi dengan server melalui HTTP untuk mengambil atau mengirim data.

### B. API yang Digunakan

| Nama API | Provider |
|----------|----------|
| **Komiku API** | ws.asepharyana.tech |
| Base URL | `https://ws.asepharyana.tech/` |
| Format Response | JSON |
| Metode | GET (Read Only) |

### C. Daftar Endpoint API

#### 1. Search Komik
```
GET /api/komik/search?query={keyword}&page={page}
```
**Contoh Request:**
```
GET https://ws.asepharyana.tech/api/komik/search?query=naruto&page=1
```
**Response:**
```json
{
  "status": "success",
  "data": [
    {
      "title": "Naruto",
      "slug": "naruto",
      "poster": "https://thumbnail.komiku.org/...",
      "chapter": "Chapter 700",
      "type": "Manga"
    }
  ],
  "pagination": {
    "current_page": 1,
    "total_pages": 5
  }
}
```

#### 2. Daftar Manga
```
GET /api/komik/manga?page={page}
```
**Fungsi:** Mengambil daftar manga terbaru

#### 3. Daftar Manhwa
```
GET /api/komik/manhwa?page={page}
```
**Fungsi:** Mengambil daftar manhwa (komik Korea)

#### 4. Daftar Manhua
```
GET /api/komik/manhua?page={page}
```
**Fungsi:** Mengambil daftar manhua (komik China)

#### 5. Komik Terbaru
```
GET /api/komik/latest?page={page}
```
**Fungsi:** Mengambil update chapter terbaru

#### 6. Komik Populer
```
GET /api/komik/popular?page={page}&period={period}
```
**Fungsi:** Mengambil komik paling populer

#### 7. Detail Komik ⭐
```
GET /api/komik/detail?komik_id={slug}
```
**Contoh Request:**
```
GET https://ws.asepharyana.tech/api/komik/detail?komik_id=one-piece
```
**Response:**
```json
{
  "status": true,
  "data": {
    "title": "One Piece",
    "author": "Eiichiro Oda",
    "description": "Kisah Monkey D. Luffy...",
    "poster": "https://thumbnail.komiku.org/...",
    "status": "Ongoing",
    "type": "Manga",
    "genres": ["Action", "Adventure", "Comedy"],
    "total_chapter": "1100+",
    "chapters": [
      {
        "chapter": "Chapter 1100",
        "chapter_id": "one-piece-chapter-1100",
        "date": "3 hari lalu"
      },
      {
        "chapter": "Chapter 1099",
        "chapter_id": "one-piece-chapter-1099",
        "date": "1 minggu lalu"
      }
    ]
  }
}
```

#### 8. Baca Chapter ⭐
```
GET /api/komik/chapter?chapter_url={chapter_id}
```
**Contoh Request:**
```
GET https://ws.asepharyana.tech/api/komik/chapter?chapter_url=one-piece-chapter-1100
```
**Response:**
```json
{
  "message": "Ok",
  "data": {
    "title": "One Piece Chapter 1100",
    "images": [
      "https://img.komiku.org/upload/one-piece/1100/1.webp",
      "https://img.komiku.org/upload/one-piece/1100/2.webp",
      "https://img.komiku.org/upload/one-piece/1100/3.webp"
    ],
    "next_chapter_id": "one-piece-chapter-1101",
    "prev_chapter_id": "one-piece-chapter-1099",
    "list_chapter": "one-piece"
  }
}
```

#### 9. Daftar Genre
```
GET /api/komik/genres
```
**Fungsi:** Mengambil daftar semua genre yang tersedia

#### 10. Filter by Genre
```
GET /api/komik/genre/{genre_slug}/{slug}?page={page}
```
**Contoh:**
```
GET https://ws.asepharyana.tech/api/komik/genre/action/all?page=1
```

### D. Response Data Models

```kotlin
// File: SearchResponse.kt
data class SearchResponse(
    val data: List<SearchItem>?,
    val pagination: Pagination?,
    val status: String?
)

data class SearchItem(
    val slug: String,        // ID unik komik
    val title: String,       // Judul komik
    val poster: String?,     // URL gambar cover
    val chapter: String?,    // Chapter terbaru
    val type: String?,       // Manga/Manhwa/Manhua
    val genres: List<String>?
)

// File: DetailResponse.kt
data class DetailResponse(
    val status: Boolean?,
    val data: DetailData?
)

data class DetailData(
    val title: String?,
    val author: String?,
    val description: String?,
    val poster: String?,
    val status: String?,      // Ongoing/Completed
    val type: String?,
    val genres: List<String>?,
    val total_chapter: String?,
    val chapters: List<Chapter>?
)

data class Chapter(
    val chapter: String?,     // "Chapter 100"
    @SerializedName("chapter_id")
    val id: String,           // "one-piece-chapter-100"
    val date: String?         // "3 hari lalu"
)

// File: ChapterResponse.kt
data class ChapterResponse(
    val message: String?,
    val data: ChapterData?
)

data class ChapterData(
    val title: String?,
    val images: List<String>?,      // List URL gambar
    val next_chapter_id: String?,   // Chapter selanjutnya
    val prev_chapter_id: String?,   // Chapter sebelumnya
    val list_chapter: String?       // Slug komik induk
)
```

---

## 6. Local Database (Room)

### A. Apa itu Room?
Room adalah library dari Google untuk membuat database SQLite di Android dengan cara yang mudah dan aman.

### B. Komponen Room

```
┌─────────────────────────────────────────────────────────────┐
│                     ROOM DATABASE                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    │
│  │   ENTITY    │    │   ENTITY    │    │   ENTITY    │    │
│  │  UserEntity │    │FavoriteEntity│   │DownloadEntity│   │
│  │  (tabel)    │    │  (tabel)    │    │  (tabel)    │    │
│  └──────┬──────┘    └──────┬──────┘    └──────┬──────┘    │
│         │                  │                  │            │
│         ↓                  ↓                  ↓            │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    │
│  │     DAO     │    │     DAO     │    │     DAO     │    │
│  │   UserDao   │    │ FavoriteDao │    │ DownloadDao │    │
│  │  (query)    │    │  (query)    │    │  (query)    │    │
│  └─────────────┘    └─────────────┘    └─────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### C. Entity (Tabel Database)

#### 1. Tabel Users
```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey 
    val userId: String,           // ID unik user
    val email: String,            // Email user
    val displayName: String?,     // Nama tampilan
    val photoUrl: String?,        // URL foto profil
    val isEmailVerified: Boolean = false,
    val passwordHash: String? = null,  // Password ter-hash
    val loginType: String = "google"   // "google" atau "local"
)
```

**Contoh Data:**
| userId | email | displayName | loginType |
|--------|-------|-------------|-----------|
| user_001 | rizal@gmail.com | Rizal | google |
| user_002 | test@test.com | Test User | local |

#### 2. Tabel Favorites
```kotlin
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey 
    val slug: String,             // ID unik komik (primary key)
    val title: String,            // Judul komik
    val poster: String?,          // URL cover
    val type: String?,            // Manga/Manhwa/Manhua
    val userId: String,           // Pemilik favorit
    val addedAt: Long = System.currentTimeMillis()  // Waktu ditambahkan
)
```

**Contoh Data:**
| slug | title | type | userId | addedAt |
|------|-------|------|--------|---------|
| one-piece | One Piece | Manga | user_001 | 1706200800000 |
| solo-leveling | Solo Leveling | Manhwa | user_001 | 1706201000000 |

#### 3. Tabel Downloads
```kotlin
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) 
    val id: Int = 0,              // ID auto-increment
    val komikSlug: String,        // Slug komik
    val komikTitle: String,       // Judul komik
    val chapterId: String,        // ID chapter
    val chapterTitle: String,     // Nama chapter
    val userId: String,           // User yang download
    val downloadedAt: Long,       // Waktu download
    val localPath: String?,       // Path file lokal
    val status: String = "completed"  // Status download
)
```

**Contoh Data:**
| id | komikSlug | chapterTitle | localPath | status |
|----|-----------|--------------|-----------|--------|
| 1 | one-piece | Chapter 1100 | /storage/.../1100 | completed |
| 2 | one-piece | Chapter 1099 | /storage/.../1099 | completed |

### D. DAO (Data Access Object)

DAO adalah interface yang berisi query database.

```kotlin
@Dao
interface UserDao {
    // SELECT - Ambil data
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUserSync(): UserEntity?
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    // LOGIN - Cek email dan password
    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash LIMIT 1")
    suspend fun getUserByEmailAndPassword(email: String, passwordHash: String): UserEntity?
    
    // INSERT - Tambah data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    // UPDATE - Ubah data
    @Query("UPDATE users SET displayName = :name WHERE userId = :userId")
    suspend fun updateUserName(userId: String, name: String)
    
    // DELETE - Hapus data
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}

@Dao
interface FavoriteDao {
    // Ambil semua favorit user
    @Query("SELECT * FROM favorites WHERE userId = :userId ORDER BY addedAt DESC")
    fun getFavoritesByUser(userId: String): Flow<List<FavoriteEntity>>
    
    // Cek apakah komik sudah difavoritkan
    @Query("SELECT * FROM favorites WHERE slug = :slug AND userId = :userId")
    suspend fun getFavoriteBySlugAndUser(slug: String, userId: String): FavoriteEntity?
    
    // Tambah favorit
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)
    
    // Hapus favorit
    @Query("DELETE FROM favorites WHERE slug = :slug AND userId = :userId")
    suspend fun deleteFavoriteBySlugAndUser(slug: String, userId: String)
}

@Dao
interface DownloadDao {
    // Ambil semua download user
    @Query("SELECT * FROM downloads WHERE userId = :userId ORDER BY downloadedAt DESC")
    fun getDownloadsByUser(userId: String): Flow<List<DownloadEntity>>
    
    // Ambil download berdasarkan komik
    @Query("SELECT * FROM downloads WHERE userId = :userId AND komikSlug = :slug ORDER BY chapterTitle ASC")
    suspend fun getDownloadsByKomikSync(userId: String, slug: String): List<DownloadEntity>
    
    // Cek apakah chapter sudah didownload
    @Query("SELECT * FROM downloads WHERE chapterId = :chapterId AND userId = :userId LIMIT 1")
    suspend fun getDownloadByChapterAndUser(chapterId: String, userId: String): DownloadEntity?
    
    // Simpan download
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)
    
    // Hapus download
    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownloadById(id: Int)
}
```

### E. Database Configuration

```kotlin
@Database(
    entities = [UserEntity::class, FavoriteEntity::class, DownloadEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    // Akses ke DAO
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun downloadDao(): DownloadDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        // Singleton pattern
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "komikita_database"  // Nama file database
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

---

## 7. Networking dengan Retrofit

### A. Apa itu Retrofit?
Retrofit adalah library untuk melakukan HTTP request ke REST API dengan mudah.

### B. Konfigurasi Retrofit

```kotlin
// File: RetrofitClient.kt
object RetrofitClient {
    
    // Logging interceptor - untuk debug
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY  // Tampilkan semua request/response
        } else {
            HttpLoggingInterceptor.Level.NONE  // Matikan di production
        }
    }
    
    // OkHttp client dengan timeout
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)  // Timeout koneksi
        .readTimeout(30, TimeUnit.SECONDS)     // Timeout baca data
        .writeTimeout(30, TimeUnit.SECONDS)    // Timeout kirim data
        .build()
    
    // Retrofit instance
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://ws.asepharyana.tech/")  // Base URL API
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())  // JSON parser
        .build()
    
    // API interface
    val komikApi: KomikApi = retrofit.create(KomikApi::class.java)
}
```

### C. API Interface

```kotlin
// File: KomikApi.kt
interface KomikApi {
    
    // Pencarian komik
    @GET("api/komik/search")
    suspend fun searchKomik(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): Response<SearchResponse>
    
    // Daftar manga
    @GET("api/komik/manga")
    suspend fun getMangaList(
        @Query("page") page: Int = 1
    ): Response<SearchResponse>
    
    // Daftar manhwa
    @GET("api/komik/manhwa")
    suspend fun getManhwaList(
        @Query("page") page: Int = 1
    ): Response<ListResponse>
    
    // Daftar manhua
    @GET("api/komik/manhua")
    suspend fun getManhuaList(
        @Query("page") page: Int = 1
    ): Response<ListResponse>
    
    // Komik terbaru
    @GET("api/komik/latest")
    suspend fun getLatest(
        @Query("page") page: Int = 1
    ): Response<LatestResponse>
    
    // Komik populer
    @GET("api/komik/popular")
    suspend fun getPopular(
        @Query("page") page: Int = 1,
        @Query("period") period: String? = null
    ): Response<PopularResponse>
    
    // Detail komik
    @GET("api/komik/detail")
    suspend fun getKomikDetail(
        @Query("komik_id") komikId: String
    ): Response<DetailResponse>
    
    // Baca chapter
    @GET("api/komik/chapter")
    suspend fun getChapter(
        @Query("chapter_url") chapterUrl: String
    ): Response<ChapterResponse>
    
    // Daftar genre
    @GET("api/komik/genres")
    suspend fun getGenres(): Response<GenreListResponse>
}
```

### D. Cara Menggunakan API

```kotlin
// Di ViewModel atau Repository
class KomikViewModel : ViewModel() {
    
    fun loadMangaList() {
        viewModelScope.launch {
            try {
                // Panggil API
                val response = RetrofitClient.komikApi.getMangaList(page = 1)
                
                if (response.isSuccessful) {
                    val mangaList = response.body()?.data
                    // Update UI dengan data
                    _mangaList.postValue(mangaList)
                } else {
                    // Handle error
                    _error.postValue("Gagal memuat data")
                }
            } catch (e: Exception) {
                // Handle network error
                _error.postValue("Tidak ada koneksi internet")
            }
        }
    }
}
```

---

## 8. Sistem Tema (Dark/Light Mode)

### A. Konfigurasi Theme

#### Light Mode (`values/themes.xml`)
```xml
<style name="Base.Theme.Komikita" parent="Theme.MaterialComponents.DayNight.NoActionBar">
    <!-- Warna utama -->
    <item name="colorPrimary">@color/brand_orange</item>
    <item name="colorPrimaryVariant">@color/brand_orange</item>
    <item name="colorOnPrimary">#FFFFFF</item>
    
    <!-- Status bar -->
    <item name="android:statusBarColor">#000000</item>
    <item name="android:navigationBarColor">#FFFFFF</item>
    <item name="android:windowLightStatusBar">false</item>
    <item name="android:windowLightNavigationBar">true</item>
</style>
```

#### Dark Mode (`values-night/themes.xml`)
```xml
<style name="Base.Theme.Komikita" parent="Theme.MaterialComponents.DayNight.NoActionBar">
    <!-- Warna utama (sama) -->
    <item name="colorPrimary">@color/brand_orange</item>
    
    <!-- Status bar untuk dark mode -->
    <item name="android:statusBarColor">#000000</item>
    <item name="android:navigationBarColor">@color/background_primary</item>
    <item name="android:windowLightStatusBar">false</item>
    <item name="android:windowLightNavigationBar">false</item>
</style>
```

### B. BaseActivity (Theme Manager)

```kotlin
// Semua Activity extends dari BaseActivity
abstract class BaseActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSystemBars()
    }
    
    private fun setupSystemBars() {
        // Cek mode saat ini
        val isDarkMode = (resources.configuration.uiMode and 
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        
        val decorView = window.decorView
        val controller = WindowInsetsControllerCompat(window, decorView)
        
        if (isDarkMode) {
            // DARK MODE: Status bar hitam, ikon putih
            window.statusBarColor = Color.BLACK
            controller.isAppearanceLightStatusBars = false
        } else {
            // LIGHT MODE: Status bar putih, ikon hitam
            window.statusBarColor = Color.WHITE
            controller.isAppearanceLightStatusBars = true
        }
    }
}
```

### C. Dark Mode Toggle

```kotlin
// Di ProfileActivity
private fun setupDarkMode() {
    // Deteksi tema aktual saat ini
    val currentNightMode = resources.configuration.uiMode and 
        Configuration.UI_MODE_NIGHT_MASK
    val isCurrentlyDark = currentNightMode == Configuration.UI_MODE_NIGHT_YES
    
    // Set posisi toggle sesuai kondisi aktual
    binding.switchDarkMode.isChecked = isCurrentlyDark
}

private fun setDarkMode(isDarkMode: Boolean) {
    // Simpan preferensi
    sharedPreferences.edit().putBoolean("dark_mode", isDarkMode).apply()
    
    // Apply theme
    val mode = if (isDarkMode) {
        AppCompatDelegate.MODE_NIGHT_YES
    } else {
        AppCompatDelegate.MODE_NIGHT_NO
    }
    AppCompatDelegate.setDefaultNightMode(mode)
}

// Listener untuk toggle
binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
    setDarkMode(isChecked)
}
```

---

## 9. Implementasi Fitur Utama

### A. Sistem Login

#### Flow Login
```
User input email & password
    ↓
Validasi format (email valid, password min 6 karakter)
    ↓
Hash password dengan SHA-256
    ↓
Cari di database: getUserByEmailAndPassword(email, hashedPassword)
    ↓
Jika ditemukan → Login sukses, simpan session
Jika tidak → Tampilkan error
```

#### Kode Password Hashing
```kotlin
fun hashPassword(password: String): String {
    val bytes = MessageDigest.getInstance("SHA-256")
        .digest(password.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

// Contoh:
// Input: "password123"
// Output: "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f"
```

### B. Sistem Download Offline

#### Flow Download
```
User klik tombol Download pada chapter
    ↓
Ambil list URL gambar dari API
    ↓
Buat folder: /Android/data/com.example.komikita/files/Downloads/{komik}/{chapter}
    ↓
Download setiap gambar ke folder tersebut
    ↓
Simpan metadata ke database (DownloadEntity)
    ↓
Update status: "completed"
```

#### Flow Baca Offline
```
User buka chapter yang sudah didownload
    ↓
ChapterReaderActivity menerima isOfflineMode = true
    ↓
Ambil localPath dari intent
    ↓
Load gambar dari file lokal (bukan URL)
    ↓
Tampilkan di ImageView menggunakan Glide
```

### C. Sistem Navigasi Chapter

#### Mode Online
```kotlin
// Terima list chapter IDs dari KomikDetailActivity
chapterIds = intent.getStringArrayExtra("CHAPTER_IDS")?.toList() ?: emptyList()

private fun calculateNavigation() {
    val currentIndex = chapterIds.indexOf(currentChapterId)
    
    if (currentIndex != -1) {
        // Chapter ditemukan di list
        hasPrev = currentIndex < chapterIds.size - 1  // Bukan chapter terakhir
        hasNext = currentIndex > 0                     // Bukan chapter pertama
        
        prevChapterId = if (hasPrev) chapterIds[currentIndex + 1] else null
        nextChapterId = if (hasNext) chapterIds[currentIndex - 1] else null
    }
    
    updateNavigationButtons(hasPrev, hasNext)
}
```

#### Mode Offline
```kotlin
// Terima info navigasi dari DownloadChaptersActivity
offlinePrevChapterId = intent.getStringExtra("OFFLINE_PREV_CHAPTER_ID")
offlinePrevLocalPath = intent.getStringExtra("OFFLINE_PREV_LOCAL_PATH")
offlineNextChapterId = intent.getStringExtra("OFFLINE_NEXT_CHAPTER_ID")
offlineNextLocalPath = intent.getStringExtra("OFFLINE_NEXT_LOCAL_PATH")

// Navigasi berdasarkan path lokal yang tersedia
val hasPrev = !offlinePrevLocalPath.isNullOrEmpty()
val hasNext = !offlineNextLocalPath.isNullOrEmpty()
```

---

## 10. Tantangan & Solusi

### A. Crash "InflateException"

**Problem:**
```
Error inflating class com.google.android.material.textfield.TextInputLayout
Caused by: IllegalArgumentException: This component requires Theme.MaterialComponents
```

**Penyebab:** 
Menggunakan style `Widget.Material3.*` dengan tema `Theme.MaterialComponents`.

**Solusi:**
Ganti semua style Material3 → MaterialComponents:
```xml
<!-- SALAH -->
style="@style/Widget.Material3.TextInputLayout.OutlinedBox"

<!-- BENAR -->
style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
```

### B. Navigasi Chapter Tidak Akurat

**Problem:**
Tombol Next muncul di chapter terakhir karena API mengembalikan data yang tidak reliable.

**Solusi:**
Gunakan list chapter IDs yang dikirim dari halaman detail, bukan dari API response chapter.

### C. Dark Mode Toggle Tidak Sinkron

**Problem:**
Toggle menunjukkan posisi salah saat app dibuka.

**Solusi:**
Gunakan deteksi tema aktual, bukan preference yang tersimpan:
```kotlin
val currentNightMode = resources.configuration.uiMode and UI_MODE_NIGHT_MASK
val isCurrentlyDark = currentNightMode == UI_MODE_NIGHT_YES
binding.switchDarkMode.isChecked = isCurrentlyDark
```

---

## 11. Struktur Project

```
app/src/main/
├── java/com/example/komikita/
│   │
│   ├── data/                          # Layer Data
│   │   ├── api/                       # Networking
│   │   │   ├── KomikApi.kt           # Interface API endpoints
│   │   │   └── RetrofitClient.kt     # Konfigurasi Retrofit
│   │   │
│   │   ├── local/                     # Database Lokal
│   │   │   ├── AppDatabase.kt        # Room Database
│   │   │   ├── dao/
│   │   │   │   └── Daos.kt           # UserDao, FavoriteDao, DownloadDao
│   │   │   └── entity/
│   │   │       └── Entities.kt       # UserEntity, FavoriteEntity, DownloadEntity
│   │   │
│   │   ├── model/                     # Data Classes
│   │   │   ├── SearchResponse.kt
│   │   │   ├── DetailResponse.kt
│   │   │   ├── ChapterResponse.kt
│   │   │   └── ...
│   │   │
│   │   └── repository/                # Repository Pattern
│   │       └── KomikRepository.kt
│   │
│   ├── ui/                            # Layer UI
│   │   ├── base/
│   │   │   └── BaseActivity.kt       # Theme management
│   │   ├── dashboard/
│   │   │   └── DashboardActivity.kt  # Halaman utama
│   │   ├── detail/
│   │   │   └── KomikDetailActivity.kt
│   │   ├── reader/
│   │   │   └── ChapterReaderActivity.kt
│   │   ├── search/
│   │   │   └── SearchActivity.kt
│   │   ├── downloads/
│   │   │   ├── DownloadsActivity.kt
│   │   │   └── DownloadChaptersActivity.kt
│   │   ├── favorites/
│   │   │   └── FavoritesActivity.kt
│   │   ├── profile/
│   │   │   └── ProfileActivity.kt
│   │   └── auth/
│   │       ├── LoginActivity.kt
│   │       └── RegisterActivity.kt
│   │
│   └── utils/                         # Helper Classes
│       ├── SessionManager.kt
│       ├── ImageDownloader.kt
│       └── NetworkUtils.kt
│
└── res/
    ├── layout/                        # XML Layouts
    ├── values/                        # Light theme, strings, colors
    ├── values-night/                  # Dark theme
    ├── drawable/                      # Icons, backgrounds
    └── menu/                          # Bottom navigation menu
```

---

## 12. Kesimpulan

### Apa yang Telah Dipelajari

1. **Android Fundamentals**: Activity, Layout, ViewBinding, Intent
2. **Kotlin Modern**: Coroutines, Null Safety, Data Classes
3. **Arsitektur MVVM**: Separation of Concerns, Repository Pattern
4. **REST API**: Retrofit, HTTP Methods, JSON Parsing
5. **Local Database**: Room, Entity, DAO, SQL Queries
6. **UI/UX**: Material Design, Dark Mode, RecyclerView
7. **Offline First**: Download Manager, File Storage

### Best Practices yang Diterapkan

1. ✅ Single Responsibility Principle
2. ✅ Repository Pattern untuk abstraksi data
3. ✅ Coroutines untuk async operations
4. ✅ ViewBinding untuk type-safe view access
5. ✅ Consistent theming dengan MaterialComponents
6. ✅ Error handling yang proper
7. ✅ Offline-first approach

---

## Referensi

- [Android Developers - Official Documentation](https://developer.android.com/)
- [Kotlin Language Guide](https://kotlinlang.org/docs/home.html)
- [Material Design Guidelines](https://material.io/design)
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Glide Image Loading](https://bumptech.github.io/glide/)

---

*Dokumen diperbarui: 25 Januari 2026*  
*Versi Aplikasi: v1.1.0*  
*Author: Muhammad Rizal Nurfirdaus*
