# Penjelasan Teknis Project Komikita
### Panduan Lengkap Konsep, Logika, dan Implementasi

Dokumen ini disusun untuk menjelaskan secara mendetail bagaimana aplikasi **Komikita** dibangun, mulai dari konsep dasar, bahasa pemrograman, teknologi yang digunakan, hingga logika sistem yang berjalan di baliknya.

---

## 1. Pendahuluan
**Komikita** adalah aplikasi Android Native untuk membaca komik (Manga, Manhwa, Manhua) secara online maupun offline. Project ini dibangun dengan standar industri modern untuk memenuhi Tugas Besar Pemrograman Mobile.

---

## 2. Bahasa Pemrograman: Kotlin 🤖
Aplikasi ini 100% ditulis menggunakan **Kotlin**. Mengapa Kotlin?

1.  **Modern & Concise**: Penulisan kode lebih ringkas dibandingkan Java. Contoh: Tidak perlu menulis `findViewById` berulang kali berkat *ViewBinding*.
2.  **Null Safety**: Kotlin mencegah error `NullPointerException` (The Billion Dollar Mistake) dengan membedakan tipe data `String` (tidak boleh null) dan `String?` (boleh null).
3.  **Coroutines**: Menangani proses berat (seperti download atau ambil data API) di background thread dengan sintaks yang mudah dibaca, seolah-olah codingan synchronous.

**Fitur Kotlin yang dipakai di project ini:**
*   `data class`: Untuk model data (seperti `Komik`, `Chapter`) yang otomatis men-generate fungsi `toString()`, `equals()`, dll.
*   `companion object`: Untuk fungsi statis (seperti `AppDatabase.getDatabase()`).
*   `Extension Functions`: Menambah fungsi ke kelas bawaan tanpa warisan.

---

## 3. Arsitektur Aplikasi: MVVM (Model-View-ViewModel) 🏗️
Kita tidak menulis semua kode di satu file Activity. Kita memisahkannya agar rapi dan mudah diurus.

*   **View (UI)**: `Activity` dan `Fragment` (XML). Tugasnya HANYA menampilkan data ke layar dan menangkap klik user. Tidak boleh ada logika bisnis di sini.
*   **ViewModel**: Penghubung antara View dan Data. Ia menyimpan data sementara (State) agar tidak hilang saat layar diputar.
*   **Model (Repository)**: Sumber data. Ia yang memutuskan apakah data diambil dari **API (Internet)** atau **Database Lokal (Room)**.

**Alur Data:**
`API/DB` ➡ `Repository` ➡ `ViewModel` ➡ `Activity` ➡ `User melihat layar`

---

## 4. Teknologi & Materi yang Diterapkan 🛠️

### A. Networking (API) - Retrofit
Untuk mengambil data komik dari server internet.
*   **Retrofit**: Library untuk mengubah URL API menjadi Interface Kotlin.
*   **Gson**: Konverter data JSON dari server menjadi objek Kotlin (`SearchResponse`, `DetailResponse`).
*   **Logika**:
    *   Kita buat interface `KomikApi`.
    *   Retrofit melakukan request HTTP GET.
    *   Jika sukses, data masuk ke `RecyclerView`.

### B. Local Database - Room
Untuk fitur **Favorit** dan **Riwayat Download**.
*   **Entity**: Tabel database (contoh: `DownloadEntity` mewakili tabel `downloads`).
*   **DAO (Data Access Object)**: Berisi perintah SQL (`SELECT`, `INSERT`, `DELETE`) tapi berbentuk fungsi Kotlin.
*   **Database**: Kelas pembungkus utama.
*   **Logika Offline**: Saat user menyimpan favorit, kita `INSERT` ke database HP. Saat dibuka lagi tanpa internet, kita `SELECT` dari database HP.

### C. Asynchronous Programming - Coroutines
Aplikasi tidak boleh macet (Freeze) saat download data.
*   **Logika**: Perintah berat dijalankan di `Dispatchers.IO` (jalur background), lalu hasilnya ditampilkan ke layar di `Dispatchers.Main` (jalur utama).

