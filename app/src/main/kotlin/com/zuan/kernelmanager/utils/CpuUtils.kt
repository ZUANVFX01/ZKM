/*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.utils

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// Data class untuk menampung parameter governor
data class GovTunable(
    val name: String,
    val value: String,
    val path: String
)

object CpuUtils {

    const val CPU_BASE_PATH = "/sys/devices/system/cpu/cpufreq"
    const val CPU_TEMP = "/sys/class/thermal/thermal_zone0/temp"
    
    const val CPU_INPUT_BOOST_MS = "/sys/devices/system/cpu/cpu_boost/input_boost_ms"
    const val CPU_SCHED_BOOST_ON_INPUT = "/sys/devices/system/cpu/cpu_boost/sched_boost_on_input"

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
            val freqInKHz = (frequency.toInt() * 1000).toString()
            Shell.cmd("echo $freqInKHz > $policyPath/$file").exec()
        } catch (e: Exception) { e.printStackTrace() }
    }
    
    suspend fun writeGov(policyPath: String, governor: String) = withContext(Dispatchers.IO) {
        try { Shell.cmd("echo $governor > $policyPath/scaling_governor").exec() } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun readAvailableFreq(policyPath: String): List<String> = withContext(Dispatchers.IO) {
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

    suspend fun readAvailableGov(policyPath: String): List<String> = withContext(Dispatchers.IO) {
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

    suspend fun getCpuUsage(): String = withContext(Dispatchers.IO) {
        SoCUtils.calculateCpuUsage() 
    }

    // === GOVERNOR TUNING LOGIC ===

    suspend fun getGovernorTunables(policyPath: String, governor: String): List<GovTunable> = withContext(Dispatchers.IO) {
        try {
            // Folder governor biasanya ada di dalam policyPath dengan nama governor tersebut
            val govDir = File("$policyPath/$governor")
            
            if (!govDir.exists() || !govDir.isDirectory) {
                return@withContext emptyList<GovTunable>()
            }

            govDir.listFiles()
                ?.filter { it.isFile && it.canRead() } // Hanya file yang bisa dibaca
                ?.sortedBy { it.name }
                ?.map { file ->
                    val value = try {
                        file.readText().trim()
                    } catch (e: Exception) {
                        "N/A"
                    }
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
