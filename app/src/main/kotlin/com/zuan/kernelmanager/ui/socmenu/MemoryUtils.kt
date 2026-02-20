/*
 * Original code from: Rem01Gaming (origami_kernel_manager)
 * Modified and integrated by: Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.socmenu

import com.topjohnwu.superuser.Shell
import com.zuan.kernelmanager.utils.Utils

object MemoryUtils {
    // --- ZRAM PATHS ---
    const val ZRAM_SIZE = "/sys/block/zram0/disksize"
    const val ZRAM_RESET = "/sys/block/zram0/reset"
    const val ZRAM_DEV = "/dev/block/zram0"
    const val ZRAM_COMP_ALGORITHM = "/sys/block/zram0/comp_algorithm"
    
    // --- VM PATHS ---
    const val SWAPPINESS = "/proc/sys/vm/swappiness"
    const val VFS_CACHE_PRESSURE = "/proc/sys/vm/vfs_cache_pressure"
    const val MIN_FREE_KBYTES = "/proc/sys/vm/min_free_kbytes"
    const val EXTRA_FREE_KBYTES = "/proc/sys/vm/extra_free_kbytes"
    const val DIRTY_RATIO = "/proc/sys/vm/dirty_ratio"
    const val DIRTY_BACKGROUND_RATIO = "/proc/sys/vm/dirty_background_ratio"
    const val LAPTOP_MODE = "/proc/sys/vm/laptop_mode"

    // --- ZRAM FUNCTIONS ---
    fun getZramSizeBytes(): Long {
        return Utils.readFile(ZRAM_SIZE).trim().toLongOrNull() ?: 0L
    }

    // Mengembalikan Pair: (Algoritma Aktif, List Semua Algoritma)
    fun getZramAlgoInfo(): Pair<String, List<String>> {
        val raw = Utils.readFile(ZRAM_COMP_ALGORITHM).trim()
        if (raw.isEmpty()) return Pair("Unknown", emptyList())
        
        // Contoh raw: "lzo [lz4] zstd"
        val allAlgos = raw.replace("[", "").replace("]", "").split("\\s+".toRegex())
        val match = "\\[([^\\]]+)\\]".toRegex().find(raw)
        val active = match?.groupValues?.get(1) ?: allAlgos.firstOrNull() ?: "Unknown"
        
        return Pair(active, allAlgos)
    }

    fun swapoff() { Shell.cmd("swapoff $ZRAM_DEV").exec() }
    fun swapon() { Shell.cmd("swapon $ZRAM_DEV").exec() }
    fun mkswap() { Shell.cmd("mkswap $ZRAM_DEV").exec() }
    fun resetZram() { Shell.cmd("echo 1 > $ZRAM_RESET").exec() }
    fun setZramSize(bytes: Long) { Shell.cmd("echo $bytes > $ZRAM_SIZE").exec() }
    fun setZramCompAlgorithm(algo: String) { Shell.cmd("echo $algo > $ZRAM_COMP_ALGORITHM").exec() }

    // --- I/O SCHEDULER FUNCTIONS ---
    
    // [FIX] Menggunakan Shell "ls" karena File.listFiles() sering gagal di Android modern
    fun getBlockDevices(): List<String> {
        val output = Shell.cmd("ls /sys/block").exec().out
        return output.filter { name ->
            // Filter hanya storage (sda, sdb, mmcblk, dm-0) dan hindari loop/ram/zram
            (name.startsWith("sd") || name.startsWith("mmcblk") || name.startsWith("dm-")) && 
            !name.startsWith("loop") && !name.startsWith("zram")
        }
    }

    // Mengembalikan Pair: (Scheduler Aktif, List Scheduler Tersedia)
    fun getIOSchedulerInfo(deviceName: String): Pair<String, List<String>> {
        val path = "/sys/block/$deviceName/queue/scheduler"
        val raw = Utils.readFile(path).trim()
        if (raw.isEmpty()) return Pair("none", emptyList())

        // Contoh: "none [mq-deadline] kyber"
        val allScheds = raw.replace("[", "").replace("]", "").split("\\s+".toRegex())
        val match = "\\[([^\\]]+)\\]".toRegex().find(raw)
        val active = match?.groupValues?.get(1) ?: "none"
        
        return Pair(active, allScheds)
    }

    fun setIOScheduler(deviceName: String, scheduler: String) {
        Shell.cmd("echo $scheduler > /sys/block/$deviceName/queue/scheduler").exec()
    }

    // Generic function untuk membaca tunables di /sys/block/X/queue/Y
    fun getIOTunable(deviceName: String, tunable: String): String {
        return Utils.readFile("/sys/block/$deviceName/queue/$tunable").trim()
    }

    fun setIOTunable(deviceName: String, tunable: String, value: String) {
        Shell.cmd("echo $value > /sys/block/$deviceName/queue/$tunable").exec()
    }
}
