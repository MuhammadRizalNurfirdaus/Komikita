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

**Komikita** adalah aplikasi Android native modern yang dikembangkan menggunakan **Kotlin** dan **MVVM Architecture**. Aplikasi ini dirancang untuk memberikan pengalaman membaca komik (Manga, Manhwa, Manhua) yang premium, responsif, dan nyaman di mata pengguna.

Project ini telah diselesaikan 100% dan memenuhi standar pengembangan aplikasi Android profesional, termasuk penggunaan **Local Database (Room)**, **Networking (Retrofit)**, dan **State Management**.

🔗 **GitHub Repository**: [https://github.com/MuhammadRizalNurfirdaus/Komikita.git](https://github.com/MuhammadRizalNurfirdaus/Komikita.git)

## 🎨 Desain & UI Reference

Design System aplikasi ini mengacu pada High-Fidelity Design di Figma.
*   **Warna Utama**: `brand_orange` (#FF6B35)
*   **Tema**: Mendukung penuh **Light Mode** dan **Dark Mode** secara otomatis.
*   **Navigasi**: Menggunakan Persistent Bottom Navigation Bar.

🔗 **Figma Design File**: [Klik disini untuk melihat Desain Lengkap](https://www.figma.com/design/WhCaIxb9lESuLnuTMszFR8/Komikita?node-id=0-1&p=f&t=ZAtUXDHCXta4IAqz-0)

> **Catatan Keamanan**: Aset sensitif dan konfigurasi rahasia tidak disertakan dalam link publik ini.

---

## ✨ Fitur Unggulan

### 1. Autentikasi & Manajemen User
*   **Secure Login & Register**: Form login/register dengan validasi realtime.
*   **Password Hashing**: Password user dienkripsi menggunakan **SHA-256** sebelum disimpan ke database lokal.
*   **Profil User**: Pengguna dapat mengganti Foto Profil (pilih dari galeri) dan mengedit data diri.
*   **Mode Tamu (Guest)**: Bisa browsing komik tanpa login, namun wajib login untuk fitur premium (Download/Favorit).

### 2. Pengalaman Membaca (Reading Experience)
*   **Chapter Reader Canggih**: Viewer gambar vertikal dengan performa tinggi (menggunakan Glide).
*   **Navigasi Chapter**: Tombol **Next/Prev Chapter** dan **Refresh** (Reload) yang memudahkan navigasi tanpa keluar halaman.
*   **Smart Scroll**: Layout Detail Komik yang presisi menggunakan `NestedScrollView`, mencegah scroll berlebih pada konten pendek.
*   **Error Handling**: Tampilan "No Internet" interaktif dengan tombol **Retry/Refresh** untuk memuat ulang data.

### 3. Manajemen Konten Offline (Real Offline Mode)
*   **Real Download System (NEW)**:
    *   **Physical File Storage**: Gambar komik benar-benar diunduh dan disimpan di penyimpanan internal perangkat (`ImageDownloader`), bukan hanya data dummy.
    *   **Background Manager**: Download berjalan lancar di latar belakang menggunakan `DownloadManager` dengan dukungan `SupervisorJob`.
    *   **Progress Monitoring**:
        *   **In-App**: Kartu status download realtime di halaman Detail.
        *   **Notification**: Notifikasi persisten di status bar yang menampilkan progress setiap chapter.
*   **Smart Offline Reader**: Otomatis mendeteksi saat tidak ada internet dan membaca file gambar dari penyimpanan lokal dengan **Zero Loading Time**.
*   **Organized Library**: Halaman Downloads kini menampilkan **Folder Judul Komik** yang rapi, dengan chapter yang terurut otomatis di dalamnya.
*   **Favorit & Sinkronisasi**: Desain halaman Favorit yang konsisten dengan tema aplikasi (Brand Orange), data terikat pada akun user.

### 4. UI/UX & Tampilan Visual
*   **Full Dark Mode Support**:
    *   Semua layar (Dashboard, Detail, List Chapter, Settings) otomatis beradaptasi dengan tema gelap/terang HP.
    *   Warna teks dan background menggunakan **Semantic Colors** (`text_primary`, `background_primary`) agar selalu kontras dan terbaca.
*   **Immersive Navigation**: Menghilangkan tombol Back (HomeAsUp) yang tidak perlu di halaman utama dan menggunakan Bottom Navigation sebagai pusat kontrol.
*   **Loading Animation**: Progress bar kustom dengan animasi berputar yang unik.

---

## 🛠️ Technology Stack

*   **Language**: Kotlin 100%
*   **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
*   **UI Framework**: XML Layouts (Material Design 3 Components)
*   **Local DB**: Android Room Database (SQLite) v3
*   **Networking**: Retrofit 2 + OkHttp 3
*   **Image Loader**: Glide 4.x
*   **Concurrency**: Kotlin Coroutines & Flow
*   **Build System**: Gradle Kotlin DSL

---

## 🔒 Catatan Keamanan (Security Note)

Demi menjaga keamanan kredensial dan API Key, file konfigurasi berikut **TIDAK DI-UPLOAD** ke repository ini (sudah dimasukkan ke `.gitignore`):

1.  `google-services.json` (Konfigurasi Firebase & Google Sign-In)
2.  `local.properties` (SDK Location & Sensitive Keys)
3.  `key.properties` (Signing Keystore Credentials)
4.  `*.jks` (KeyStore File untuk Signing Release APK)

> **Penting**: Jika Anda meng-clone project ini, aplikasi tetap bisa berjalan (build success) namun fitur seperti Google Sign-In mungkin memerlukan konfigurasi `google-services.json` milik Anda sendiri.

---

## 🚀 Cara Menjalankan Project

1.  **Clone Repository**:
    ```bash
    git clone https://github.com/MuhammadRizalNurfirdaus/Komikita.git
    ```
2.  **Buka di Android Studio**: Gunakan versi terbaru (Ladybug/Jellyfish).
3.  **Sync Gradle**: Biarkan Android Studio mendownload dependency.
4.  **Run App**: Tekan tombol **Run** (`Shift+F10`) dan pilih Emulator/Device Anda.
    *   Aplikasi akan otomatis membuat Database Lokal saat pertama kali dijalankan.
    *   Anda bisa langsung mencoba fitur Register/Login.

---

*Dibuat dengan ❤️ untuk memenuhi tugas UAS pemrograman.*
