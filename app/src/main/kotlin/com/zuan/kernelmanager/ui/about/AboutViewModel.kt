/*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.ui.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Model data sederhana untuk item di halaman About
data class AboutItem(
    val title: String,
    val description: String,
    val iconUrl: String? = null, // Bisa null jika tidak ada gambar
    val url: String? = null,     // Link jika item diklik
    val type: ItemType = ItemType.INFO
)

enum class ItemType {
    HEADER, DEVELOPER, CREDIT, LINK, INFO
}

class AboutViewModel : ViewModel() {

    private val _aboutItems = MutableStateFlow<List<AboutItem>>(emptyList())
    val aboutItems: StateFlow<List<AboutItem>> = _aboutItems.asStateFlow()

    init {
        loadAboutInfo()
    }

    private fun loadAboutInfo() {
        viewModelScope.launch {
            // Kita hardcode data di sini karena ini halaman About
            val items = listOf(
                // --- SECTION: HEADER APP ---
                AboutItem(
                    title = "Zuan Kernel Manager",
                    description = "Version 1.0.0 (Beta)",
                    iconUrl = "https://raw.githubusercontent.com/ZUANVFX01/ZKM/main/logo/logo.jpg", // Icon ZKM/Github mu
                    type = ItemType.HEADER
                ),

                // --- SECTION: DEVELOPER (YOU) ---
                AboutItem(
                    title = "ZuanVFX01",
                    description = "Lead Developer & Logic Architect.\nBuilding complex apps on Android.",
                    iconUrl = "https://avatars.githubusercontent.com/u/207377902?v=4", // Avatar Kamu
                    url = "https://github.com/ZUANVFX01",
                    type = ItemType.DEVELOPER
                ),

                // --- SECTION: CREDITS (Etika Open Source) ---
                AboutItem(
                    title = "Rve27",
                    description = "Original Developer of Kernel Manager.\nBase functionality credit.",
                    iconUrl = "https://github.com/rve27.png",
                    url = "https://github.com/rve27",
                    type = ItemType.CREDIT
                ),
                AboutItem(
                    title = "Rem01Gaming",
                    description = "Origami Kernel Manager.\nFeature inspiration & adaptation.",
                    iconUrl = "https://github.com/Rem01Gaming.png",
                    url = "https://github.com/Rem01Gaming",
                    type = ItemType.CREDIT
                ),
                
                AboutItem(
                    title = "helloklf",
                    description = "Creator of vtools.\nSource for FPS Meter & MediaTek GPU Logic.",
                    iconUrl = "https://github.com/helloklf.png", // Mengambil avatar github otomatis
                    url = "https://github.com/helloklf/vtools",  // Mengarah ke repo vtools atau profilnya
                    type = ItemType.CREDIT
                ),

                // --- SECTION: LINKS ---
                AboutItem(
                    title = "Source Code",
                    description = "View on GitHub",
                    iconUrl = null,
                    url = "https://github.com/ZUANVFX01/ZKM",
                    type = ItemType.LINK
                ),
                AboutItem(
                    title = "Telegram Channel",
                    description = "Join our community",
                    iconUrl = null,
                    url = "https://t.me/ZuanKernelManager", // Ganti dengan link TG mu
                    type = ItemType.LINK
                )
            )
            _aboutItems.value = items
        }
    }
}
