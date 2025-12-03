/*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.utils

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object MiscUtils {

    // === THERMAL ===
    const val THERMAL_ZONE0_POLICY = "/sys/class/thermal/thermal_zone0/policy"
    const val THERMAL_ZONE0_AVAIL = "/sys/class/thermal/thermal_zone0/available_policies" 
    const val THERMAL_ZONE0_AVAIL_ALT = "/sys/class/thermal/thermal_zone0/scaling_available_governors"

    // === I/O SCHEDULER ===
    val IO_SCHED_PATHS = listOf(
        "/sys/block/sda/queue/scheduler",
        "/sys/block/mmcblk0/queue/scheduler",
        "/sys/block/sdb/queue/scheduler",
        "/sys/block/dm-0/queue/scheduler"
    )

    // === TOUCHPANEL ===
    const val TP_GAME_MODE = "/proc/touchpanel/game_switch_enable"
    const val TP_LIMIT_ENABLE = "/proc/touchpanel/oplus_tp_limit_enable"
    const val TP_DIRECTION = "/proc/touchpanel/oplus_tp_direction"
    
    // === DT2W ===
    const val DT2W_PATH_A = "/proc/touchpanel/double_tap_enable" 
    const val DT2W_PATH_B = "/sys/touchpanel/double_tap" 
    const val DT2W_PATH_C = "/sys/android_touch/doubletap2wake"

    // === MTK SPECIFIC ===
    const val MTK_VIBRATOR_LEVEL = "/sys/kernel/thunderquake_engine/level"
    const val MTK_PBM_STOP = "/proc/pbm/pbm_stop"
    const val MTK_BATOC_STOP = "/proc/mtk_batoc_throttling/battery_oc_protect_stop"
    const val MTK_EARA_ENABLE = "/sys/kernel/eara_thermal/enable"
    const val MTK_EARA_FAKE = "/sys/kernel/eara_thermal/fake_throttle"

    // === DEVFREQ ===
    const val DEVFREQ_BASE = "/sys/class/devfreq"

    // --- HELPERS ---

    suspend fun getIoSchedPath(): String? = withContext(Dispatchers.IO) {
        IO_SCHED_PATHS.firstOrNull { File(it).exists() }
    }

    suspend fun getDt2wPath(): String? = withContext(Dispatchers.IO) {
        if (File(DT2W_PATH_A).exists()) DT2W_PATH_A
        else if (File(DT2W_PATH_B).exists()) DT2W_PATH_B
        else if (File(DT2W_PATH_C).exists()) DT2W_PATH_C
        else null
    }

    suspend fun toggleGeneric(path: String, enable: Boolean) = withContext(Dispatchers.IO) {
        if (File(path).exists()) {
            Utils.writeFile(path, if (enable) "1" else "0")
        }
    }
}