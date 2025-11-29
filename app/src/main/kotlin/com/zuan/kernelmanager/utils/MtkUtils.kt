/*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.utils

import com.topjohnwu.superuser.Shell
import java.io.File

object MtkUtils {

    // === PATHS V2 (Dimensity Modern: 8300, 9000, dst) ===
    private const val V2_DIR = "/proc/gpufreqv2"
    private const val V2_OPP_TABLE = "/proc/gpufreqv2/stack_signed_opp_table"
    private const val V2_OPP_TABLE_ALT = "/proc/gpufreqv2/gpu_working_opp_table"
    private const val V2_IDX = "/proc/gpufreqv2/fix_target_opp_index"
    private const val V2_VOLT = "/proc/gpufreqv2/fix_custom_freq_volt"
    
    // === PATHS LEGACY (Helio G series, P series, Dimensity Lama) ===
    private const val LEGACY_DIR = "/proc/gpufreq"
    private const val LEGACY_OPP_DUMP = "/proc/gpufreq/gpufreq_opp_dump"
    private const val LEGACY_IDX = "/proc/gpufreq/gpufreq_opp_idx"
    
    // === COMMON PATHS ===
    const val MTK_MAX_FREQ_BOUND = "/sys/kernel/ged/hal/custom_upbound_gpu_freq"
    const val MTK_FPSGO = "/sys/kernel/fpsgo/common/fpsgo_enable"
    const val MTK_GED_KPI = "/sys/module/sspm_v3/holders/ged/parameters/is_GED_KPI_enabled"
    
    // Perfmgr paths
    const val MTK_PERFMGR_1 = "/sys/module/mtk_fpsgo/parameters/perfmgr_enable"
    const val MTK_PERFMGR_2 = "/sys/module/perfmgr_mtk/parameters/perfmgr_enable"

    // === GED FEATURES PATHS ===
    const val GED_BOOST_ENABLE = "/sys/module/ged/parameters/ged_boost_enable"
    const val GED_EXTRA_BOOST = "/sys/module/ged/parameters/boost_extra"
    const val GED_GPU_BOOST = "/sys/module/ged/parameters/boost_gpu_enable"
    const val GED_GAME_MODE = "/sys/module/ged/parameters/gx_game_mode"
    const val GED_DVFS_ENABLE = "/sys/module/ged/parameters/gpu_dvfs_enable"

    // === POWER LIMITS ===
    const val MTK_POWER_LIMITED = "/proc/gpufreq/gpufreq_power_limited"

    data class MtkPowerPolicy(
        val ignoreOverCurrent: Boolean = false,
        val ignoreLowBattPercent: Boolean = false,
        val ignoreLowBatt: Boolean = false,
        val ignoreThermal: Boolean = false,
        val ignorePbm: Boolean = false
    )

    // Cek apakah perangkat menggunakan struktur V2
    fun isMtkV2(): Boolean = File(V2_DIR).exists()
    
    // Cek apakah perangkat menggunakan struktur Legacy
    fun isMtkLegacy(): Boolean = File(LEGACY_DIR).exists()

    // Ambil path Index yang benar berdasarkan versi
    fun getFixedIndexPath(): String {
        return if (isMtkV2()) V2_IDX else LEGACY_IDX
    }

    fun getMtkPerfmgrPath(): String? {
        return when {
            File(MTK_PERFMGR_1).exists() -> MTK_PERFMGR_1
            File(MTK_PERFMGR_2).exists() -> MTK_PERFMGR_2
            else -> null
        }
    }

    fun getMtkFreqMap(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        
        val path = when {
            isMtkV2() -> if (File(V2_OPP_TABLE).exists()) V2_OPP_TABLE else V2_OPP_TABLE_ALT
            isMtkLegacy() -> LEGACY_OPP_DUMP
            else -> return emptyMap()
        }

        try {
            val output = Shell.cmd("cat $path").exec().out
            // Regex untuk menangkap angka di dalam kurung siku [0] dan angka setelah freq =
            val regex = Regex("""\[\s*(\d+)\s*].*freq\s*=\s*(\d+)""")

            output.forEach { line ->
                val match = regex.find(line)
                if (match != null) {
                    val (index, freqKHz) = match.destructured
                    val freqMHz = try {
                        (freqKHz.toLong() / 1000).toString()
                    } catch (e: Exception) {
                        freqKHz 
                    }
                    result[freqMHz] = index.trim()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun setMtkFixedFreq(index: String) {
        val cmds = mutableListOf<String>()
        val idxPath = getFixedIndexPath()
        
        if (index == "-1") {
            // Reset to Dynamic
            cmds.add("chmod 644 $idxPath")
            cmds.add("echo -1 > $idxPath")
            cmds.add("chmod 444 $idxPath")
            
            if (isMtkV2()) {
                cmds.add("chmod 644 $V2_VOLT")
                cmds.add("echo 0 0 > $V2_VOLT")
                cmds.add("chmod 444 $V2_VOLT")
            }
        } else {
            // Set Fixed Frequency
            if (isMtkV2()) {
                cmds.add("chmod 644 $V2_VOLT")
                cmds.add("echo 0 0 > $V2_VOLT")
                cmds.add("chmod 444 $V2_VOLT")
            }

            cmds.add("chmod 644 $idxPath")
            cmds.add("echo $index > $idxPath")
            cmds.add("chmod 444 $idxPath")
        }
        Shell.cmd(*cmds.toTypedArray()).exec()
    }
    
    fun setMtkMaxFreq(index: String) {
        val cmds = mutableListOf<String>()
        cmds.add("chmod 644 $MTK_MAX_FREQ_BOUND")
        cmds.add("echo $index > $MTK_MAX_FREQ_BOUND")
        cmds.add("chmod 444 $MTK_MAX_FREQ_BOUND")
        Shell.cmd(*cmds.toTypedArray()).exec()
    }

    fun setMtkFeature(path: String?, enable: Boolean) {
        if (path == null) return
        val value = if (enable) "1" else "0"
        Shell.cmd(
            "chmod 644 $path",
            "echo $value > $path",
            "chmod 444 $path"
        ).exec()
    }

    fun getPowerPolicy(): MtkPowerPolicy {
        if (!File(MTK_POWER_LIMITED).exists()) return MtkPowerPolicy()
        
        return try {
            val lines = Shell.cmd("cat $MTK_POWER_LIMITED").exec().out
            
            fun parseState(index: Int): Boolean {
                if (lines.size <= index) return false
                val parts = lines[index].split(" ").filter { it.isNotBlank() }
                return parts.lastOrNull() == "1"
            }

            MtkPowerPolicy(
                ignoreOverCurrent = parseState(1),
                ignoreLowBattPercent = parseState(2),
                ignoreLowBatt = parseState(3),
                ignoreThermal = parseState(4),
                ignorePbm = parseState(5)
            )
        } catch (e: Exception) {
            MtkPowerPolicy()
        }
    }

    fun setPowerPolicy(key: String, enable: Boolean) {
        val value = if (enable) "1" else "0"
        val cmdPrefix = when(key) {
            "oc" -> "ignore_batt_oc"
            "low_batt_p" -> "ignore_batt_percent"
            "low_batt" -> "ignore_low_batt"
            "thermal" -> "ignore_thermal_protect"
            "pbm" -> "ignore_pbm_limited"
            else -> return
        }
        Shell.cmd("echo \"$cmdPrefix $value\" > $MTK_POWER_LIMITED").exec()
    }
}
