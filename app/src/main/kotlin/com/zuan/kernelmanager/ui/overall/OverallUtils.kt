/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.overall

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.SystemClock
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sqrt

// --- DATA CLASSES ---

data class ProcessInfo(
    val name: String,
    val packageName: String,
    val pid: String,
    val cpuUsage: Float,
    val icon: ImageBitmap? = null
)

// Data Class untuk Detail Core
data class CoreDetailInfo(
    val coreIndex: Int,
    val governor: String,
    val tunables: Map<String, String> 
)

data class OverallBatteryInfo(
    val level: String = "50%",
    val status: String = "Unknown",
    val tech: String = "Li-ion",
    val voltage: String = "0 mV",
    val temp: String = "0 °C",
    val currentNow: Int = 0,
    val wattage: Float = 0f,
    val health: String = "Good",
    val deepSleep: String = "0%",
    val uptime: String = "0s",
    val cycleCount: String = "0",
    val designCapacity: String = "N/A",
    val maximumCapacity: String = "N/A"
)

data class MemoryData(
    val totalMem: Long, 
    val usedMem: Long, 
    val freeMem: Long,  
    val totalSwap: Long,
    val usedSwap: Long, 
    val freeSwap: Long,
    val zramAlgorithm: String = "Unknown"
)

// --- NEW: VM Parameters Data Class ---
data class VMParameters(
    val swappiness: String = "Unknown",
    val extraFreeKbytes: String = "Unknown",
    val watermarkScaleFactor: String = "Unknown",
    val vfsCachePressure: String = "Unknown",
    val dirtyRatio: String = "Unknown",
    val dirtyBgRatio: String = "Unknown"
)

// --- NEW: Display Info Data Class ---
data class DisplayInfo(
    val resolution: String = "Unknown",
    val densityDpi: String = "Unknown",
    val density: String = "Unknown",
    val technology: String = "Unknown",
    val diagonalSize: String = "Unknown",
    val physicalSize: String = "Unknown",
    val refreshRate: String = "Unknown",
    val orientation: String = "Unknown",
    val hdrSupport: String = "Unknown"
)

object OverallUtils {

    // --- DEVICE INFO ---
    suspend fun getDeviceModel(): String = withContext(Dispatchers.IO) {
        Shell.cmd("getprop ro.product.model").exec().out.firstOrNull() ?: Build.MODEL
    }

    suspend fun getDeviceCodename(): String = withContext(Dispatchers.IO) {
        Shell.cmd("getprop ro.product.device").exec().out.firstOrNull() ?: Build.DEVICE
    }

    suspend fun getSoCName(): String = withContext(Dispatchers.IO) {
        val platform = Shell.cmd("getprop ro.board.platform").exec().out.firstOrNull()
        if (!platform.isNullOrEmpty()) return@withContext platform.uppercase()
        
        val hardware = Shell.cmd("grep Hardware /proc/cpuinfo").exec().out.firstOrNull()
        return@withContext hardware?.substringAfter(":")?.trim() ?: "Unknown SoC"
    }

    suspend fun getKernelVersion(): String = withContext(Dispatchers.IO) {
        Shell.cmd("uname -r").exec().out.firstOrNull() ?: "Unknown Kernel"
    }

    // --- NEW: VM PARAMETERS ---
    suspend fun getVMParameters(): VMParameters = withContext(Dispatchers.IO) {
        fun readSysctl(param: String): String {
            return Shell.cmd("sysctl -n $param").exec().out.firstOrNull() 
                ?: Shell.cmd("cat /proc/sys/$param").exec().out.firstOrNull() 
                ?: "N/A"
        }
        
        return@withContext VMParameters(
            swappiness = readSysctl("vm.swappiness"),
            extraFreeKbytes = readSysctl("vm.extra_free_kbytes"),
            watermarkScaleFactor = readSysctl("vm.watermark_scale_factor"),
            vfsCachePressure = readSysctl("vm.vfs_cache_pressure"),
            dirtyRatio = readSysctl("vm.dirty_ratio"),
            dirtyBgRatio = readSysctl("vm.dirty_background_ratio")
        )
    }

    // --- NEW: DISPLAY INFO ---
    suspend fun getDisplayInfo(context: Context): DisplayInfo = withContext(Dispatchers.IO) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        
        // Get metrics for all API levels
        val metrics = android.util.DisplayMetrics()
        display.getRealMetrics(metrics)
        
