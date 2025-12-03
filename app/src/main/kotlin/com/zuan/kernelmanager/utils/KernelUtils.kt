/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.utils

import android.annotation.SuppressLint
import android.system.Os
import com.topjohnwu.superuser.Shell
import java.io.File

object KernelUtils {
    // --- PROFILES ---
    @SuppressLint("SdCardPath")
    const val KERNEL_PROFILE_PATH = "/sdcard/RvKernel-Manager/kernel-profile"
    const val KERNEL_PROFILE_CURRENT = "$KERNEL_PROFILE_PATH/current_profile"
    const val KERNEL_PROFILE_POWERSAVE = "$KERNEL_PROFILE_PATH/powersave.sh"
    const val KERNEL_PROFILE_BALANCE = "$KERNEL_PROFILE_PATH/balance.sh"
    const val KERNEL_PROFILE_PERFORMANCE = "$KERNEL_PROFILE_PATH/performance.sh"

    // --- SYSTEM INFO ---
    const val FULL_KERNEL_VERSION = "/proc/version"

    // --- SCHEDULER (Origami) ---
    const val SCHED_AUTO_GROUP = "/proc/sys/kernel/sched_autogroup_enabled"
    const val SCHED_CHILD_RUNS_FIRST = "/proc/sys/kernel/sched_child_runs_first"
    const val SCHED_CSTATE_AWARE = "/proc/sys/kernel/sched_cstate_aware"
    const val SCHED_ISOLATION_HINT = "/proc/sys/kernel/sched_isolation_hint"
    const val SCHED_TUNABLE_SCALING = "/proc/sys/kernel/sched_tunable_scaling"
    const val SCHED_SCHEDSTATS = "/proc/sys/kernel/sched_schedstats"
    
    // --- BORE & UCLAMP ---
    const val PRINTK = "/proc/sys/kernel/printk"
    const val SCHED_UTIL_CLAMP_MAX = "/proc/sys/kernel/sched_util_clamp_max"
    const val SCHED_UTIL_CLAMP_MIN = "/proc/sys/kernel/sched_util_clamp_min"
    const val SCHED_UTIL_CLAMP_MIN_RT_DEFAULT = "/proc/sys/kernel/sched_util_clamp_min_rt_default"
    const val BORE = "/proc/sys/kernel/sched_bore"

    // --- MEMORY (Origami) ---
    const val ZRAM = "/dev/block/zram0"
    const val ZRAM_RESET = "/sys/block/zram0/reset"
    const val ZRAM_SIZE = "/sys/block/zram0/disksize"
    const val ZRAM_COMP_ALGORITHM = "/sys/block/zram0/comp_algorithm"
    
    const val DROP_CACHES = "/proc/sys/vm/drop_caches"
    const val SWAPPINESS = "/proc/sys/vm/swappiness"
    const val VFS_CACHE_PRESSURE = "/proc/sys/vm/vfs_cache_pressure"
    const val MIN_FREE_KBYTES = "/proc/sys/vm/min_free_kbytes"
    const val EXTRA_FREE_KBYTES = "/proc/sys/vm/extra_free_kbytes"
    const val DIRTY_RATIO = "/proc/sys/vm/dirty_ratio"
    const val DIRTY_BACKGROUND_RATIO = "/proc/sys/vm/dirty_background_ratio"
    const val LAPTOP_MODE = "/proc/sys/vm/laptop_mode"
    const val OOM_KILL_ALLOCATING_TASK = "/proc/sys/vm/oom_kill_allocating_task"

    // --- NETWORK (Origami) ---
    const val TCP_CONGESTION_ALGORITHM = "/proc/sys/net/ipv4/tcp_congestion_control"
    const val TCP_AVAILABLE_CONGESTION_ALGORITHM = "/proc/sys/net/ipv4/tcp_available_congestion_control"
    const val TCP_SYNCOOKIES = "/proc/sys/net/ipv4/tcp_syncookies"
    const val TCP_MAX_SYN_BACKLOG = "/proc/sys/net/ipv4/tcp_max_syn_backlog"
    const val TCP_KEEPALIVE_TIME = "/proc/sys/net/ipv4/tcp_keepalive_time"
    const val TCP_REUSE = "/proc/sys/net/ipv4/tcp_tw_reuse"
    const val TCP_ECN = "/proc/sys/net/ipv4/tcp_ecn"
    const val TCP_FASTOPEN = "/proc/sys/net/ipv4/tcp_fastopen"
    const val TCP_SACK = "/proc/sys/net/ipv4/tcp_sack"
    const val TCP_TIMESTAMPS = "/proc/sys/net/ipv4/tcp_timestamps"
    const val BPF_JIT_HARDEN = "/proc/sys/net/core/bpf_jit_harden"
    const val WIREGUARD_VERSION = "/sys/module/wireguard/version"

    // --- DISPLAY BACKLIGHT & SF ---
    const val BACKLIGHT_DIR = "/sys/class/backlight"

    // --- FUNCTIONS ---

    fun getKernelProfile(): Int {
        return Utils.readFile(KERNEL_PROFILE_CURRENT).trim().toIntOrNull() ?: 1
    }

