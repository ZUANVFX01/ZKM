/*
 * Original code from: Rem01Gaming (origami_kernel_manager) and helloklf (vtools)
 * Modified and integrated by: Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.socmenu

import com.topjohnwu.superuser.Shell
import com.zuan.kernelmanager.utils.GenericGpuUtils
import com.zuan.kernelmanager.utils.MtkUtils
import com.zuan.kernelmanager.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class GovTunable(
    val name: String,
    val value: String,
    val path: String
)

// Data Class untuk status per-Core
data class CoreStatus(
    val id: Int,
    val isOnline: Boolean
)

// Data Class untuk Cpuset
data class CpusetData(
    val name: String,
    val value: String, // e.g. "0-3"
    val path: String,
    val key: String // e.g. "top-app"
)

object CpuGpuUtils {

    // --- Constants ---
    const val GPU_TEMP = "/sys/class/kgsl/kgsl-3d0/temp"
    const val CURRENT_FREQ_GPU = "/sys/class/kgsl/kgsl-3d0/gpuclk"
    const val CPU_BASE_PATH = "/sys/devices/system/cpu/cpufreq"
    const val CPU_TEMP = "/sys/class/thermal/thermal_zone0/temp"
    
    // Path Baru
    const val CPU_ONLINE_PATH = "/sys/devices/system/cpu/cpu%d/online"
    const val CPUSET_BASE = "/dev/cpuset" 

    // Boost Paths
    const val CPU_INPUT_BOOST_MS = "/sys/devices/system/cpu/cpu_boost/input_boost_ms"
    const val CPU_SCHED_BOOST_ON_INPUT = "/sys/devices/system/cpu/cpu_boost/sched_boost_on_input"

    private var sPrevTotal: Long = -1
    private var sPrevIdle: Long = -1
    private val numCores by lazy { Runtime.getRuntime().availableProcessors() }

    enum class GpuType {
        ADRENO, MEDIATEK_V2, MEDIATEK_LEGACY, GENERIC_DEVFREQ, UNKNOWN
    }

    // --- SoC Info ---
    suspend fun getCpuInfo(): String = withContext(Dispatchers.IO) {
        try {
            val manufacturer = Utils.getSystemProperty("ro.soc.manufacturer").trim()
            val model = Utils.getSystemProperty("ro.soc.model").trim()
            if (manufacturer.isNotEmpty() && model.isNotEmpty()) "$manufacturer $model"
            else Utils.getSystemProperty("ro.board.platform").takeIf { it.isNotEmpty() } ?: "Unknown SoC"
        } catch (e: Exception) { "Unknown Processor" }
    }

    // --- CPU Logic ---
    suspend fun getCpuPolicies(): List<String> = withContext(Dispatchers.IO) {
        try {
            val dir = File(CPU_BASE_PATH)
            if (!dir.exists() || !dir.isDirectory) return@withContext emptyList<String>()
            dir.listFiles()?.filter { it.name.startsWith("policy") }
                ?.sortedBy { it.name.removePrefix("policy").toIntOrNull() ?: 999 }
                ?.map { it.absolutePath } ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun readFreq(policyPath: String, file: String): String = withContext(Dispatchers.IO) {
        try {
            val f = File("$policyPath/$file")
            if (f.exists()) (f.readText().trim().toInt() / 1000).toString() else ""
        } catch (e: Exception) { "" }
    }

    suspend fun writeFreq(policyPath: String, file: String, frequency: String) = withContext(Dispatchers.IO) {
        try {
            val freqInt = frequency.toIntOrNull()
            if (freqInt != null) {
                val freqInKHz = (freqInt * 1000).toString()
                Shell.cmd("echo $freqInKHz > $policyPath/$file").exec()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
    
    suspend fun writeGov(policyPath: String, governor: String) = withContext(Dispatchers.IO) {
        try { Shell.cmd("echo $governor > $policyPath/scaling_governor").exec() } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun readAvailableFreq(policyPath: String): List<String> = withContext(Dispatchers.IO) {
        val freqSet = mutableSetOf<Int>()
        try {
            val availFile = File("$policyPath/scaling_available_frequencies")
            if (availFile.exists()) {
                availFile.readText().trim().split("\\s+".toRegex())
                    .mapNotNull { it.toIntOrNull() }
                    .forEach { freqSet.add(it) }
            }
            val maxInfoFile = File("$policyPath/cpuinfo_max_freq")
            if (maxInfoFile.exists()) {
                maxInfoFile.readText().trim().toIntOrNull()?.let { freqSet.add(it) }
            }
            val minInfoFile = File("$policyPath/cpuinfo_min_freq")
            if (minInfoFile.exists()) {
                minInfoFile.readText().trim().toIntOrNull()?.let { freqSet.add(it) }
            }
            freqSet.sorted().map { (it / 1000).toString() }
        } catch (e: Exception) { 
            emptyList() 
        }
    }

    suspend fun readAvailableGov(policyPath: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val file = File("$policyPath/scaling_available_governors")
            if (file.exists()) {
                file.readText().trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }
    
    suspend fun readGovernor(policyPath: String): String = withContext(Dispatchers.IO) {
         try {
            val file = File("$policyPath/scaling_governor")
            if (file.exists()) file.readText().trim() else "N/A"
        } catch (e: Exception) { "N/A" }
    }

    // --- CPU CORE CONTROL [NEW] ---

    // Mendapatkan list core ID yang masuk dalam policy cluster tersebut
    suspend fun getAffectedCpus(policyPath: String): List<Int> = withContext(Dispatchers.IO) {
        try {
            val file = File("$policyPath/affected_cpus")
            if (file.exists()) {
                file.readText().trim().split(" ").mapNotNull { it.toIntOrNull() }
            } else {
                val id = policyPath.substringAfterLast("policy").toIntOrNull() ?: 0
                listOf(id) 
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getCoreStatus(coreId: Int): Boolean = withContext(Dispatchers.IO) {
        if (coreId == 0) return@withContext true // CPU0 usually always on
        try {
            val file = File(String.format(CPU_ONLINE_PATH, coreId))
            if (file.exists()) {
                file.readText().trim() == "1"
            } else true
        } catch (e: Exception) { true }
    }

    suspend fun setCoreOnline(coreId: Int, online: Boolean) = withContext(Dispatchers.IO) {
        if (coreId == 0) return@withContext 
        val value = if (online) "1" else "0"
        Shell.cmd("echo $value > ${String.format(CPU_ONLINE_PATH, coreId)}").exec()
    }

    suspend fun getTotalOnlineCores(): Int = withContext(Dispatchers.IO) {
        var count = 0
        for (i in 0 until numCores) {
            if (getCoreStatus(i)) count++
        }
        count
    }

    fun getMaxCoreCount(): Int = numCores

    // --- CPUSET LOGIC [NEW] ---
    
    suspend fun getCpusetInfo(): List<CpusetData> = withContext(Dispatchers.IO) {
        val groups = listOf("top-app", "foreground", "background", "system-background")
        val result = mutableListOf<CpusetData>()
        
        groups.forEach { group ->
            val path = "$CPUSET_BASE/$group/cpus"
            val file = File(path)
            if (file.exists()) {
                try {
                    val value = file.readText().trim()
                    val uiName = when(group) {
                        "top-app" -> "Top App"
                        "foreground" -> "Foreground App"
                        "background" -> "User's bg app"
                        "system-background" -> "System bg app"
                        else -> group
                    }
                    result.add(CpusetData(uiName, value, path, group))
                } catch (e: Exception) { /* Ignore */ }
            }
        }
        result
    }

    suspend fun applyCpuset(path: String, selectedCores: List<Int>) = withContext(Dispatchers.IO) {
        if (selectedCores.isEmpty()) return@withContext
        val sorted = selectedCores.sorted()
        // Format: "0,1,2,3" (Comma separated works on most Android kernels for cpuset)
        val value = sorted.joinToString(",") 
        Shell.cmd("echo \"$value\" > $path").exec()
    }
    
    // Helper: Parse "0-3,6-7" to List [0,1,2,3,6,7]
    fun parseCpusetCores(cpusetString: String): List<Int> {
        val cores = mutableListOf<Int>()
        try {
            val parts = cpusetString.split(",")
            parts.forEach { part ->
                if (part.contains("-")) {
                    val range = part.split("-")
                    val start = range[0].toInt()
                    val end = range[1].toInt()
                    for (i in start..end) cores.add(i)
                } else {
                    part.trim().toIntOrNull()?.let { cores.add(it) }
                }
            }
        } catch (e: Exception) { return emptyList() }
        return cores.distinct().sorted()
    }

    // --- GPU Logic ---
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

    suspend fun getGpuUsage(): String = withContext(Dispatchers.IO) {
        val kgslUsage = Utils.readFile("/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage")
        if (kgslUsage.isNotEmpty()) return@withContext kgslUsage.replace("%", "").trim()
        
        val genericPath = GenericGpuUtils.getGpuPath()
        if (genericPath != null) {
            val loadFile = File("$genericPath/load")
            if (loadFile.exists()) return@withContext Utils.readFile(loadFile.absolutePath).split("@")[0].trim()
        }
        "0"
    }

    // --- Governor Tunables ---
    suspend fun getGovernorTunables(policyPath: String, governor: String): List<GovTunable> = withContext(Dispatchers.IO) {
        try {
            val govDir = File("$policyPath/$governor")
            if (!govDir.exists() || !govDir.isDirectory) return@withContext emptyList<GovTunable>()

            govDir.listFiles()
                ?.filter { it.isFile && it.canRead() }
                ?.sortedBy { it.name }
                ?.map { file ->
                    val value = try { file.readText().trim() } catch (e: Exception) { "N/A" }
                    GovTunable(file.name, value, file.absolutePath)
                } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun writeTunable(path: String, value: String) = withContext(Dispatchers.IO) {
        try {
            Shell.cmd("echo \"$value\" > $path").exec()
        } catch (e: Exception) { e.printStackTrace() }
    }
}
