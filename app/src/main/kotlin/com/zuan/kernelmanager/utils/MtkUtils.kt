/*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.utils

import com.topjohnwu.superuser.Shell
import java.io.File

object MtkUtils {

    // === DRAM / MEMORY PATHS ===
    private val DRAM_DEVFREQ_CANDIDATES = listOf(
        "/sys/class/devfreq/mtk-dvfsrc-devfreq",
        "/sys/class/devfreq/10012000.dvfsrc",
        "/sys/class/devfreq/dram",
        "/sys/kernel/helios/fliper"
    )

    private val DRAM_MTK_FLIPER_DIR = "/proc/fliper"
    private const val FLIPER_OPP_TABLE = "/proc/fliper/opp_table"
    private const val FLIPER_REQ_OPP = "/proc/fliper/req_opp"

    enum class DramType {
        NONE, DEVFREQ, MTK_FLIPER
    }

    // === EXISTING MTK PATHS ===
    private const val V2_DIR = "/proc/gpufreqv2"
    private const val V2_OPP_TABLE = "/proc/gpufreqv2/stack_signed_opp_table"
    private const val V2_OPP_TABLE_ALT = "/proc/gpufreqv2/gpu_working_opp_table"
    private const val V2_IDX = "/proc/gpufreqv2/fix_target_opp_index"
    private const val V2_VOLT = "/proc/gpufreqv2/fix_custom_freq_volt"
    
    private const val LEGACY_DIR = "/proc/gpufreq"
    private const val LEGACY_OPP_DUMP = "/proc/gpufreq/gpufreq_opp_dump"
    private const val LEGACY_IDX = "/proc/gpufreq/gpufreq_opp_idx"
    
    const val MTK_MAX_FREQ_BOUND = "/sys/kernel/ged/hal/custom_upbound_gpu_freq"
    const val MTK_FPSGO = "/sys/kernel/fpsgo/common/fpsgo_enable"
    const val MTK_GED_KPI = "/sys/module/sspm_v3/holders/ged/parameters/is_GED_KPI_enabled"
    
    const val MTK_PERFMGR_1 = "/sys/module/mtk_fpsgo/parameters/perfmgr_enable"
    const val MTK_PERFMGR_2 = "/sys/module/perfmgr_mtk/parameters/perfmgr_enable"

    const val GED_BOOST_ENABLE = "/sys/module/ged/parameters/ged_boost_enable"
    const val GED_GPU_BOOST = "/sys/module/ged/parameters/boost_gpu_enable"
    const val GED_GAME_MODE = "/sys/module/ged/parameters/gx_game_mode"

    const val MTK_POWER_LIMITED = "/proc/gpufreq/gpufreq_power_limited"

    const val MTK_CCI_MODE = "/proc/cpufreq/cpufreq_cci_mode"
    const val MTK_POWER_MODE = "/proc/cpufreq/cpufreq_power_mode"
    const val MTK_SCHED_BOOST = "/sys/devices/system/cpu/sched/sched_boost"
    const val MTK_PPM_ENABLED = "/proc/ppm/enabled"
    const val MTK_EEM_DIR = "/proc/eem"

    data class MtkPowerPolicy(
        val ignoreOverCurrent: Boolean = false,
        val ignoreLowBattPercent: Boolean = false,
        val ignoreLowBatt: Boolean = false,
        val ignoreThermal: Boolean = false,
        val ignorePbm: Boolean = false
    )

    // === DRAM LOGIC ===

    fun getDramType(): DramType {
        if (getDramDevfreqPath() != null) return DramType.DEVFREQ
        if (File(FLIPER_OPP_TABLE).exists() && File(FLIPER_REQ_OPP).exists()) return DramType.MTK_FLIPER
        return DramType.NONE
    }

    fun getDramDevfreqPath(): String? {
        return DRAM_DEVFREQ_CANDIDATES.find { File(it).exists() }
    }

    fun getDramFreqs(type: DramType): Pair<List<String>, Map<String, String>> {
        val list = mutableListOf<String>()
        val map = mutableMapOf<String, String>()

        if (type == DramType.DEVFREQ) {
            val path = getDramDevfreqPath() ?: return Pair(emptyList(), emptyMap())
            val avail = Utils.readFile("$path/available_frequencies")
            avail.split(" ").filter { it.isNotBlank() }.forEach { freqRaw ->
                try {
                    val freqLong = freqRaw.toLong()
                    val display = if (freqRaw.length > 7) "${freqLong / 1000000} MHz" else "${freqLong / 1000} MHz"
                    list.add(display)
                    map[display] = freqRaw
                } catch (e: Exception) {}
            }
        } else if (type == DramType.MTK_FLIPER) {
            try {
                val lines = Shell.cmd("cat $FLIPER_OPP_TABLE").exec().out
                val regex = Regex("""\[(\d+)\]\s*:?\s*(\d+)""")
                lines.forEach { line ->
                    regex.find(line)?.let { match ->
                        val (idx, freq) = match.destructured
                        val display = "${freq.toLong() / 1000} MHz (OPP $idx)"
                        list.add(display)
                        map[display] = idx
                    }
                }
            } catch (e: Exception) {}
        }
        return Pair(list, map)
    }

    fun getDramGovs(): List<String> {
        val path = getDramDevfreqPath() ?: return emptyList()
        return Utils.readFile("$path/available_governors").split(" ").filter { it.isNotBlank() }
    }

    fun getDramCurrentInfo(type: DramType): Map<String, String> {
        val info = mutableMapOf<String, String>()
        if (type == DramType.DEVFREQ) {
            val path = getDramDevfreqPath()!!
            info["cur"] = Utils.readFile("$path/cur_freq")
            info["min"] = Utils.readFile("$path/min_freq")
            info["max"] = Utils.readFile("$path/max_freq")
            info["gov"] = Utils.readFile("$path/governor")
        } else if (type == DramType.MTK_FLIPER) {
            val idx = Utils.readFile(FLIPER_REQ_OPP)
            info["idx"] = idx
        }
        return info
    }

    fun setDramFreq(type: DramType, target: String, value: String) {
        if (type == DramType.DEVFREQ) {
            val path = getDramDevfreqPath() ?: return
            val file = if (target == "min") "min_freq" else "max_freq"
            Shell.cmd("echo $value > $path/$file").exec()
        } else if (type == DramType.MTK_FLIPER) {
            Shell.cmd("echo $value > $FLIPER_REQ_OPP").exec()
        }
    }

    fun setDramGov(gov: String) {
        val path = getDramDevfreqPath() ?: return
        Shell.cmd("echo $gov > $path/governor").exec()
    }

    // === GPU LOGIC ===

    fun isMtkV2(): Boolean = File(V2_DIR).exists()
    fun isMtkLegacy(): Boolean = File(LEGACY_DIR).exists()
    fun getFixedIndexPath(): String = if (isMtkV2()) V2_IDX else LEGACY_IDX
    fun getMtkPerfmgrPath(): String? = when { File(MTK_PERFMGR_1).exists() -> MTK_PERFMGR_1; File(MTK_PERFMGR_2).exists() -> MTK_PERFMGR_2; else -> null }
    fun hasCciMode(): Boolean = File(MTK_CCI_MODE).exists()
    fun hasPowerMode(): Boolean = File(MTK_POWER_MODE).exists()
    fun hasSchedBoost(): Boolean = File(MTK_SCHED_BOOST).exists()
    fun hasPpm(): Boolean = File(MTK_PPM_ENABLED).exists()
    fun hasEem(): Boolean = File(MTK_EEM_DIR).exists()

    fun getMtkFreqMap(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val path = when {
            isMtkV2() -> if (File(V2_OPP_TABLE).exists()) V2_OPP_TABLE else V2_OPP_TABLE_ALT
            isMtkLegacy() -> LEGACY_OPP_DUMP
            else -> return emptyMap()
        }
        try {
            val output = Shell.cmd("cat $path").exec().out
            val regex = Regex("""\[\s*(\d+)\s*].*freq\s*=\s*(\d+)""")
            output.forEach { line ->
                val match = regex.find(line)
                if (match != null) {
                    val (index, freqKHz) = match.destructured
                    val freqMHz = try { (freqKHz.toLong() / 1000).toString() } catch (e: Exception) { freqKHz }
                    result[freqMHz] = index.trim()
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return result
    }

    fun setMtkFixedFreq(index: String) {
        val cmds = mutableListOf<String>()
        val idxPath = getFixedIndexPath()
        cmds.add("chmod 644 $idxPath")
        if (index == "-1") {
            cmds.add("echo -1 > $idxPath")
            if (isMtkV2()) {
                cmds.add("chmod 644 $V2_VOLT"); cmds.add("echo 0 0 > $V2_VOLT"); cmds.add("chmod 444 $V2_VOLT")
            }
        } else {
            if (isMtkV2()) {
                cmds.add("chmod 644 $V2_VOLT"); cmds.add("echo 0 0 > $V2_VOLT"); cmds.add("chmod 444 $V2_VOLT")
            }
            cmds.add("echo $index > $idxPath")
        }
        cmds.add("chmod 444 $idxPath")
        Shell.cmd(*cmds.toTypedArray()).exec()
    }
    
    fun setMtkMaxFreq(index: String) { Shell.cmd("chmod 644 $MTK_MAX_FREQ_BOUND", "echo $index > $MTK_MAX_FREQ_BOUND", "chmod 444 $MTK_MAX_FREQ_BOUND").exec() }
    fun setMtkFeature(path: String?, enable: Boolean) { if (path == null) return; val value = if (enable) "1" else "0"; Shell.cmd("chmod 644 $path", "echo $value > $path", "chmod 444 $path").exec() }
    fun setPpmState(enable: Boolean) { val value = if (enable) "1" else "0"; Shell.cmd("echo $value > $MTK_PPM_ENABLED").exec() }
    fun isPpmEnabled(): Boolean { val out = Utils.readFile(MTK_PPM_ENABLED); return out.contains("enabled") || out == "1" }

    fun getPowerPolicy(): MtkPowerPolicy {
        if (!File(MTK_POWER_LIMITED).exists()) return MtkPowerPolicy()
        return try {
            val lines = Shell.cmd("cat $MTK_POWER_LIMITED").exec().out
            fun parseState(index: Int): Boolean = if (lines.size > index) lines[index].endsWith("1") else false
            MtkPowerPolicy(
                ignoreOverCurrent = parseState(1),
                ignoreLowBattPercent = parseState(2),
                ignoreLowBatt = parseState(3),
                ignoreThermal = parseState(4),
                ignorePbm = parseState(5)
            )
        } catch (e: Exception) { MtkPowerPolicy() }
    }

    fun setPowerPolicy(key: String, enable: Boolean) {
        val value = if (enable) "1" else "0"
        val cmdPrefix = when(key) {
            "oc" -> "ignore_batt_oc"; "low_batt_p" -> "ignore_batt_percent"; "low_batt" -> "ignore_low_batt"; "thermal" -> "ignore_thermal_protect"; "pbm" -> "ignore_pbm_limited"; else -> return
        }
        Shell.cmd("echo \"$cmdPrefix $value\" > $MTK_POWER_LIMITED").exec()
    }

    /**
     * Helper untuk membersihkan output "debug" dari kernel MTK Modern (G99/Dimensity).
     * Mengubah "[GPUFREQ-DEBUG] ... index is disabled" menjadi "-1".
     * Aman untuk device lama (akan langsung return angka jika input sudah bersih).
     */
    fun parseMtkIndex(raw: String): String {
        val cleanRaw = raw.trim()
        if (cleanRaw.isEmpty() || cleanRaw == "-1") return "-1"
        // Handle G99 Debug Text: "disabled" = Dynamic
        if (cleanRaw.contains("disabled", ignoreCase = true)) return "-1"
        // Handle Standard Device (Cuma angka)
        if (cleanRaw.all { it.isDigit() || it == '-' }) return cleanRaw
        
        // Handle G99 Debug Text: "... index is 2" -> Ambil angka terakhir
        val numbers = Regex("-?\\d+").findAll(cleanRaw).map { it.value }.toList()
        return if (numbers.isNotEmpty()) numbers.last() else "-1"
    }
}