### D. Image Loading - Glide
Untuk menampilkan gambar komik dari URL. Glide otomatis mengurus caching (simpan sementara) agar hemat kuota.

### E. Background Manager
Untuk fitur **Real Download**. Menggunakan `DownloadManager` singleton yang berjalan independen dari lifecycle Activity, sehingga download tidak putus meski user pindah halaman.

---

## 5. Logika Sistem (Deep Dive) 🧠

### A. Logika Login & Register
1.  User input email & password.
2.  Sistem mengecek format email (Regex).
3.  **Hashing**: Password diubah menjadi kode acak (SHA-256) sebelum disimpan, agar aman dari peretas.
4.  Data disimpan di `Room Database`.
5.  **Session**: Status login disimpan di `SharedPreferences`. Jika user tutup aplikasi dan buka lagi, tidak perlu login ulang.

### B. Logika Real Offline Download (Fitur Unggulan)
Sistem download di aplikasi ini bukan sekadar simulasi. Ini alurnya:
1.  **Request**: User klik download chapter.
2.  **Fetch URLs**: Aplikasi minta daftar URL gambar ke API.
3.  **Physical Download**: `ImageDownloader` mengunduh setiap file gambar (`.jpg`) dan menyimpannya di folder privat aplikasi (`Android/data/...`).
4.  **Database Record**: Aplikasi mencatat "Chapter 1 tersimpan di folder X" ke database `downloads`.
5.  **Reading**:
    *   Cek koneksi internet.
    *   Jika **Offline**: Cek database ➡ Ambil path folder X ➡ Tampilkan gambar dari file HP.
    *   Jika **Online**: Ambil URL dari API ➡ Tampilkan gambar dari internet (Glide).

---

## 6. Langkah Pembuatan dari Nol (Step-by-Step) 📝

1.  **Setup Project**:
    *   Buka Android Studio, pilih "Empty Views Activity".
    *   Tambahkan library di `build.gradle` (Retrofit, Glide, Room, Coroutines).

2.  **Desain UI (Layouting)**:
    *   Buat `activity_main.xml`, `item_komik.xml`, dll.
    *   Gunakan `ConstraintLayout` agar responsif di berbagai ukuran HP.

3.  **Buat Struktur Data (Model)**:
    *   Bikin data class `Komik`, `Chapter` sesuai respon JSON API.

4.  **Setup Database (Room)**:
    *   Bikin `UserEntity`, `FavoriteEntity`.
    *   Bikin `AppDatabase`.

5.  **Setup Networking**:
    *   Bikin `RetrofitClient` (Singleton).
    *   Bikin `KomikApi` interface.

6.  **Implementasi Repository**:
    *   Bikin kelas `KomikRepository` untuk menggabungkan API dan DAO.

7.  **Coding Logic (Activity/ViewModel)**:
    *   **Dashboard**: Panggil `repository.getLatest()`, tampilkan di RecyclerView.
    *   **Detail**: Panggil `repository.getDetail()`, tampilkan info + list chapter.
    *   **Reader**: Ambil list gambar, tampilkan di RecyclerView vertical.

8.  **Fitur Tambahan (Polishing)**:
    *   Tambahkan **Dark Mode** support (folder `values-night`).
    *   Tambahkan **Notifikasi** download.
    *   Tambahkan **Validasi** input form.

9.  **Finishing & Build**:
    *   Cek error (Debugging).
    *   Build APK (`Build > Build Bundle(s) / APK > Build APK`).

---

## 7. Kesimpulan
Aplikasi Komikita adalah implementasi nyata dari konsep **Full Stack Mobile Development** (sisi klien). Project ini mengajarkan kita cara mengelola data yang kompleks, menangani kondisi jaringan (Online/Offline), dan menciptakan pengalaman pengguna (UX) yang mulus.

---
*Dokumen ini dibuat otomatis oleh AI Assistant untuk keperluan dokumentasi project Muhammad Rizal Nurfirdaus.*
