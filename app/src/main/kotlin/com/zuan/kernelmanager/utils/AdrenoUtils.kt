/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.utils

import com.topjohnwu.superuser.Shell
import java.io.File

object AdrenoUtils {

    // === HELPER IPC SUPER CEPAT UNTUK VIEWMODEL ===
    fun checkExists(path: String): Boolean {
        return try { RootIpcManager.ipc?.nodeExists(path) ?: File(path).exists() } catch (e: Exception) { false }
    }

    fun readData(path: String): String {
        return try { RootIpcManager.ipc?.readNode(path)?.trim() ?: "" } catch (e: Exception) { "" }
    }

    fun writeData(path: String, value: String): Boolean {
        return try {
            RootIpcManager.ipc?.writeNode(path, value) ?: run {
                Shell.cmd("su -c 'echo \"$value\" > $path'").exec().isSuccess
            }
        } catch (e: Exception) { false }
    }

    // === ADRENO PATHS ===
    const val KGSL_3D0_DIR = "/sys/class/kgsl/kgsl-3d0"
    const val MIN_FREQ_GPU = "$KGSL_3D0_DIR/min_clock_mhz"
    const val MAX_FREQ_GPU = "$KGSL_3D0_DIR/max_clock_mhz"
    const val CURRENT_FREQ_GPU = "$KGSL_3D0_DIR/gpuclk"
    const val AVAILABLE_FREQ_GPU = "$KGSL_3D0_DIR/gpu_available_frequencies"
    const val GOV_GPU = "$KGSL_3D0_DIR/devfreq/governor"
    const val AVAILABLE_GOV_GPU = "$KGSL_3D0_DIR/devfreq/available_governors"
    
    const val ADRENO_BOOST = "$KGSL_3D0_DIR/devfreq/adrenoboost"
    const val GPU_THROTTLING = "$KGSL_3D0_DIR/throttling"
    
    // === QCOM BUS DCVS PATHS ===
    const val BUS_DCVS_DIR = "/sys/devices/system/cpu/bus_dcvs"
    
    // === DEVFREQ BUSMON & UFSHC ===
    const val DEVFREQ_DIR = "/sys/class/devfreq"
    const val BUSMON_DIR = "$DEVFREQ_DIR/kgsl-busmon"

    // === ADRENO IDLER & SIMPLE GPU CONSTANTS (FIXED) ===
    const val IDLER_DIR = "/sys/module/adreno_idler/parameters"
    const val IDLER_ACTIVE = "$IDLER_DIR/adreno_idler_active"
    const val IDLER_IDLEWAIT = "$IDLER_DIR/adreno_idler_idlewait"
    const val IDLER_DOWNDIFF = "$IDLER_DIR/adreno_idler_downdifferential"
    const val IDLER_WORKLOAD = "$IDLER_DIR/adreno_idler_idleworkload"
    
    const val SIMPLE_GPU_DIR = "/sys/module/simple_gpu_algorithm/parameters"
    const val SIMPLE_GPU_ACTIVATE = "$SIMPLE_GPU_DIR/simple_gpu_activate"
    const val SIMPLE_GPU_LAZINESS = "$SIMPLE_GPU_DIR/simple_laziness"
    const val SIMPLE_RAMP_THRESHOLD = "$SIMPLE_GPU_DIR/simple_ramp_threshold"

    fun isAdreno(): Boolean = checkExists(KGSL_3D0_DIR)
    fun hasAdrenoIdler(): Boolean = checkExists(IDLER_DIR)
    fun hasSimpleGpu(): Boolean = checkExists(SIMPLE_GPU_DIR)
    fun hasGpuThrottling(): Boolean = checkExists(GPU_THROTTLING)
    fun hasBusDcvs(): Boolean = checkExists(BUS_DCVS_DIR)
    fun hasBusmon(): Boolean = checkExists(BUSMON_DIR)

    // Pencari UFSHC Dinamis
    fun getUfshcPath(): String? {
        val dirs = RootIpcManager.ipc?.listDirectories(DEVFREQ_DIR) ?: emptyList()
        val ufshcDir = dirs.find { it.endsWith(".ufshc", ignoreCase = true) || it.endsWith("ufshc", ignoreCase = true) }
        return if (ufshcDir != null) "$DEVFREQ_DIR/$ufshcDir" else null
    }

