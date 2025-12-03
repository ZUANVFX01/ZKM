/*
 * Copyright (c) 2025 ZKM
 * Monitor Reader - Optimized Version (Battery API)
 */
package com.zuan.kernelmanager.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.topjohnwu.superuser.Shell
import kotlin.math.abs

object MonitorReader {
    private const val TAG = "MonitorReader"
    private var lastTotal = 0L
    private var lastIdle = 0L
    
    // 🔥 PASTIKAN SHELL SELALU DI-INIT
    // Digunakan hanya untuk CPU dan Power Watt
    private fun ensureShell(): Boolean {
        return try {
            Shell.getShell()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // --- CPU LOAD (Shell) ---
    fun getCpuLoad(): Int {
        if (!ensureShell()) return 0
        
        return try {
            // Metode: /proc/stat (Reliable)
            val statOutput = Shell.cmd("cat /proc/stat | head -n 1").exec().out
            val firstLine = statOutput.firstOrNull() ?: return 0
            
            val parts = firstLine.trim().split("\\s+".toRegex())
            if (parts.size < 5) return 0

            val user = parts[1].toLongOrNull() ?: 0L
            val nice = parts[2].toLongOrNull() ?: 0L
            val system = parts[3].toLongOrNull() ?: 0L
            val idle = parts[4].toLongOrNull() ?: 0L
            
            val total = user + nice + system + idle
            
            // Jika ini run pertama
            if (lastTotal == 0L) {
                lastTotal = total
                lastIdle = idle
                return 0
            }

            val diffTotal = total - lastTotal
            val diffIdle = idle - lastIdle
            
            lastTotal = total
            lastIdle = idle

            if (diffTotal <= 0L) return 0
            
            val usage = ((diffTotal - diffIdle) * 100 / diffTotal).toInt()
            usage.coerceIn(0, 100)
        } catch (e: Exception) {
            Log.e(TAG, "CPU Error: ${e.message}")
            0
        }
    }
    
    // --- BATTERY TEMP (Android API) ---
    // ✅ Fast, No Root needed for this part, No Lag
    fun getBatteryTemp(context: Context): Float {
        return try {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val tempRaw = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            
            // Android API return int (e.g. 370 = 37.0°C)
            tempRaw / 10f
        } catch (e: Exception) {
            Log.e(TAG, "Temp API Error: ${e.message}")
            0f
        }
    }
    
    // --- POWER WATT (Shell) ---
    fun getPowerWatt(): Float {
        if (!ensureShell()) return 0f
        
        return try {
            // 1. Baca Voltage (Microvolts)
            val voltagePaths = listOf(
                "/sys/class/power_supply/battery/voltage_now",
                "/sys/class/power_supply/bms/voltage_now"
            )
            
            var voltage = 0L
            for (path in voltagePaths) {
                try {
                    val output = Shell.cmd("cat $path 2>/dev/null || echo 0").exec()
                    if (output.isSuccess) {
                        val value = output.out.joinToString("").trim().toLongOrNull() ?: 0L
                        if (value > 0) {
                            voltage = value
                            break
                        }
                    }
                } catch (e: Exception) { continue }
            }
            
            // 2. Baca Current (Microamps)
            val currentPaths = listOf(
                "/sys/class/power_supply/battery/current_now",
                "/sys/class/power_supply/bms/current_now"
            )
            
            var current = 0L
            for (path in currentPaths) {
                try {
                    val output = Shell.cmd("cat $path 2>/dev/null || echo 0").exec()
                    if (output.isSuccess) {
                        val value = output.out.joinToString("").trim().toLongOrNull() ?: 0L
                        if (value != 0L) {
                            current = value
                            break
                        }
                    }
                } catch (e: Exception) { continue }
            }
            
            // 3. Hitung Watt (P = V * I)
            if (voltage > 0 && current != 0L) {
                val power = (abs(voltage * current).toDouble()) / 1_000_000_000_000.0
                return power.toFloat()
            }
            
            0f
        } catch (e: Exception) {
            Log.e(TAG, "Power Error: ${e.message}")
            0f
        }
    }
    
    // Helper untuk debugging (Updated with Context)
    fun getDebugInfo(context: Context): String {
        return try {
            val cpu = getCpuLoad()
            val temp = getBatteryTemp(context)
            val power = getPowerWatt()
            "CPU: $cpu%, Temp: ${temp}°C, Power: ${power}W"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
