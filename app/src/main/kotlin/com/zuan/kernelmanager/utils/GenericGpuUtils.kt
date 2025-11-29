/*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.utils

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object GenericGpuUtils {

    // Cache path agar tidak perlu searching ulang setiap kali request
    private var cachedGpuPath: String? = null

    // Path statis yang umum digunakan (fallback)
    private val POTENTIAL_PATHS = listOf(
        "/sys/class/devfreq/mtk-dvfsrc-devfreq",
        "/sys/class/devfreq/dfrgx", // Samsung/Mali
        "/sys/devices/platform/dfrgx/devfreq/dfrgx",
        "/sys/class/kgsl/kgsl-3d0/devfreq", 
        "/sys/kernel/gpu", // Generic Kernel interface
        "/sys/kernel/tegra_gpu" // Nvidia Tegra
    )

    /**
     * Mencari path devfreq GPU yang valid.
     * Meniru logika 'init_run.sh' Origami yang menggunakan command 'find'.
     */
    suspend fun getGpuPath(): String? = withContext(Dispatchers.IO) {
        if (cachedGpuPath != null) return@withContext cachedGpuPath

        // 1. Coba cari menggunakan command 'find' (Paling akurat untuk Mali/Exynos)
        val searchCmds = listOf(
            "find /sys/class/devfreq/ -iname \"*.mali\" -print -quit",
            "find /sys/class/devfreq/ -iname \"*.gpu\" -print -quit",
            "find /sys/devices/platform/ -iname \"*.mali\" -print -quit"
        )

        for (cmd in searchCmds) {
            try {
                val res = Shell.cmd(cmd).exec()
                if (res.isSuccess && res.out.isNotEmpty()) {
                    val path = res.out.first().trim()
                    if (isValidDevfreq(path)) {
                        cachedGpuPath = path
                        return@withContext path
                    }
                }
            } catch (e: Exception) { /* ignore */ }
        }

        // 2. Cek List Path Statis
        for (path in POTENTIAL_PATHS) {
            if (isValidDevfreq(path)) {
                cachedGpuPath = path
                return@withContext path
            }
        }
        
        // 3. Scan manual folder /sys/class/devfreq sebagai upaya terakhir
        val devfreqDir = File("/sys/class/devfreq")
        if (devfreqDir.exists()) {
             devfreqDir.listFiles()?.forEach { 
                 if (isValidDevfreq(it.absolutePath)) {
                     cachedGpuPath = it.absolutePath
                     return@withContext it.absolutePath
                 }
             }
        }

        null
    }

    private fun isValidDevfreq(path: String): Boolean {
        // Path dianggap valid jika folder ada dan memiliki node frequencies atau governor
        val f = File(path)
        return f.exists() && 
               (File("$path/available_frequencies").exists() || 
                File("$path/available_governors").exists() ||
                File("$path/trans_stat").exists()) // trans_stat juga indikator devfreq valid
    }

    // === COMMANDS ===

    suspend fun getMinFreq(path: String): String = Utils.readFile("$path/min_freq")
    suspend fun getMaxFreq(path: String): String = Utils.readFile("$path/max_freq")
    suspend fun getCurFreq(path: String): String = Utils.readFile("$path/cur_freq")
    suspend fun getGov(path: String): String = Utils.readFile("$path/governor")

    suspend fun getAvailableFreqs(path: String): List<String> = withContext(Dispatchers.IO) {
        // Beberapa kernel menamakan file berbeda
        val file = File("$path/available_frequencies")
        if (!file.exists()) return@withContext emptyList()
        
        try {
            file.readText().trim().split(" ").filter { it.isNotBlank() }.map { 
                // Normalisasi: Devfreq biasanya Hz atau KHz. Kita ubah ke MHz agar seragam di UI.
                val freqVal = it.toLongOrNull() ?: 0L
                when {
                    freqVal > 1_000_000 -> (freqVal / 1_000_000).toString() // Hz -> MHz
                    freqVal > 1_000 -> (freqVal / 1_000).toString() // KHz -> MHz
                    else -> it // Anggap sudah MHz atau format lain
                }
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getAvailableGovs(path: String): List<String> = withContext(Dispatchers.IO) {
        val file = File("$path/available_governors")
        if (file.exists()) file.readText().trim().split(" ") else emptyList()
    }

    suspend fun setFreq(path: String, type: String, freqMHz: String) {
        // Kita harus mengembalikan MHz ke unit asli kernel (biasanya Hz atau KHz)
        // Cara paling aman adalah membaca salah satu freq dari available_frequencies untuk menebak unit,
        // Tapi untuk simplifikasi, kita asumsikan input dari UI sudah sesuai map yang kita buat di getAvailableFreqs.
        // NOTE: Implementasi ini butuh logika konversi balik jika kernel pakai Hz.
        
        // Versi simpel: tulis langsung (generic biasanya nerima raw value)
        val node = if (type == "min") "min_freq" else "max_freq"
        
        // Cari raw value dari available_frequencies yang cocok dengan MHz user
        val rawContent = Utils.readFile("$path/available_frequencies")
        val rawFreqs = rawContent.split(" ")
        
        // Cari frekuensi mentah yang cocok
        val targetRaw = rawFreqs.find { 
             val valL = it.toLongOrNull() ?: 0L
             val mhz = if (valL > 1000000) valL/1000000 else if (valL > 1000) valL/1000 else valL
             mhz.toString() == freqMHz
        } ?: freqMHz // Fallback pakai input user

        Utils.writeFile("$path/$node", targetRaw)
    }

    suspend fun setGov(path: String, gov: String) {
        Utils.writeFile("$path/governor", gov)
    }
}
