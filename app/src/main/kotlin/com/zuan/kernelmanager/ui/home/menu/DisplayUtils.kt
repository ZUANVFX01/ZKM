/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.home.menu

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import com.topjohnwu.superuser.Shell
import kotlin.math.pow
import kotlin.math.sqrt

data class DisplayDashboardInfo(
    val resolution: String,
    val technology: String, // AMOLED/IPS (Estimasi)
    val physicalSize: String, // mm
    val diagonalSize: String, // inch
    val density: String,
    val xdpiYdpi: String,
    val gpuRenderer: String,
    val refreshRate: String,
    val orientation: String,
    val openGlVersion: String,
    val hdrCapabilities: String,
    val isWideColorGamut: Boolean
)

object DisplayUtils {

    fun getCurrentResolution(context: Context): DisplayResolution {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        return DisplayResolution(size.x, size.y)
    }

    fun getCurrentDpi(context: Context): Int {
        return try {
            val result = Shell.cmd("wm density").exec()
            if (result.isSuccess) {
                val output = result.out.joinToString(" ")
                val regex = "Physical density: (\\d+)".toRegex()
                regex.find(output)?.groupValues?.get(1)?.toIntOrNull() ?: 
                context.resources.configuration.densityDpi
            } else {
                context.resources.configuration.densityDpi
            }
        } catch (e: Exception) {
            context.resources.configuration.densityDpi
        }
    }

    fun setDpi(dpi: Int): Boolean {
        val result = Shell.cmd("wm density $dpi").exec()
        return result.isSuccess
    }

    fun resetDpi(): Boolean {
        val result = Shell.cmd("wm density reset").exec()
        return result.isSuccess
    }

    fun setResolution(width: Int, height: Int): Boolean {
        val result = Shell.cmd("wm size ${width}x$height").exec()
        return result.isSuccess
    }

    fun resetResolution(): Boolean {
        val result = Shell.cmd("wm size reset").exec()
        return result.isSuccess
    }

    // --- SATURATION CONTROL (Root SurfaceFlinger) ---
    fun setSaturation(saturation: Float): Boolean {
        // Saturation range biasanya 0.0 (B&W) sampai 2.0 (Oversaturated). Default 1.0.
        // Command 1022 adalah kode umum untuk saturation di SurfaceFlinger Android
        val result = Shell.cmd("service call SurfaceFlinger 1022 f $saturation").exec()
        return result.isSuccess
    }

    // --- DASHBOARD DETAILED INFO ---
    fun getDashboardInfo(context: Context): DisplayDashboardInfo {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val metrics = DisplayMetrics()
        display.getRealMetrics(metrics)

        // Hitung Ukuran Fisik & Diagonal
        val widthMm = metrics.widthPixels / metrics.xdpi * 25.4
        val heightMm = metrics.heightPixels / metrics.ydpi * 25.4
        val widthIn = metrics.widthPixels / metrics.xdpi
        val heightIn = metrics.heightPixels / metrics.ydpi
        val diagonal = sqrt(widthIn.pow(2) + heightIn.pow(2))

        // Get GPU/OpenGL Info via getprop/dumpsys (Estimasi)
        val gpu = Shell.cmd("getprop ro.hardware.egl").exec().out.firstOrNull() ?: "Unknown Adreno/Mali"
        val opengl = try {
            val res = Shell.cmd("dumpsys SurfaceFlinger | grep \"GLES:\"").exec()
            res.out.firstOrNull()?.substringAfter("GLES:")?.trim() ?: "ES 3.2"
        } catch (e: Exception) { "3.2" }

        val hdr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            display.hdrCapabilities?.supportedHdrTypes?.joinToString(", ") { type ->
                when(type) {
                    1 -> "Dolby Vision"; 2 -> "HDR10"; 3 -> "HLG"; 4 -> "HDR10+"; else -> "HDR"
                }
            } ?: "None"
        } else "N/A"

        val orientation = when(context.resources.configuration.orientation) {
            1 -> "Portrait"; 2 -> "Landscape"; else -> "Unknown"
        }

