# Penjelasan Teknis Project Komikita
### Panduan Lengkap Konsep, Logika, dan Implementasi

Dokumen ini disusun untuk menjelaskan secara mendetail bagaimana aplikasi **Komikita** dibangun, konsep dasar, teknologi yang digunakan, serta solusi atas tantangan teknis yang dihadapi selama pengembangan.

---

## 1. Pendahuluan
**Komikita** adalah aplikasi Android Native untuk membaca komik (Manga, Manhwa, Manhua) secara online maupun offline. Project ini dibangun dengan standar industri modern untuk memenuhi Tugas Besar Pemrograman Mobile.

---

## 2. Bahasa Pemrograman: Kotlin 🤖
Aplikasi ini 100% ditulis menggunakan **Kotlin**. Keunggulan utama yang dimanfaatkan:

1.  **Modern & Concise**: Menggunakan *ViewBinding* untuk menggantikan `findViewById` yang repot.
2.  **Null Safety**: Mencegah *Force Close* akibat `NullPointerException` dengan manajemen tipe data nullable (`?`).
3.  **Coroutines & Flow**: Menangani proses *asynchronous* (seperti download file besar atau request API) dengan efisien tanpa memblokir UI Thread.

---

## 3. Arsitektur Aplikasi: MVVM (Model-View-ViewModel) 🏗️
Pemisahan tanggung jawab kode (Separation of Concerns) diterapkan dengan ketat:

*   **View (Activities/Fragments)**: 
    *   Hanya bertanggung jawab menampilkan data dan menangani interaksi UI. 
    *   Contoh: `ChapterReaderActivity` menampilkan gambar, `SearchActivity` menampilkan hasil pencarian.
    *   *Update UI*: Sistem UI Modern dengan **Status Bar Hitam** (Atas) dan **Navigation Bar Putih** (Bawah).
*   **ViewModel**: 
    *   Menyimpan state UI agar data tidak hilang saat rotasi layar.
    *   Menggunakan `LiveData`/`StateFlow` untuk memberi tahu View jika ada data baru.
*   **Repository**: 
    *   Single Source of Truth. Memutuskan apakah akan mengambil data dari Server (Online) atau Room Database (Offline).

**Alur Data:**
`API/DB` ➡ `Repository` ➡ `ViewModel` ➡ `Activity` ➡ `User Interface`

---

## 4. Teknologi & Stack Teknis 🛠️

### A. Networking (API) - Retrofit
*   Menggunakan **Retrofit 2** untuk komunikasi dengan REST API.
*   **Gson** sebagai converter JSON ke Object Kotlin.
*   Interceptors (OkHttp) untuk logging dan header handling.

### B. Local Database - Room
*   Menyimpan data **Favorit**, **Riwayat Download**, dan **User Session**.
*   **DAO (Data Access Object)** menangani query SQL yang kompleks secara aman.
*   **Offline First**: Aplikasi memprioritaskan data lokal jika internet tidak tersedia.

### C. Image Loading - Glide
*   Menangani caching gambar secara agresif untuk performa scrolling yang mulus.
*   Placeholder & Error handling otomatis.

### D. Background Services
*   **DownloadManager**: Menangani pengunduhan file chapter di latar belakang, bahkan jika aplikasi ditutup.
*   **BroadcastReceiver**: Mendengarkan event ketika download selesai untuk mengupdate status di database.

---

## 5. Logika Sistem & Fitur Unggulan (Deep Dive) 🧠

### A. Logika Login & Register (Secure)
1.  **Validasi Input**: Email regex & password length check.
2.  **Hashing**: Password di-hash (SHA-256) + Salt sebelum disimpan.
3.  **Session Persistence**: Login statis disimpan aman di Encrypted SharedPreferences/Room.

### B. Logika Real Offline Download 
Sistem ini memungkinkan user membaca tanpa kuota:
1.  **Download Flow**: API ➡ Get Image List ➡ `ImageDownloader` save to `Example/Komikita/ChapterX`.
2.  **Offline Reader Flow**:
    *   Sistem mendeteksi `No Internet`.
    *   Repository cek Database: "Apakah Chapter ID ini ada di tabel Download?".
    *   Jika Ya ➡ Load file dari Storage HP.
    *   Jika Tidak ➡ Tampilkan Error "Koneksi Terputus".

### C. Logika Navigasi Chapter (Smart Navigation)
*   **Dynamic Visibility**: Tombol **Next** dan **Prev** hanya muncul jika ID chapter sebelum/sesudahnya valid (tidak null/undefined).
*   **Single Chapter Mode**: Jika user mendownload hanya 1 chapter dan membacanya secara offline, tombol navigasi otomatis disembunyikan untuk memberikan pengalaman baca yang fokus.

---

## 6. Tantangan Pengembangan & Solusi 💡

Selama pengembangan, beberapa tantangan teknis ditemukan dan berhasil diselesaikan:

### A. Masalah Status Bar Overlap (UI "Bentrok")
*   **Masalah**: Tampilan toolbar di halaman Search, Favorit, dan Detail "menabrak" status bar.
*   **Solusi**: 
    *   Mengimplementasikan `android:fitsSystemWindows="true"` untuk mencegah konten menabrak system bars.
    *   Mengimplementasikan **Dynamic Theme Detection** di `BaseActivity`.
    *   **Logika**:
        *   Cek `Configuration.UI_MODE_NIGHT_MASK`.
        *   Jika **Dark**: Set background Hitam, Ikon Putih.
        *   Jika **Light**: Set background Putih, Ikon Hitam.
    *   Hasil: Status bar selalu kontras dan terbaca (jam/baterai tidak pernah hilang).

### B. Masalah Navigasi Logic
*   **Masalah**: Tombol Next/Prev muncul padahal tidak ada chapter selanjutnya.
*   **Solusi**: Menambahkan validasi ketat di `ChapterReaderActivity`. Mengecek nilai `nextChapterId` dan `prevChapterId` dari API.

---

## 7. Langkah Instalasi & Build 📝
1.  Clone repo.
2.  Sync Gradle di Android Studio.
3.  Pastikan device/emulator memiliki koneksi internet untuk first run.
4.  Build & Run.

---

## 8. Kesimpulan
Aplikasi Komikita adalah implementasi nyata dari konsep **Full Stack Mobile Development** (sisi klien). Project ini mengajarkan kita cara mengelola data yang kompleks, menangani kondisi jaringan (Online/Offline), dan menciptakan pengalaman pengguna (UX) yang mulus.

---
*Dokumen diperbarui terakhir: 2026-01-25*
