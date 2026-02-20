/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.about.opensource

data class OpenSourceContributor(
    val username: String,
    val githubUrl: String,
    val avatarUrl: String,
    val contribution: String,
    val repository: String? = null,
    val license: String = "GPL-3.0"
)

data class OpenSourceLibrary(
    val name: String,
    val author: String,
    val license: String,
    val url: String,
    val description: String
)

object OpenSourceUtils {
    
    fun getContributors(): List<OpenSourceContributor> = listOf(
        OpenSourceContributor(
            username = "Rve27",
            githubUrl = "https://github.com/Rve27",
            avatarUrl = "https://avatars.githubusercontent.com/Rve27",
            contribution = "Core Developer & Kernel Features",
            repository = "Kernel Manager Core",
            license = "GPL-3.0"
        ),
        OpenSourceContributor(
            username = "libxzr",
            githubUrl = "https://github.com/libxzr",
            avatarUrl = "https://avatars.githubusercontent.com/libxzr",
            contribution = "Kernel Flasher",
            repository = "Horizon Flasher",
            license = "GPL-3.0"
        ),
        OpenSourceContributor(
            username = "5ec1cff",
            githubUrl = "https://github.com/5ec1cff",
            avatarUrl = "https://avatars.githubusercontent.com/5ec1cff",
            contribution = "KsuWebUI Standalone",
            repository = "KsuWebUIStandalone",
            license = "GPL-3.0"
        ),
        OpenSourceContributor(
            username = "Rem01Gaming",
            githubUrl = "https://github.com/Rem01Gaming",
            avatarUrl = "https://avatars.githubusercontent.com/Rem01Gaming",
            contribution = "The logic of how Mediatek works",
            repository = "Origami Kernel Manager",
            license = "GPL-3.0"
        ),
        OpenSourceContributor(
            username = "helloklf",
            githubUrl = "https://github.com/helloklf",
            avatarUrl = "https://avatars.githubusercontent.com/helloklf",
            contribution = "Kernel Utils, Fps logic, Mediatek Logic.",
            repository = "Kernel Tweaks & Utils",
            license = "GPL-3.0"
        ),
        OpenSourceContributor(
            username = "capntrips",
            githubUrl = "https://github.com/capntrips",
            avatarUrl = "https://avatars.githubusercontent.com/capntrips",
            contribution = "Capntris Flasher",
            repository = "Kernel Flasher",
            license = "Apache-2.0 & GPL-3.0"
        ),
        OpenSourceContributor(
            username = "termux",
            githubUrl = "https://github.com/termux",
            avatarUrl = "https://avatars.githubusercontent.com/termux",
            contribution = "Terminal Emulator",
            repository = "Termux",
            license = "Apache-2.0"
        ),
        OpenSourceContributor(
            username = "Kyant0",
            githubUrl = "https://github.com/Kyant0",
            avatarUrl = "https://avatars.githubusercontent.com/Kyant0",
            contribution = "Capsule iOS Navigation",
            repository = "Android Liquid Glass",
            license = "Apache-2.0"
        )
    )
    
    fun getLibraries(): List<OpenSourceLibrary> = listOf(
        // Core & UI
        OpenSourceLibrary(
            name = "Jetpack Compose",
            author = "Google",
            license = "Apache-2.0",
            url = "https://developer.android.com/jetpack/compose",
            description = "Modern UI toolkit for Android"
        ),
        OpenSourceLibrary(
            name = "Material3 Expressive",
            author = "Google",
            license = "Apache-2.0",
            url = "https://m3.material.io/",
            description = "Material Design 3 components"
        ),
        OpenSourceLibrary(
            name = "AndroidX Navigation",
            author = "Google",
            license = "Apache-2.0",
            url = "https://developer.android.com/guide/navigation",
            description = "Framework for in-app navigation"
        ),
        
        // System & Root
        OpenSourceLibrary(
            name = "libsu",
            author = "topjohnwu",
            license = "Apache-2.0",
            url = "https://github.com/topjohnwu/libsu",
            description = "Android root shell library"
        ),
        OpenSourceLibrary(
            name = "Shizuku API",
            author = "RikkaApps",
            license = "Apache-2.0",
            url = "https://github.com/RikkaApps/Shizuku-API",
            description = "API for using system APIs directly with adb/root privileges"
        ),

        // Visuals & Effects
        OpenSourceLibrary(
            name = "Haze",
            author = "Chris Banes",
            license = "Apache-2.0",
            url = "https://github.com/chrisbanes/haze",
            description = "Glassmorphism blur effects for Jetpack Compose"
        ),
        OpenSourceLibrary(
            name = "Coil",
            author = "Coil Team",
            license = "Apache-2.0",
            url = "https://github.com/coil-kt/coil",
            description = "Image loading for Android backed by Kotlin Coroutines"
        ),
        OpenSourceLibrary(
            name = "Backdrop",
            author = "Kyant0",
            license = "Apache-2.0",
            url = "https://github.com/kyant0/backdrop",
            description = "Backdrop component for Jetpack Compose"
        ),
        OpenSourceLibrary(
            name = "Composables Core",
            author = "Alex Styl",
            license = "Apache-2.0",
            url = "https://github.com/alexstyl/composables",
            description = "Unstyled, fully accessible UI components for Jetpack Compose"
        ),

        // Data & Architecture
        OpenSourceLibrary(
            name = "Kotlinx Coroutines",
            author = "JetBrains",
            license = "Apache-2.0",
            url = "https://github.com/Kotlin/kotlinx.coroutines",
            description = "Library support for Kotlin coroutines"
        ),
        OpenSourceLibrary(
            name = "Kotlinx Serialization",
            author = "JetBrains",
            license = "Apache-2.0",
            url = "https://github.com/Kotlin/kotlinx.serialization",
            description = "Kotlin multiplatform / multi-format serialization"
        ),
        OpenSourceLibrary(
            name = "Room Database",
            author = "Google",
            license = "Apache-2.0",
            url = "https://developer.android.com/training/data-storage/room",
            description = "SQLite object mapping library"
        ),

        // Media
        OpenSourceLibrary(
            name = "Media3 (ExoPlayer)",
            author = "Google",
            license = "Apache-2.0",
            url = "https://github.com/androidx/media",
            description = "Libraries for media playback, including ExoPlayer"
        )
    )
}
