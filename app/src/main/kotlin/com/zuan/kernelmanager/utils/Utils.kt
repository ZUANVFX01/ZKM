/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * All Rights Reserved.
 */
 /*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.text.format.Formatter
import android.util.Log
import com.topjohnwu.superuser.Shell
import java.io.File

object Utils {
    // --- DEVICE INFO UTILS ---
    fun getDeviceName() = Build.MODEL
    fun getDeviceCodename() = Build.DEVICE
    fun getAndroidVersion() = Build.VERSION.RELEASE
    fun getSdkVersion() = Build.VERSION.SDK_INT.toString()
    fun getManufacturer() = Build.MANUFACTURER

    // --- UPDATED: Mengambil Tags (release-keys / test-keys) ---
    fun getBuildTags() = Build.TAGS
    // ----------------------------------------------------------

    // --- SYSTEM PROPERTY ---
    fun getSystemProperty(key: String): String = runCatching {
        Shell.cmd("getprop $key").exec()
            .takeIf { it.isSuccess }?.out?.firstOrNull()?.trim()
    }.getOrNull().orEmpty()

    // --- FILE OPERATIONS ---
    fun getTemp(filePath: String): String {
        val value = readFile(filePath).trim()
        return value.toFloatOrNull()?.div(1000)?.let { "%.1f".format(it) } ?: "N/A"
    }

    fun testFile(filePath: String): Boolean = File(filePath).exists() ||
        Shell.cmd("test -f $filePath && echo true || echo false").exec()
            .takeIf { it.isSuccess }?.out?.firstOrNull() == "true"

    fun setPermissions(permission: Int, filePath: String) {
        Shell.cmd("chmod $permission $filePath").exec()
    }

    fun readFile(filePath: String): String =
        Shell.cmd("cat $filePath").exec().takeIf { it.isSuccess }?.out?.joinToString("\n")?.trim().orEmpty()

    fun writeFile(filePath: String, value: String): Boolean = runCatching {
        Shell.cmd("echo \"$value\" > $filePath").exec().isSuccess
    }.getOrElse {
        Log.e("writeFile", "Error writing to $filePath: ${it.message}", it)
        false
    }

    // --- APP INFO ---
    fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.longVersionCode})"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    // --- RAM & MEMORY UTILS ---
    fun getTotalRam(context: Context): String {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val totalMemVars = memInfo.totalMem.toDouble()
        return String.format("%.1f GB", totalMemVars / (1024 * 1024 * 1024))
    }

    fun getSelinuxStatus(): String {
        val result = Shell.cmd("getenforce").exec()
        return result.out.firstOrNull()?.trim() ?: "Unknown"
    }

    // --- FUNGSI UNTUK AMBIL LABEL DINAMIS DARI COMMAND SHELL ---
    fun getBuildUser(): String {
        // Command: getprop ro.build.fingerprint | tr '/' '\n' | grep user | cut -f1 -d:
        val cmd = "getprop ro.build.fingerprint 2>/dev/null | tr '/' '\\n' 2>/dev/null | grep user 2>/dev/null | cut -f1 -d: 2>/dev/null"
        
        val result = Shell.cmd(cmd).exec()
        val output = result.out.firstOrNull()?.trim()
        
        // Kalau kosong, default ke "CPU" atau "USER" biar gak blank
        return if (!output.isNullOrEmpty()) output else "CPU"
    }

    // --- RAM STATUS ---
    data class RamStatus(
        val totalMem: Long,
        val availMem: Long,
        val usedMem: Long,
        val usagePercent: Float,
        val availString: String,
        val totalString: String
    )

    fun getRamStatus(context: Context): RamStatus {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)

        val total = memInfo.totalMem
        val avail = memInfo.availMem
        val used = total - avail
        val percent = if (total > 0) used.toFloat() / total.toFloat() else 0f

        return RamStatus(
            totalMem = total,
            availMem = avail,
            usedMem = used,
            usagePercent = percent,
            availString = Formatter.formatShortFileSize(context, avail),
            totalString = Formatter.formatShortFileSize(context, total)
        )
    }

    // --- NEW: SERVICE CHECKER UTILS (Untuk Smart Cut-off Persistence) ---
    @Suppress("DEPRECATION") // getRunningServices deprecated tapi masih work untuk service sendiri
    fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager?.getRunningServices(Int.MAX_VALUE) ?: emptyList()) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
