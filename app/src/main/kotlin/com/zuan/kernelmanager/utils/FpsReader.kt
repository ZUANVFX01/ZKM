/*
 * Copyright (c) 2025 ZKM
 * FPS Reader Logic (Adapted from Scene 4.7 Open Source)
 */
package com.zuan.kernelmanager.utils

import java.util.regex.Pattern

object FpsReader {
    private var fpsFilePath: String? = null
    private var useSurfaceFlinger = true
    
    // Variabel untuk perhitungan SurfaceFlinger
    private var lastTime = -1L
    private var lastFrames = -1

    fun getRealFps(): Float {
        // 1. Coba baca dari File Kernel (Lebih akurat & Hemat baterai)
        if (!fpsFilePath.isNullOrEmpty()) {
            val output = ShellExecutor.executeWithResult("cat $fpsFilePath | awk '{print \$2}'")
            return output.toFloatOrNull() ?: 0f
        }

        // 2. Jika path belum ketemu, cari path-nya
        if (fpsFilePath == null) {
            findFpsPath()
            // Jika ketemu, recursive call sekali lagi
            if (!fpsFilePath.isNullOrEmpty()) {
                return getRealFps() 
            }
        }

        // 3. Fallback: Gunakan SurfaceFlinger (Metode Universal Android)
        return getSurfaceFlingerFps()
    }

    private fun findFpsPath() {
        // Cek path umum satu per satu
        val paths = listOf(
            "/sys/class/drm/sde-crtc-0/measured_fps",
            "/sys/class/graphics/fb0/measured_fps",
            "/sys/class/video/fps_info" // Tambahan untuk beberapa device
        )

        for (path in paths) {
            val check = ShellExecutor.executeWithResult("[ -f $path ] && echo 1 || echo 0")
            if (check == "1") {
                fpsFilePath = path
                return
            }
        }
        
        // Jika tidak ketemu, tandai string kosong agar tidak mencari terus
        fpsFilePath = ""
    }

    private fun getSurfaceFlingerFps(): Float {
        try {
            // Command legendaris untuk cek frame render
            val result = ShellExecutor.executeWithResult("service call SurfaceFlinger 1013")
            
            // Output biasanya: Result: Parcel(00000000 0000002A ...) -> 2A adalah hex frame count
            if (result.contains("Parcel")) {
                val hexPattern = Pattern.compile("([0-9a-fA-F]{8})\\s+([0-9a-fA-F]{8})")
                val matcher = hexPattern.matcher(result)
                
                if (matcher.find()) {
                    // Ambil hex pertama (frame count)
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
                    
                    // Filter anomali (misal fps > 200 di layar 60hz)
                    return if (fps > 240) 0f else fps
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0f
    }
}