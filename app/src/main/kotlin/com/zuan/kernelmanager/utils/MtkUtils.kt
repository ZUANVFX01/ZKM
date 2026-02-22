/*
 * Original code from: Rem01Gaming (origami_kernel_manager) and helloklf (vtools)
 * Modified and integrated by: Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.utils

import com.topjohnwu.superuser.Shell
import java.io.File

object MtkUtils {

    // === Helper Functions untuk IPC (Dengan Fallback) ===
    // Dibuat PUBLIC agar bisa dipakai oleh ViewModel nantinya
    fun checkExists(path: String): Boolean {
        return try {
            RootIpcManager.ipc?.nodeExists(path) ?: File(path).exists()
        } catch (e: Exception) { File(path).exists() }
    }

    fun readData(path: String): String {
        return try {
            RootIpcManager.ipc?.readNode(path) ?: Utils.readFile(path).trim()
        } catch (e: Exception) { "" }
    }

    fun writeData(path: String, value: String): Boolean {
        return try {
            // Coba pakai IPC (Super cepat)
            RootIpcManager.ipc?.writeNode(path, value) ?: run {
                // Fallback pakai Shell (Cara lama)
                Shell.cmd("su -c 'chmod 666 $path 2>/dev/null; echo \"$value\" > $path; chmod 444 $path 2>/dev/null || true'").exec().isSuccess
            }
        } catch (e: Exception) { false }
    }

    fun readLines(path: String): List<String> {
        val content = readData(path)
        if (content.isNotEmpty()) return content.lines()
        
        return try {
            Shell.cmd("cat $path").exec().out
        } catch (e: Exception) { emptyList() }
    }

    // === GED PATHS ===
    private const val GED_MODULE_PARAMS = "/sys/module/ged/parameters"
    const val GED_DFS_ENABLE = "$GED_MODULE_PARAMS/gpu_dvfs_enable"
    const val GED_BOOST_ENABLE = "$GED_MODULE_PARAMS/ged_boost_enable"
    const val GED_GAME_MODE = "$GED_MODULE_PARAMS/gx_game_mode"
    const val GED_EXTRA_BOOST = "$GED_MODULE_PARAMS/boost_extra"
    const val GED_GPU_BOOST = "$GED_MODULE_PARAMS/boost_gpu_enable"
    
    private const val GED_KERNEL_HAL = "/sys/kernel/ged/hal"
    private const val WRITE_MIN_FREQ = "$GED_KERNEL_HAL/custom_boost_gpu_freq"
    private const val WRITE_MAX_FREQ = "$GED_KERNEL_HAL/custom_upbound_gpu_freq"
    const val READ_MIN_FREQ = "$GED_MODULE_PARAMS/custom_boost_gpu_freq"
    const val READ_MAX_FREQ = "$GED_MODULE_PARAMS/custom_upbound_gpu_freq"
    const val CURRENT_FREQ_PATH = "$GED_KERNEL_HAL/current_freqency"

    // === LEGACY & V2 GPUFREQ ===
    private const val V2_DIR = "/proc/gpufreqv2"
    private const val LEGACY_DIR = "/proc/gpufreq"
    private const val V2_FIXED_INDEX = "/proc/gpufreqv2/fix_target_opp_index"
    private const val LEGACY_FIXED_IDX = "/proc/gpufreq/gpufreq_opp_freq"
    private const val V2_OPP_TABLE = "/proc/gpufreqv2/stack_signed_opp_table"
    private const val V2_OPP_TABLE_ALT = "/proc/gpufreqv2/gpu_working_opp_table"
    private const val LEGACY_OPP_DUMP = "/proc/gpufreq/gpufreq_opp_dump"

    // === DEVFREQ PATH (STANDAR GKI BARU) ===
    const val GPU_DEVFREQ_DIR = "/sys/class/devfreq"

    // === DRAM PATHS (FIX G95) ===
    private val DRAM_DEVFREQ_CANDIDATES = listOf(
        "/sys/class/devfreq/mtk-dvfsrc-devfreq", 
        "/sys/class/devfreq/10012000.dvfsrc", 
        "/sys/class/devfreq/dram",
        "/sys/class/devfreq/13000000.dvfsrc"
    )
    private const val DRAM_MMDVFS = "/sys/kernel/mmdvfs/pmqos"
    private const val DRAM_MMDVFS_PROC = "/proc/mmdvfs_pmqos"
    private const val FLIPER_OPP_TABLE = "/proc/fliper/opp_table"
    private const val FLIPER_REQ_OPP = "/proc/fliper/req_opp"

    // === PPM PATHS ===
    const val PPM_ENABLED = "/proc/ppm/enabled"
    const val PPM_POLICY_STATUS = "/proc/ppm/policy_status"

    // === CPU MISC ===
    const val CPU_CCI_MODE = "/proc/cpufreq/cpufreq_cci_mode"
    const val CPU_POWER_MODE = "/proc/cpufreq/cpufreq_power_mode"
    const val EEM_DIR = "/proc/eem"
    const val SCHED_BOOST = "/sys/devices/system/cpu/sched/sched_boost"
    
    // === EARA THERMAL ===
    const val EARA_ENABLE = "/sys/kernel/eara_thermal/enable"
    const val EARA_FAKE_THROTTLE = "/sys/kernel/eara_thermal/fake_throttle"

    // === FPSGO ===
    const val MTK_FPSGO = "/sys/kernel/fpsgo/common/fpsgo_enable"

    enum class DramType { NONE, DEVFREQ, MMDVFS, MTK_FLIPER }
    enum class GpuArch { UNKNOWN, LEGACY_GED, V2_GED, DEVFREQ } // Tambahan DEVFREQ

    // Fungsi untuk mencari path node Devfreq GPU
    fun getGpuDevfreqNode(): String? {
        val dirs = try {
            RootIpcManager.ipc?.listDirectories(GPU_DEVFREQ_DIR) ?: File(GPU_DEVFREQ_DIR).listFiles()?.map { it.name } ?: emptyList()
        } catch (e: Exception) { emptyList() }
        
        // Cari folder yang mengandung nama mali, gpu, atau dfrgx
        val targetName = dirs.find { it.contains("mali", true) || it.contains("gpu", true) || it == "dfrgx" }
        return targetName?.let { "$GPU_DEVFREQ_DIR/$it" }
    }

    fun getGpuArch(): GpuArch {
        return when {
            checkExists(V2_DIR) -> GpuArch.V2_GED
            checkExists(LEGACY_DIR) -> GpuArch.LEGACY_GED
            getGpuDevfreqNode() != null -> GpuArch.DEVFREQ // Fallback ke GKI standar
            else -> GpuArch.UNKNOWN
        }
    }

    fun isMtkV2(): Boolean = checkExists(V2_DIR)
    fun isMtkLegacy(): Boolean = checkExists(LEGACY_DIR)
    fun getFixedIndexPath(): String = if (isMtkV2()) V2_FIXED_INDEX else LEGACY_FIXED_IDX

    fun getGpuLoad(): String {
        return try {
            val idlePath = "$GED_MODULE_PARAMS/gpu_idle"
            if (checkExists(idlePath)) {
                val idleStr = readData(idlePath)
                val idle = idleStr.toIntOrNull() ?: 100
                "${100 - idle}%"
            } else {
                val path = listOf(
                    "$GED_KERNEL_HAL/gpu_utilization", 
                    "/proc/mali/utilization"
                ).find { checkExists(it) }
                path?.let { "${readData(it).split(" ").first()}%" } ?: "N/A"
            }
        } catch (e: Exception) { "N/A" }
    }

    fun getCurrentGpuFreq(): String {
        return try {
            // Cek GED Terlebih Dahulu
            if (checkExists(CURRENT_FREQ_PATH)) {
                val freqRaw = readData(CURRENT_FREQ_PATH)
                val freq = freqRaw.split(" ").firstOrNull()?.toLongOrNull() ?: 0L
                if (freq > 0) "${freq / 1000} MHz" else "N/A"
            } else {
                // Fallback ke Devfreq
                val devfreqNode = getGpuDevfreqNode()
                if (devfreqNode != null && checkExists("$devfreqNode/cur_freq")) {
                    val freqRaw = readData("$devfreqNode/cur_freq")
                    val freq = freqRaw.toLongOrNull() ?: 0L
                    if (freq > 1000000) "${freq / 1000000} MHz" else "${freq / 1000} MHz"
                } else {
                    "N/A"
                }
            }
        } catch (e: Exception) { "N/A" }
    }

    fun getMtkFreqMap(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val path = when {
            checkExists(V2_OPP_TABLE) -> V2_OPP_TABLE
            checkExists(V2_OPP_TABLE_ALT) -> V2_OPP_TABLE_ALT
            checkExists(LEGACY_OPP_DUMP) -> LEGACY_OPP_DUMP
            else -> return emptyMap()
        }
        
        try {
            readLines(path).forEach { line ->
                val regex = Regex("""\[\s*(\d+)\s*\].*?(\d+)(?:\s*MHz)?""")
                regex.find(line)?.let { match ->
                    val (indexStr, freqVal) = match.destructured
                    result[freqVal] = indexStr.trim()
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return result
    }

    fun lockGpuFreq(index: String) {
        val path = when (getGpuArch()) {
            GpuArch.V2_GED -> V2_FIXED_INDEX
            GpuArch.LEGACY_GED -> LEGACY_FIXED_IDX
            else -> return
        }
        writeData(path, index)
    }

    fun resetGpuLock() {
        lockGpuFreq("-1")
    }

    fun setMtkMinFreq(index: String) {
        val finalIdx = if (index == "-1") "-1" else parseMtkIndex(index)
        writeData(WRITE_MIN_FREQ, finalIdx)
    }

    fun setMtkMaxFreq(index: String) {
        val finalIdx = if (index == "-1") "-1" else parseMtkIndex(index)
        writeData(WRITE_MAX_FREQ, finalIdx)
    }

    fun getCurrentMinIndex(): String = parseMtkIndex(readData(READ_MIN_FREQ))
    fun getCurrentMaxIndex(): String = parseMtkIndex(readData(READ_MAX_FREQ))

    fun parseMtkIndex(raw: String): String {
        val cl = raw.trim()
        if (cl.isEmpty() || cl == "-1" || cl.contains("disabled", true) || cl.contains("dynamic", true)) return "-1"
        return Regex("-?\\d+").findAll(cl).map { it.value }.toList().lastOrNull() ?: "-1"
    }

    // === DRAM METHODS ===
    fun getDramType(): DramType {
        if (checkExists(DRAM_MMDVFS) || checkExists(DRAM_MMDVFS_PROC)) return DramType.MMDVFS
        if (checkExists("/proc/mmdvfs/dump_setting")) return DramType.MMDVFS
        if (DRAM_DEVFREQ_CANDIDATES.any { checkExists(it) }) return DramType.DEVFREQ
        if (checkExists(FLIPER_OPP_TABLE)) return DramType.MTK_FLIPER
        return DramType.NONE
    }

    fun getDramFreqs(type: DramType): Pair<List<String>, Map<String, String>> {
        val list = mutableListOf<String>()
        val map = mutableMapOf<String, String>()
        
        when (type) {
            DramType.MMDVFS -> {
                val paths = listOf(
                    "$DRAM_MMDVFS/dump_setting",
                    "$DRAM_MMDVFS_PROC/dump_setting",
                    "/proc/mmdvfs/dump_setting",
                    "/sys/kernel/mmdvfs/dump_setting"
                )
                
                paths.find { checkExists(it) }?.let { path ->
                    readLines(path).forEach { line ->
                        Regex("""\[(\d+)\].*?(\d+)\s*MHz""").find(line)?.let { m ->
                            val (idx, freq) = m.destructured
                            list.add("$freq MHz")
                            map["$freq MHz"] = idx
                        }
                    }
                }
            }
            DramType.DEVFREQ -> {
                DRAM_DEVFREQ_CANDIDATES.find { checkExists(it) }?.let { path ->
                    readData("$path/available_frequencies").split(" ")
                        .filter { it.isNotBlank() }.forEach { freq ->
                        val mhz = freq.toLongOrNull() ?: return@forEach
                        val label = if (mhz > 1000) "${mhz/1000} MHz" else "$mhz MHz"
                        list.add(label)
                        map[label] = freq
                    }
                }
            }
            DramType.MTK_FLIPER -> {
                readLines(FLIPER_OPP_TABLE).forEach { l ->
                    Regex("""\[(\d+)\]\s*:?\s*(\d+)""").find(l)?.let { m ->
                        val (i, f) = m.destructured
                        val d = "${f.toLong()/1000} MHz"
                        list.add(d)
                        map[d] = i
                    }
                }
            }
            else -> {}
        }
        return Pair(list, map)
    }

    fun getDramCurrentInfo(type: DramType): Map<String, String> {
        val info = mutableMapOf<String, String>()
        when (type) {
            DramType.MMDVFS -> {
                val forcePaths = listOf("$DRAM_MMDVFS/force_step", "$DRAM_MMDVFS_PROC/force_step", "/proc/mmdvfs/force_step")
                val dumpPaths = listOf("$DRAM_MMDVFS/dump_setting", "$DRAM_MMDVFS_PROC/dump_setting", "/proc/mmdvfs/dump_setting")
                
                val forcePath = forcePaths.find { checkExists(it) }
                val dumpPath = dumpPaths.find { checkExists(it) }
                
                info["force_step"] = forcePath?.let { readData(it) } ?: "-1"
                info["current"] = dumpPath?.let { 
                    readData(it).lines().find { l -> l.contains("*") || l.contains(">") }?.trim()
                } ?: "N/A"
            }
            DramType.DEVFREQ -> {
                val p = DRAM_DEVFREQ_CANDIDATES.find { checkExists(it) }
                p?.let {
                    info["cur"] = readData("$it/cur_freq")
                    info["min"] = readData("$it/min_freq")
                    info["max"] = readData("$it/max_freq")
                    info["gov"] = readData("$it/governor")
                }
            }
            DramType.MTK_FLIPER -> {
                info["current"] = readData(FLIPER_REQ_OPP)
            }
            else -> {}
        }
        return info
    }

    fun setDramFreq(type: DramType, displayFreq: String, value: String) {
        when (type) {
            DramType.MMDVFS -> {
                val paths = listOf("$DRAM_MMDVFS/force_step", "$DRAM_MMDVFS_PROC/force_step", "/proc/mmdvfs/force_step")
                paths.find { checkExists(it) }?.let { writeData(it, value) }
            }
            DramType.DEVFREQ -> {
                val p = DRAM_DEVFREQ_CANDIDATES.find { checkExists(it) }
                p?.let {
                    writeData("$it/min_freq", value)
                    writeData("$it/max_freq", value)
                }
            }
            DramType.MTK_FLIPER -> writeData(FLIPER_REQ_OPP, value)
            else -> {}
        }
    }

    fun getDramGovs(): List<String> {
        val p = DRAM_DEVFREQ_CANDIDATES.find { checkExists(it) }
        return p?.let { readData("$it/available_governors").split(" ") } ?: emptyList()
    }

    fun setDramGov(gov: String) {
        val p = DRAM_DEVFREQ_CANDIDATES.find { checkExists(it) }
        p?.let { writeData("$it/governor", gov) }
    }

    // === PPM METHODS ===
    data class PpmPolicy(
        val idx: Int, val name: String, val enabled: Boolean,
        val priority: Int = 0, val isActivated: Boolean = false
    )
    
    fun isPpmAvailable(): Boolean = checkExists(PPM_POLICY_STATUS)
    
    fun isPpmEnabled(): Boolean {
        val content = readData(PPM_ENABLED)
        return content == "1" || content.lowercase().contains("enabled")
    }

    fun getPpmPolicies(): List<PpmPolicy> {
        val policies = mutableListOf<PpmPolicy>()
        if (!isPpmAvailable()) return policies
        
        try {
            val statusLines = readData(PPM_POLICY_STATUS).lines()
            val dumpLines = readData("/proc/ppm/dump_policy_list").lines()
            
            val policyInfoMap = mutableMapOf<Int, Triple<String, Int, Boolean>>()
            var currentIdx = -1
            var currentName = ""
            var currentPriority = 0
            
            dumpLines.forEach { line ->
                val headerMatch = Regex("""\[(\d+)\]\s+(PPM_POLICY_\w+).+priority:\s*(\d+)""").find(line)
                if (headerMatch != null) {
                    val (idx, name, priority) = headerMatch.destructured
                    currentIdx = idx.toInt()
                    currentName = name
                    currentPriority = priority.toIntOrNull() ?: 0
                }
                
                if (currentIdx >= 0 && line.contains("is_activated")) {
                    val activated = line.contains("= 1") || line.contains("=1")
                    policyInfoMap[currentIdx] = Triple(currentName, currentPriority, activated)
                }
            }
            
            statusLines.forEach { line ->
                if (line.isBlank() || line.startsWith("Usage:")) return@forEach
                
                val idxMatch = Regex("""^\s*\[(\d+)\]""").find(line)
                val idx = idxMatch?.groupValues?.get(1)?.toIntOrNull() ?: return@forEach
                
                val nameFromLine = Regex("""PPM_POLICY_\w+""").find(line)?.value 
                    ?: policyInfoMap[idx]?.first ?: "Policy_$idx"
                
                val afterColon = line.substringAfter(":", line)
                val enabled = when {
                    afterColon.contains("enabled", ignoreCase = true) -> true
                    afterColon.contains("disabled", ignoreCase = true) -> false
                    afterColon.trim() == "1" -> true
                    afterColon.trim() == "0" -> false
                    else -> !line.lowercase().contains("disabled")
                }
                
                val info = policyInfoMap[idx]
                policies.add(PpmPolicy(
                    idx = idx, name = nameFromLine, enabled = enabled,
                    priority = info?.second ?: 0, isActivated = info?.third ?: false
                ))
            }
        } catch (e: Exception) { e.printStackTrace() }
        
        return policies.sortedBy { it.idx }
    }

    fun togglePpmPolicy(idx: Int, enable: Boolean) {
        writeData(PPM_POLICY_STATUS, "$idx ${if(enable) 1 else 0}")
    }

    fun setPpmEnabled(enable: Boolean) {
        writeData(PPM_ENABLED, if(enable) "1" else "0")
    }

    // === CPU MISC & THERMAL ===
    fun getCpuCciMode(): String = readData(CPU_CCI_MODE).ifEmpty { "0" }
    fun setCpuCciMode(mode: String) { writeData(CPU_CCI_MODE, mode) }
    
    fun getCpuPowerMode(): String = readData(CPU_POWER_MODE).ifEmpty { "0" }
    fun setCpuPowerMode(mode: String) { writeData(CPU_POWER_MODE, mode) }
    
    fun getEemOffsets(): List<Pair<String, String>> {
        val offsets = mutableListOf<Pair<String, String>>()
        // Menggunakan IPC listDirectories agar aman bypass namespace
        val dirs = RootIpcManager.ipc?.listDirectories(EEM_DIR) ?: try {
            File(EEM_DIR).listFiles()?.map { it.name } ?: emptyList()
        } catch (e: Exception) { emptyList() }
        
        dirs.filter { it.startsWith("EEM_DET_") && !it.contains("GPU") }.forEach { dirName ->
            val path = "$EEM_DIR/$dirName/eem_offset"
            if (checkExists(path)) {
                offsets.add(dirName to readData(path))
            }
        }
        return offsets
    }
    
    fun setEemOffset(detName: String, offset: String) {
        writeData("$EEM_DIR/$detName/eem_offset", offset)
    }

    fun hasEaraThermal(): Boolean = checkExists(EARA_ENABLE)
    fun isEaraEnabled(): Boolean = readData(EARA_ENABLE) == "1"
    fun setEaraEnabled(enable: Boolean) { writeData(EARA_ENABLE, if(enable) "1" else "0") }
    
    fun isEaraFakeThrottle(): Boolean = readData(EARA_FAKE_THROTTLE) == "1"
    fun setEaraFakeThrottle(enable: Boolean) { writeData(EARA_FAKE_THROTTLE, if(enable) "1" else "0") }

    fun isGedDvfsEnabled(): Boolean {
        val content = readData(GED_DFS_ENABLE)
        return content == "1" || content.contains("enabled", true)
    }
    
    fun setGedDvfsEnabled(enable: Boolean) {
        writeData(GED_DFS_ENABLE, if(enable) "1" else "0")
    }
    
    fun setMtkFeature(path: String, enable: Boolean) {
        writeData(path, if(enable) "1" else "0")
    }
}
