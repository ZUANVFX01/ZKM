/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.settings

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.zuan.kernelmanager.ui.about.AboutScreen
import com.zuan.kernelmanager.ui.navigation.NavigationRoute
import com.zuan.kernelmanager.ui.theme.ThemeMode
import com.zuan.kernelmanager.ui.theme.ZuanKernelManagerTheme
import com.zuan.kernelmanager.ui.components.VideoWallpaperPlayer
import com.zuan.kernelmanager.ui.components.WeatherEffectOverlay
import com.zuan.kernelmanager.utils.ContextUtils
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

class SettingsActivity : AppCompatActivity() {
    
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ContextUtils.updateBaseContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = SettingsPreference.getInstance(this)
        val savedDpi = prefs.appDpi.value

        setContent {
            val themeMode by prefs.themeMode.collectAsState()
            val isDynamicColor by prefs.isDynamicColor.collectAsState()
            val themeColorName by prefs.themeColorName.collectAsState()
            
            // [UPDATE] Custom colors
            val isCustomColor by prefs.isCustomColor.collectAsState()
            val customPrimary by prefs.customPrimaryColor.collectAsState()
            val customSecondary by prefs.customSecondaryColor.collectAsState()
            val customTertiary by prefs.customTertiaryColor.collectAsState()
            
            val isCustomBg by prefs.isCustomBackground.collectAsState()
            val bgUriString by prefs.backgroundImageUri.collectAsState()
            
            // [UPDATE] Collect isVideo
            val isVideo by prefs.isVideoWallpaper.collectAsState()
            
            val savedDpiState by prefs.appDpi.collectAsState()
            val isBgBlur by prefs.isBgBlur.collectAsState()
            val bgContrast by prefs.bgContrast.collectAsState()
            
            // [UPDATE] Weather effects
            val weatherEffect by prefs.weatherEffect.collectAsState()
            val weatherIntensity by prefs.weatherIntensity.collectAsState()
            
            // [UPDATE] Background adjustments
            val bgSaturation by prefs.bgSaturation.collectAsState()
            val blurStrength by prefs.blurStrength.collectAsState()

            val useDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM_DEFAULT -> isSystemInDarkTheme()
            }

            val context = LocalContext.current
            
            // [UPDATE] Get theme colors
            val themeColors = remember(isDynamicColor, themeColorName, isCustomColor, customPrimary, customSecondary, customTertiary) {
                when {
                    isCustomColor -> Triple(
                        Color(customPrimary),
                        Color(customSecondary),
                        Color(customTertiary)
                    )
                    isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> null
                    else -> {
                        val selectedColor = availableColors.find { it.name == themeColorName } ?: availableColors[1]
                        Triple(selectedColor.primary, selectedColor.secondary, selectedColor.tertiary)
                    }
                }
            }

            val currentDensity = LocalDensity.current
            val targetDpi = if (savedDpiState != 0) savedDpiState else savedDpi
            val appDensity = remember(targetDpi) {
                if (targetDpi > 0) Density(targetDpi.toFloat() / 160f, currentDensity.fontScale) else currentDensity
            }

            CompositionLocalProvider(LocalDensity provides appDensity) {
                ZuanKernelManagerTheme(
                    darkTheme = useDarkTheme,
                    customPrimary = themeColors?.first,
                    customSecondary = themeColors?.second,
                    customTertiary = themeColors?.third
                ) {
                    val backgroundColor = MaterialTheme.colorScheme.background
                    val hazeState = rememberHazeState()
                    
                    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
                        
                        // --- GLOBAL BACKGROUND RENDERER ---
                        if (isCustomBg && bgUriString != null) {
                            // [UPDATE] Apply blur strength
                            val blurModifier = if (isBgBlur && blurStrength > 0f) {
                                Modifier.blur(blurStrength.dp)
                            } else if (isBgBlur) {
                                Modifier.blur(20.dp)
                            } else {
                                Modifier
                            }
                            
                            // [UPDATE] Apply saturation
                            val saturationMatrix = ColorMatrix().apply {
                                setToSaturation(bgSaturation)
                            }
                            val colorFilter = ColorFilter.colorMatrix(saturationMatrix)
                            
                            val uri = Uri.parse(bgUriString)

                            if (isVideo) {
                                // [UPDATE] Render Video dengan effects
                                Box(modifier = Modifier.fillMaxSize().hazeSource(state = hazeState, zIndex = 0f)) {
                                    VideoWallpaperPlayer(
                                        uri = uri,
                                        modifier = Modifier.fillMaxSize().then(blurModifier)
                                    )
                                }
                            } else {
                                // Render Image dengan effects
                                AsyncImage(
                                    model = ImageRequest.Builder(this@SettingsActivity).data(uri).build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().then(blurModifier).hazeSource(state = hazeState, zIndex = 0f),
                                    contentScale = ContentScale.Crop,
                                    colorFilter = colorFilter
                                )
                            }

                            if (bgContrast > 0f) {
                                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = bgContrast)))
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(backgroundColor).hazeSource(state = hazeState, zIndex = 0f))
                        }
                        
                        // [UPDATE] Weather Effect Overlay
                        WeatherEffectOverlay(
                            effect = weatherEffect,
                            intensity = weatherIntensity,
                            modifier = Modifier.fillMaxSize()
                        )

                        // --- CONTENT ---
                        val navController = rememberNavController()
                        NavHost(navController = navController, startDestination = NavigationRoute.Settings.route) {
                            composable(NavigationRoute.Settings.route) { SettingsScreen(navController = navController, hazeState = hazeState) }
                            composable(NavigationRoute.WallpaperStyle.route) { WallpaperStyleScreen(navController = navController) }
                            composable(NavigationRoute.Language.route) { LanguageScreen(navController = navController) }
                            composable(NavigationRoute.About.route) { AboutScreen(navController = navController, hazeState = hazeState) }
                        }
                    }
                }
            }
        }
    }
}
