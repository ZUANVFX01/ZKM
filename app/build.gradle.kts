plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.zuan.kernelmanager"
    compileSdk = 36  // Disarankan 34, bukan 36

    defaultConfig {
        applicationId = "com.zuan.kernelmanager"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // DEFAULT VALUE - TARUH DI SINI
        buildConfigField("boolean", "ENABLE_BETA_FEATURES", "true")
    }

    // TAMBAH BUILD TYPES YANG BENAR
    buildTypes {
        debug {
            // Untuk debug build
            buildConfigField("boolean", "ENABLE_BETA_FEATURES", "true")
        }
        release {
            // Untuk release build  
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "ENABLE_BETA_FEATURES", "false")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    
    kotlin {
        jvmToolchain(21)  // LEBIH SIMPLE
    }
}

dependencies {
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    
    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    
    // Compose Testing
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation("dev.chrisbanes.haze:haze:1.7.0") // Atau versi terbaru
implementation("dev.chrisbanes.haze:haze-materials:1.7.0")
    
    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Kotlin
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    // Third Party
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.libsu.core)
    implementation(libs.composables.core)
}