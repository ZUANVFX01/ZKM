/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.edit
import com.zuan.kernelmanager.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// [UPDATE] Enums untuk Navigasi
enum class NavStyle { LIQUID_CAPSULE, CLASSIC_MATERIAL, MODERN_TABS }
enum class NavLabelState { ALWAYS_SHOW, ONLY_SELECTED, ALWAYS_HIDE }

// [UPDATE] Enums untuk Weather Effects
enum class WeatherEffect { NONE, FOG, RAIN, SNOW, SUN_RAYS }

// [UPDATE] UiConfig sekarang punya properti isVideo dan weather effects
 data class UiConfig(
    val bgType: BgType = BgType.SYSTEM,
    val solidColor: Int = 0xFF1A1A1A.toInt(),
    val expressiveThemeId: Int = 0,
    val customImageUri: String? = null,
    val isBgBlur: Boolean = false,
    val bgContrast: Float = 0f,
    val cardDarkness: Float = 0.15f,
    val isHazeEnabled: Boolean = true,
    val isVideo: Boolean = false,
    // [BARU] Weather Effects
    val weatherEffect: WeatherEffect = WeatherEffect.NONE,
    val weatherIntensity: Float = 0.5f,
    // [BARU] Background adjustments
    val bgSaturation: Float = 1f,
    val blurStrength: Float = 20f,
    // [BARU] Custom Colors
    val isCustomColor: Boolean = false,
    val customPrimaryColor: Int = Color(0xFF4A6595).toArgb(),
    val customSecondaryColor: Int = Color(0xFF586275).toArgb(),
    val customTertiaryColor: Int = Color(0xFF5E4B8B).toArgb()
)

// Enum Background
enum class BgType { SYSTEM, PRESET_SOLID, EXPRESSIVE, GALLERY }

// Data Class Warna
 data class AppThemeColor(
    val name: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color
)

// [UPDATE] Expanded color palette - 16 warna + dynamic
val availableColors = listOf(
    // Original colors
    AppThemeColor("Blue", Color(0xFF4A6595), Color(0xFF586275), Color(0xFF5E4B8B)),
    AppThemeColor("Green", Color(0xFF556B2F), Color(0xFF4F6A55), Color(0xFF356A4F)),
    AppThemeColor("Purple", Color(0xFF7B52AB), Color(0xFF685675), Color(0xFF8B4C70)),
    AppThemeColor("Orange", Color(0xFFB36B36), Color(0xFF8B6B55), Color(0xFF9C551F)),
    AppThemeColor("Pink", Color(0xFFBC4C73), Color(0xFF9C4C66), Color(0xFF8B4C66)),
    AppThemeColor("Gray", Color(0xFF6B6B6B), Color(0xFF555555), Color(0xFF3E3E3E)),
    AppThemeColor("Yellow", Color(0xFF9C8B1F), Color(0xFF7B6B1F), Color(0xFF6B551F)),
    // [BARU] Additional colors
    AppThemeColor("Light Green", Color(0xFF7CB342), Color(0xFF689F38), Color(0xFF558B2F)),
    AppThemeColor("Dark Green", Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF388E3C)),
    AppThemeColor("Teal", Color(0xFF00897B), Color(0xFF00796B), Color(0xFF00695C)),
    AppThemeColor("Cyan", Color(0xFF00ACC1), Color(0xFF0097A7), Color(0xFF00838F)),
    AppThemeColor("Light Blue", Color(0xFF29B6F6), Color(0xFF03A9F4), Color(0xFF0288D1)),
    AppThemeColor("Indigo", Color(0xFF5C6BC0), Color(0xFF3F51B5), Color(0xFF3949AB)),
    AppThemeColor("Deep Purple", Color(0xFF7E57C2), Color(0xFF673AB7), Color(0xFF5E35B1)),
    AppThemeColor("Magenta", Color(0xFFD81B60), Color(0xFFC2185B), Color(0xFFAD1457)),
    AppThemeColor("Rose", Color(0xFFF06292), Color(0xFFEC407A), Color(0xFFE91E63)),
    AppThemeColor("Coral", Color(0xFFFF7043), Color(0xFFFF5722), Color(0xFFF4511E)),
    AppThemeColor("Amber", Color(0xFFFFB300), Color(0xFFFFA000), Color(0xFFFF8F00)),
    AppThemeColor("Lime", Color(0xFFAFB42B), Color(0xFF9E9D24), Color(0xFF827717)),
    AppThemeColor("Mint", Color(0xFF26A69A), Color(0xFF4DB6AC), Color(0xFF80CBC4)),
    AppThemeColor("Navy", Color(0xFF1A237E), Color(0xFF283593), Color(0xFF303F9F)),
    AppThemeColor("Slate", Color(0xFF455A64), Color(0xFF546E7A), Color(0xFF607D8B)),
    AppThemeColor("Charcoal", Color(0xFF37474F), Color(0xFF455A64), Color(0xFF263238)),
    AppThemeColor("Burgundy", Color(0xFF880E4F), Color(0xFFAD1457), Color(0xFFC2185B)),
    AppThemeColor("Peach", Color(0xFFFFAB91), Color(0xFFFF8A65), Color(0xFFFF7043))
)

