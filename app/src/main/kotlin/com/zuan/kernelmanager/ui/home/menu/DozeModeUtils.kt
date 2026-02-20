/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.home.menu

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.topjohnwu.superuser.Shell

data class DozeApp(
    val packageName: String,
    val label: String,
    val isSystem: Boolean,
    val isWhitelisted: Boolean,
    val importance: String
)

object DozeModeUtils {

    // --- FITUR BARU: HARDCORE GMS DOZE LOGIC ---
    private const val GMS_PACKAGE = "com.google.android.gms"

    fun getGmsDozeMode(): String {
        return try {
            val bgResult = Shell.cmd("cmd appops get $GMS_PACKAGE RUN_IN_BACKGROUND").exec().out.joinToString("")
            val wakeResult = Shell.cmd("cmd appops get $GMS_PACKAGE WAKE_LOCK").exec().out.joinToString("")
            
            when {
                wakeResult.contains("ignore") -> "Aggressive"
                bgResult.contains("ignore") -> "Standard"
                else -> "Default"
            }
        } catch (e: Exception) {
            "Default"
        }
    }

    fun applyGmsDozeMode(mode: String): Boolean {
        // Hapus GMS dari whitelist Doze standar dulu
        Shell.cmd("dumpsys deviceidle whitelist -$GMS_PACKAGE").exec()
        
        val result = when (mode) {
            "Aggressive" -> {
                // Hardcore: Blokir Background, Wakelock, dan Alarm (Baterai super awet, notif GMS mungkin telat)
                Shell.cmd(
                    "cmd appops set $GMS_PACKAGE RUN_IN_BACKGROUND ignore",
                    "cmd appops set $GMS_PACKAGE WAKE_LOCK ignore",
                    "cmd appops set $GMS_PACKAGE ALARM_WAKEUP ignore",
                    "am set-standby-bucket $GMS_PACKAGE restricted"
                ).exec()
            }
            "Standard" -> {
                // Menengah: Blokir Background saja, Wakelock dan Alarm tetap jalan (Aman untuk notif)
                Shell.cmd(
                    "cmd appops set $GMS_PACKAGE RUN_IN_BACKGROUND ignore",
                    "cmd appops set $GMS_PACKAGE WAKE_LOCK allow",
                    "cmd appops set $GMS_PACKAGE ALARM_WAKEUP allow",
                    "am set-standby-bucket $GMS_PACKAGE restricted"
                ).exec()
            }
            else -> { // Default / Off
                // Kembalikan seperti pabrik
                Shell.cmd(
                    "cmd appops set $GMS_PACKAGE RUN_IN_BACKGROUND allow",
                    "cmd appops set $GMS_PACKAGE WAKE_LOCK allow",
                    "cmd appops set $GMS_PACKAGE ALARM_WAKEUP allow",
                    "am set-standby-bucket $GMS_PACKAGE active"
                ).exec()
            }
        }
        return result.isSuccess
    }
    // --------------------------------

