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

    fun isAdreno(): Boolean {
        return File("/sys/class/kgsl/kgsl-3d0").exists()
    }

    fun readFreqGPU(filePath: String): String {
        return try {
            val result = Shell.cmd("cat $filePath").exec()
            if (result.isSuccess) result.out.firstOrNull()?.trim()?.let { (it.toLong() / 1000000).toString() } ?: ""
            else ""
        } catch (e: Exception) { "" }
    }

    fun writeFreqGPU(filePath: String, frequency: String) {
        try {
            val freqInKHz = frequency.replace("000000", "")
            Shell.cmd("echo $freqInKHz > $filePath").exec()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun readAvailableFreqGPU(): List<String> {
        return try {
            val result = Shell.cmd("cat $AVAILABLE_FREQ_GPU").exec()
            if (result.isSuccess) result.out.firstOrNull()?.trim()?.split(" ")?.map { (it.toInt() / 1000000).toString() } ?: emptyList()
            else emptyList()
        } catch (e: Exception) { emptyList() }
    }
}