class SettingsPreference(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    // --- STATES ---
    private val _themeMode = MutableStateFlow(getThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _themeColorName = MutableStateFlow(getThemeColorName())
    val themeColorName: StateFlow<String> = _themeColorName.asStateFlow()

    private val _isDynamicColor = MutableStateFlow(getDynamicColorEnabled())
    val isDynamicColor: StateFlow<Boolean> = _isDynamicColor.asStateFlow()

    private val _pollingInterval = MutableStateFlow(getPollingInterval())
    val pollingInterval: StateFlow<Long> = _pollingInterval.asStateFlow()

    private val _currentLanguageCode = MutableStateFlow(getLanguageCode())
    val currentLanguageCode: StateFlow<String> = _currentLanguageCode.asStateFlow()

    private val _appDpi = MutableStateFlow(getAppDpi())
    val appDpi: StateFlow<Int> = _appDpi.asStateFlow()

    // --- VISUAL STATES ---
    private val _isCustomBackground = MutableStateFlow(getCustomBackgroundEnabled())
    val isCustomBackground: StateFlow<Boolean> = _isCustomBackground.asStateFlow()

    private val _backgroundImageUri = MutableStateFlow(getBackgroundImageUri())
    val backgroundImageUri: StateFlow<String?> = _backgroundImageUri.asStateFlow()

    // [BARU] State Video Flag
    private val _isVideoWallpaper = MutableStateFlow(getIsVideoWallpaper())
    val isVideoWallpaper: StateFlow<Boolean> = _isVideoWallpaper.asStateFlow()

    private val _navBarTransparency = MutableStateFlow(getNavBarTransparency())
    val navBarTransparency: StateFlow<Float> = _navBarTransparency.asStateFlow()

    private val _cardDarkness = MutableStateFlow(getCardDarkness())
    val cardDarkness: StateFlow<Float> = _cardDarkness.asStateFlow()

    private val _isBgBlur = MutableStateFlow(getBgBlurEnabled())
    val isBgBlur: StateFlow<Boolean> = _isBgBlur.asStateFlow()

    private val _bgContrast = MutableStateFlow(getBgContrast())
    val bgContrast: StateFlow<Float> = _bgContrast.asStateFlow()
    
    private val _isHazeEnabled = MutableStateFlow(getHazeEnabled())
    val isHazeEnabled: StateFlow<Boolean> = _isHazeEnabled.asStateFlow()

    private val _bgType = MutableStateFlow(getBgType())
    val bgType: StateFlow<BgType> = _bgType.asStateFlow()

    private val _solidColor = MutableStateFlow(getSolidColor())
    val solidColor: StateFlow<Int> = _solidColor.asStateFlow()

    private val _expressiveThemeId = MutableStateFlow(getExpressiveThemeId())
    val expressiveThemeId: StateFlow<Int> = _expressiveThemeId.asStateFlow()

    // --- NAVIGATION STATES ---
    private val _navStyle = MutableStateFlow(getNavStyle())
    val navStyle: StateFlow<NavStyle> = _navStyle.asStateFlow()

    private val _navLabelState = MutableStateFlow(getNavLabelState())
    val navLabelState: StateFlow<NavLabelState> = _navLabelState.asStateFlow()

    // [BARU] WEATHER EFFECT STATES ---
    private val _weatherEffect = MutableStateFlow(getWeatherEffect())
    val weatherEffect: StateFlow<WeatherEffect> = _weatherEffect.asStateFlow()

    private val _weatherIntensity = MutableStateFlow(getWeatherIntensity())
    val weatherIntensity: StateFlow<Float> = _weatherIntensity.asStateFlow()

    // [BARU] BACKGROUND ADJUSTMENT STATES ---
    private val _bgSaturation = MutableStateFlow(getBgSaturation())
    val bgSaturation: StateFlow<Float> = _bgSaturation.asStateFlow()

    private val _blurStrength = MutableStateFlow(getBlurStrength())
    val blurStrength: StateFlow<Float> = _blurStrength.asStateFlow()

    // [BARU] CUSTOM COLOR STATES ---
    private val _isCustomColor = MutableStateFlow(getIsCustomColor())
    val isCustomColor: StateFlow<Boolean> = _isCustomColor.asStateFlow()

    private val _customPrimaryColor = MutableStateFlow(getCustomPrimaryColor())
    val customPrimaryColor: StateFlow<Int> = _customPrimaryColor.asStateFlow()

    private val _customSecondaryColor = MutableStateFlow(getCustomSecondaryColor())
    val customSecondaryColor: StateFlow<Int> = _customSecondaryColor.asStateFlow()

    private val _customTertiaryColor = MutableStateFlow(getCustomTertiaryColor())
    val customTertiaryColor: StateFlow<Int> = _customTertiaryColor.asStateFlow()

    // [BARU] INTRO STATES ---
    private val _hasCompletedIntro = MutableStateFlow(getHasCompletedIntro())
    val hasCompletedIntro: StateFlow<Boolean> = _hasCompletedIntro.asStateFlow()
    
    private val _acceptedTerms = MutableStateFlow(getAcceptedTerms())
    val acceptedTerms: StateFlow<Boolean> = _acceptedTerms.asStateFlow()
    
    private val _acceptedRisk = MutableStateFlow(getAcceptedRisk())
    val acceptedRisk: StateFlow<Boolean> = _acceptedRisk.asStateFlow()

    companion object {
        private const val THEME_KEY = "theme_mode"
        private const val THEME_COLOR_KEY = "theme_color_name"
        private const val DYNAMIC_COLOR_KEY = "dynamic_color_enabled"
        private const val POLLING_INTERVAL_KEY = "soc_polling_interval"
        private const val LANGUAGE_KEY = "app_language_code"
        private const val DPI_KEY = "app_custom_dpi"
        
        private const val CUSTOM_BG_KEY = "custom_bg_enabled"
        private const val BG_URI_KEY = "custom_bg_uri"
        private const val IS_VIDEO_KEY = "is_video_wallpaper_flag"
        
        private const val NAVBAR_TRANS_KEY = "navbar_transparency"
        private const val CARD_DARKNESS_KEY = "card_darkness"
        
        private const val BG_BLUR_KEY = "bg_blur_enabled"
        private const val BG_CONTRAST_KEY = "bg_contrast_value"
        private const val HAZE_ENABLED_KEY = "haze_ui_enabled"

        private const val BG_TYPE_KEY = "bg_type_selection"
        private const val SOLID_COLOR_KEY = "bg_solid_color_value"
        private const val EXPRESSIVE_ID_KEY = "bg_expressive_theme_id"

        private const val NAV_STYLE_KEY = "nav_style_selection"
        private const val NAV_LABEL_KEY = "nav_label_behavior"

        // [BARU] Weather Effect Keys
        private const val WEATHER_EFFECT_KEY = "weather_effect_type"
        private const val WEATHER_INTENSITY_KEY = "weather_effect_intensity"

        // [BARU] Background Adjustment Keys
        private const val BG_SATURATION_KEY = "bg_saturation_value"
        private const val BLUR_STRENGTH_KEY = "blur_strength_value"

        // [BARU] Custom Color Keys
        private const val IS_CUSTOM_COLOR_KEY = "is_custom_color_enabled"
        private const val CUSTOM_PRIMARY_KEY = "custom_primary_color"
        private const val CUSTOM_SECONDARY_KEY = "custom_secondary_color"
        private const val CUSTOM_TERTIARY_KEY = "custom_tertiary_color"

        // [BARU] Intro Keys
        private const val HAS_COMPLETED_INTRO_KEY = "has_completed_intro"
        private const val ACCEPTED_TERMS_KEY = "accepted_terms"
        private const val ACCEPTED_RISK_KEY = "accepted_risk"

        private const val DEFAULT_POLLING_INTERVAL = 3000L
        private const val DEFAULT_DPI = 0
        
        @Volatile
        private var INSTANCE: SettingsPreference? = null

        fun getInstance(context: Context): SettingsPreference {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsPreference(context).also { INSTANCE = it }
            }
        }
    }

    // --- Getters/Setters ---
    fun setThemeMode(mode: ThemeMode) { prefs.edit { putString(THEME_KEY, mode.name) }; _themeMode.value = mode }
    private fun getThemeMode(): ThemeMode { return try { ThemeMode.valueOf(prefs.getString(THEME_KEY, ThemeMode.SYSTEM_DEFAULT.name)!!) } catch (e: Exception) { ThemeMode.SYSTEM_DEFAULT } }

    fun setThemeColor(name: String) { prefs.edit { putString(THEME_COLOR_KEY, name) }; _themeColorName.value = name }
    private fun getThemeColorName(): String = prefs.getString(THEME_COLOR_KEY, "Green") ?: "Green"

    fun setDynamicColorEnabled(enabled: Boolean) { prefs.edit { putBoolean(DYNAMIC_COLOR_KEY, enabled) }; _isDynamicColor.value = enabled }
    private fun getDynamicColorEnabled(): Boolean = prefs.getBoolean(DYNAMIC_COLOR_KEY, false)

    fun setPollingInterval(interval: Long) { prefs.edit { putLong(POLLING_INTERVAL_KEY, interval) }; _pollingInterval.value = interval }
    private fun getPollingInterval(): Long = prefs.getLong(POLLING_INTERVAL_KEY, DEFAULT_POLLING_INTERVAL)

    fun setLanguageCode(code: String) { prefs.edit { putString(LANGUAGE_KEY, code) }; _currentLanguageCode.value = code }
    private fun getLanguageCode(): String = prefs.getString(LANGUAGE_KEY, "system") ?: "system"

    fun setAppDpi(dpi: Int) { prefs.edit { putInt(DPI_KEY, dpi) }; _appDpi.value = dpi }
    private fun getAppDpi(): Int = prefs.getInt(DPI_KEY, DEFAULT_DPI)

    fun setCustomBackgroundEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(CUSTOM_BG_KEY, enabled) }
        _isCustomBackground.value = enabled
    }
    private fun getCustomBackgroundEnabled(): Boolean = prefs.getBoolean(CUSTOM_BG_KEY, false)

    // [UPDATE] Set Video Flag
    fun setIsVideoWallpaper(isVideo: Boolean) {
        prefs.edit { putBoolean(IS_VIDEO_KEY, isVideo) }
        _isVideoWallpaper.value = isVideo
    }
    private fun getIsVideoWallpaper(): Boolean = prefs.getBoolean(IS_VIDEO_KEY, false)

    fun setBackgroundImageUri(uri: String?) {
        prefs.edit { putString(BG_URI_KEY, uri) }
        _backgroundImageUri.value = uri
        // Reset flag jika URI dihapus
        if (uri == null) setIsVideoWallpaper(false)
    }
    private fun getBackgroundImageUri(): String? = prefs.getString(BG_URI_KEY, null)

    fun setNavBarTransparency(value: Float) { prefs.edit { putFloat(NAVBAR_TRANS_KEY, value) }; _navBarTransparency.value = value }
    private fun getNavBarTransparency(): Float = prefs.toFloat(NAVBAR_TRANS_KEY, 0.5f)

    fun setCardDarkness(value: Float) { prefs.edit { putFloat(CARD_DARKNESS_KEY, value) }; _cardDarkness.value = value }
    private fun getCardDarkness(): Float = prefs.toFloat(CARD_DARKNESS_KEY, 0.5f)

    fun setBgBlurEnabled(enabled: Boolean) { prefs.edit { putBoolean(BG_BLUR_KEY, enabled) }; _isBgBlur.value = enabled }
    private fun getBgBlurEnabled(): Boolean = prefs.getBoolean(BG_BLUR_KEY, false)

    fun setBgContrast(value: Float) { prefs.edit { putFloat(BG_CONTRAST_KEY, value) }; _bgContrast.value = value }
    private fun getBgContrast(): Float = prefs.toFloat(BG_CONTRAST_KEY, 0f)

    fun setHazeEnabled(enabled: Boolean) { prefs.edit { putBoolean(HAZE_ENABLED_KEY, enabled) }; _isHazeEnabled.value = enabled }
    private fun getHazeEnabled(): Boolean = prefs.getBoolean(HAZE_ENABLED_KEY, true)

    fun setBgType(type: BgType) {
        prefs.edit { putString(BG_TYPE_KEY, type.name) }
        _bgType.value = type
        setCustomBackgroundEnabled(type == BgType.GALLERY)
    }
    private fun getBgType(): BgType {
        return try { BgType.valueOf(prefs.getString(BG_TYPE_KEY, BgType.SYSTEM.name)!!) } catch (e: Exception) { BgType.SYSTEM }
    }

    fun setSolidColor(color: Int) { prefs.edit { putInt(SOLID_COLOR_KEY, color) }; _solidColor.value = color }
    private fun getSolidColor(): Int = prefs.getInt(SOLID_COLOR_KEY, Color(0xFF1A1A1A).toArgb())

    fun setExpressiveThemeId(id: Int) { prefs.edit { putInt(EXPRESSIVE_ID_KEY, id) }; _expressiveThemeId.value = id }
    private fun getExpressiveThemeId(): Int = prefs.getInt(EXPRESSIVE_ID_KEY, 0)

    fun setNavStyle(style: NavStyle) { prefs.edit { putString(NAV_STYLE_KEY, style.name) }; _navStyle.value = style }
    private fun getNavStyle(): NavStyle {
        return try { NavStyle.valueOf(prefs.getString(NAV_STYLE_KEY, NavStyle.LIQUID_CAPSULE.name)!!) } catch (e: Exception) { NavStyle.LIQUID_CAPSULE }
    }

    fun setNavLabelState(state: NavLabelState) { prefs.edit { putString(NAV_LABEL_KEY, state.name) }; _navLabelState.value = state }
    private fun getNavLabelState(): NavLabelState {
        return try { NavLabelState.valueOf(prefs.getString(NAV_LABEL_KEY, NavLabelState.ALWAYS_SHOW.name)!!) } catch (e: Exception) { NavLabelState.ALWAYS_SHOW }
    }

    // [BARU] Weather Effect Getters/Setters
    fun setWeatherEffect(effect: WeatherEffect) { prefs.edit { putString(WEATHER_EFFECT_KEY, effect.name) }; _weatherEffect.value = effect }
    private fun getWeatherEffect(): WeatherEffect {
        return try { WeatherEffect.valueOf(prefs.getString(WEATHER_EFFECT_KEY, WeatherEffect.NONE.name)!!) } catch (e: Exception) { WeatherEffect.NONE }
    }

    fun setWeatherIntensity(intensity: Float) { prefs.edit { putFloat(WEATHER_INTENSITY_KEY, intensity) }; _weatherIntensity.value = intensity }
    private fun getWeatherIntensity(): Float = prefs.toFloat(WEATHER_INTENSITY_KEY, 0.5f)

    // [BARU] Background Adjustment Getters/Setters
    fun setBgSaturation(saturation: Float) { prefs.edit { putFloat(BG_SATURATION_KEY, saturation) }; _bgSaturation.value = saturation }
    private fun getBgSaturation(): Float = prefs.toFloat(BG_SATURATION_KEY, 1f)

    fun setBlurStrength(strength: Float) { prefs.edit { putFloat(BLUR_STRENGTH_KEY, strength) }; _blurStrength.value = strength }
    private fun getBlurStrength(): Float = prefs.toFloat(BLUR_STRENGTH_KEY, 20f)

    // [BARU] Custom Color Getters/Setters
    fun setIsCustomColor(enabled: Boolean) { prefs.edit { putBoolean(IS_CUSTOM_COLOR_KEY, enabled) }; _isCustomColor.value = enabled }
    private fun getIsCustomColor(): Boolean = prefs.getBoolean(IS_CUSTOM_COLOR_KEY, false)

    fun setCustomPrimaryColor(color: Int) { prefs.edit { putInt(CUSTOM_PRIMARY_KEY, color) }; _customPrimaryColor.value = color }
    private fun getCustomPrimaryColor(): Int = prefs.getInt(CUSTOM_PRIMARY_KEY, Color(0xFF4A6595).toArgb())

    fun setCustomSecondaryColor(color: Int) { prefs.edit { putInt(CUSTOM_SECONDARY_KEY, color) }; _customSecondaryColor.value = color }
    private fun getCustomSecondaryColor(): Int = prefs.getInt(CUSTOM_SECONDARY_KEY, Color(0xFF586275).toArgb())

    fun setCustomTertiaryColor(color: Int) { prefs.edit { putInt(CUSTOM_TERTIARY_KEY, color) }; _customTertiaryColor.value = color }
    private fun getCustomTertiaryColor(): Int = prefs.getInt(CUSTOM_TERTIARY_KEY, Color(0xFF5E4B8B).toArgb())

    // [BARU] Intro Getters/Setters
    fun setHasCompletedIntro(completed: Boolean) { 
        prefs.edit { putBoolean(HAS_COMPLETED_INTRO_KEY, completed) } 
        _hasCompletedIntro.value = completed 
    }
    private fun getHasCompletedIntro(): Boolean = prefs.getBoolean(HAS_COMPLETED_INTRO_KEY, false)

    fun setAcceptedTerms(accepted: Boolean) { 
        prefs.edit { putBoolean(ACCEPTED_TERMS_KEY, accepted) } 
        _acceptedTerms.value = accepted 
    }
    private fun getAcceptedTerms(): Boolean = prefs.getBoolean(ACCEPTED_TERMS_KEY, false)

    fun setAcceptedRisk(accepted: Boolean) { 
        prefs.edit { putBoolean(ACCEPTED_RISK_KEY, accepted) } 
        _acceptedRisk.value = accepted 
    }
    private fun getAcceptedRisk(): Boolean = prefs.getBoolean(ACCEPTED_RISK_KEY, false)
}

private fun SharedPreferences.toFloat(key: String, defValue: Float): Float = try { this.getFloat(key, defValue) } catch (e: Exception) { defValue }
