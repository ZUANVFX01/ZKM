plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.zuan.kernelmanager"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.zuan.kernelmanager"
        minSdk = 29
        targetSdk = 36
        versionCode = 12000
        versionName = "1.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        
        buildConfigField("boolean", "ENABLE_BETA_FEATURES", "true")
    }

    
    buildTypes {
        debug {
            // debug build
            buildConfigField("boolean", "ENABLE_BETA_FEATURES", "true")
        }
        release {
            // release build  
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
        aidl = true
    }
    
    lint {
        disable += "Instantiatable"
    }
    
    packaging {
        jniLibs {
        useLegacyPackaging = true 
            keepDebugSymbols += "**/*.so"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    
    kotlin {
        //jvmToolchain(21)
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
    implementation("androidx.compose.material:material:1.11.0-alpha05")
    
    // KSUWEB
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.webkit)
    implementation(libs.material)
    implementation("org.json:json:20251224") 
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.github.kyant0:backdrop:2.0.0-alpha03")
    // Kalau mau shape kapsul asli Kyant: 
    //implementation("io.github.kyant0:capsule:2.1.3")
    
    // TERMINAL
    implementation(project(":terminal-emulator"))
    implementation(project(":terminal-view"))
    
    // HORIZON KERNEL FLASHER
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    implementation("androidx.media3:media3-exoplayer:1.10.0-alpha01")
    implementation("androidx.media3:media3-ui:1.10.0-alpha01")
    implementation("androidx.media3:media3-common:1.10.0-alpha01")
    implementation(project(":kernel-flasher"))
}