# Komikita - Aplikasi Baca Komik Modern

## 👨‍🎓 Identitas Mahasiswa

| Informasi | Detail |
| :--- | :--- |
| **Nama** | **Muhammad Rizal Nurfirdaus** |
| **NIM** | **20230810088** |
| **Kelas** | **TINFC-2023-04** |
| **Mata Kuliah** | **Praktikum Bahasa Pemrograman 3** |
| **Dosen Pengampu** | **Dede Husen, M.Kom.** |

---

## 📱 Tentang Project

**Komikita** adalah aplikasi Android native modern yang dikembangkan menggunakan **Kotlin** dan **MVVM Architecture**. Aplikasi ini dirancang untuk memberikan pengalaman membaca komik (Manga, Manhwa, Manhua) yang premium, responsif, dan nyaman di mata pengguna, dengan dukungan penuh untuk mode Offline.

📥 **Download APK Terbaru**: [app-debug.apk](app-debug.apk) 
*(File APK telah disiapkan di folder artifacts project ini)*

Project ini telah diselesaikan 100% dan memenuhi standar pengembangan aplikasi Android profesional, termasuk penggunaan **Local Database (Room)**, **Networking (Retrofit)**, dan **Advanced UI Management**.

🔗 **GitHub Repository**: [https://github.com/MuhammadRizalNurfirdaus/Komikita.git](https://github.com/MuhammadRizalNurfirdaus/Komikita.git)

## 🎨 Desain & UI Reference

Design System aplikasi ini mengacu pada High-Fidelity Design di Figma dengan penyesuaian UX Android:
*   **Warna Utama**: `brand_orange` (#FF6B35) - Identitas visual yang kuat.
*   **System Bars (Premium Look)**:
    *   **Status Bar (Atas)**: **Dinamis (Smart Adaptation)**. 
        *   *Light Mode*: Background Putih, Ikon Hitam.
        *   *Dark Mode*: Background Hitam, Ikon Putih.
        *   Memastikan jam & baterai selalu terlihat jelas di kondisi apapun.
    *   **Navigation Bar (Bawah)**: Mengikuti tema (Putih/Hitam) dengan kontras ikon yang sesuai.
*   **Tema**: Mendukung penuh **Light Mode** dan **Dark Mode** secara otomatis.
*   **Navigasi**: Menggunakan Persistent Bottom Navigation Bar & Immersive Reader.

🔗 **Figma Design File**: [Klik disini untuk melihat Desain Lengkap](https://www.figma.com/design/WhCaIxb9lESuLnuTMszFR8/Komikita?node-id=0-1&p=f&t=ZAtUXDHCXta4IAqz-0)

---

## ✨ Fitur Unggulan

### 1. Autentikasi & Manajemen User
*   **Secure Login & Register**: Form login/register dengan validasi realtime.
*   **Password Hashing**: Password user dienkripsi menggunakan **SHA-256** sebelum disimpan ke database lokal.
*   **Profil User**: Pengguna dapat mengganti Foto Profil (pilih dari galeri) dan mengedit data diri.
*   **Mode Tamu (Guest)**: Bisa browsing komik tanpa login, namun wajib login untuk fitur premium (Download/Favorit).

### 2. Pengalaman Membaca (Reading Experience)
*   **Chapter Reader Canggih**: Viewer gambar vertikal dengan performa tinggi (menggunakan Glide).
*   **Smart Navigation**: 
    *   Tombol **Next/Prev** yang cerdas (hanya muncul jika chapter tersedia).
    *   Mode Layar Penuh yang nyaman namun tetap menampilkan status bar yang terbaca.
*   **Smart Scroll**: Layout Detail Komik yang presisi menggunakan `NestedScrollView`.
*   **Error Handling**: Tampilan "No Internet" interaktif dengan tombol **Retry/Refresh**.

### 3. Manajemen Konten Offline (Real Offline Mode)
*   **Real Download System**:
    *   **Physical File Storage**: Gambar komik diunduh fisik dan disimpan di penyimpanan internal perangkat (`ImageDownloader`).
    *   **Background Manager**: Download berjalan lancar di latar belakang menggunakan `DownloadManager`.
*   **Smart Offline Reader**: Otomatis mendeteksi saat tidak ada internet dan membaca file gambar dari penyimpanan lokal dengan **Zero Loading Time**.
*   **Logic Offline Cerdas**: Jika hanya 1 chapter terdownload, navigasi bar otomatis disembunyikan untuk fokus membaca.

### 4. UI/UX & Tampilan Visual
*   **Full Dark Mode Support**: Semua layar beradaptasi dengan tema.
*   **Immersive Navigation**: Menghilangkan tombol Back fisik yang tidak perlu, digantikan gesture/tombol in-app.
*   **Loading Animation**: Progress bar kustom dengan animasi berputar yang unik.

---

## 🛠️ Technology Stack

*   **Language**: Kotlin 100%
*   **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
*   **UI Framework**: XML Layouts (Material Design 3) + `ConstraintLayout` & `CoordinatorLayout`
*   **Local DB**: Android Room Database (SQLite) v3
*   **Networking**: Retrofit 2 + OkHttp 3
*   **Image Loader**: Glide 4.x
*   **Concurrency**: Kotlin Coroutines & Flow
*   **Build System**: Gradle Kotlin DSL

---

## 🔒 Catatan Keamanan (Security Note)

Demi menjaga keamanan kredensial dan API Key, file konfigurasi berikut **TIDAK DI-UPLOAD** ke repository umum:
1.  `google-services.json`
2.  `local.properties`
3.  `key.properties`
4.  `*.jks`

---

## 🚀 Cara Menjalankan Project

1.  **Clone Repository**:
    ```bash
    git clone https://github.com/MuhammadRizalNurfirdaus/Komikita.git
    ```
2.  **Buka di Android Studio**: Versi terbaru (Ladybug/Jellyfish).
3.  **Sync Gradle**: Biarkan Android Studio mendownload dependency.
4.  **Run App**: Tekan tombol **Run** (`Shift+F10`).

---

*Dibuat dengan ❤️ untuk memenuhi tugas UAS pemrograman.*
