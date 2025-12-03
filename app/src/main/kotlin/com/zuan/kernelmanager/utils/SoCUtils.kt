/*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.utils

import android.app.ActivityManager
import android.content.Context
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.ceil

object SoCUtils {

    // GPU Paths
    const val GPU_TEMP = "/sys/class/kgsl/kgsl-3d0/temp"
    const val CURRENT_FREQ_GPU = "/sys/class/kgsl/kgsl-3d0/gpuclk"
    
    // Variables for CPU Usage calc (Total System)
    private var sPrevTotal: Long = -1
    private var sPrevIdle: Long = -1

    // Cache jumlah core
    private val numCores by lazy { Runtime.getRuntime().availableProcessors() }

    enum class GpuType {
        ADRENO, MEDIATEK_V2, MEDIATEK_LEGACY, GENERIC_DEVFREQ, UNKNOWN
    }

    // --- GENERAL INFO ---

    suspend fun getCpuInfo(): String = withContext(Dispatchers.IO) {
        try {
            val manufacturer = Utils.getSystemProperty("ro.soc.manufacturer").trim()
            val model = Utils.getSystemProperty("ro.soc.model").trim()
            if (manufacturer.isNotEmpty() && model.isNotEmpty()) {
                "$manufacturer $model"
            } else {
                Utils.getSystemProperty("ro.board.platform").takeIf { it.isNotEmpty() } ?: "Unknown SoC"
            }
        } catch (e: Exception) { "Unknown" }
    }

    suspend fun getGpuInfo(): Triple<String, String, String> = withContext(Dispatchers.IO) {
        try {
            val result = Shell.cmd("dumpsys SurfaceFlinger | grep \"GLES:\"").exec()
            if (result.isSuccess && result.out.isNotEmpty()) {
                val line = result.out.firstOrNull() ?: return@withContext Triple("N/A", "N/A", "N/A")
                val parts = line.split(",")
                val vendor = parts.getOrNull(0)?.substringAfter("GLES:")?.trim() ?: "Unknown"
                val renderer = parts.getOrNull(1)?.trim() ?: "Unknown"
                val version = if (parts.size > 2) parts.drop(2).joinToString(",").trim() else "Unknown"
                Triple(vendor, renderer, version)
            } else { Triple("N/A", "N/A", "N/A") }
        } catch (e: Exception) { Triple("N/A", "N/A", "N/A") }
    }

    suspend fun getGpuType(): GpuType = withContext(Dispatchers.IO) {
        when {
            File("/sys/class/kgsl/kgsl-3d0").exists() -> GpuType.ADRENO
            MtkUtils.isMtkV2() -> GpuType.MEDIATEK_V2
            MtkUtils.isMtkLegacy() -> GpuType.MEDIATEK_LEGACY
            GenericGpuUtils.getGpuPath() != null -> GpuType.GENERIC_DEVFREQ
            else -> GpuType.UNKNOWN
        }
    }

    suspend fun readFreqGPU(filePath: String): String = withContext(Dispatchers.IO) {
        try {
            val result = Shell.cmd("cat $filePath").exec()
            if (result.isSuccess) result.out.firstOrNull()?.trim()?.let { (it.toLong() / 1000000).toString() } ?: ""
            else ""
        } catch (e: Exception) { "" }
    }

    // --- CPU USAGE CALCULATION (System Wide) ---

    suspend fun calculateCpuUsage(): String = withContext(Dispatchers.IO) {
        val stat = Utils.readFile("/proc/stat")
        if (stat.isEmpty()) return@withContext "N/A"
        
        val parts = stat.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        if (parts.size < 8 || !parts[0].startsWith("cpu")) return@withContext "N/A"
        
        try {
            val user = parts[1].toLong(); val nice = parts[2].toLong(); val system = parts[3].toLong()
            val idle = parts[4].toLong(); val total = user + nice + system + idle + parts[5].toLong() + parts[6].toLong() + parts[7].toLong()
            
            if (sPrevTotal != -1L && total > sPrevTotal) {
                val usage = 100 * ((total - sPrevTotal) - (idle - sPrevIdle)) / (total - sPrevTotal)
                sPrevTotal = total; sPrevIdle = idle
                usage.toString()
            } else {
                sPrevTotal = total; sPrevIdle = idle
                "N/A"
            }
        } catch (e: Exception) { "N/A" }
    }

    // --- PER CORE MONITORING (NEW LOGIC) ---

    fun getCoreCount(): Int = numCores

    // Get Freq per core index (e.g. cpu0)
    fun getCoreFreq(index: Int): Long {
        return try {
            val path = "/sys/devices/system/cpu/cpu$index/cpufreq/scaling_cur_freq"
            val content = Utils.readFile(path).trim()
            if (content.isNotEmpty()) content.toLong() / 1000 else 0L
        } catch (e: Exception) { 0L }
    }

    // Get Governor per core index
    fun getCoreGov(index: Int): String {
        return try {
            val path = "/sys/devices/system/cpu/cpu$index/cpufreq/scaling_governor"
            Utils.readFile(path).trim().ifEmpty { "offline" }
        } catch (e: Exception) { "offline" }
    }

    // New Logic: Hitung Load berdasarkan (Current - Min) / (Max - Min)
    fun getCoreLoadBasedOnFreq(index: Int, currentFreq: Long): Float {
        return try {
            val minPath = "/sys/devices/system/cpu/cpu$index/cpufreq/scaling_min_freq"
            val maxPath = "/sys/devices/system/cpu/cpu$index/cpufreq/scaling_max_freq"
            
            val minRaw = Utils.readFile(minPath).trim().toLongOrNull() ?: 0L
            val maxRaw = Utils.readFile(maxPath).trim().toLongOrNull() ?: 0L
            
            val min = minRaw / 1000
            val max = maxRaw / 1000
            
            if (max > min && currentFreq >= min) {
                val percentage = ((currentFreq - min).toFloat() / (max - min).toFloat()) * 100f
                percentage.coerceIn(0f, 100f)
            } else {
                if (currentFreq > 0) 100f else 0f // Jika Max==Min (Locked), anggap 100% jika hidup
            }
        } catch (e: Exception) { 0f }
    }

    suspend fun getGpuUsage(): String = withContext(Dispatchers.IO) {
        val kgslUsage = Utils.readFile("/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage")
        if (kgslUsage.isNotEmpty()) return@withContext kgslUsage.replace("%", "").trim()
        
        val genericPath = GenericGpuUtils.getGpuPath()
        if (genericPath != null) {
            val loadFile = File("$genericPath/load")
            if (loadFile.exists()) return@withContext Utils.readFile(loadFile.absolutePath).split("@")[0].trim()
        }
        "N/A"
    }
    
    suspend fun getTotalRam(context: Context): String = withContext(Dispatchers.IO) {
        try {
            val memoryInfo = ActivityManager.MemoryInfo().apply {
                (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(this)
            }
            "${ceil(memoryInfo.totalMem / (1024.0 * 1024 * 1024)).toInt()} GB"
        } catch (e: Exception) { "N/A" }
    }
}
