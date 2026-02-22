plugins {
    // --- UBAH DARI alias(...) JADI id("...") ---
    // Jangan pake version/alias, biar dia pake yg punya Zuan
    id("com.android.library") 
    id("org.jetbrains.kotlin.android")

    // --- SISANYA TETAP BOLEH PAKE ALIAS ---
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0"
}

android {
    // JANGAN UBAH NAMESPACE INI (Biar kodingan lama gak error)
    namespace = "com.github.capntrips.kernelflasher"
    compileSdk = 36 

    defaultConfig {
        // HAPUS applicationId (Library tidak boleh punya ID)
        minSdk = 29
        
        // Hapus versionCode & versionName (Library ikut app utama)
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
        
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // Room Schema Config
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Proguard rules library
            consumerProguardFiles("consumer-rules.pro")
        }
    }

        sourceSets {
        getByName("main") {
            // Baris ini tidak error, tapi karena kita pindah ke assets, 
            // folder jniLibs nanti kosong. Itu tidak masalah.
            jniLibs.srcDirs("src/main/jniLibs") 
            assets.srcDirs("src/main/assets") // Pastikan assets terbaca
        }
    }


    buildFeatures {
        aidl = true    // WAJIB TRUE (Buat IFilesystemService)
        compose = true // WAJIB TRUE (Buat UI)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
       //jvmTarget = "21"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            // Kita set false biar modern, tapi kalau crash balikin ke true
            useLegacyPackaging = true
        }
    }
}

dependencies {
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.appcompat)
    
    
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
    
    // Haze
    implementation(libs.haze)
    implementation(libs.haze.materials)
    
    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Kotlin
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    
    //Shizuku
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    
    //Service
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.savedstate.ktx)

    // Third Party
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.libsu.core)
    implementation(libs.libsu.service)
    implementation(libs.libsu.nio)
    implementation(libs.composables.core)
    
    // KSUWEB
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.webkit)
    implementation(libs.material)
    implementation("org.json:json:20251224") 
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.github.kyant0:backdrop:1.0.4")
    // Kalau mau shape kapsul asli Kyant: 
    //implementation("io.github.kyant0:capsule:2.1.2")
    
    // TERMINAL
    //implementation(project(":terminal-emulator"))
    //implementation(project(":terminal-view"))
    
    // HORIZON KERNEL FLASHER
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Document File
    implementation(libs.androidx.documentfile)
    implementation("com.squareup.okhttp3:okhttp:5.3.2") 
}
