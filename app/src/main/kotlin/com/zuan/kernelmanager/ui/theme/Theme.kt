/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Default theme colors
private val DefaultPurple80 = Color(0xFFD0BCFF)
private val DefaultPurpleGrey80 = Color(0xFFCCC2DC)
private val DefaultPink80 = Color(0xFFEFB8C8)
private val DefaultPurple40 = Color(0xFF6650a4)
private val DefaultPurpleGrey40 = Color(0xFF625b71)
private val DefaultPink40 = Color(0xFF7D5260)

private val DarkColorScheme = darkColorScheme(
    primary = DefaultPurple80,
    secondary = DefaultPurpleGrey80,
    tertiary = DefaultPink80
)

private val LightColorScheme = lightColorScheme(
    primary = DefaultPurple40,
    secondary = DefaultPurpleGrey40,
    tertiary = DefaultPink40
)

// [UPDATE] Create color scheme with custom colors
fun createCustomColorScheme(
    darkTheme: Boolean,
    customPrimary: Color?,
    customSecondary: Color?,
    customTertiary: Color?
): androidx.compose.material3.ColorScheme {
    return if (darkTheme) {
        darkColorScheme(
            primary = customPrimary ?: DefaultPurple80,
            secondary = customSecondary ?: DefaultPurpleGrey80,
            tertiary = customTertiary ?: DefaultPink80,
            primaryContainer = (customPrimary ?: DefaultPurple80).copy(alpha = 0.3f),
            secondaryContainer = (customSecondary ?: DefaultPurpleGrey80).copy(alpha = 0.3f),
            tertiaryContainer = (customTertiary ?: DefaultPink80).copy(alpha = 0.3f)
        )
    } else {
        lightColorScheme(
            primary = customPrimary ?: DefaultPurple40,
            secondary = customSecondary ?: DefaultPurpleGrey40,
            tertiary = customTertiary ?: DefaultPink40,
            primaryContainer = (customPrimary ?: DefaultPurple40).copy(alpha = 0.2f),
            secondaryContainer = (customSecondary ?: DefaultPurpleGrey40).copy(alpha = 0.2f),
            tertiaryContainer = (customTertiary ?: DefaultPink40).copy(alpha = 0.2f)
        )
    }
}

@Composable
fun ZuanKernelManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // [FIX 1] Ubah default jadi true, ATAU kita abaikan parameternya di logika bawah
    dynamicColor: Boolean = true, 
    customPrimary: Color? = null,
    customSecondary: Color? = null,
    customTertiary: Color? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // 1. Jika Custom Colors ada (Preset atau Custom Picker), pakai ini.
        customPrimary != null -> {
            createCustomColorScheme(darkTheme, customPrimary, customSecondary, customTertiary)
        }
        // 2. [FIX 2] Jika customPrimary NULL, berarti MainActivity minta Dynamic.
        // Cek SDK version. Kita hapus 'dynamicColor &&' agar lebih memaksa, atau biarkan jika default sudah true.
        // Disini saya pakai SDK check saja agar pasti jalan sesuai logika MainActivity.
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // 3. Fallback ke Default (Ungu) jika HP < Android 12
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography, // Pastikan Type.kt kamu ada AppTypography
        content = content
    )
}
