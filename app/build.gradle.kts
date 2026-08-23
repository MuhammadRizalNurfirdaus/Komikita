plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.compose.compiler)
}

import java.util.Properties
import java.io.FileInputStream

android {
    namespace = "com.komikita.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.komikita.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Vector drawable support untuk Compose
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
        viewBinding = true   // Legacy - dipertahankan sementara
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Inject API base URL dari local.properties
val localProps = Properties()
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) {
    localProps.load(FileInputStream(localPropsFile))
}

// URL Scraper API (konten publik read-only)
val scraperBaseUrl = localProps.getProperty("SCRAPER_BASE_URL")
    ?: "https://scraper.asepharyana.my.id/api/"

// URL Backend API (PostgreSQL via REST API - JANGAN pernah koneksi langsung ke DB!)
val backendBaseUrl = localProps.getProperty("BACKEND_BASE_URL")
    ?: "https://api.komikita.example.com/"

android.defaultConfig {
    buildConfigField("String", "SCRAPER_BASE_URL", "\"$scraperBaseUrl\"")
    buildConfigField("String", "BACKEND_BASE_URL", "\"$backendBaseUrl\"")
    buildConfigField("String", "API_BASE_URL", "\"$scraperBaseUrl\"") // Legacy compatibility
}

dependencies {
    // === CORE ANDROID ===
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)

    // === JETPACK COMPOSE (Material 3) ===
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.activity)
    implementation(libs.compose.viewmodel)
    implementation(libs.compose.runtime.livedata)
    implementation(libs.compose.navigation)
    debugImplementation(libs.compose.ui.tooling)

    // === LIFECYCLE ===
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.runtime.ktx)

    // === HILT (Dependency Injection) ===
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // === NETWORKING (Retrofit2 + OkHttp + Jsoup) ===
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
    implementation(libs.jsoup)

    // === COROUTINES ===
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // === COIL (Image Loading untuk Compose) ===
    implementation(libs.coil.compose)
    implementation(libs.coil)

    // === ROOM DATABASE (Cache/Offline Lokal) ===
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // === DATASTORE (Preferensi User Modern) ===
    implementation(libs.datastore.preferences)

    // === WORKMANAGER (Background Download Chapter Offline) ===
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // === GOOGLE SIGN-IN & FIREBASE AUTH ===
    implementation(libs.play.services.auth)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // === TESTING ===
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)

    // === LEGACY (dipertahankan untuk kompatibilitas code lama) ===
    implementation(libs.recyclerview)
    implementation(libs.glide)
    implementation(libs.androidx.constraintlayout)
}
