# 🔧 CARA MENGATASI GOOGLE SIGN-IN FAILED

## ❗ Masalah yang Terjadi

Google Sign-In dialog muncul, user bisa pilih akun, **TAPI setelah pilih akun langsung gagal/cancel**.

## 🎯 Penyebab Utama

**Google Sign-In belum dikonfigurasi di Google Cloud Console / Firebase Console.**

Aplikasi butuh **OAuth 2.0 Client ID** yang valid dengan:
- ✅ Package name yang cocok: `com.example.komikita`
- ✅ SHA-1 fingerprint yang cocok: `89:D7:9E:42:B8:B9:40:57:58:37:5A:34:B0:D3:72:CD:31:97:B0:49`

## 🚀 SOLUSI - Pilih salah satu:

---

### 🔥 CARA 1: FIREBASE CONSOLE (PALING MUDAH)

#### Step 1: Buat Firebase Project
1. Buka: https://console.firebase.google.com/
2. Klik **Add project** atau pilih project existing
3. Nama project: **Komikita** (atau sesuai keinginan)
4. Google Analytics: Optional (bisa di-skip)
5. Klik **Create project**

#### Step 2: Add Android App
1. Di Firebase project dashboard, klik **Android icon** atau **Add app**
2. Isi form:
   - **Android package name**: `com.example.komikita`
   - **App nickname**: Komikita (optional)
   - **Debug signing certificate SHA-1**: `89:D7:9E:42:B8:B9:40:57:58:37:5A:34:B0:D3:72:CD:31:97:B0:49`
3. Klik **Register app**

#### Step 3: Download google-services.json
1. Download file **google-services.json** yang ditampilkan
2. **REPLACE** file yang lama di: `/home/rizal/MyProject/Aplikasi/Komikita/app/google-services.json`
3. Pastikan file ada di folder `app/` (bukan di root project)

#### Step 4: Enable Authentication
1. Di sidebar Firebase Console, pilih **Build** > **Authentication**
2. Klik **Get started**
3. Tab **Sign-in method**
4. Klik **Google** > **Enable**
5. **Project support email**: Pilih email kamu
6. Klik **Save**

#### Step 5: Rebuild App
```bash
cd /home/rizal/MyProject/Aplikasi/Komikita
./gradlew clean
./gradlew assembleDebug
```

#### Step 6: Install & Test
```bash
# Install APK ke device/emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Test login dengan Google
```

✅ **Selesai! Google Sign-In sekarang harusnya berfungsi.**

---

### ☁️ CARA 2: GOOGLE CLOUD CONSOLE

#### Step 1: Buka Google Cloud Console
1. URL: https://console.cloud.google.com/
2. Login dengan Google account

#### Step 2: Create/Select Project
1. Di top bar, klik **Select a project** > **NEW PROJECT**
2. Project name: **Komikita**
3. Klik **CREATE**

#### Step 3: Enable Google Sign-In API
1. Di sidebar: **APIs & Services** > **Library**
2. Search: **Google Sign-In API** atau **Google Identity**
3. Klik > **ENABLE**

#### Step 4: Configure OAuth Consent Screen
1. Sidebar: **APIs & Services** > **OAuth consent screen**
2. User Type: **External** (untuk testing)
3. Klik **CREATE**
4. Form isi:
   - **App name**: Komikita
   - **User support email**: email kamu
   - **Developer contact email**: email kamu
5. Klik **SAVE AND CONTINUE** sampai selesai

#### Step 5: Create OAuth 2.0 Client ID
1. Sidebar: **APIs & Services** > **Credentials**
2. Klik **+ CREATE CREDENTIALS** > **OAuth client ID**
3. Application type: **Android**
4. Isi form:
   - **Name**: Komikita Android Client
   - **Package name**: `com.example.komikita`
   - **SHA-1 certificate fingerprint**: `89:D7:9E:42:B8:B9:40:57:58:37:5A:34:B0:D3:72:CD:31:97:B0:49`
5. Klik **CREATE**
6. **CATAT Client ID** yang muncul (format: `xxx-xxx.apps.googleusercontent.com`)

#### Step 6: Update google-services.json
Option 1: Download dari Firebase (ikuti Cara 1 Step 3)
Option 2: Edit manual `app/google-services.json`:
- Ganti `client_id` di dalam `oauth_client` array dengan Client ID yang baru
- Ganti `project_id`, `project_number` sesuai project Google Cloud

