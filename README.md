# Komikita - Aplikasi Baca Komik Modern

## 👨‍🎓 Identitas Mahasiswa

| Informasi | Detail |
| :--- | :--- |
| **Nama** | **Muhammad Rizal Nurfirdaus** |
| **NIM** | **20230810088** |
| **Kelas** | **TINFC-2023-04** |
| **Mata Kuliah** | **Bahasa Pemrograman 3** |
| **Dosen Pengampu** | **Dede Husen, M.Kom.** |

---

## 📱 Tentang Project

**Komikita** adalah aplikasi Android native yang dibangun dengan Kotlin untuk membaca berbagai judul komik (Manga, Manhwa, Manhua). Aplikasi ini dirancang dengan antarmuka modern yang responsif dan fitur lengkap layaknya aplikasi profesional.

Aplikasi ini memenuhi dan melampaui syarat tugas UAS dengan menyediakan >15 Activity, manajemen user dengan session, dan fitur database lokal.

🔗 **GitHub Repository**: [https://github.com/MuhammadRizalNurfirdaus/Komikita.git](https://github.com/MuhammadRizalNurfirdaus/Komikita.git)

## ✨ Fitur Unggulan

### 1. Autentikasi & Profil (User Management)
*   **Login & Register Canggih**: Sistem autentikasi yang menyimpan data user (Nama, Email, Foto) ke sesi lokal dan database.
*   **Edit Profile**: User dapat mengubah **Display Name** dan **Foto Profil** (mengambil dari galeri HP).
*   **Google Sign-In**: Opsi login cepat menggunakan akun Google.
*   **Mode Tamu**: Akses aplikasi tanpa login.

### 2. Tampilan & UI/UX (Terbaru)
*   **Modern Branding**: Menggunakan warna khas Oranye (#FF6B35) yang konsisten di semua mode (Light/Dark).
*   **Dark Mode Optimization**:
    *   **Chapter List**: Teks chapter kini berwarna putih dan jelas terbaca di mode gelap.
    *   **Bottom Sheets**: Menggunakan tema gelap yang elegan.
*   **Figma Replication**: Desain aplikasi telah direplikasi secara presisi di Figma (Login Screen, Detail, Download).
*   **Isi Konten Responsif**: Layout otomatis menyesuaikan ukuran layar HP, teks tidak terpotong.
*   **Custom Animation**: Loading screen menggunakan logo komik berputar yang unik.

### 3. Fitur Utama
*   **Dashboard**: Menampilkan daftar komik terbaru dari API publik (Mangamint Source).
*   **Pencarian (Search)**: Cari komik berdasarkan judul atau filter genre.
*   **Detail Komik**: Informasi lengkap, sinopsis, dan daftar chapter.
*   **Baca Komik**: Chapter viewer dengan gambar berkualitas tinggi.
*   **Download**: Fitur unduh chapter untuk dibaca nanti.
*   **Favorit**: Simpan komik kesukaan ke database lokal.

## 🛠️ Technology Stack

*   **Bahasa**: Kotlin 100%
*   **Database**: Android Room Database (SQLite)
*   **Networking**: Retrofit & OkHttp
*   **Image Loading**: Glide
*   **Concurrency**: Kotlin Coroutines & Flow
*   **Design**: XML Layouts (Material Design 3)
*   **API Source**: Menggunakan Endpoint publik Komik.

## 🔒 Keamanan (Sensitive Files)

File-file sensitif berikut telah **di-ignore** dari repository untuk keamanan:
*   `google-services.json` (API Key Firebase/Google)
*   `local.properties` (SDK Paths)
*   `key.properties` (Signing Keys)
*   `*.jks` (Keystore)

## 🚀 Cara Menjalankan

1.  Clone repository ini:
    ```bash
    git clone https://github.com/MuhammadRizalNurfirdaus/Komikita.git
    ```
2.  Buka di Android Studio Ladybug atau versi terbaru.
3.  Pastikan JDK 17 atau 21 terinstall.
4.  Run (`Shift+F10`) ke Emulator atau Device fisik.

---
*Dibuat untuk memenuhi Tugas Ujian Akhir Semester (UAS) Mata Kuliah Bahasa Pemrograman 3.*
