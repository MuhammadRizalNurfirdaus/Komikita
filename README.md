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

**Komikita** adalah aplikasi Android native yang dibangun dengan Kotlin untuk membaca berbagai judul komik (Manga, Manhwa, Manhua). Aplikasi ini dirancang dengan antarmuka modern yang responsif dan fitur lengkap layaknya aplikasi profesional.

Aplikasi ini memenuhi dan melampaui syarat tugas UAS dengan menyediakan >15 Activity, manajemen user dengan session, dan fitur database lokal.

🔗 **GitHub Repository**: [https://github.com/MuhammadRizalNurfirdaus/Komikita.git](https://github.com/MuhammadRizalNurfirdaus/Komikita.git)

## 🎨 Desain & UI Reference

Semua desain antarmuka aplikasi ini merujuk pada High-Fidelity Design yang telah dibuat di Figma. Desain mencakup Light Mode dan Dark Mode dengan Color Palette konsisten `brand_orange` (#FF6B35).

🔗 **Figma Design File**: [Klik disini untuk melihat Desain Lengkap](https://www.figma.com/design/WhCaIxb9lESuLnuTMszFR8/Komikita?node-id=0-1&p=f&t=ZAtUXDHCXta4IAqz-0)

> **Catatan**: Password dan aset sensitif tidak disertakan dalam link publik ini.

---

## ✨ Fitur Unggulan (Updated)

### 1. Autentikasi & Profil (User Management)
*   **Login & Register Canggih**: Sistem autentikasi yang menyimpan data user (Nama, Email, Password Hash) ke lokal database Room.
*   **Edit Profile**: User dapat mengubah **Display Name**, **Email**, dan **Foto Profil** (mengambil dari galeri HP).
*   **Google Sign-In**: Opsi login cepat menggunakan akun Google (Firebase Auth ready).
*   **Mode Tamu**: Akses aplikasi tanpa login (dengan pembatasan pada fitur Download/Favorit).

### 2. Tampilan & Experience (UX)
*   **Persistent Bottom Navigation**: Navigasi antar menu utama (Dashboard, Search, Favorites, Downloads, Profile) yang seamless dan mempertahankan state halaman.
*   **Smart Dark Mode**: Aplikasi otomatis mendeteksi pengaturan sistem HP (Gelap/Terang) dan menyesuaikan warna background (`background_primary`) serta teks agar tetap nyaman dibaca.
*   **Semantic Colors**: Menggunakan sistem warna semantik untuk konsistensi visual di seluruh aplikasi.
*   **Custom Loading Animation**: Animasi loading unik dengan logo Komikita berputar.

### 3. Manajemen Konten Offline
*   **Sistem Favorit**: Simpan komik ke daftar Favorit (tersimpan di Database Lokal per-user).
*   **Download Manager**: Unduh chapter untuk dibaca tanpa koneksi internet.
*   **Offline Reader**: Akses konten yang sudah didownload langsung dari menu Downloads.

### 4. Fitur Utama Lainnya
*   **Dashboard**: Menampilkan daftar komik terbaru dari API publik (Mangamint Source).
*   **Pencarian (Search)**: Cari komik berdasarkan judul, filter `Manga`/`Manhwa`/`Manhua`, dan filter `Genre`.
*   **Baca Komik**: Chapter viewer responsif dengan navigasi Next/Prev Chapter.

## 🛠️ Technology Stack

*   **Bahasa**: Kotlin 100%
*   **Architecture**: MVVM (Model-View-ViewModel)
*   **Database**: Android Room Database (SQLite) v3
*   **Networking**: Retrofit & OkHttp
*   **Image Loading**: Glide + Custom Placeholders
*   **Concurrency**: Kotlin Coroutines & Flow
*   **Design**: XML Layouts (Material Design 3 Component)
*   **Security**: Password Hashing (SHA-256)

## 🔒 Keamanan & Privasi

Demi keamanan, file konfigurasi berikut **TIDAK DISERTAKAN** dalam repository ini (tercantum dalam `.gitignore`):
*   `google-services.json` (Konfigurasi Firebase & Google Sign-In)
*   `local.properties` (SDK Location & sensitive keys)
*   `key.properties` (Signing Keystore credentials)
*   `*.jks` (Keystore file untuk rilis)

> Jika Anda ingin men-deploy ulang aplikasi ini, harap buat Firebase Project Anda sendiri dan masukkan `google-services.json` milik Anda.

## 🚀 Cara Menjalankan

1.  Clone repository ini:
    ```bash
    git clone https://github.com/MuhammadRizalNurfirdaus/Komikita.git
    ```
2.  Buka di Android Studio Ladybug atau versi terbaru.
3.  Pastikan JDK 17 atau 21 terinstall.
4.  Sync Project dengan Gradle Files.
5.  Run (`Shift+F10`) ke Emulator atau Device fisik.

---
*Dibuat untuk memenuhi Tugas Ujian Akhir Semester (UAS) Mata Kuliah Bahasa Pemrograman 3.*
