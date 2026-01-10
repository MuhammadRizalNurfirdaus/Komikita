# 🔐 Setup Google Sign-In - Step by Step

## ⚠️ MASALAH SAAT INI
Google Sign-In gagal karena **belum ada OAuth Client ID yang valid** di Google Cloud Console.

## ✅ LANGKAH SETUP (WAJIB!)

### 1. Buka Google Cloud Console
URL: https://console.cloud.google.com/

### 2. Buat/Pilih Project
- Nama project: **Komikita** (atau terserah Anda)
- Catat Project ID yang dibuat

### 3. Enable Google Sign-In API
- Di sidebar, pilih: **APIs & Services** > **Library**
- Cari: **Google Sign-In API** atau **Google+ API**
- Klik **ENABLE**

### 4. Create OAuth 2.0 Client ID

#### 4.1 Configure OAuth Consent Screen
- Pilih: **APIs & Services** > **OAuth consent screen**
- User Type: **External** (untuk testing)
- App name: **Komikita**
- User support email: email Anda
- Developer contact email: email Anda
- Klik **SAVE AND CONTINUE**
- Scopes: Lewati saja (SAVE AND CONTINUE)
- Test users: Tambahkan email Anda untuk testing
- Klik **SAVE AND CONTINUE**

#### 4.2 Create Credentials
- Pilih: **APIs & Services** > **Credentials**
- Klik: **+ CREATE CREDENTIALS** > **OAuth client ID**
- Application type: **Android**

**⚡ PENTING - ISI DATA INI:**

**Package name:**
```
com.example.komikita
```

**SHA-1 certificate fingerprint:**
```
89:D7:9E:42:B8:B9:40:57:58:37:5A:34:B0:D3:72:CD:31:97:B0:49
```

- Klik **CREATE**
- **CATAT Client ID** yang muncul (format: xxx-xxx.apps.googleusercontent.com)

### 5. Update google-services.json

Setelah dapat Client ID dari Google Console, kita perlu update file konfigurasi:

**Download google-services.json BARU** dari Firebase Console:
- URL: https://console.firebase.google.com/
- Pilih project yang sama
- Project Settings > General
- Scroll ke bawah, klik **google-services.json** untuk download
- Replace file di: `app/google-services.json`

ATAU update manual client_id di google-services.json dengan Client ID yang baru.

### 6. Rebuild App
```bash
./gradlew clean
./gradlew assembleDebug
```

### 7. Install & Test
Install APK ke device dan coba login dengan Google.

---

## 🔧 INFORMASI TECHNICAL

**Package Name Aplikasi:**
```
com.example.komikita
```

**SHA-1 Fingerprint (Debug):**
```
89:D7:9E:42:B8:B9:40:57:58:37:5A:34:B0:D3:72:CD:31:97:B0:49
```

**SHA-256 Fingerprint (Debug):**
```
FD:65:7B:BF:4D:49:05:F0:B7:68:FB:32:7A:67:8C:04:88:F0:76:92:C5:7D:01:8A:3B:B9:01:F6:A0:15:6D:85
```

**Cara Cek SHA-1 Sendiri:**
```bash
./gradlew signingReport
```

---

## 🚀 ALTERNATIVE: Gunakan Firebase Authentication

Jika terlalu ribet dengan Google Cloud Console, bisa gunakan Firebase:

### 1. Buka Firebase Console
URL: https://console.firebase.google.com/

### 2. Buat Project Baru
- Nama: **Komikita**
- Enable Google Analytics (optional)

### 3. Add Android App
- Package name: `com.example.komikita`
- App nickname: Komikita
- SHA-1: `89:D7:9E:42:B8:B9:40:57:58:37:5A:34:B0:D3:72:CD:31:97:B0:49`
- Download **google-services.json**
- Replace di folder: `app/google-services.json`

### 4. Enable Authentication
- Di sidebar: **Build** > **Authentication**
- Klik: **Get Started**
- Tab: **Sign-in method**
- Enable: **Google**
- Project support email: pilih email Anda
- Klik **SAVE**

### 5. Rebuild & Test
```bash
./gradlew clean assembleDebug
```

---

## 📱 TESTING

Setelah setup:

1. Install APK baru
2. Buka aplikasi
3. Klik "Sign in with Google"
4. Pilih akun Google
5. **Harus berhasil!** ✅

Jika masih gagal, cek:
- SHA-1 sudah benar di Google Console?
- Package name cocok: `com.example.komikita`?
- google-services.json sudah yang terbaru?
- Sudah rebuild setelah ganti google-services.json?

---

## 🆘 ERROR CODES

- **12501**: User membatalkan sign in
- **12500**: Sign in failed - biasanya SHA-1/Client ID salah
- **10**: Developer error - google-services.json tidak valid

**Jika error 12500 terus muncul:**
1. Pastikan SHA-1 di Google Console 100% cocok
2. Pastikan package name cocok
3. Download ulang google-services.json dari Firebase
4. Clean build: `./gradlew clean`
5. Rebuild: `./gradlew assembleDebug`
6. Uninstall app lama dari device
7. Install APK baru

---

Made with ❤️ for Komikita