    fun readFreqGPU(filePath: String): String {
        return try {
            val out = readData(filePath)
            if (out.isNotEmpty()) {
                 try {
                     if (out.length > 6) (out.toLong() / 1000000).toString() else out
                 } catch (e: Exception) { out }
            } else ""
        } catch (e: Exception) { "" }
    }

    fun writeFreqGPU(filePath: String, frequencyMHz: String) {
        try {
            val freqMHzLong = frequencyMHz.toLongOrNull() ?: return
            val freqHz = freqMHzLong * 1000000
            writeData(filePath, freqHz.toString())
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun readAvailableFreqGPU(): List<String> {
        return readData(AVAILABLE_FREQ_GPU).split(" ").filter { it.isNotBlank() }.map { 
            try { (it.toLong() / 1000000).toString() } catch (e: Exception) { it } 
        }
    }
    
    fun readParam(path: String): String = if (checkExists(path)) readData(path) else "N/A"

    // === BUS DCVS METHODS ===
    fun getBusComponents(): List<String> = RootIpcManager.ipc?.listDirectories(BUS_DCVS_DIR)?.sorted() ?: emptyList()

    fun getBusAvailableFreqs(busName: String): List<String> {
        val rootPath = "$BUS_DCVS_DIR/$busName/available_frequencies"
        val childPath = "$BUS_DCVS_DIR/$busName/0/available_frequencies"
        val content = if (checkExists(rootPath)) readData(rootPath) else if (checkExists(childPath)) readData(childPath) else ""
        return content.split(" ").filter { it.isNotBlank() }
    }
    
    fun getBusMinFreq(busName: String): String {
        val dirs = RootIpcManager.ipc?.listDirectories("$BUS_DCVS_DIR/$busName") ?: emptyList()
        val targetDir = dirs.find { checkExists("$BUS_DCVS_DIR/$busName/$it/min_freq") }
        return if (targetDir != null) readData("$BUS_DCVS_DIR/$busName/$targetDir/min_freq") else "N/A"
    }
    
    fun getBusMaxFreq(busName: String): String {
        val dirs = RootIpcManager.ipc?.listDirectories("$BUS_DCVS_DIR/$busName") ?: emptyList()
        val targetDir = dirs.find { checkExists("$BUS_DCVS_DIR/$busName/$it/max_freq") }
        return if (targetDir != null) readData("$BUS_DCVS_DIR/$busName/$targetDir/max_freq") else "N/A"
    }

    // === SMART SET BUS (BRUTE-FORCE BOUNDARY) ===
    fun setBusFreq(busName: String, target: String, freq: String) { 
        val targetFreq = freq.trim().toLongOrNull() ?: return
        val dirs = RootIpcManager.ipc?.listDirectories("$BUS_DCVS_DIR/$busName") ?: return
        
        val availFreqs = getBusAvailableFreqs(busName).mapNotNull { it.toLongOrNull() }
        val absoluteMax = if (availFreqs.isNotEmpty()) availFreqs.maxOrNull().toString() else "9999999999"
        val absoluteMin = if (availFreqs.isNotEmpty()) availFreqs.minOrNull().toString() else "0"

        dirs.forEach { dirName ->
            val minPath = "$BUS_DCVS_DIR/$busName/$dirName/min_freq"
            val maxPath = "$BUS_DCVS_DIR/$busName/$dirName/max_freq"
            
            if (checkExists(minPath) && checkExists(maxPath)) {
                val currentMin = try { readData(minPath).trim().toLong() } catch(e:Exception) { 0L }
                val currentMax = try { readData(maxPath).trim().toLong() } catch(e:Exception) { Long.MAX_VALUE }

                if (target == "min") {
                    writeData(maxPath, absoluteMax) 
                    writeData(minPath, freq)
                    val restoredMax = if (currentMax < targetFreq) freq else currentMax.toString()
                    writeData(maxPath, restoredMax)
                } else { 
                    writeData(minPath, absoluteMin)
                    writeData(maxPath, freq)
                    val restoredMin = if (currentMin > targetFreq) freq else currentMin.toString()
                    writeData(minPath, restoredMin)
                }
            }
        }
    }
}