        return DisplayDashboardInfo(
            resolution = "${metrics.widthPixels} x ${metrics.heightPixels}",
            technology = "OLED/IPS", // Tidak bisa dideteksi akurat via software tanpa root logs spesifik
            physicalSize = "${widthMm.toInt()} x ${heightMm.toInt()} mm",
            diagonalSize = "%.2f inches".format(diagonal),
            density = "${metrics.densityDpi} dpi (${getDensityName(metrics.densityDpi)})",
            xdpiYdpi = "%.1f / %.1f dpi".format(metrics.xdpi, metrics.ydpi),
            gpuRenderer = gpu,
            refreshRate = "${display.refreshRate} Hz",
            orientation = orientation,
            openGlVersion = opengl,
            hdrCapabilities = hdr,
            isWideColorGamut = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) display.isWideColorGamut else false
        )
    }

    private fun getDensityName(dpi: Int): String {
        return when {
            dpi <= 120 -> "ldpi"
            dpi <= 160 -> "mdpi"
            dpi <= 240 -> "hdpi"
            dpi <= 320 -> "xhdpi"
            dpi <= 480 -> "xxhdpi"
            dpi <= 640 -> "xxxhdpi"
            else -> "xxxxhdpi"
        }
    }

    // --- PRESETS ---
    fun getAvailableResolutions(): List<DisplayResolution> {
        return listOf(
            DisplayResolution(1080, 2400), DisplayResolution(1080, 2340),
            DisplayResolution(1080, 1920), DisplayResolution(720, 1600),
            DisplayResolution(720, 1520), DisplayResolution(1440, 3200),
            DisplayResolution(1440, 3088), DisplayResolution(1220, 2712)
        )
    }

    fun getAvailableDpiValues(): List<Int> {
        return listOf(320, 360, 392, 400, 420, 440, 480, 560, 600)
    }

    fun getAvailableRefreshRates(): List<Int> {
        return listOf(30, 48, 60, 90, 120, 144, 165)
    }

    // --- SETTINGS PUT HELPERS ---
    fun setRefreshRate(rate: Int): Boolean {
        val result = Shell.cmd("settings put system min_refresh_rate $rate").exec()
        val result2 = Shell.cmd("settings put system peak_refresh_rate $rate").exec()
        return result.isSuccess
    }

    fun setScreenTimeout(seconds: Int): Boolean {
        val result = Shell.cmd("settings put system screen_off_timeout ${seconds * 1000}").exec()
        return result.isSuccess
    }

    fun getScreenTimeout(context: Context): Int {
        return try {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT) / 1000
        } catch (e: Exception) { 30 }
    }

    fun setBrightnessMode(auto: Boolean): Boolean {
        val mode = if (auto) 1 else 0
        val result = Shell.cmd("settings put system screen_brightness_mode $mode").exec()
        return result.isSuccess
    }

    fun getBrightnessMode(context: Context): Boolean {
        return try {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE) == 1
        } catch (e: Exception) { false }
    }

    fun setBrightness(value: Int): Boolean {
        val brightness = value.coerceIn(0, 255)
        val result = Shell.cmd("settings put system screen_brightness $brightness").exec()
        return result.isSuccess
    }

    fun getBrightness(context: Context): Int {
        return try {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Exception) { 128 }
    }

    fun setNightMode(enabled: Boolean): Boolean {
        val value = if (enabled) 1 else 0
        val result = Shell.cmd("settings put secure night_display_activated $value").exec()
        return result.isSuccess
    }

    fun isNightModeEnabled(context: Context): Boolean {
        return try {
            Settings.Secure.getInt(context.contentResolver, "night_display_activated") == 1
        } catch (e: Exception) { false }
    }

    fun setFontScale(scale: Float): Boolean {
        val result = Shell.cmd("settings put system font_scale $scale").exec()
        return result.isSuccess
    }

    fun getFontScale(context: Context): Float {
        return try {
            Settings.System.getFloat(context.contentResolver, Settings.System.FONT_SCALE)
        } catch (e: Exception) { 1.0f }
    }

    fun setAnimationScale(scale: Float): Boolean {
        val result = Shell.cmd("settings put global window_animation_scale $scale").exec()
        Shell.cmd("settings put global transition_animation_scale $scale").exec()
        Shell.cmd("settings put global animator_duration_scale $scale").exec()
        return result.isSuccess
    }

    fun getAnimationScale(context: Context): Float {
        return try {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE)
        } catch (e: Exception) { 1.0f }
    }

    fun setDisplayInversion(enabled: Boolean): Boolean {
        val value = if (enabled) 1 else 0
        val result = Shell.cmd("settings put secure accessibility_display_inversion_enabled $value").exec()
        return result.isSuccess
    }

    fun isDisplayInversionEnabled(context: Context): Boolean {
        return try {
            Settings.Secure.getInt(context.contentResolver, "accessibility_display_inversion_enabled") == 1
        } catch (e: Exception) { false }
    }
}

data class DisplayResolution(val width: Int, val height: Int) {
    override fun toString(): String = "${width}x$height"
}
