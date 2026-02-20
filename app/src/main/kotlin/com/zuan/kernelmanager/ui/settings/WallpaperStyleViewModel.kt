/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.settings

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zuan.kernelmanager.ui.theme.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WallpaperStyleViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsPreference = SettingsPreference.getInstance(application)
    private val context = application.applicationContext

    // --- STATES ---
    val themeMode = settingsPreference.themeMode
    val isDynamicColor = settingsPreference.isDynamicColor
    
    val bgType = settingsPreference.bgType
    val solidColor = settingsPreference.solidColor
    val expressiveThemeId = settingsPreference.expressiveThemeId
    
    val isCustomBackground = settingsPreference.isCustomBackground
    val backgroundImageUri = settingsPreference.backgroundImageUri
    val isVideoWallpaper = settingsPreference.isVideoWallpaper
    
    val cardDarkness = settingsPreference.cardDarkness
    val isBgBlur = settingsPreference.isBgBlur
    val bgContrast = settingsPreference.bgContrast
    
    // [BARU] State untuk Card Blur
    val isCardBlur = settingsPreference.isHazeEnabled

    // [BARU] Weather Effect States
    val weatherEffect = settingsPreference.weatherEffect
    val weatherIntensity = settingsPreference.weatherIntensity

    // [BARU] Background Adjustment States
    val bgSaturation = settingsPreference.bgSaturation
    val blurStrength = settingsPreference.blurStrength

    // [BARU] Custom Color States
    val isCustomColor = settingsPreference.isCustomColor
    val customPrimaryColor = settingsPreference.customPrimaryColor
    val customSecondaryColor = settingsPreference.customSecondaryColor
    val customTertiaryColor = settingsPreference.customTertiaryColor

    val currentThemeColor: StateFlow<AppThemeColor> = settingsPreference.themeColorName
        .map { name -> availableColors.find { it.name == name } ?: availableColors[1] }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), availableColors[1])

    val isBlurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    // [BARU] Get effective theme color (considering custom colors)
    val effectiveThemeColor: StateFlow<AppThemeColor> = combineStates(
        isCustomColor,
        customPrimaryColor,
        customSecondaryColor,
        customTertiaryColor,
        currentThemeColor
    ) { isCustom, primary, secondary, tertiary, preset ->
        if (isCustom) {
            AppThemeColor(
                name = "Custom",
                primary = Color(primary),
                secondary = Color(secondary),
                tertiary = Color(tertiary)
            )
        } else {
            preset
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), availableColors[1])

    // --- ACTIONS ---

    fun setThemeMode(mode: ThemeMode) { viewModelScope.launch { settingsPreference.setThemeMode(mode) } }
    fun setDynamicColor(enabled: Boolean) { 
        viewModelScope.launch { 
            settingsPreference.setDynamicColorEnabled(enabled)
            if (enabled) {
                settingsPreference.setIsCustomColor(false)
            }
        } 
    }
    fun setThemeColor(color: AppThemeColor) { 
        viewModelScope.launch { 
            settingsPreference.setThemeColor(color.name)
            settingsPreference.setIsCustomColor(false)
            settingsPreference.setDynamicColorEnabled(false)
        } 
    }
    
    // [BARU] Custom Color Actions
    fun setCustomColorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreference.setIsCustomColor(enabled)
            if (enabled) {
                settingsPreference.setDynamicColorEnabled(false)
            }
        }
    }
    
    fun setCustomPrimaryColor(color: Color) {
        viewModelScope.launch {
            settingsPreference.setCustomPrimaryColor(color.toArgb())
        }
    }
    
    fun setCustomSecondaryColor(color: Color) {
        viewModelScope.launch {
            settingsPreference.setCustomSecondaryColor(color.toArgb())
        }
    }
    
    fun setCustomTertiaryColor(color: Color) {
        viewModelScope.launch {
            settingsPreference.setCustomTertiaryColor(color.toArgb())
        }
    }
    
    fun setAllCustomColors(primary: Color, secondary: Color, tertiary: Color) {
        viewModelScope.launch {
            settingsPreference.setCustomPrimaryColor(primary.toArgb())
            settingsPreference.setCustomSecondaryColor(secondary.toArgb())
            settingsPreference.setCustomTertiaryColor(tertiary.toArgb())
            settingsPreference.setIsCustomColor(true)
            settingsPreference.setDynamicColorEnabled(false)
        }
    }
    
    fun setBgType(type: BgType) { viewModelScope.launch { settingsPreference.setBgType(type) } }
    fun setSolidColor(color: Int) { viewModelScope.launch { settingsPreference.setSolidColor(color) } }
    fun setExpressiveThemeId(id: Int) { viewModelScope.launch { settingsPreference.setExpressiveThemeId(id) } }
    
    fun setBgBlurEnabled(enabled: Boolean) { viewModelScope.launch { settingsPreference.setBgBlurEnabled(enabled) } }
    fun setBgContrast(value: Float) { viewModelScope.launch { settingsPreference.setBgContrast(value) } }
    fun setCardDarkness(value: Float) { viewModelScope.launch { settingsPreference.setCardDarkness(value) } }

    // [BARU] Toggle Card Blur
    fun setCardBlurEnabled(enabled: Boolean) { viewModelScope.launch { settingsPreference.setHazeEnabled(enabled) } }

    // [BARU] Weather Effect Actions
    fun setWeatherEffect(effect: WeatherEffect) {
        viewModelScope.launch { settingsPreference.setWeatherEffect(effect) }
    }
    
    fun setWeatherIntensity(intensity: Float) {
        viewModelScope.launch { settingsPreference.setWeatherIntensity(intensity) }
    }

    // [BARU] Background Adjustment Actions
    fun setBgSaturation(saturation: Float) {
        viewModelScope.launch { settingsPreference.setBgSaturation(saturation) }
    }
    
    fun setBlurStrength(strength: Float) {
        viewModelScope.launch { settingsPreference.setBlurStrength(strength) }
    }

    fun setBackgroundImage(uri: Uri) {
        viewModelScope.launch {
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                settingsPreference.setBackgroundImageUri(uri.toString())
                settingsPreference.setBgType(BgType.GALLERY)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // [BARU] Set background media dengan video detection
    fun setBackgroundMedia(uri: Uri) {
        viewModelScope.launch {
            try {
                // 1. Persist Permission
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)

                // 2. Cek Mime Type
                val mimeType = context.contentResolver.getType(uri)
                val isVideo = mimeType?.startsWith("video") == true

                // 3. Simpan
                settingsPreference.setBackgroundImageUri(uri.toString())
                settingsPreference.setIsVideoWallpaper(isVideo)
                settingsPreference.setBgType(BgType.GALLERY)
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback
                settingsPreference.setBackgroundImageUri(uri.toString())
                val isVideo = uri.toString().contains("mp4") || uri.toString().contains("mkv") || uri.toString().contains("webm")
                settingsPreference.setIsVideoWallpaper(isVideo)
                settingsPreference.setBgType(BgType.GALLERY)
            }
        }
    }
}

// Helper function to combine multiple StateFlows
inline fun <T1, T2, T3, T4, T5, R> combineStates(
    flow1: StateFlow<T1>,
    flow2: StateFlow<T2>,
    flow3: StateFlow<T3>,
    flow4: StateFlow<T4>,
    flow5: StateFlow<T5>,
    crossinline transform: (T1, T2, T3, T4, T5) -> R
): kotlinx.coroutines.flow.Flow<R> = kotlinx.coroutines.flow.combine(
    flow1, flow2, flow3, flow4, flow5
) { v1, v2, v3, v4, v5 ->
    @Suppress("UNCHECKED_CAST")
    transform(v1 as T1, v2 as T2, v3 as T3, v4 as T4, v5 as T5)
}
