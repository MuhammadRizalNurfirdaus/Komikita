package com.example.komikita

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class KOMIKITA.
 *
 * Annotation @HiltAndroidApp diperlukan untuk menginisialisasi Hilt.
 * Ini HARUS direferensikan di AndroidManifest.xml pada tag <application android:name=".KomikitaApplication">.
 *
 * Tanpa ini, Hilt tidak bisa menyuntikkan dependency ke ViewModel, Repository, dll.
 */
@HiltAndroidApp
class KomikitaApplication : Application()
