/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.BatteryManager
import android.util.Log
import com.topjohnwu.superuser.Shell
import kotlin.math.abs
import java.util.regex.Pattern

object MonitorReader {
    private const val TAG = "MonitorReader"
    private var lastTotal = 0L
    private var lastIdle = 0L
    
    private var isShellInitialized = false
    private const val SHELL_TIMEOUT = 5000L
    
    fun initializeShell() {
        if (isShellInitialized) return
        Thread {
            try { Shell.getShell(); isShellInitialized = true } catch (e: Exception) { isShellInitialized = false }
        }.start()
    }
    
    private fun ensureShell(): Boolean {
        if (!isShellInitialized) initializeShell()
        return isShellInitialized
    }

    // 🔥 FIX UTAMA: Menggunakan Regex untuk mencari nama paket
    fun getForegroundPackage(): String {
        return try {
            // COBA 1: dumpsys activity (Paling akurat untuk Game)
            val dump1 = ShellExecutor.executeWithResult("dumpsys activity activities | grep mResumedActivity")
            val pkg1 = parsePackageFromDump(dump1)
            if (pkg1.isNotEmpty()) return pkg1

            // COBA 2: dumpsys window (Fallback standar)
            val dump2 = ShellExecutor.executeWithResult("dumpsys window | grep mCurrentFocus")
            val pkg2 = parsePackageFromDump(dump2)
            if (pkg2.isNotEmpty()) return pkg2
            
            // COBA 3: cmd activity (Android 11+)
            val dump3 = ShellExecutor.executeWithResult("cmd activity get-top-activity")
            val pkg3 = parsePackageFromDump(dump3)
            if (pkg3.isNotEmpty()) return pkg3

            ""
        } catch (e: Exception) {
            Log.e(TAG, "Pkg Error: ${e.message}")
            ""
        }
    }

    // Fungsi Pintar: Mencari pola "com.abc.xyz/" di dalam teks sampah
    private fun parsePackageFromDump(rawOutput: String): String {
        if (rawOutput.isEmpty()) return ""
        try {
            // Pola Regex: Mencari string yang diakhiri "/" dan mengandung setidaknya satu titik "."
            // Contoh target: u0 com.mobile.legends/com.activity...
            val pattern = Pattern.compile("([a-zA-Z0-9_]+\\.[a-zA-Z0-9_\\.]+)/")
            val matcher = pattern.matcher(rawOutput)
            
            if (matcher.find()) {
                val found = matcher.group(1) ?: ""
                // Filter hasil palsu
                if (found != "com.android.systemui" && found != "com.miui.home" && found != "android") {
                    return found
                }
            }
        } catch (e: Exception) {}
        return ""
    }

    // Helper UI: Ubah Package Name -> Nama Aplikasi (Contoh: com.tencent.ig -> PUBG Mobile)
    fun getAppName(context: Context, packageName: String): String {
        if (packageName == "Unknown App" || packageName.isEmpty()) return "Unknown App"
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName // Kalau gagal, kembalikan ID-nya saja
        }
    }

    // Helper UI: Ubah Package Name -> Icon Gambar
    fun getAppIcon(context: Context, packageName: String): Drawable? {
        if (packageName == "Unknown App" || packageName.isEmpty()) return null
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
    }
    
    // --- MONITORING LAINNYA TETAP SAMA ---
    fun getCurrentRenderer(): String {
        try {
            val pkgName = getForegroundPackage()
            if (pkgName.isNotEmpty()) {
                val pidRaw = ShellExecutor.executeWithResult("pidof $pkgName")
                val pid = pidRaw.split(" ").firstOrNull()
                if (!pid.isNullOrEmpty()) {
                    val checkVulkan = ShellExecutor.executeWithResult("grep -c 'libvulkan.so' /proc/$pid/maps")
                    if (checkVulkan.toIntOrNull() ?: 0 > 0) return "VULKAN"
                    val checkGl = ShellExecutor.executeWithResult("grep -c 'libGLES' /proc/$pid/maps")
                    if (checkGl.toIntOrNull() ?: 0 > 0) return "OPENGL"
                }
            }
        } catch (e: Exception) {}
        return "FPS"
    }

    fun getCpuLoad(): Int {
        if (!ensureShell()) return 0
        return try {
            val statOutput = Shell.cmd("cat /proc/stat | head -n 1").exec().out
            val firstLine = statOutput.firstOrNull() ?: return 0
            val parts = firstLine.trim().split("\\s+".toRegex())
            if (parts.size < 5) return 0
            val user = parts[1].toLongOrNull() ?: 0L
            val system = parts[3].toLongOrNull() ?: 0L
            val idle = parts[4].toLongOrNull() ?: 0L
            val total = user + system + idle + (parts[2].toLongOrNull()?:0L)
            
            if (lastTotal == 0L) { lastTotal = total; lastIdle = idle; return 0 }
            val diffTotal = total - lastTotal
            val diffIdle = idle - lastIdle
            lastTotal = total; lastIdle = idle
            
            if (diffTotal <= 0L) return 0
            ((diffTotal - diffIdle) * 100 / diffTotal).toInt().coerceIn(0, 100)
        } catch (e: Exception) { 0 }
    }
    
    fun getPowerWatt(): Float {
        if (!ensureShell()) return 0f
        return try {
            val v = ShellExecutor.executeWithResult("cat /sys/class/power_supply/battery/voltage_now").toLongOrNull() ?: 0L
            val c = ShellExecutor.executeWithResult("cat /sys/class/power_supply/battery/current_now").toLongOrNull() ?: 0L
            if (v > 0 && c != 0L) (abs(v * c).toDouble() / 1_000_000_000_000.0).toFloat() else 0f
        } catch (e: Exception) { 0f }
    }

    fun getBatteryTemp(context: Context): Float {
        return try {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val tempRaw = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            tempRaw / 10f
        } catch (e: Exception) { 0f }
    }
    
    data class RamInfo(val usedMb: Int, val totalMb: Int, val percent: Int)
    fun getRamInfo(context: Context): RamInfo {
        return try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memInfo)
            val totalMb = (memInfo.totalMem / 1048576L).toInt()
            val usedMb = ((memInfo.totalMem - memInfo.availMem) / 1048576L).toInt()
            RamInfo(usedMb, totalMb, ((usedMb.toDouble() / totalMb) * 100).toInt())
        } catch (e: Exception) { RamInfo(0, 0, 0) }
    }
    
    fun getDebugInfo(context: Context): String = "Ready"
}
