/*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.utils

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.ceil

object SoCUtils {

    const val CPU_TEMP = "/sys/class/thermal/thermal_zone0/temp"
    const val GPU_TEMP = "/sys/class/kgsl/kgsl-3d0/temp"
    const val CURRENT_FREQ_GPU = "/sys/class/kgsl/kgsl-3d0/gpuclk"
    
    const val CPU_BASE_PATH = "/sys/devices/system/cpu/cpufreq"

    const val CPU_INPUT_BOOST_MS = "/sys/devices/system/cpu/cpu_boost/input_boost_ms"
    const val CPU_SCHED_BOOST_ON_INPUT = "/sys/devices/system/cpu/cpu_boost/sched_boost_on_input"

    private var sPrevTotal: Long = -1
    private var sPrevIdle: Long = -1

    enum class GpuType {
        ADRENO, 
        MEDIATEK_V2, 
        MEDIATEK_LEGACY, 
        GENERIC_DEVFREQ, 
        UNKNOWN
    }

    suspend fun getCpuInfo(): String = withContext(Dispatchers.IO) {
        try {
            val manufacturer = Utils.getSystemProperty("ro.soc.manufacturer").trim()
            val model = Utils.getSystemProperty("ro.soc.model").trim()
            val hardware = Utils.getSystemProperty("ro.hardware").trim()
            val platform = Utils.getSystemProperty("ro.board.platform").trim()

            if (manufacturer.isNotEmpty() && model.isNotEmpty()) {
                val cleanManuf = when {
                    manufacturer.contains("QTI", ignoreCase = true) || manufacturer.contains("Qualcomm", ignoreCase = true) -> "Qualcomm"
                    manufacturer.contains("MTK", ignoreCase = true) || manufacturer.contains("MediaTek", ignoreCase = true) -> "MediaTek"
                    manufacturer.contains("S.LSI", ignoreCase = true) -> "Samsung"
                    else -> manufacturer
                }
                "$cleanManuf $model"
            } else {
                val target = if (hardware.isNotEmpty()) hardware else platform
                val lowerTarget = target.lowercase()

                when {
                    lowerTarget.contains("qcom") || lowerTarget.contains("sdm") || lowerTarget.contains("sm") -> "Qualcomm Snapdragon ($target)"
                    lowerTarget.startsWith("mt") || lowerTarget.contains("helio") || lowerTarget.contains("dimensity") -> "MediaTek ($target)"
                    lowerTarget.contains("exynos") || lowerTarget.contains("samsung") || lowerTarget.contains("universal") -> "Samsung Exynos ($target)"
                    lowerTarget.contains("gs1") || lowerTarget.contains("gs2") || lowerTarget.contains("zuma") || lowerTarget.contains("tensor") -> "Google Tensor ($target)"
                    lowerTarget.contains("kirin") || lowerTarget.contains("hi") -> "HiSilicon Kirin ($target)"
                    lowerTarget.contains("unisoc") || lowerTarget.contains("sc") || lowerTarget.contains("sp") -> "Unisoc ($target)"
                    target.isNotEmpty() -> target.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    else -> "Unknown SoC"
                }
            }
        } catch (e: Exception) {
            "Unknown"
        }
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
            } else {
                Triple("N/A", "N/A", "N/A")
            }
        } catch (e: Exception) {
            Triple("N/A", "N/A", "N/A")
        }
    }

    suspend fun getCpuPolicies(): List<String> = withContext(Dispatchers.IO) {
        try {
            val dir = File(CPU_BASE_PATH)
            if (!dir.exists() || !dir.isDirectory) return@withContext emptyList<String>()

            dir.listFiles()
                ?.filter { it.name.startsWith("policy") }
                ?.sortedBy { 
                    it.name.removePrefix("policy").toIntOrNull() ?: 999 
                }
                ?.map { it.absolutePath } 
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
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

    suspend fun readFreqCPU(policyPath: String, file: String): String = withContext(Dispatchers.IO) {
        try {
            val fullPath = "$policyPath/$file"
            val f = File(fullPath)
            if (f.exists()) (f.readText().trim().toInt() / 1000).toString() else ""
        } catch (e: Exception) { "" }
    }

    suspend fun writeFreqCPU(policyPath: String, file: String, frequency: String) = withContext(Dispatchers.IO) {
        try {
            val fullPath = "$policyPath/$file"
            val freqInKHz = (frequency.toInt() * 1000).toString()
            Shell.cmd("echo $freqInKHz > $fullPath").exec()
        } catch (e: Exception) { e.printStackTrace() }
    }
    
    suspend fun writeGovCPU(policyPath: String, governor: String) = withContext(Dispatchers.IO) {
        try {
             val fullPath = "$policyPath/scaling_governor"
             Shell.cmd("echo $governor > $fullPath").exec()
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun readAvailableFreqCPU(policyPath: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val file = File("$policyPath/scaling_available_frequencies")
            if (file.exists()) file.readText().trim().split(" ").map { (it.toInt() / 1000).toString() } else emptyList()
        } catch (e: Exception) { emptyList() }
    }
    
    suspend fun readAvailableBoostFreq(policyPath: String): List<String> = withContext(Dispatchers.IO) {
         try {
            val file = File("$policyPath/scaling_boost_frequencies")
            if (file.exists()) file.readText().trim().split(" ").map { (it.toInt() / 1000).toString() } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun readAvailableGovCPU(policyPath: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val file = File("$policyPath/scaling_available_governors")
            if (file.exists()) file.readText().trim().split(" ") else emptyList()
        } catch (e: Exception) { emptyList() }
    }
    
    suspend fun readGovernor(policyPath: String): String = withContext(Dispatchers.IO) {
         try {
            val file = File("$policyPath/scaling_governor")
            if (file.exists()) file.readText().trim() else "N/A"
        } catch (e: Exception) { "N/A" }
    }

    suspend fun readFreqGPU(filePath: String): String = withContext(Dispatchers.IO) {
        try {
            val result = Shell.cmd("cat $filePath").exec()
            if (result.isSuccess) result.out.firstOrNull()?.trim()?.let { (it.toLong() / 1000000).toString() } ?: ""
            else ""
        } catch (e: Exception) { "" }
    }

    suspend fun getCpuUsage(): String = withContext(Dispatchers.IO) {
        val stat = Utils.readFile("/proc/stat") ?: return@withContext "N/A"
        val trimmedStat = stat.trim()
        if (!trimmedStat.startsWith("cpu")) return@withContext "N/A"
        val parts = trimmedStat.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        if (parts.size < 8) return@withContext "N/A"
        
        try {
            val user = parts[1].toLong(); val nice = parts[2].toLong(); val system = parts[3].toLong()
            val idle = parts[4].toLong(); val iowait = parts[5].toLong(); val irq = parts[6].toLong()
            val softirq = parts[7].toLong(); val steal = if (parts.size > 8) parts[8].toLong() else 0
            val total = user + nice + system + idle + iowait + irq + softirq + steal
            
            if (sPrevTotal != -1L && total > sPrevTotal) {
                val diffTotal = total - sPrevTotal
                val diffIdle = idle - sPrevIdle
                val usage = 100 * (diffTotal - diffIdle) / diffTotal
                sPrevTotal = total; sPrevIdle = idle
                usage.toString()
            } else {
                sPrevTotal = total; sPrevIdle = idle
                "N/A"
            }
        } catch (e: NumberFormatException) { "N/A" }
    }

    suspend fun getGpuUsage(): String = withContext(Dispatchers.IO) {
        val kgslUsage = Utils.readFile("/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage")
        if (kgslUsage.isNotEmpty()) {
            return@withContext kgslUsage.replace("%", "").trim()
        }
        
        val genericPath = GenericGpuUtils.getGpuPath()
        if (genericPath != null) {
            val loadFile = File("$genericPath/load")
            if (loadFile.exists()) {
                val content = Utils.readFile(loadFile.absolutePath)
                return@withContext content.split("@")[0].trim() 
            }
        }

        "N/A"
    }
    
    suspend fun getTotalRam(context: Context): String = withContext(Dispatchers.IO) {
        try {
            val memoryInfo = ActivityManager.MemoryInfo().apply {
                (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(this)
            }
            val sizeInGb = memoryInfo.totalMem / (1024.0 * 1024 * 1024)
            "${ceil(sizeInGb).toInt()} GB"
        } catch (e: Exception) {
            "N/A"
        }
    }
}
