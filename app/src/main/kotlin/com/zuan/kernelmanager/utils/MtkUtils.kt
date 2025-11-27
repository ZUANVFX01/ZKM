/*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.utils

import com.topjohnwu.superuser.Shell
import java.io.File

object MtkUtils {

    // === MEDIATEK SPECIFIC PATHS ===
    const val MTK_OPP_TABLE_V2 = "/proc/gpufreqv2/stack_signed_opp_table"
    const val MTK_OPP_TABLE_V2_ALT = "/proc/gpufreqv2/gpu_working_opp_table"
    const val MTK_FIXED_INDEX = "/proc/gpufreqv2/fix_target_opp_index"
    const val MTK_FIXED_VOLT = "/proc/gpufreqv2/fix_custom_freq_volt"
    const val MTK_MAX_FREQ_BOUND = "/sys/kernel/ged/hal/custom_upbound_gpu_freq"
    const val MTK_FPSGO = "/sys/kernel/fpsgo/common/fpsgo_enable"
    const val MTK_GED_KPI = "/sys/module/sspm_v3/holders/ged/parameters/is_GED_KPI_enabled"
    const val MTK_PERFMGR_1 = "/sys/module/mtk_fpsgo/parameters/perfmgr_enable"
    const val MTK_PERFMGR_2 = "/sys/module/perfmgr_mtk/parameters/perfmgr_enable"

    fun isMtkV2(): Boolean {
        return File(MTK_OPP_TABLE_V2).exists() || File(MTK_OPP_TABLE_V2_ALT).exists()
    }

    fun getMtkPerfmgrPath(): String? {
        return when {
            File(MTK_PERFMGR_1).exists() -> MTK_PERFMGR_1
            File(MTK_PERFMGR_2).exists() -> MTK_PERFMGR_2
            else -> null
        }
    }

    fun getMtkFreqMap(): Map<String, String> {
        val path = if (File(MTK_OPP_TABLE_V2).exists()) MTK_OPP_TABLE_V2 else MTK_OPP_TABLE_V2_ALT
        val result = mutableMapOf<String, String>()
        try {
            val output = Shell.cmd("cat $path").exec().out
            output.forEach { line ->
                if (line.contains("[")) {
                    val cleanLine = line.trim()
                    val index = cleanLine.substringAfter("[").substringBefore("]")
                    val freqRaw = cleanLine.substringAfter("] ").split(",")[0].trim()
                    val freqMhz = try {
                         (freqRaw.toInt() / 1000).toString()
                    } catch (e: Exception) { freqRaw }
                    result[freqMhz] = index
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun setMtkFixedFreq(index: String) {
        val cmds = mutableListOf<String>()
        
        if (index == "-1") {
            cmds.add("chmod 644 $MTK_FIXED_INDEX")
            cmds.add("echo -1 > $MTK_FIXED_INDEX")
            cmds.add("chmod 444 $MTK_FIXED_INDEX")
            
            cmds.add("chmod 644 $MTK_FIXED_VOLT")
            cmds.add("echo 0 0 > $MTK_FIXED_VOLT")
            cmds.add("chmod 444 $MTK_FIXED_VOLT")
        } else {
            cmds.add("chmod 644 $MTK_FIXED_VOLT")
            cmds.add("echo 0 0 > $MTK_FIXED_VOLT")
            cmds.add("chmod 444 $MTK_FIXED_VOLT")

            cmds.add("chmod 644 $MTK_FIXED_INDEX")
            cmds.add("echo $index > $MTK_FIXED_INDEX")
            cmds.add("chmod 444 $MTK_FIXED_INDEX")
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
}