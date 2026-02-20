/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.settings

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zuan.kernelmanager.ui.theme.ThemeMode
import com.zuan.kernelmanager.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsPreference = SettingsPreference.getInstance(application)
    private val context = application.applicationContext

    // --- STATES ---
    val themeMode: StateFlow<ThemeMode> = settingsPreference.themeMode
    val pollingInterval: StateFlow<Long> = settingsPreference.pollingInterval
    val isDynamicColor: StateFlow<Boolean> = settingsPreference.isDynamicColor
    val currentLanguage: StateFlow<String> = settingsPreference.currentLanguageCode
    val appDpi: StateFlow<Int> = settingsPreference.appDpi

    val currentThemeColor: StateFlow<AppThemeColor> = settingsPreference.themeColorName
        .map { name -> availableColors.find { it.name == name } ?: availableColors[1] }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), availableColors[1])

    private val _appVersion = MutableStateFlow("Unknown")
    val appVersion: StateFlow<String> = _appVersion

    val isCustomBackground = settingsPreference.isCustomBackground
    val backgroundImageUri = settingsPreference.backgroundImageUri
    
    val isVideoWallpaper = settingsPreference.isVideoWallpaper
    
    val navBarTransparency = settingsPreference.navBarTransparency
    val cardDarkness = settingsPreference.cardDarkness
    val isBgBlur = settingsPreference.isBgBlur
    val bgContrast = settingsPreference.bgContrast
    val isHazeEnabled = settingsPreference.isHazeEnabled

    val bgType: StateFlow<BgType> = settingsPreference.bgType
    val solidColor: StateFlow<Int> = settingsPreference.solidColor
    val expressiveThemeId: StateFlow<Int> = settingsPreference.expressiveThemeId

    val navStyle = settingsPreference.navStyle
    val navLabelState = settingsPreference.navLabelState
    
    val weatherEffect = settingsPreference.weatherEffect
    val weatherIntensity = settingsPreference.weatherIntensity
    
    val bgSaturation = settingsPreference.bgSaturation
    val blurStrength = settingsPreference.blurStrength
    
    val isCustomColor = settingsPreference.isCustomColor
    val customPrimaryColor = settingsPreference.customPrimaryColor
    val customSecondaryColor = settingsPreference.customSecondaryColor
    val customTertiaryColor = settingsPreference.customTertiaryColor

    // --- UI CONFIG COMBINED ---
    private val visualConfigFlow = combine(
        bgType, solidColor, expressiveThemeId, backgroundImageUri, isVideoWallpaper
    ) { type, color, themeId, uri, isVideo ->
        VisualConfig(type, color, themeId, uri, isVideo)
    }

    private val effectConfigFlow = combine(
        isBgBlur, bgContrast, cardDarkness, isHazeEnabled
    ) { blur, contrast, darkness, haze ->
        EffectConfig(blur, contrast, darkness, haze)
    }
    
    private val weatherConfigFlow = combine(
        weatherEffect, weatherIntensity
    ) { effect, intensity ->
        WeatherConfig(effect, intensity)
    }
    
    private val bgAdjustmentFlow = combine(
        bgSaturation, blurStrength
    ) { saturation, blur ->
        BgAdjustmentConfig(saturation, blur)
    }

    val uiConfig: StateFlow<UiConfig> = combine(
        visualConfigFlow,
        effectConfigFlow,
        weatherConfigFlow,
        bgAdjustmentFlow
    ) { visual, effect, weather, bgAdj ->
        UiConfig(
            bgType = visual.bgType,
            solidColor = visual.solidColor,
            expressiveThemeId = visual.expressiveThemeId,
            customImageUri = visual.customImageUri,
            isBgBlur = effect.isBgBlur,
            bgContrast = effect.bgContrast,
            cardDarkness = effect.cardDarkness,
            isHazeEnabled = effect.isHazeEnabled,
            isVideo = visual.isVideo,
            weatherEffect = weather.effect,
            weatherIntensity = weather.intensity,
            bgSaturation = bgAdj.saturation,
            blurStrength = bgAdj.blurStrength
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiConfig()
    )

    // --- FUNCTIONS ---
    fun setThemeMode(mode: ThemeMode) { viewModelScope.launch { settingsPreference.setThemeMode(mode) } }
    fun setThemeColor(color: AppThemeColor) { viewModelScope.launch { settingsPreference.setThemeColor(color.name) } }
    fun setDynamicColor(enabled: Boolean) { viewModelScope.launch { settingsPreference.setDynamicColorEnabled(enabled) } }
    fun setPollingInterval(interval: Long) { viewModelScope.launch { settingsPreference.setPollingInterval(interval) } }
    fun setAppDpi(dpi: Int) { viewModelScope.launch { settingsPreference.setAppDpi(dpi) } }
    
    fun setHazeEnabled(enabled: Boolean) { viewModelScope.launch { settingsPreference.setHazeEnabled(enabled) } }
    
    fun setNavStyle(style: NavStyle) { viewModelScope.launch { settingsPreference.setNavStyle(style) } }
    fun setNavLabelState(state: NavLabelState) { viewModelScope.launch { settingsPreference.setNavLabelState(state) } }

    fun loadSettingsData(context: Context) { 
        viewModelScope.launch(Dispatchers.IO) { 
            _appVersion.value = Utils.getAppVersion(context)
            validateBackgroundUri() 
        } 
    }

    // [UPDATE] Locale handling untuk semua bahasa baru
    fun setAppLanguage(code: String) {
        viewModelScope.launch {
            settingsPreference.setLanguageCode(code)
            
            val localeList = when (code) {
                "system" -> LocaleListCompat.getEmptyLocaleList()
                // Chinese Variants
                "zh-CN" -> LocaleListCompat.forLanguageTags("zh-CN")
                "zh-SG" -> LocaleListCompat.forLanguageTags("zh-SG")
                "zh-TW" -> LocaleListCompat.forLanguageTags("zh-TW")
                "zh-HK" -> LocaleListCompat.forLanguageTags("zh-HK")
                "zh-MO" -> LocaleListCompat.forLanguageTags("zh-MO")
                // Cyrillic & Eastern European
                "ru" -> LocaleListCompat.forLanguageTags("ru-RU")
                "uk" -> LocaleListCompat.forLanguageTags("uk-UA")
                "be" -> LocaleListCompat.forLanguageTags("be-BY")
                "kk" -> LocaleListCompat.forLanguageTags("kk-KZ")
                "ro-MD" -> LocaleListCompat.forLanguageTags("ro-MD")
                // Existing
                "in" -> LocaleListCompat.forLanguageTags("in-ID")
                "en" -> LocaleListCompat.forLanguageTags("en-US")
                else -> LocaleListCompat.forLanguageTags(code)
            }
            
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }

    fun setCustomBackground(enabled: Boolean) { viewModelScope.launch { settingsPreference.setCustomBackgroundEnabled(enabled) } }

    fun setBackgroundMedia(uri: Uri) {
        viewModelScope.launch {
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)

                val mimeType = context.contentResolver.getType(uri)
                val isVideo = mimeType?.startsWith("video") == true

                settingsPreference.setBackgroundImageUri(uri.toString())
                settingsPreference.setIsVideoWallpaper(isVideo)
                settingsPreference.setBgType(BgType.GALLERY)
            } catch (e: Exception) {
                e.printStackTrace()
                settingsPreference.setBackgroundImageUri(uri.toString())
                val isVideo = uri.toString().contains("mp4") || uri.toString().contains("mkv") || uri.toString().contains("webm")
                settingsPreference.setIsVideoWallpaper(isVideo)
                settingsPreference.setBgType(BgType.GALLERY)
            }
        }
    }

    private fun validateBackgroundUri() {
        val uriString = settingsPreference.backgroundImageUri.value
        val isEnabled = settingsPreference.isCustomBackground.value

        if (isEnabled && uriString != null) {
            try {
                context.contentResolver.openInputStream(Uri.parse(uriString))?.close()
            } catch (e: Exception) {
                viewModelScope.launch { settingsPreference.setCustomBackgroundEnabled(false) }
            }
        }
    }

    fun setNavBarTransparency(value: Float) { viewModelScope.launch { settingsPreference.setNavBarTransparency(value) } }
    fun setCardDarkness(value: Float) { viewModelScope.launch { settingsPreference.setCardDarkness(value) } }
    fun setBgBlurEnabled(enabled: Boolean) { viewModelScope.launch { settingsPreference.setBgBlurEnabled(enabled) } }
    fun setBgContrast(value: Float) { viewModelScope.launch { settingsPreference.setBgContrast(value) } }
    fun setBgType(type: BgType) { viewModelScope.launch { settingsPreference.setBgType(type) } }
    fun setSolidColor(color: Int) { viewModelScope.launch { settingsPreference.setSolidColor(color) } }
    fun setExpressiveThemeId(id: Int) { viewModelScope.launch { settingsPreference.setExpressiveThemeId(id) } }
    
    fun setCardBlurEnabled(enabled: Boolean) { viewModelScope.launch { settingsPreference.setHazeEnabled(enabled) } }
    
    fun setWeatherEffect(effect: WeatherEffect) { viewModelScope.launch { settingsPreference.setWeatherEffect(effect) } }
    fun setWeatherIntensity(intensity: Float) { viewModelScope.launch { settingsPreference.setWeatherIntensity(intensity) } }
    
    fun setBgSaturation(saturation: Float) { viewModelScope.launch { settingsPreference.setBgSaturation(saturation) } }
    fun setBlurStrength(strength: Float) { viewModelScope.launch { settingsPreference.setBlurStrength(strength) } }
    
    fun setIsCustomColor(enabled: Boolean) { viewModelScope.launch { settingsPreference.setIsCustomColor(enabled) } }
    fun setCustomPrimaryColor(color: Int) { viewModelScope.launch { settingsPreference.setCustomPrimaryColor(color) } }
    fun setCustomSecondaryColor(color: Int) { viewModelScope.launch { settingsPreference.setCustomSecondaryColor(color) } }
    fun setCustomTertiaryColor(color: Int) { viewModelScope.launch { settingsPreference.setCustomTertiaryColor(color) } }
}

// Helper Classes
private data class VisualConfig(
    val bgType: BgType,
    val solidColor: Int,
    val expressiveThemeId: Int,
    val customImageUri: String?,
    val isVideo: Boolean
)

private data class EffectConfig(
    val isBgBlur: Boolean,
    val bgContrast: Float,
    val cardDarkness: Float,
    val isHazeEnabled: Boolean
)

private data class WeatherConfig(
    val effect: WeatherEffect,
    val intensity: Float
)

private data class BgAdjustmentConfig(
    val saturation: Float,
    val blurStrength: Float
)