        // Resolution
        val resolution = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            "${bounds.width()} x ${bounds.height()}"
        } else {
            "${metrics.widthPixels} x ${metrics.heightPixels}"
        }
        
        // Density
        val densityDpi = metrics.densityDpi
        val density = "${metrics.density}x"
        
        // Refresh Rate
        val refreshRate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            "${display.refreshRate.toInt()} Hz"
        } else {
            "Unknown"
        }
        
        // Orientation
        val orientation = when (display.rotation) {
            Surface.ROTATION_0 -> "Portrait"
            Surface.ROTATION_90 -> "Landscape"
            Surface.ROTATION_180 -> "Portrait (Reversed)"
            Surface.ROTATION_270 -> "Landscape (Reversed)"
            else -> "Unknown"
        }
        
        // HDR Support
        val hdrSupport = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val hdrCapabilities = display.hdrCapabilities
            val hdrTypes = hdrCapabilities?.supportedHdrTypes
            when {
                hdrTypes == null -> "Not Supported"
                hdrTypes.isEmpty() -> "Not Supported"
                hdrTypes.contains(Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS) -> "HDR10+"
                hdrTypes.contains(Display.HdrCapabilities.HDR_TYPE_HDR10) -> "HDR10"
                hdrTypes.contains(Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION) -> "Dolby Vision"
                hdrTypes.contains(Display.HdrCapabilities.HDR_TYPE_HLG) -> "HLG"
                else -> "Supported"
            }
        } else {
            "Not Supported"
        }
        
        // Screen Technology (from build.prop)
        val technology = Shell.cmd("getprop ro.vendor.display.type").exec().out.firstOrNull()
            ?: Shell.cmd("getprop ro.display.type").exec().out.firstOrNull()
            ?: "Unknown"
        
        // Physical Size & Diagonal
        val widthInches = metrics.widthPixels / metrics.xdpi
        val heightInches = metrics.heightPixels / metrics.ydpi
        val diagonalInches = sqrt(widthInches * widthInches + heightInches * heightInches)
        val physicalSize = "${widthInches.toInt()}\" x ${heightInches.toInt()}\""
        val diagonalSize = "%.1f\"".format(diagonalInches)
        
        return@withContext DisplayInfo(
            resolution = resolution,
            densityDpi = "$densityDpi DPI",
            density = density,
            technology = technology.uppercase(),
            diagonalSize = diagonalSize,
            physicalSize = physicalSize,
            refreshRate = refreshRate,
            orientation = orientation,
            hdrSupport = hdrSupport
        )
    }

    // --- CPU GOVERNOR & TUNABLES ---
    suspend fun getCpuGovernor(coreIndex: Int): String = withContext(Dispatchers.IO) {
        val path = "/sys/devices/system/cpu/cpu$coreIndex/cpufreq/scaling_governor"
        return@withContext Shell.cmd("cat $path").exec().out.firstOrNull() ?: "Unknown"
    }

    suspend fun getGovernorTunables(coreIndex: Int, governor: String): Map<String, String> = withContext(Dispatchers.IO) {
        val resultMap = mutableMapOf<String, String>()
        if (governor == "Unknown") return@withContext emptyMap()

        val possiblePaths = listOf(
            "/sys/devices/system/cpu/cpufreq/policy$coreIndex/$governor",
            "/sys/devices/system/cpu/cpu$coreIndex/cpufreq/$governor",
            "/sys/devices/system/cpu/cpufreq/$governor"
        )

        var validPath: String? = null
        for (path in possiblePaths) {
            val check = Shell.cmd("[ -d \"$path\" ] && echo \"exist\"").exec().out.firstOrNull()
            if (check == "exist") {
                validPath = path
                break
            }
        }

        if (validPath != null) {
            val files = Shell.cmd("ls $validPath").exec().out
            for (file in files) {
                if (file.trim().isEmpty()) continue
                val value = Shell.cmd("cat $validPath/$file").exec().out.firstOrNull() ?: ""
                if (value.length < 50) { 
                    resultMap[file] = value
                }
            }
        } else {
            resultMap["Error"] = "Parameters folder not found for $governor"
        }

        return@withContext resultMap
    }

    // --- GPU ---
    suspend fun getGpuDetail(): String = withContext(Dispatchers.IO) {
        try {
            val dumpsys = Shell.cmd("dumpsys SurfaceFlinger | grep 'GLES:'").exec().out.firstOrNull()
            if (!dumpsys.isNullOrEmpty()) {
                return@withContext dumpsys.replace("GLES:", "").trim()
            }
        } catch (e: Exception) { /* Ignore */ }

        val model = Shell.cmd("cat /sys/class/kgsl/kgsl-3d0/gpu_model").exec().out.firstOrNull()
        if (!model.isNullOrEmpty()) return@withContext model.replace("gpu", "").trim()
        
        return@withContext "GPU Info Not Available"
    }

    suspend fun getGpuLoadAndFreq(): Pair<String, Int> = withContext(Dispatchers.IO) {
        val freq = Shell.cmd("cat /sys/class/kgsl/kgsl-3d0/gpuclk").exec().out.firstOrNull()?.toLongOrNull()
        val freqStr = if (freq != null) "${freq / 1000 / 1000} MHz" else "0 MHz"

        val busyRaw = Shell.cmd("cat /sys/class/kgsl/kgsl-3d0/gpubusy").exec().out.firstOrNull()
        if (!busyRaw.isNullOrEmpty()) {
            val parts = busyRaw.trim().split("\\s+".toRegex())
            if (parts.size >= 2) {
                val busy = parts[0].toFloatOrNull() ?: 0f
                val total = parts[1].toFloatOrNull() ?: 1f
                if (total > 0) {
                    val percent = ((busy / total) * 100).toInt()
                    return@withContext Pair(freqStr, percent)
                }
            }
        }

        val busyPercentRaw = Shell.cmd("cat /sys/class/kgsl/kgsl-3d0/gpu_busy_percentage").exec().out.firstOrNull()
        if (!busyPercentRaw.isNullOrEmpty()) {
            val cleanStr = busyPercentRaw.replace(Regex("[^0-9]"), "")
            val percent = cleanStr.toIntOrNull()
            if (percent != null) return@withContext Pair(freqStr, percent)
        }

        val paths = listOf(
            "/sys/class/kgsl/kgsl-3d0/gpu_load",
            "/sys/class/misc/mali0/device/utilization",
            "/sys/kernel/gpu/gpu_busy"
        )
        
        for (path in paths) {
            val raw = Shell.cmd("cat $path").exec().out.firstOrNull()
            if (!raw.isNullOrEmpty()) {
                val clean = raw.replace(Regex("[^0-9]"), "")
                val p = clean.toIntOrNull()
                if (p != null) return@withContext Pair(freqStr, p)
            }
        }

        return@withContext Pair(freqStr, 0)
    }

    // --- MEMORY ---
    suspend fun getMemoryInfo(): MemoryData = withContext(Dispatchers.IO) {
        val out = Shell.cmd("cat /proc/meminfo").exec().out
        var memTotal = 0L; var memFree = 0L; var memAvail = 0L; var buffers = 0L; var cached = 0L
        var swapTotal = 0L; var swapFree = 0L

        for (line in out) {
            val parts = line.split("\\s+".toRegex())
            if (parts.size < 2) continue
            val key = parts[0].replace(":", "")
            val value = parts[1].toLongOrNull() ?: 0L
            
            when(key) {
                "MemTotal" -> memTotal = value
                "MemFree" -> memFree = value
                "MemAvailable" -> memAvail = value
                "Buffers" -> buffers = value
                "Cached" -> cached = value
                "SwapTotal" -> swapTotal = value
                "SwapFree" -> swapFree = value
            }
        }
        
        val used = if (memAvail > 0) memTotal - memAvail else memTotal - memFree - buffers - cached
        val swapUsed = swapTotal - swapFree
        
        // Get ZRAM Algorithm
        val zramAlgo = getZramAlgorithm()
        
        return@withContext MemoryData(memTotal, used, if (memAvail>0) memAvail else memFree, swapTotal, swapUsed, swapFree, zramAlgo)
    }

    // --- NEW: Get ZRAM Algorithm ---
    suspend fun getZramAlgorithm(): String = withContext(Dispatchers.IO) {
        val compAlgorithm = Shell.cmd("cat /sys/block/zram0/comp_algorithm").exec().out.firstOrNull()
        if (!compAlgorithm.isNullOrEmpty()) {
            // Extract the selected algorithm (marked with [])
            val selected = compAlgorithm.split(" ").find { it.startsWith("[") && it.endsWith("]") }
            return@withContext selected?.removePrefix("[")?.removeSuffix("]") 
                ?: compAlgorithm.split(" ").firstOrNull() 
                ?: "Unknown"
        }
        
        // Try alternative paths
        val altPaths = listOf(
            "/sys/block/zram1/comp_algorithm",
            "/sys/devices/virtual/block/zram0/comp_algorithm"
        )
        
        for (path in altPaths) {
            val algo = Shell.cmd("cat $path").exec().out.firstOrNull()
            if (!algo.isNullOrEmpty()) {
                return@withContext algo.split(" ").firstOrNull() ?: "Unknown"
            }
        }
        
        return@withContext "Unknown"
    }

    // --- PROCESSES ---
    suspend fun getTopProcesses(context: Context): List<ProcessInfo> = withContext(Dispatchers.IO) {
        val out = Shell.cmd("top -n 1 -m 5").exec().out
        val list = mutableListOf<ProcessInfo>()
        val packageManager = context.packageManager
        
        for (line in out) {
            val trimLine = line.trim()
            if (trimLine.startsWith("PID") || trimLine.isBlank() || trimLine.contains("User")) continue
            
            val parts = trimLine.split("\\s+".toRegex())
            if (parts.size >= 8) { 
                val pid = parts[0]
                if (pid.toIntOrNull() == null) continue

                var cpuUsage = 0f
                if (parts.size > 8) {
                    val rawCpu = parts[8].replace("%","")
                    cpuUsage = rawCpu.toFloatOrNull() ?: 0f
                }
                
                if (cpuUsage == 0f && parts.size > 3) {
                     val indexGuess = parts.size - 4
                     if (indexGuess > 0) {
                         cpuUsage = parts[indexGuess].replace("%","").toFloatOrNull() ?: 0f
                     }
                }
                
                val processName = parts.last()
                val isSystemKernel = processName.startsWith("[") && processName.endsWith("]")
                
                var iconBitmap: ImageBitmap? = null
                var displayName = processName

                if (!isSystemKernel) {
                    try {
                        val appInfo = packageManager.getApplicationInfo(processName, 0)
                        val drawable = packageManager.getApplicationIcon(appInfo)
                        displayName = packageManager.getApplicationLabel(appInfo).toString()
                        iconBitmap = drawableToBitmap(drawable).asImageBitmap()
                    } catch (e: Exception) { }
                }
                
                if (cpuUsage > 0 || list.size < 5) {
                    list.add(ProcessInfo(displayName, processName, pid, cpuUsage, iconBitmap))
                }
            }
        }
        return@withContext list.sortedByDescending { it.cpuUsage }
    }
    
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) return drawable.bitmap
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 48
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 48
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    // --- CPU LOAD & FREQ ---
    suspend fun getCpuFrequencies(): List<String> = withContext(Dispatchers.IO) {
        val cores = Shell.cmd("ls /sys/devices/system/cpu/cpu*/cpufreq/scaling_cur_freq").exec().out
        return@withContext cores.map { 
            val freq = Shell.cmd("cat $it").exec().out.firstOrNull()?.toLongOrNull()
            if (freq != null) "${freq / 1000} MHz" else "Offline"
        }
    }

    suspend fun getCpuTotalLoad(): Float = withContext(Dispatchers.IO) {
        val split1 = readProcStat()
        if (split1.isEmpty()) return@withContext 0f
        delay(300)
        val split2 = readProcStat()
        if (split2.isEmpty()) return@withContext 0f
        
        val total1 = split1.sum(); val work1 = total1 - split1[3]
        val total2 = split2.sum(); val work2 = total2 - split2[3]
        
        val totalDelta = total2 - total1
        val workDelta = work2 - work1
        
        return@withContext if (totalDelta > 0) workDelta.toFloat() / totalDelta else 0f
    }
    
    private fun readProcStat(): List<Long> {
        val line = Shell.cmd("head -n 1 /proc/stat").exec().out.firstOrNull() ?: return emptyList()
        return line.substringAfter("cpu").trim().split("\\s+".toRegex()).mapNotNull { it.toLongOrNull() }
    }

    suspend fun getPerCoreLoad(): List<Int> = withContext(Dispatchers.IO) {
        val stats1 = readAllCpuStats()
        delay(300)
        val stats2 = readAllCpuStats()
        val loads = mutableListOf<Int>()
        for (i in 0 until stats1.size.coerceAtMost(stats2.size)) {
            val s1 = stats1[i]; val s2 = stats2[i]
            val total1 = s1.sum(); val work1 = total1 - s1[3]
            val total2 = s2.sum(); val work2 = total2 - s2[3]
            val tD = total2 - total1
            val wD = work2 - work1
            loads.add(if (tD > 0) ((wD.toFloat() / tD) * 100).toInt() else 0)
        }
        return@withContext loads
    }

    private fun readAllCpuStats(): List<List<Long>> {
        val lines = Shell.cmd("grep '^cpu[0-9]' /proc/stat").exec().out
        return lines.map { line ->
            line.substringAfter(" ").trim().split("\\s+".toRegex()).mapNotNull { it.toLongOrNull() }
        }
    }

    // --- BATTERY & DEEP SLEEP ---
    suspend fun getDetailedBatteryInfo(): OverallBatteryInfo = withContext(Dispatchers.IO) {
        val path = "/sys/class/power_supply/battery"
        fun read(file: String) = Shell.cmd("cat $path/$file").exec().out.firstOrNull() ?: ""

        val statusRaw = read("status").ifEmpty { "Unknown" }
        val voltRaw = read("voltage_now").toFloatOrNull() ?: 0f
        val voltFloat = if (voltRaw > 1000000) voltRaw / 1000000 else if (voltRaw > 1000) voltRaw / 1000 else voltRaw
        
        var currentNow = read("current_now").toIntOrNull() ?: 0
        if (abs(currentNow) > 100000) currentNow /= 1000 
        
        val tempRaw = read("temp").toFloatOrNull() ?: 0f
        val tempFloat = when {
            tempRaw > 10000 -> tempRaw / 1000f
            tempRaw > 100 -> tempRaw / 10f
            else -> tempRaw
        }

        val wattage = abs(voltFloat * (currentNow.toFloat() / 1000f))
        val level = read("capacity") + "%"
        val cycles = read("cycle_count").ifEmpty { "0" }

        val designCap = read("charge_full_design").toDoubleOrNull() ?: 0.0
        val maxCap = read("charge_full").toDoubleOrNull() ?: 0.0
        val dCap = if(designCap > 100000) designCap/1000 else designCap
        val mCap = if(maxCap > 100000) maxCap/1000 else maxCap

        val uptimeMillis = SystemClock.uptimeMillis()
        val realMillis = SystemClock.elapsedRealtime()
        val deepSleepMillis = realMillis - uptimeMillis
        
        val uptimeStr = formatDuration(realMillis)
        val dsStr = formatDuration(deepSleepMillis)
        val dsPercent = if (realMillis > 0) (deepSleepMillis.toFloat() / realMillis.toFloat()) * 100f else 0f
        val dsFormatted = "$dsStr (${"%.0f".format(dsPercent)}%)"

        return@withContext OverallBatteryInfo(
            level = level,
            status = statusRaw,
            voltage = "%.3f V".format(Locale.US, voltFloat),
            temp = "%.1f °C".format(Locale.US, tempFloat),
            currentNow = currentNow,
            wattage = wattage,
            cycleCount = cycles,
            uptime = uptimeStr,
            designCapacity = "${dCap.toInt()} mAh",
            maximumCapacity = "${mCap.toInt()} mAh",
            deepSleep = dsFormatted 
        )
    }

    suspend fun getBatteryInfoOld(): Triple<String, String, String> = withContext(Dispatchers.IO) {
        val info = getDetailedBatteryInfo()
        return@withContext Triple(
            "${info.currentNow}mA", 
            "${info.level} (${info.voltage})", 
            info.temp
        )
    }

    private fun formatDuration(millis: Long): String {
        val seconds = millis / 1000
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        return String.format(Locale.US, "%02dh %02dm", h, m)
    }
}