    fun setKernelProfile(profile: Int) {
        when (profile) {
            0 -> {
                Shell.cmd("su -c sh $KERNEL_PROFILE_POWERSAVE").exec()
                Utils.writeFile(KERNEL_PROFILE_CURRENT, "0")
            }
            1 -> {
                Shell.cmd("su -c sh $KERNEL_PROFILE_BALANCE").exec()
                Utils.writeFile(KERNEL_PROFILE_CURRENT, "1")
            }
            2 -> {
                Shell.cmd("su -c sh $KERNEL_PROFILE_PERFORMANCE").exec()
                Utils.writeFile(KERNEL_PROFILE_CURRENT, "2")
            }
        }
    }

    fun getKernelVersion(): String {
        return Os.uname().release
    }
    
    fun getFullKernelVersion(): String {
        return Utils.readFile(FULL_KERNEL_VERSION)
    }

    fun getWireGuardVersion(): String {
        return Utils.readFile(WIREGUARD_VERSION)
    }

    fun getZramSize(): String {
        val sizeInBytes = Utils.readFile(ZRAM_SIZE).trim().toLongOrNull() ?: 0L
        val sizeInGb = sizeInBytes / 1073741824.0
        return if (sizeInGb == 0.0) "Unknown" else "${sizeInGb.toInt()} GB"
    }

    fun getZramUsagePercent(): Int {
        val memInfo = Utils.readFile("/proc/meminfo")
        try {
            val totalRegex = "SwapTotal:\\s+(\\d+) kB".toRegex()
            val freeRegex = "SwapFree:\\s+(\\d+) kB".toRegex()

            val totalMatch = totalRegex.find(memInfo)
            val freeMatch = freeRegex.find(memInfo)

            val total = totalMatch?.groupValues?.get(1)?.toLong() ?: 0L
            val free = freeMatch?.groupValues?.get(1)?.toLong() ?: 0L

            if (total == 0L) return 0

            val used = total - free
            return ((used.toDouble() / total.toDouble()) * 100).toInt()
        } catch (e: Exception) {
            return 0
        }
    }

    fun getZramCompAlgorithm(): String {
        val algorithms = Utils.readFile(ZRAM_COMP_ALGORITHM)
        return if (algorithms.isNotEmpty()) {
            val regex = "\\[([^\\]]+)\\]".toRegex()
            val match = regex.find(algorithms)
            match?.groupValues?.get(1) ?: "Unknown"
        } else {
            "Unknown"
        }
    }

    fun getAvailableZramCompAlgorithms(): List<String> {
        val algorithms = Utils.readFile(ZRAM_COMP_ALGORITHM)
        return if (algorithms.isNotEmpty()) {
            algorithms.replace("[", "").replace("]", "")
                .split("\\s+".toRegex())
                .filter { it.isNotBlank() }
        } else {
            emptyList()
        }
    }

    // Helper Functions
    fun swapoffZram() { Shell.cmd("swapoff $ZRAM").exec() }
    fun swaponZram() { Shell.cmd("swapon $ZRAM").exec() }
    fun mkswapZram() { Shell.cmd("mkswap $ZRAM").exec() }
    fun resetZram() { Shell.cmd("echo 1 > $ZRAM_RESET").exec() }
    fun setZramCompAlgorithm(algorithm: String) { Shell.cmd("echo $algorithm > $ZRAM_COMP_ALGORITHM").exec() }

    fun getTcpCongestionAlgorithm(): String {
        return Utils.readFile(TCP_CONGESTION_ALGORITHM).trim().takeIf { it.isNotEmpty() } ?: "Unknown"
    }

    fun getAvailableTcpCongestionAlgorithm(): List<String> {
        val algorithms = Utils.readFile(TCP_AVAILABLE_CONGESTION_ALGORITHM)
        return if (algorithms.isNotEmpty()) {
            algorithms.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        } else {
            emptyList()
        }
    }

    fun setTcpCongestionAlgorithm(algorithm: String) {
        Utils.writeFile(TCP_CONGESTION_ALGORITHM, algorithm)
    }

    // --- DISPLAY HELPER FUNCTIONS ---

    fun getBacklightPath(): String? {
        val dir = File(BACKLIGHT_DIR)
        if (dir.exists() && dir.isDirectory) {
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    return file.absolutePath
                }
            }
        }
        return null
    }

    fun getMaxBrightness(): Int {
        val path = getBacklightPath() ?: return 255
        return Utils.readFile("$path/max_brightness").trim().toIntOrNull() ?: 255
    }

    fun getCurrentBrightness(): Int {
        val path = getBacklightPath() ?: return 0
        return Utils.readFile("$path/brightness").trim().toIntOrNull() ?: 0
    }

    fun setBrightness(value: Int) {
        val path = getBacklightPath() ?: return
        Utils.writeFile("$path/brightness", value.toString())
    }

    fun setSurfaceFlingerSaturation(saturation: Float) {
        Shell.cmd("service call SurfaceFlinger 1022 f $saturation").exec()
    }
}