#### Step 7: Rebuild & Test
```bash
./gradlew clean assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🧪 CARA TEST APAKAH SUDAH BENAR

### 1. Lihat Setup Info (New Feature!)
Di Login screen:
- **Long press pada logo Komikita** (tahan 2-3 detik)
- Akan muncul screen **"Google Sign-In Setup"**
- Ada tombol **Copy Package Name** dan **Copy SHA-1**
- Gunakan untuk paste ke Firebase/Google Console

### 2. Test Google Sign-In
1. Klik **"Sign in with Google"**
2. Pilih akun Google
3. **Jika berhasil**: Muncul screen Register untuk lengkapi profil
4. **Jika gagal**: Lihat error message yang detail:
   - Error Code 12500: SHA-1/Client ID belum didaftarkan
   - Error Code 12501: User cancel
   - Error Code 10: google-services.json invalid

### 3. Cek Logcat (jika masih error)
```bash
adb logcat | grep LoginActivity
```
Lihat error message lengkap di console.

---

## 📝 INFORMASI PENTING

### Package Name
```
com.example.komikita
```
⚠️ **HARUS 100% sama** di:
- `app/build.gradle.kts` → `applicationId`
- `google-services.json` → `client[].client_info.android_client_info.package_name`
- Google Cloud/Firebase Console → OAuth Client ID

### SHA-1 Fingerprint (Debug Keystore)
```
89:D7:9E:42:B8:B9:40:57:58:37:5A:34:B0:D3:72:CD:31:97:B0:49
```
⚠️ **HARUS 100% sama** di:
- Firebase Console → Android App registration
- Google Cloud Console → OAuth Client ID for Android

### Cara Cek SHA-1 Sendiri
```bash
cd /home/rizal/MyProject/Aplikasi/Komikita
./gradlew signingReport
```
Output akan tampilkan SHA-1 dan SHA-256.

### File google-services.json
Lokasi: `/home/rizal/MyProject/Aplikasi/Komikita/app/google-services.json`

⚠️ **HARUS**:
- Ada di folder `app/` (bukan di root)
- Package name = `com.example.komikita`
- Client ID valid dari Firebase/Google Cloud

---

## 🐛 TROUBLESHOOTING

### Error: "Sign in failed. Please try again" (Code 12500)
**Penyebab**: SHA-1 fingerprint tidak cocok atau Client ID belum terdaftar

**Solusi**:
1. Pastikan SHA-1 yang didaftarkan **PERSIS SAMA** dengan output `./gradlew signingReport`
2. Tunggu 5-10 menit setelah create OAuth Client ID (propagation time)
3. Clear cache app: Settings > Apps > Komikita > Clear Data
4. Uninstall app, install ulang APK baru

### Error: "Developer error" (Code 10)
**Penyebab**: google-services.json tidak valid atau package name salah

**Solusi**:
1. Download ulang google-services.json dari Firebase Console
2. Cek package name di google-services.json: **HARUS** `com.example.komikita`
3. Rebuild: `./gradlew clean assembleDebug`

### Error: "Sign in cancelled" (Code 12501)
**Penyebab**: User cancel atau back button ditekan

**Solusi**: Ini normal, user memang cancel. Coba lagi.

### Error: No error code, tapi langsung balik ke Login
**Penyebab**: Biasanya SHA-1 tidak cocok

**Solusi**:
1. Cek logcat: `adb logcat | grep "SignInActivity\|GoogleSignIn"`
2. Pastikan SHA-1 di Firebase/Google Console **PERSIS SAMA**
3. Rebuild dari awal: clean + assembleDebug

---

## 📱 SETELAH BERHASIL SETUP

### Flow Normal:
1. **Splash Screen** → Auto-login jika sudah pernah login
2. **Login Screen** → Klik "Sign in with Google"
3. **Google Account Picker** → Pilih akun
4. **Register Screen** → Isi nama (min 3 karakter), foto opsional
5. **Dashboard** → Aplikasi siap digunakan

### Features yang Berfungsi:
- ✅ Login dengan Google
- ✅ Register dengan validasi form
- ✅ Auto-login di splash
- ✅ Profile dengan nama, email, foto dari Google
- ✅ Dark mode toggle
- ✅ Logout (sign out Google + clear local data)

---

## 🎉 KESIMPULAN

Google Sign-In **TIDAK AKAN BERFUNGSI** tanpa setup di Firebase/Google Cloud Console!

**Yang WAJIB dilakukan:**
1. ✅ Daftar aplikasi di Firebase Console atau Google Cloud Console
2. ✅ Create OAuth 2.0 Client ID untuk Android
3. ✅ Daftarkan SHA-1 fingerprint: `89:D7:9E:42:B8:B9:40:57:58:37:5A:34:B0:D3:72:CD:31:97:B0:49`
4. ✅ Daftarkan package name: `com.example.komikita`
5. ✅ Download dan replace google-services.json
6. ✅ Enable Google Authentication (jika pakai Firebase)
7. ✅ Rebuild aplikasi
8. ✅ Test login

**Tanpa langkah di atas, Google Sign-In akan selalu gagal dengan error code 12500!**

---

Jika masih bermasalah setelah ikuti semua langkah di atas, silakan share:
- Screenshot error message
- Output dari `adb logcat | grep LoginActivity`
- Screenshot konfigurasi Firebase/Google Cloud Console

Good luck! 🚀
