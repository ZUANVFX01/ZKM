/*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.utils

import com.topjohnwu.superuser.Shell
import java.io.File

object AdrenoUtils {

    // === ADRENO PATHS ===
    const val MIN_FREQ_GPU = "/sys/class/kgsl/kgsl-3d0/min_clock_mhz"
    const val MAX_FREQ_GPU = "/sys/class/kgsl/kgsl-3d0/max_clock_mhz"
    const val CURRENT_FREQ_GPU = "/sys/class/kgsl/kgsl-3d0/gpuclk"
    const val AVAILABLE_FREQ_GPU = "/sys/class/kgsl/kgsl-3d0/gpu_available_frequencies"
    const val GOV_GPU = "/sys/class/kgsl/kgsl-3d0/devfreq/governor"
    const val AVAILABLE_GOV_GPU = "/sys/class/kgsl/kgsl-3d0/devfreq/available_governors"
    const val MAX_PWRLEVEL = "/sys/class/kgsl/kgsl-3d0/max_pwrlevel"
    const val MIN_PWRLEVEL = "/sys/class/kgsl/kgsl-3d0/min_pwrlevel"
    const val DEFAULT_PWRLEVEL = "/sys/class/kgsl/kgsl-3d0/default_pwrlevel"
    const val ADRENO_BOOST = "/sys/class/kgsl/kgsl-3d0/devfreq/adrenoboost"
    const val GPU_THROTTLING = "/sys/class/kgsl/kgsl-3d0/throttling"
    
    // === QCOM BUS DCVS PATHS ===
    const val BUS_DCVS_DIR = "/sys/devices/system/cpu/bus_dcvs"

    // === ADRENO IDLER & SIMPLE GPU ===
    const val IDLER_DIR = "/sys/module/adreno_idler/parameters"
    const val IDLER_ACTIVE = "$IDLER_DIR/adreno_idler_active"
    const val IDLER_IDLEWAIT = "$IDLER_DIR/adreno_idler_idlewait"
    const val IDLER_DOWNDIFF = "$IDLER_DIR/adreno_idler_downdifferential"
    const val IDLER_WORKLOAD = "$IDLER_DIR/adreno_idler_idleworkload"
    
    const val SIMPLE_GPU_DIR = "/sys/module/simple_gpu_algorithm/parameters"
    const val SIMPLE_GPU_ACTIVATE = "$SIMPLE_GPU_DIR/simple_gpu_activate"
    const val SIMPLE_GPU_LAZINESS = "$SIMPLE_GPU_DIR/simple_laziness"
    const val SIMPLE_RAMP_THRESHOLD = "$SIMPLE_GPU_DIR/simple_ramp_threshold"

    fun isAdreno(): Boolean = File("/sys/class/kgsl/kgsl-3d0").exists()
    fun hasAdrenoIdler(): Boolean = File(IDLER_DIR).exists()
    fun hasSimpleGpu(): Boolean = File(SIMPLE_GPU_DIR).exists()
    fun hasGpuThrottling(): Boolean = File(GPU_THROTTLING).exists()
    
    fun hasBusDcvs(): Boolean = File(BUS_DCVS_DIR).exists()

    fun readFreqGPU(filePath: String): String {
        return try {
            val out = Utils.readFile(filePath)
            if (out.isNotEmpty()) {
                 try {
                     // Jika output raw panjang (Hz), convert ke MHz untuk display
                     if (out.length > 6) (out.toLong() / 1000000).toString() else out
                 } catch (e: Exception) { out }
            } else ""
        } catch (e: Exception) { "" }
    }

    fun writeFreqGPU(filePath: String, frequencyMHz: String) {
        try {
            val freqMHzLong = frequencyMHz.toLongOrNull() ?: return
            val freqHz = freqMHzLong * 1000000
            Shell.cmd("echo $freqHz > $filePath").exec()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun readAvailableFreqGPU(): List<String> {
        val out = Utils.readFile(AVAILABLE_FREQ_GPU)
        return out.split(" ").map { 
            try { (it.toLong() / 1000000).toString() } catch (e: Exception) { it } 
        }
    }
    
    fun readParam(path: String): String = if (File(path).exists()) Utils.readFile(path) else "N/A"

    // === BUS DCVS METHODS (SMART LOGIC) ===
    
    fun getBusComponents(): List<String> {
        if (!hasBusDcvs()) return emptyList()
        return File(BUS_DCVS_DIR).listFiles()
            ?.filter { it.isDirectory }
            ?.map { it.name }
            ?.sorted() ?: emptyList()
    }

    fun getBusAvailableFreqs(busName: String): List<String> {
        val rootPath = "$BUS_DCVS_DIR/$busName/available_frequencies"
        val childPath = "$BUS_DCVS_DIR/$busName/0/available_frequencies" // Fallback path
        
        var content = if (File(rootPath).exists()) Utils.readFile(rootPath) 
                      else if (File(childPath).exists()) Utils.readFile(childPath)
                      else ""
        
        return content.split(" ").filter { it.isNotBlank() }
    }
    
    fun getBusMinFreq(busName: String): String {
        val busDir = File("$BUS_DCVS_DIR/$busName")
        val targetDir = busDir.listFiles()?.find { 
            File(it, "min_freq").exists() && (File(it, "available_frequencies").exists() || it.name == "0")
        } ?: busDir.listFiles()?.find { File(it, "min_freq").exists() } 
        
        return if (targetDir != null) Utils.readFile(File(targetDir, "min_freq").absolutePath) else "N/A"
    }
    
    fun getBusMaxFreq(busName: String): String {
        val busDir = File("$BUS_DCVS_DIR/$busName")
        val targetDir = busDir.listFiles()?.find { 
            File(it, "max_freq").exists() && (File(it, "available_frequencies").exists() || it.name == "0")
        } ?: busDir.listFiles()?.find { File(it, "max_freq").exists() }
        
        return if (targetDir != null) Utils.readFile(File(targetDir, "max_freq").absolutePath) else "N/A"
    }

    /**
     * SMART SET FREQUENCY
     * Menangani konflik di mana kernel menolak jika Max < Min atau Min > Max.
     */
    fun setBusFreq(busName: String, target: String, freq: String) { 
        val busDir = File("$BUS_DCVS_DIR/$busName")
        val subDirs = busDir.listFiles()?.filter { it.isDirectory } ?: return
        
        val targetFreq = freq.toLongOrNull() ?: return
        
        val cmds = mutableListOf<String>()
        
        subDirs.forEach { dir ->
            val minFile = File(dir, "min_freq")
            val maxFile = File(dir, "max_freq")
            
            // Hanya proses folder yang memiliki kedua file kontrol
            if (minFile.exists() && maxFile.exists()) {
                val minPath = minFile.absolutePath
                val maxPath = maxFile.absolutePath
                
                // Baca nilai saat ini untuk mencegah konflik boundary
                val currentMin = try { Utils.readFile(minPath).trim().toLong() } catch(e:Exception) { 0L }
                val currentMax = try { Utils.readFile(maxPath).trim().toLong() } catch(e:Exception) { Long.MAX_VALUE }

                cmds.add("chmod 644 $minPath || true")
                cmds.add("chmod 644 $maxPath || true")

                if (target == "min") {
                    // Kasus: User ingin set Min. 
                    // Jika Target Min > Current Max, kita harus naikkan Max dulu.
                    if (targetFreq > currentMax) {
                         cmds.add("echo $freq > $maxPath || true") 
                    }
                    cmds.add("echo $freq > $minPath || true")
                } else {
                    // Kasus: User ingin set Max.
                    // Jika Target Max < Current Min, kita harus turunkan Min dulu.
                    if (targetFreq < currentMin) {
                        cmds.add("echo $freq > $minPath || true")
                    }
                    cmds.add("echo $freq > $maxPath || true")
                }
                
                cmds.add("chmod 444 $minPath || true")
                cmds.add("chmod 444 $maxPath || true")
            }
        }
        
        if (cmds.isNotEmpty()) {
            Shell.cmd(*cmds.toTypedArray()).exec()
        }
    }
}
