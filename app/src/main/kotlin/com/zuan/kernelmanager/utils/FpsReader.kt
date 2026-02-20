/*
 * Original code from: helloklf (vtools)
 * Modified and integrated by: Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.utils

import android.content.Context
import android.content.SharedPreferences
import java.util.regex.Pattern

enum class FpsMode {
    UNIVERSAL_ANDROID, // Mode 1: Service Call 1013
    UNIVERSAL_GPU,     // Mode 2: Kernel Files (sys/class/drm...)
    UNIVERSAL_BETA     // Mode 3: Dumpsys Timestats
}

object FpsReader {
    private const val PREF_NAME = "fps_settings"
    private const val KEY_MODE = "reader_mode"
    
    // Default mode
    var currentMode: FpsMode = FpsMode.UNIVERSAL_ANDROID
        private set
        
    private var fpsFilePath: String? = null
    
    // Variabel Cache untuk Mode 1
    private var lastTime = -1L
    private var lastFrames = -1

    // Init: Load saved preference
    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val modeIdx = prefs.getInt(KEY_MODE, FpsMode.UNIVERSAL_ANDROID.ordinal)
        currentMode = FpsMode.values().getOrElse(modeIdx) { FpsMode.UNIVERSAL_ANDROID }
        
        // Pre-search path jika mode GPU dipilih
        if (currentMode == FpsMode.UNIVERSAL_GPU) {
            findFpsPath()
        }
    }

    // Save preference
    fun setMode(context: Context, mode: FpsMode) {
        currentMode = mode
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_MODE, mode.ordinal).apply()
        
        // Reset variables
        lastTime = -1L
        lastFrames = -1
        if (mode == FpsMode.UNIVERSAL_GPU) findFpsPath()
    }

    fun getRealFps(): Float {
        return when (currentMode) {
            FpsMode.UNIVERSAL_ANDROID -> getSurfaceFlingerFps()
            FpsMode.UNIVERSAL_GPU -> getKernelFps()
            FpsMode.UNIVERSAL_BETA -> getDumpsysFps()
        }
    }

    // --- MODE 1: Universal Android (Service Call 1013) ---
    private fun getSurfaceFlingerFps(): Float {
        try {
            // Command: service call SurfaceFlinger 1013
            val result = ShellExecutor.executeWithResult("service call SurfaceFlinger 1013")
            
            if (result.contains("Parcel")) {
                val hexPattern = Pattern.compile("([0-9a-fA-F]{8})\\s+([0-9a-fA-F]{8})")
                val matcher = hexPattern.matcher(result)
                
                if (matcher.find()) {
                    val frameHex = matcher.group(1) ?: return 0f
                    val currentFrames = frameHex.toLong(16).toInt()
                    val currentTime = System.currentTimeMillis()

                    var fps = 0f
                    if (lastTime > 0 && lastFrames > 0) {
                        val timeDiff = currentTime - lastTime
                        if (timeDiff > 0) {
                            fps = (currentFrames - lastFrames) * 1000f / timeDiff
                        }
                    }

                    lastFrames = currentFrames
                    lastTime = currentTime
                    return if (fps > 240) 0f else fps
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return 0f
    }

    // --- MODE 2: Universal GPU (Kernel File) ---
    private fun getKernelFps(): Float {
        if (fpsFilePath.isNullOrEmpty()) {
            findFpsPath()
            if (fpsFilePath.isNullOrEmpty()) return 0f
        }
        val output = ShellExecutor.executeWithResult("cat $fpsFilePath | awk '{print \$2}'")
        return output.toFloatOrNull() ?: 0f
    }

    private fun findFpsPath() {
        val paths = listOf(
            "/sys/class/drm/sde-crtc-0/measured_fps",
            "/sys/class/graphics/fb0/measured_fps",
            "/sys/class/video/fps_info"
        )
        for (path in paths) {
            val check = ShellExecutor.executeWithResult("[ -f $path ] && echo 1 || echo 0")
            if (check == "1") {
                fpsFilePath = path
                return
            }
        }
        fpsFilePath = "" // Not found
    }

    // --- MODE 3: Universal Devices (Dumpsys Timestats - BETA) ---
    private fun getDumpsysFps(): Float {
        try {
            // Command Logic: Enable timestats -> dump & clear -> grep averageFPS
            // Command asli user: (dumpsys SurfaceFlinger --timestats -dump && dumpsys SurfaceFlinger --timestats -clear -enable) | grep "averageFPS" | head -n 1 | cut -d"=" -f2 | tr -d '[:space:]'
            
            // Kita sederhanakan command string-nya agar aman di shell executor
            val cmd = "(dumpsys SurfaceFlinger --timestats -dump && dumpsys SurfaceFlinger --timestats -clear -enable) | grep \"averageFPS\""
            val result = ShellExecutor.executeWithResult(cmd)
            
            // Result biasanya: "averageFPS = 60.000"
            if (result.contains("averageFPS")) {
                val parts = result.split("=")
                if (parts.size > 1) {
                    return parts[1].trim().toFloatOrNull() ?: 0f
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return 0f
    }
}
