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
    const val GPU_BUSY = "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage"

    // === ADRENO IDLER PATHS ===
    const val IDLER_DIR = "/sys/module/adreno_idler/parameters"
    const val IDLER_ACTIVE = "$IDLER_DIR/adreno_idler_active"
    const val IDLER_IDLEWAIT = "$IDLER_DIR/adreno_idler_idlewait"
    const val IDLER_DOWNDIFF = "$IDLER_DIR/adreno_idler_downdifferential"
    const val IDLER_WORKLOAD = "$IDLER_DIR/adreno_idler_idleworkload"

    // === SIMPLE GPU ALGO PATHS ===
    const val SIMPLE_GPU_DIR = "/sys/module/simple_gpu_algorithm/parameters"
    const val SIMPLE_GPU_ACTIVATE = "$SIMPLE_GPU_DIR/simple_gpu_activate"
    const val SIMPLE_GPU_LAZINESS = "$SIMPLE_GPU_DIR/simple_laziness"
    const val SIMPLE_RAMP_THRESHOLD = "$SIMPLE_GPU_DIR/simple_ramp_threshold"

    fun isAdreno(): Boolean {
        return File("/sys/class/kgsl/kgsl-3d0").exists()
    }

    fun hasAdrenoIdler(): Boolean = File(IDLER_DIR).exists()
    fun hasSimpleGpu(): Boolean = File(SIMPLE_GPU_DIR).exists()

    fun readFreqGPU(filePath: String): String {
        return try {
            val result = Shell.cmd("cat $filePath").exec()
            if (result.isSuccess) result.out.firstOrNull()?.trim()?.let { 
                try {
                    (it.toLong() / 1000000).toString() 
                } catch (e: Exception) { "" }
            } ?: ""
            else ""
        } catch (e: Exception) { "" }
    }

    fun writeFreqGPU(filePath: String, frequency: String) {
        try {
            // frequency input biasanya sudah dalam MHz dari UI (misal "800")
            // Kernel biasanya butuh Hz (800000000) atau KHz. 
            // Implementasi awal kamu menghapus 000000, kita asumsikan input UI sudah benar.
            val freqInHzOrRaw = frequency.replace("000000", "") 
            Shell.cmd("echo $freqInHzOrRaw > $filePath").exec()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun readAvailableFreqGPU(): List<String> {
        return try {
            val result = Shell.cmd("cat $AVAILABLE_FREQ_GPU").exec()
            if (result.isSuccess) result.out.firstOrNull()?.trim()?.split(" ")?.map { 
                try {
                    (it.toLong() / 1000000).toString() 
                } catch (e: Exception) { it }
            } ?: emptyList()
            else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    // Fungsi Helper yang hilang sebelumnya
    fun readParam(path: String): String {
        if (!File(path).exists()) return "N/A"
        return Utils.readFile(path)
    }
}
