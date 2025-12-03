/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.ui.kernelParameter

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zuan.kernelmanager.utils.BetaFeatures
import com.zuan.kernelmanager.utils.KernelUtils
import com.zuan.kernelmanager.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class KernelParameterViewModel : ViewModel() {

    // --- DATA CLASSES ---
    data class KernelProfile(
        val currentProfile: Int = 1,
        val hasCurrentProfile: Boolean = false,
        val hasProfilePowersave: Boolean = false,
        val hasProfileBalance: Boolean = false,
        val hasProfilePerformance: Boolean = false,
    )

    data class OrigamiScheduler(
        val schedAutogroup: String = "0",
        val hasSchedAutogroup: Boolean = false,
        val childRunsFirst: String = "0",
        val hasChildRunsFirst: Boolean = false,
        val cstateAware: String = "0",
        val hasCstateAware: Boolean = false,
        val isolationHint: String = "0",
        val hasIsolationHint: Boolean = false,
        val tunableScaling: String = "0",
        val hasTunableScaling: Boolean = false,
        val schedStats: String = "0",
        val hasSchedStats: Boolean = false
    )

    data class OrigamiNetwork(
        val tcpCongestion: String = "N/A",
        val availableTcp: List<String> = emptyList(),
        val syncookies: String = "0",
        val hasSyncookies: Boolean = false,
        val maxSynBacklog: String = "0",
        val hasMaxSynBacklog: Boolean = false,
        val keepAliveTime: String = "0",
        val hasKeepAliveTime: Boolean = false,
        val reuse: String = "0",
        val hasReuse: Boolean = false,
        val ecn: String = "0",
        val hasEcn: Boolean = false,
        val fastOpen: String = "0",
        val hasFastOpen: Boolean = false,
        val sack: String = "0",
        val hasSack: Boolean = false,
        val timestamps: String = "0",
        val hasTimestamps: Boolean = false,
        val bpfJitHarden: String = "0",
        val hasBpfJitHarden: Boolean = false
    )

    data class OrigamiMemory(
        val zramSize: String = "N/A",
        val zramAlgo: String = "N/A",
        val availableZramAlgo: List<String> = emptyList(),
        val zramUsagePercent: Int = 0,
        
        val swappiness: String = "0",
        val hasSwappiness: Boolean = false,
        val vfsCachePressure: String = "0",
        val hasVfsCachePressure: Boolean = false,
        val minFreeKbytes: String = "0",
        val hasMinFreeKbytes: Boolean = false,
        val extraFreeKbytes: String = "0",
        val hasExtraFreeKbytes: Boolean = false,
        val dirtyRatio: String = "0",
        val hasDirtyRatio: Boolean = false,
        val dirtyBackgroundRatio: String = "0",
        val hasDirtyBackgroundRatio: Boolean = false,
        val laptopMode: String = "0",
        val hasLaptopMode: Boolean = false,
        val oomKillAllocating: String = "0",
        val hasOomKillAllocating: Boolean = false
    )

    data class Uclamp(
        val hasUclampMax: Boolean = false, val uclampMax: String = "N/A",
        val hasUclampMin: Boolean = false, val uclampMin: String = "N/A",
        val hasUclampMinRt: Boolean = false, val uclampMinRt: String = "N/A",
    )

    data class BoreScheduler(
        val hasBore: Boolean = false, val bore: Int = 0
    )

    data class DisplayState(
        val refreshRate: Float = 60f,
        val resolution: String = "N/A",
        val tech: String = "Unknown",
        val sizeInch: String = "N/A",
        val hdrCaps: String = "No",
        val density: String = "N/A",
        val xdpi: String = "0",
        val ydpi: String = "0",
        val brightness: Int = 0,
        val maxBrightness: Int = 255,
        val sfSaturation: Float = 1.0f
    )

    // --- STATE FLOWS ---
    private val _kernelProfile = MutableStateFlow(KernelProfile())
    val kernelProfile: StateFlow<KernelProfile> = _kernelProfile

    private val _sched = MutableStateFlow(OrigamiScheduler())
    val sched: StateFlow<OrigamiScheduler> = _sched

    private val _net = MutableStateFlow(OrigamiNetwork())
    val net: StateFlow<OrigamiNetwork> = _net

    private val _mem = MutableStateFlow(OrigamiMemory())
    val mem: StateFlow<OrigamiMemory> = _mem

    private val _uclamp = MutableStateFlow(Uclamp())
    val uclamp: StateFlow<Uclamp> = _uclamp

    private val _boreScheduler = MutableStateFlow(BoreScheduler())
    val boreScheduler: StateFlow<BoreScheduler> = _boreScheduler
    
    private val _displayState = MutableStateFlow(DisplayState())
    val displayState: StateFlow<DisplayState> = _displayState
    
    // Misc
    private val _printk = MutableStateFlow("N/A")
    val printk: StateFlow<String> = _printk

    private val refreshRequests = Channel<Unit>(1)
    var isRefreshing by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch(Dispatchers.IO) {
            refreshAll()
            for (r in refreshRequests) {
                isRefreshing = true
                try { delay(1000) } finally { isRefreshing = false }
            }
        }
    }

    fun refresh() {
        refreshRequests.trySend(Unit)
        refreshAll()
    }

    private fun refreshAll() {
        if (BetaFeatures.isBetaFeaturesEnabled) loadKernelProfile()
        loadScheduler()
        loadNetwork()
        loadMemory()
        loadUclamp()
        loadBore()
        loadMisc()
    }
    
    // --- DISPLAY LOADERS ---
    fun loadDisplayInfo(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val metrics = DisplayMetrics()
            display.getRealMetrics(metrics)

            val width = metrics.widthPixels / metrics.xdpi
            val height = metrics.heightPixels / metrics.ydpi
            val diagonal = kotlin.math.sqrt((width * width + height * height).toDouble())
            
            val isHdr = display.isHdr
            
            val curBright = KernelUtils.getCurrentBrightness()
            val maxBright = KernelUtils.getMaxBrightness()

            _displayState.value = _displayState.value.copy(
                refreshRate = display.refreshRate,
                resolution = "${metrics.widthPixels} x ${metrics.heightPixels}",
                tech = if(metrics.densityDpi > 400) "OLED/AMOLED (Est)" else "IPS LCD (Est)",
                sizeInch = "%.1f\"".format(diagonal),
                hdrCaps = if (isHdr) "Supported" else "None",
                density = "${metrics.densityDpi} DPI",
                xdpi = "%.1f".format(metrics.xdpi),
                ydpi = "%.1f".format(metrics.ydpi),
                brightness = curBright,
                maxBrightness = maxBright
            )
        }
    }

    fun updateRealtimeRefreshRate(rate: Float) {
        _displayState.value = _displayState.value.copy(refreshRate = rate)
    }

    fun updateBacklight(value: Float) {
        val intVal = value.toInt()
        _displayState.value = _displayState.value.copy(brightness = intVal)
        viewModelScope.launch(Dispatchers.IO) {
            KernelUtils.setBrightness(intVal)
        }
    }

    fun updateSurfaceFlingerSaturation(value: Float) {
        _displayState.value = _displayState.value.copy(sfSaturation = value)
        viewModelScope.launch(Dispatchers.IO) {
            KernelUtils.setSurfaceFlingerSaturation(value)
        }
    }

    // --- OTHER LOADERS ---

    private fun loadKernelProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentProf = try { KernelUtils.getKernelProfile() } catch (e: Exception) { 1 }
            _kernelProfile.value = KernelProfile(
                currentProfile = currentProf,
                hasCurrentProfile = Utils.testFile(KernelUtils.KERNEL_PROFILE_CURRENT),
                hasProfilePowersave = Utils.testFile(KernelUtils.KERNEL_PROFILE_POWERSAVE),
                hasProfileBalance = Utils.testFile(KernelUtils.KERNEL_PROFILE_BALANCE),
                hasProfilePerformance = Utils.testFile(KernelUtils.KERNEL_PROFILE_PERFORMANCE),
            )
        }
        if (!_kernelProfile.value.hasCurrentProfile) Utils.writeFile(KernelUtils.KERNEL_PROFILE_CURRENT, "1")
    }

    private fun loadScheduler() {
        viewModelScope.launch(Dispatchers.IO) {
            _sched.value = OrigamiScheduler(
                schedAutogroup = Utils.readFile(KernelUtils.SCHED_AUTO_GROUP).trim(),
                hasSchedAutogroup = Utils.testFile(KernelUtils.SCHED_AUTO_GROUP),
                childRunsFirst = Utils.readFile(KernelUtils.SCHED_CHILD_RUNS_FIRST).trim(),
                hasChildRunsFirst = Utils.testFile(KernelUtils.SCHED_CHILD_RUNS_FIRST),
                cstateAware = Utils.readFile(KernelUtils.SCHED_CSTATE_AWARE).trim(),
                hasCstateAware = Utils.testFile(KernelUtils.SCHED_CSTATE_AWARE),
                isolationHint = Utils.readFile(KernelUtils.SCHED_ISOLATION_HINT).trim(),
                hasIsolationHint = Utils.testFile(KernelUtils.SCHED_ISOLATION_HINT),
                tunableScaling = Utils.readFile(KernelUtils.SCHED_TUNABLE_SCALING).trim(),
                hasTunableScaling = Utils.testFile(KernelUtils.SCHED_TUNABLE_SCALING),
                schedStats = Utils.readFile(KernelUtils.SCHED_SCHEDSTATS).trim(),
                hasSchedStats = Utils.testFile(KernelUtils.SCHED_SCHEDSTATS)
            )
        }
    }

    private fun loadNetwork() {
        viewModelScope.launch(Dispatchers.IO) {
            _net.value = OrigamiNetwork(
                tcpCongestion = KernelUtils.getTcpCongestionAlgorithm(),
                availableTcp = KernelUtils.getAvailableTcpCongestionAlgorithm(),
                syncookies = Utils.readFile(KernelUtils.TCP_SYNCOOKIES).trim(),
                hasSyncookies = Utils.testFile(KernelUtils.TCP_SYNCOOKIES),
                maxSynBacklog = Utils.readFile(KernelUtils.TCP_MAX_SYN_BACKLOG).trim(),
                hasMaxSynBacklog = Utils.testFile(KernelUtils.TCP_MAX_SYN_BACKLOG),
                keepAliveTime = Utils.readFile(KernelUtils.TCP_KEEPALIVE_TIME).trim(),
                hasKeepAliveTime = Utils.testFile(KernelUtils.TCP_KEEPALIVE_TIME),
                reuse = Utils.readFile(KernelUtils.TCP_REUSE).trim(),
                hasReuse = Utils.testFile(KernelUtils.TCP_REUSE),
                ecn = Utils.readFile(KernelUtils.TCP_ECN).trim(),
                hasEcn = Utils.testFile(KernelUtils.TCP_ECN),
                fastOpen = Utils.readFile(KernelUtils.TCP_FASTOPEN).trim(),
                hasFastOpen = Utils.testFile(KernelUtils.TCP_FASTOPEN),
                sack = Utils.readFile(KernelUtils.TCP_SACK).trim(),
                hasSack = Utils.testFile(KernelUtils.TCP_SACK),
                timestamps = Utils.readFile(KernelUtils.TCP_TIMESTAMPS).trim(),
                hasTimestamps = Utils.testFile(KernelUtils.TCP_TIMESTAMPS),
                bpfJitHarden = Utils.readFile(KernelUtils.BPF_JIT_HARDEN).trim(),
                hasBpfJitHarden = Utils.testFile(KernelUtils.BPF_JIT_HARDEN)
            )
        }
    }

    private fun loadMemory() {
        viewModelScope.launch(Dispatchers.IO) {
            _mem.value = OrigamiMemory(
                zramSize = KernelUtils.getZramSize(),
                zramAlgo = KernelUtils.getZramCompAlgorithm(),
                availableZramAlgo = KernelUtils.getAvailableZramCompAlgorithms(),
                zramUsagePercent = KernelUtils.getZramUsagePercent(),
                
                swappiness = Utils.readFile(KernelUtils.SWAPPINESS).trim(),
                hasSwappiness = Utils.testFile(KernelUtils.SWAPPINESS),
                vfsCachePressure = Utils.readFile(KernelUtils.VFS_CACHE_PRESSURE).trim(),
                hasVfsCachePressure = Utils.testFile(KernelUtils.VFS_CACHE_PRESSURE),
                minFreeKbytes = Utils.readFile(KernelUtils.MIN_FREE_KBYTES).trim(),
                hasMinFreeKbytes = Utils.testFile(KernelUtils.MIN_FREE_KBYTES),
                extraFreeKbytes = Utils.readFile(KernelUtils.EXTRA_FREE_KBYTES).trim(),
                hasExtraFreeKbytes = Utils.testFile(KernelUtils.EXTRA_FREE_KBYTES),
                dirtyRatio = Utils.readFile(KernelUtils.DIRTY_RATIO).trim(),
                hasDirtyRatio = Utils.testFile(KernelUtils.DIRTY_RATIO),
                dirtyBackgroundRatio = Utils.readFile(KernelUtils.DIRTY_BACKGROUND_RATIO).trim(),
                hasDirtyBackgroundRatio = Utils.testFile(KernelUtils.DIRTY_BACKGROUND_RATIO),
                laptopMode = Utils.readFile(KernelUtils.LAPTOP_MODE).trim(),
                hasLaptopMode = Utils.testFile(KernelUtils.LAPTOP_MODE),
                oomKillAllocating = Utils.readFile(KernelUtils.OOM_KILL_ALLOCATING_TASK).trim(),
                hasOomKillAllocating = Utils.testFile(KernelUtils.OOM_KILL_ALLOCATING_TASK)
            )
        }
    }

    private fun loadUclamp() {
        viewModelScope.launch(Dispatchers.IO) {
            _uclamp.value = Uclamp(
                hasUclampMax = Utils.testFile(KernelUtils.SCHED_UTIL_CLAMP_MAX),
                uclampMax = Utils.readFile(KernelUtils.SCHED_UTIL_CLAMP_MAX),
                hasUclampMin = Utils.testFile(KernelUtils.SCHED_UTIL_CLAMP_MIN),
                uclampMin = Utils.readFile(KernelUtils.SCHED_UTIL_CLAMP_MIN),
                hasUclampMinRt = Utils.testFile(KernelUtils.SCHED_UTIL_CLAMP_MIN_RT_DEFAULT),
                uclampMinRt = Utils.readFile(KernelUtils.SCHED_UTIL_CLAMP_MIN_RT_DEFAULT),
            )
        }
    }

    private fun loadBore() {
        viewModelScope.launch(Dispatchers.IO) {
            _boreScheduler.value = BoreScheduler(
                hasBore = Utils.testFile(KernelUtils.BORE),
                bore = Utils.readFile(KernelUtils.BORE).trim().toIntOrNull() ?: 0
            )
        }
    }
    
    private fun loadMisc() {
        viewModelScope.launch(Dispatchers.IO) {
            _printk.value = Utils.readFile(KernelUtils.PRINTK)
        }
    }

    // --- UPDATERS ---

    fun updateGeneric(path: String, value: String, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Utils.writeFile(path, value)
            when (type) {
                // SCHEDULER
                "sched_autogroup" -> _sched.value = _sched.value.copy(schedAutogroup = value)
                "sched_child_runs_first" -> _sched.value = _sched.value.copy(childRunsFirst = value)
                "sched_cstate_aware" -> _sched.value = _sched.value.copy(cstateAware = value)
                "sched_isolation_hint" -> _sched.value = _sched.value.copy(isolationHint = value)
                "sched_tunable_scaling" -> _sched.value = _sched.value.copy(tunableScaling = value)
                "sched_schedstats" -> _sched.value = _sched.value.copy(schedStats = value)
                
                // MEMORY
                "swappiness" -> _mem.value = _mem.value.copy(swappiness = value)
                "vfs_cache_pressure" -> _mem.value = _mem.value.copy(vfsCachePressure = value)
                "min_free_kbytes" -> _mem.value = _mem.value.copy(minFreeKbytes = value)
                "extra_free_kbytes" -> _mem.value = _mem.value.copy(extraFreeKbytes = value)
                "dirty_ratio" -> _mem.value = _mem.value.copy(dirtyRatio = value)
                "dirty_background_ratio" -> _mem.value = _mem.value.copy(dirtyBackgroundRatio = value)
                "laptop_mode" -> _mem.value = _mem.value.copy(laptopMode = value)
                "oom_kill" -> _mem.value = _mem.value.copy(oomKillAllocating = value)

                // NETWORK
                "tcp_syncookies" -> _net.value = _net.value.copy(syncookies = value)
                "tcp_max_syn_backlog" -> _net.value = _net.value.copy(maxSynBacklog = value)
                "tcp_keepalive_time" -> _net.value = _net.value.copy(keepAliveTime = value)
                "tcp_reuse" -> _net.value = _net.value.copy(reuse = value)
                "tcp_ecn" -> _net.value = _net.value.copy(ecn = value)
                "tcp_fastopen" -> _net.value = _net.value.copy(fastOpen = value)
                "tcp_sack" -> _net.value = _net.value.copy(sack = value)
                "tcp_timestamps" -> _net.value = _net.value.copy(timestamps = value)
                "bpf_jit_harden" -> _net.value = _net.value.copy(bpfJitHarden = value)
                
                "printk" -> _printk.value = value
            }
        }
    }

    fun dropCaches(mode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Utils.writeFile(KernelUtils.DROP_CACHES, mode)
        }
    }

    fun updateZramSize(sizeInGb: Int) {
        val sizeInBytes = (sizeInGb * 1073741824L).toString()
        viewModelScope.launch(Dispatchers.IO) {
            KernelUtils.swapoffZram()
            KernelUtils.resetZram()
            Utils.writeFile(KernelUtils.ZRAM_SIZE, sizeInBytes)
            KernelUtils.mkswapZram()
            KernelUtils.swaponZram()
            _mem.value = _mem.value.copy(zramSize = KernelUtils.getZramSize())
        }
    }

    fun updateZramCompAlgorithm(algorithm: String) {
        val currentSize = Utils.readFile(KernelUtils.ZRAM_SIZE)
        viewModelScope.launch(Dispatchers.IO) {
            KernelUtils.swapoffZram()
            KernelUtils.resetZram()
            KernelUtils.setZramCompAlgorithm(algorithm)
            Utils.writeFile(KernelUtils.ZRAM_SIZE, currentSize)
            KernelUtils.mkswapZram()
            KernelUtils.swaponZram()
            _mem.value = _mem.value.copy(zramAlgo = KernelUtils.getZramCompAlgorithm())
        }
    }

    fun updateTcpCongestionAlgorithm(algorithm: String) {
        viewModelScope.launch(Dispatchers.IO) {
            KernelUtils.setTcpCongestionAlgorithm(algorithm)
            _net.value = _net.value.copy(tcpCongestion = KernelUtils.getTcpCongestionAlgorithm())
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}