    // --- KODE ASLI LU DI BAWAH INI (TIDAK ADA YANG DIHAPUS) ---
    fun getDozeWhitelist(context: Context): List<DozeApp> {
        val apps = mutableListOf<DozeApp>()
        
        try {
            val result = Shell.cmd("dumpsys deviceidle whitelist").exec()
            if (!result.isSuccess) return emptyList()
            
            val whitelistedPackages = result.out
                .filter { it.contains("system-ex:") || it.contains("user:") }
                .map { it.substringAfterLast(" ").trim() }
                .toSet()
            
            val pm = context.packageManager
            val packages = pm.getInstalledPackages(PackageManager.GET_META_DATA)
            
            packages.forEach { pkg ->
                try {
                    val appInfo = pkg.applicationInfo ?: return@forEach
                    val label = pm.getApplicationLabel(appInfo).toString()
                    val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val isWhitelisted = whitelistedPackages.contains(pkg.packageName)
                    
                    apps.add(DozeApp(
                        packageName = pkg.packageName,
                        label = label,
                        isSystem = isSystem,
                        isWhitelisted = isWhitelisted,
                        importance = if (isSystem) "System" else "User"
                    ))
                } catch (e: Exception) {
                    // Skip
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return apps.sortedBy { it.label }
    }

    fun addToWhitelist(packageName: String): Boolean {
        val result = Shell.cmd("dumpsys deviceidle whitelist +$packageName").exec()
        return result.isSuccess
    }

    fun removeFromWhitelist(packageName: String): Boolean {
        val result = Shell.cmd("dumpsys deviceidle whitelist -$packageName").exec()
        return result.isSuccess
    }

    fun getDozeState(): String {
        val result = Shell.cmd("dumpsys deviceidle state").exec()
        return if (result.isSuccess) {
            val output = result.out.joinToString(" ")
            when {
                output.contains("ACTIVE") -> "Active"
                output.contains("IDLE_PENDING") -> "Idle Pending"
                output.contains("SENSING") -> "Sensing"
                output.contains("LOCATING") -> "Locating"
                output.contains("IDLE") -> "Idle"
                output.contains("IDLE_MAINTENANCE") -> "Maintenance"
                else -> "Unknown"
            }
        } else "Unknown"
    }

    fun forceIdle(): Boolean {
        val result = Shell.cmd("dumpsys deviceidle force-idle").exec()
        return result.isSuccess
    }

    fun unforceIdle(): Boolean {
        val result = Shell.cmd("dumpsys deviceidle unforce").exec()
        return result.isSuccess
    }

    fun stepIdleState(): Boolean {
        val result = Shell.cmd("dumpsys deviceidle step").exec()
        return result.isSuccess
    }

    fun getDozeSettings(): DozeSettings {
        val enabledResult = Shell.cmd("settings get global device_idle_enabled").exec()
        val enabled = enabledResult.isSuccess && enabledResult.out.joinToString("").trim() != "0"
        
        val aggressiveResult = Shell.cmd("settings get global device_idle_aggressive").exec()
        val aggressive = aggressiveResult.isSuccess && aggressiveResult.out.joinToString("").trim() == "1"
        
        return DozeSettings(
            isEnabled = enabled,
            isAggressive = aggressive,
            lightDozeEnabled = true,
            deepDozeEnabled = true
        )
    }

    fun setDozeEnabled(enabled: Boolean): Boolean {
        val value = if (enabled) "1" else "0"
        val result = Shell.cmd("settings put global device_idle_enabled $value").exec()
        return result.isSuccess
    }

    fun setAggressiveDoze(enabled: Boolean): Boolean {
        val value = if (enabled) "1" else "0"
        val result = Shell.cmd("settings put global device_idle_aggressive $value").exec()
        return result.isSuccess
    }

    fun getBatteryOptimizationStatus(context: Context): List<BatteryOptimizedApp> {
        val apps = mutableListOf<BatteryOptimizedApp>()
        
        try {
            val result = Shell.cmd("dumpsys deviceidle").exec()
            val output = result.out.joinToString("\n")
            
            val pm = context.packageManager
            val packages = pm.getInstalledPackages(PackageManager.GET_META_DATA)
            
            packages.forEach { pkg ->
                try {
                    val appInfo = pkg.applicationInfo ?: return@forEach
                    val label = pm.getApplicationLabel(appInfo).toString()
                    val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    
                    val isOptimized = !output.contains("Whitelist (except idle): ${pkg.packageName}") &&
                                     !output.contains("Whitelist (all): ${pkg.packageName}")
                    
                    apps.add(BatteryOptimizedApp(
                        packageName = pkg.packageName,
                        label = label,
                        isSystem = isSystem,
                        isOptimized = isOptimized
                    ))
                } catch (e: Exception) {
                    // Skip
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return apps.sortedBy { it.label }
    }

    fun setBatteryOptimization(packageName: String, optimize: Boolean): Boolean {
        val cmd = if (optimize) {
            "cmd appops set $packageName RUN_IN_BACKGROUND ignore"
        } else {
            "cmd appops set $packageName RUN_IN_BACKGROUND allow"
        }
        val result = Shell.cmd(cmd).exec()
        return result.isSuccess
    }

    fun getDozeStats(): DozeStats {
        val result = Shell.cmd("dumpsys deviceidle").exec()
        val output = result.out.joinToString("\n")
        
        val idleCount = Regex("Idle count: (\\d+)").find(output)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val lightIdleCount = Regex("Light idle count: (\\d+)").find(output)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        
        return DozeStats(
            idleCount = idleCount,
            lightIdleCount = lightIdleCount
        )
    }

    fun resetDozeStats(): Boolean {
        val result = Shell.cmd("dumpsys deviceidle reset").exec()
        return result.isSuccess
    }

    fun getIdleTimeouts(): IdleTimeouts {
        val result = Shell.cmd("settings get global device_idle_constants").exec()
        val output = result.out.joinToString("")
        
        return IdleTimeouts(
            lightIdleAfterInactive = parseTimeout(output, "light_idle_after_inactive_to"),
            lightIdlePreIdle = parseTimeout(output, "light_pre_idle_to"),
            lightIdleTimeout = parseTimeout(output, "light_idle_to"),
            lightIdleFactor = parseFloat(output, "light_idle_factor"),
            minLightMaintenance = parseTimeout(output, "min_light_maintenance_time"),
            minDeepMaintenance = parseTimeout(output, "min_deep_maintenance_time")
        )
    }

    private fun parseTimeout(output: String, key: String): Long {
        val regex = "$key=(\\d+)".toRegex()
        return regex.find(output)?.groupValues?.get(1)?.toLongOrNull() ?: 0
    }

    private fun parseFloat(output: String, key: String): Float {
        val regex = "$key=(\\d+\\.?\\d*)".toRegex()
        return regex.find(output)?.groupValues?.get(1)?.toFloatOrNull() ?: 1.0f
    }
}

data class DozeSettings(
    val isEnabled: Boolean,
    val isAggressive: Boolean,
    val lightDozeEnabled: Boolean,
    val deepDozeEnabled: Boolean
)

data class BatteryOptimizedApp(
    val packageName: String,
    val label: String,
    val isSystem: Boolean,
    val isOptimized: Boolean
)

data class DozeStats(
    val idleCount: Int,
    val lightIdleCount: Int
)

data class IdleTimeouts(
    val lightIdleAfterInactive: Long,
    val lightIdlePreIdle: Long,
    val lightIdleTimeout: Long,
    val lightIdleFactor: Float,
    val minLightMaintenance: Long,
    val minDeepMaintenance: Long
)
