/*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.ui.soc

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.Shell
import com.zuan.kernelmanager.ui.settings.SettingsPreference
import com.zuan.kernelmanager.utils.* // Import semua Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File

class SoCViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsPreference = SettingsPreference.getInstance(application)

    // === UI EVENTS (Toast) ===
    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
    }
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // === STATES ===
    data class CPUState(
        val name: String, val policyPath: String, val minFreq: String, val maxFreq: String,
        val currentFreq: String, val gov: String, val availableFreq: List<String>, 
        val availableGov: List<String>, val isPrime: Boolean = false
    ) 

    data class GPUState(
        val type: SoCUtils.GpuType, val currentFreq: String, val mtkFixedIndex: String = "-1",
    ) { companion object { val EMPTY = GPUState(SoCUtils.GpuType.UNKNOWN, "N/A") } }

    data class MiscState(
        val selinuxMode: String = "Unknown", val dt2w: Boolean = false,
        val thermalGov: String = "N/A", val availThermalGovs: List<String> = emptyList(),
        val ioSched: String = "N/A", val availIoScheds: List<String> = emptyList(),
        val tpGameMode: Boolean = false, val tpLimit: Boolean = false, val tpDirection: Boolean = false,
        val mtkVibratorLevel: Int = 0, val mtkPbm: Boolean = false, val mtkBatoc: Boolean = false,
        val mtkEara: Boolean = false, val mtkEaraFake: Boolean = false
    )

    data class DevfreqItem(
        val name: String, val path: String, val curFreq: String, val minFreq: String, 
        val maxFreq: String, val gov: String
    )

    // NEW: Real-time Core Monitor Data
    data class CoreMonitorData(
        val index: Int,
        val freq: String,
        val gov: String,
        val usage: Float
    )

    // === FLOWS ===
    private val _clusterStates = MutableStateFlow<List<CPUState>>(emptyList())
    val clusterStates: StateFlow<List<CPUState>> = _clusterStates
    
    // Usage & Temp
    private val _cpuUsage = MutableStateFlow("N/A"); val cpuUsage: StateFlow<String> = _cpuUsage
    private val _cpuTemp = MutableStateFlow("N/A"); val cpuTemp: StateFlow<String> = _cpuTemp
    private val _gpuTemp = MutableStateFlow("N/A"); val gpuTemp: StateFlow<String> = _gpuTemp
    private val _gpuUsage = MutableStateFlow("N/A"); val gpuUsage: StateFlow<String> = _gpuUsage

    private val _gpuState = MutableStateFlow(GPUState.EMPTY); val gpuState: StateFlow<GPUState> = _gpuState
    private val _miscState = MutableStateFlow(MiscState()); val miscState: StateFlow<MiscState> = _miscState
    private val _devfreqList = MutableStateFlow<List<DevfreqItem>>(emptyList()); val devfreqList: StateFlow<List<DevfreqItem>> = _devfreqList
    
    // NEW: Core Monitor Flow
    private val _coreMonitorList = MutableStateFlow<List<CoreMonitorData>>(emptyList())
    val coreMonitorList: StateFlow<List<CoreMonitorData>> = _coreMonitorList

    // New: Governor Tunables List
    private val _govTunables = MutableStateFlow<List<GovTunable>>(emptyList())
    val govTunables: StateFlow<List<GovTunable>> = _govTunables.asStateFlow()

    private var job: Job? = null

    init { loadSoCData() }

    fun startJob() {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            settingsPreference.pollingInterval.collect { interval ->
                while (true) {
                    loadDynamicCPUData()
                    loadGPUData()
                    loadTemperatureAndUsageData()
                    loadCoreMonitorData() // Update real-time cores
                    delay(interval)
                }
            }
        }
    }

    fun stopJob() { job?.cancel(); job = null }

    private fun loadSoCData() {
        viewModelScope.launch(Dispatchers.IO) {
            loadDynamicCPUData()
            loadGPUData()
            loadTemperatureAndUsageData()
            loadCoreMonitorData()
            loadMiscData()
            loadDevfreqData()
        }
    }

    // === REFACTORED LOADERS ===

    private suspend fun loadCoreMonitorData() {
        val numCores = SoCUtils.getCoreCount()
        val newList = ArrayList<CoreMonitorData>()
        
        for (i in 0 until numCores) {
            val currentFreq = SoCUtils.getCoreFreq(i)
            val load = SoCUtils.getCoreLoadBasedOnFreq(i, currentFreq) // Pakai kalkulasi baru
            val gov = SoCUtils.getCoreGov(i)
            
            newList.add(
                CoreMonitorData(
                    index = i,
                    freq = if (currentFreq > 0) "$currentFreq" else "Sleep",
                    gov = gov,
                    usage = load
                )
            )
        }
        _coreMonitorList.value = newList
    }

    private suspend fun loadMiscData() {
        val selinuxRes = Shell.cmd("getenforce").exec()
        val selinux = if (selinuxRes.isSuccess) selinuxRes.out.firstOrNull() ?: "Unknown" else "Unknown"

        val dt2wPath = MiscUtils.getDt2wPath()
        val dt2w = if (dt2wPath != null) Utils.readFile(dt2wPath) == "1" else false

        val tGov = Utils.readFile(MiscUtils.THERMAL_ZONE0_POLICY)
        var tAvailRaw = Utils.readFile(MiscUtils.THERMAL_ZONE0_AVAIL)
        if (tAvailRaw.isBlank()) tAvailRaw = Utils.readFile(MiscUtils.THERMAL_ZONE0_AVAIL_ALT)
        val tAvail = tAvailRaw.split(" ").filter { it.isNotBlank() }
        
        val ioPath = MiscUtils.getIoSchedPath()
        var ioSched = "N/A"
        var ioAvail = emptyList<String>()
        if (ioPath != null) {
            val rawIo = Utils.readFile(ioPath).trim()
            if (rawIo.isNotEmpty()) {
                ioSched = rawIo.substringAfter("[").substringBefore("]")
                ioAvail = rawIo.replace("[", "").replace("]", "").split("\\s+".toRegex()).filter { it.isNotBlank() }
            }
        }

        val tpGame = Utils.readFile(MiscUtils.TP_GAME_MODE) == "1"
        val tpLimit = Utils.readFile(MiscUtils.TP_LIMIT_ENABLE) == "1"
        val tpDir = Utils.readFile(MiscUtils.TP_DIRECTION) == "1"

        val vibLevel = Utils.readFile(MiscUtils.MTK_VIBRATOR_LEVEL).toIntOrNull() ?: 0
        val pbm = !Utils.readFile(MiscUtils.MTK_PBM_STOP).contains("1")
        val batoc = !Utils.readFile(MiscUtils.MTK_BATOC_STOP).contains("1")
        val eara = Utils.readFile(MiscUtils.MTK_EARA_ENABLE) == "1"
        val earaFake = Utils.readFile(MiscUtils.MTK_EARA_FAKE) == "1"

        _miscState.value = MiscState(
            selinuxMode = selinux, dt2w = dt2w, thermalGov = tGov, availThermalGovs = tAvail,
            ioSched = ioSched, availIoScheds = ioAvail, tpGameMode = tpGame, tpLimit = tpLimit, tpDirection = tpDir,
            mtkVibratorLevel = vibLevel, mtkPbm = pbm, mtkBatoc = batoc, mtkEara = eara, mtkEaraFake = earaFake
        )
    }

    private suspend fun loadDevfreqData() {
        val dir = File(MiscUtils.DEVFREQ_BASE)
        if (!dir.exists() || !dir.isDirectory) return

        val list = dir.listFiles()?.map { file ->
            val path = file.absolutePath
            DevfreqItem(
                name = file.name, path = path,
                curFreq = Utils.readFile("$path/cur_freq"),
                minFreq = Utils.readFile("$path/min_freq"),
                maxFreq = Utils.readFile("$path/max_freq"),
                gov = Utils.readFile("$path/governor")
            )
        } ?: emptyList()
        _devfreqList.value = list
    }

    private suspend fun loadDynamicCPUData() {
        val policies = CpuUtils.getCpuPolicies() 
        if (policies.isEmpty()) return

        val rawData = policies.map { path ->
            val maxFreq = CpuUtils.readFreq(path, "scaling_max_freq").toIntOrNull() ?: 0
            Triple(path, maxFreq, CpuUtils.readAvailableFreq(path))
        }

        val sortedByFreq = rawData.sortedBy { it.second } 
        val minMaxFreq = sortedByFreq.first().second
        val maxMaxFreq = sortedByFreq.last().second

        val newStates = rawData.mapIndexed { index, (path, maxFreq, availFreq) ->
            val name = when {
                rawData.size == 1 -> "CPU Cluster" 
                maxFreq == minMaxFreq -> "Little Cluster"
                maxFreq == maxMaxFreq -> if (rawData.size > 2) "Prime Cluster" else "Big Cluster"
                else -> "Big Cluster" 
            }
            val isPrime = name.contains("Prime") || (name.contains("Big") && rawData.size == 2 && index == 1)
            val boostFreqs = CpuUtils.readAvailableBoostFreq(path)
            val finalAvailFreqs = (availFreq + boostFreqs).distinct().sortedBy { it.toIntOrNull() ?: 0 }

            CPUState(
                name = name, policyPath = path,
                minFreq = CpuUtils.readFreq(path, "scaling_min_freq"),
                maxFreq = CpuUtils.readFreq(path, "scaling_max_freq"),
                currentFreq = CpuUtils.readFreq(path, "scaling_cur_freq"),
                gov = CpuUtils.readGovernor(path),
                availableFreq = finalAvailFreqs,
                availableGov = CpuUtils.readAvailableGov(path),
                isPrime = isPrime
            )
        }.sortedBy { it.policyPath }
        _clusterStates.value = newStates
    }

    private suspend fun loadGPUData() {
        val type = SoCUtils.getGpuType()
        when (type) {
            SoCUtils.GpuType.ADRENO -> {
                val freq = SoCUtils.readFreqGPU(SoCUtils.CURRENT_FREQ_GPU)
                _gpuState.value = GPUState(type, freq)
            }
            SoCUtils.GpuType.MEDIATEK_V2, SoCUtils.GpuType.MEDIATEK_LEGACY -> {
                val freqMap = MtkUtils.getMtkFreqMap()
                val path = MtkUtils.getFixedIndexPath()
                val currentFixedIdx = Utils.readFile(path).trim()
                val displayFreq = if (currentFixedIdx == "-1" || currentFixedIdx.isEmpty()) "Dynamic" else {
                    freqMap.entries.find { it.value == currentFixedIdx }?.key ?: "Unknown"
                }
                _gpuState.value = GPUState(type, displayFreq, mtkFixedIndex = currentFixedIdx)
            }
            SoCUtils.GpuType.GENERIC_DEVFREQ -> {
                val path = GenericGpuUtils.getGpuPath()
                val curFreq = if (path != null) GenericGpuUtils.getCurFreq(path) else "N/A"
                _gpuState.value = GPUState(type, curFreq)
            }
            else -> _gpuState.value = GPUState.EMPTY
        }
    }

    private suspend fun loadTemperatureAndUsageData() {
        _cpuUsage.value = CpuUtils.getCpuUsage() // Use CpuUtils
        _cpuTemp.value = Utils.getTemp(CpuUtils.CPU_TEMP) // Use CpuUtils
        _gpuTemp.value = Utils.getTemp(SoCUtils.GPU_TEMP)
        _gpuUsage.value = SoCUtils.getGpuUsage()
    }

    // === ACTION FUNCTIONS ===

    fun toggleSelinux(isEnforcing: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!Shell.cmd("which getenforce").exec().isSuccess) {
                _uiEvent.send(UiEvent.ShowToast("SELinux control not supported"))
                return@launch
            }
            Shell.cmd("setenforce ${if (isEnforcing) "1" else "0"}").exec()
            loadMiscData()
        }
    }
    fun toggleDt2w(enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val path = MiscUtils.getDt2wPath()
            if (path == null) {
                _uiEvent.send(UiEvent.ShowToast("DT2W not supported"))
                return@launch
            }
            Utils.writeFile(path, if (enable) "1" else "0")
            loadMiscData()
        }
    }
    fun setThermalGov(gov: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val zones = File("/sys/class/thermal").listFiles()?.filter { it.name.startsWith("thermal_zone") }
            if (zones.isNullOrEmpty()) {
                _uiEvent.send(UiEvent.ShowToast("Thermal zones not found"))
                return@launch
            }
            zones.forEach { zone ->
                val policyFile = File(zone, "policy")
                if (policyFile.exists()) Utils.writeFile(policyFile.absolutePath, gov)
            }
            loadMiscData()
        }
    }
    fun setIoSched(sched: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val path = MiscUtils.getIoSchedPath()
            if (path == null) {
                 _uiEvent.send(UiEvent.ShowToast("I/O Scheduler not found"))
                 return@launch
            }
            Utils.writeFile(path, sched)
            loadMiscData()
        }
    }
    fun toggleGeneric(path: String, enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!File(path).exists()) {
                _uiEvent.send(UiEvent.ShowToast("Feature not supported"))
                return@launch
            }
            Utils.writeFile(path, if (enable) "1" else "0")
            loadMiscData()
        }
    }
    fun toggleMtkStop(path: String, enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!File(path).exists()) {
                _uiEvent.send(UiEvent.ShowToast("Feature not supported"))
                return@launch
            }
            Utils.writeFile(path, if (enable) "stop 0" else "stop 1")
            loadMiscData()
        }
    }
    fun setVibrator(level: Float) {
        viewModelScope.launch(Dispatchers.IO) {
             if (!File(MiscUtils.MTK_VIBRATOR_LEVEL).exists()) {
                 _uiEvent.send(UiEvent.ShowToast("Vibrator control not supported"))
                 return@launch
             }
             Utils.writeFile(MiscUtils.MTK_VIBRATOR_LEVEL, level.toInt().toString())
        }
    }

    fun updateFreq(target: String, selectedFreq: String, policyPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = if (target == "min") "scaling_min_freq" else "scaling_max_freq"
            CpuUtils.writeFreq(policyPath, file, selectedFreq)
            loadDynamicCPUData()
        }
    }

    fun updateGov(selectedGov: String, policyPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            CpuUtils.writeGov(policyPath, selectedGov)
            loadDynamicCPUData()
        }
    }

    // New: Governor Tuning
    fun loadGovTunables(policyPath: String, governor: String) {
        viewModelScope.launch {
            _govTunables.value = emptyList() // Reset loading
            val tunables = CpuUtils.getGovernorTunables(policyPath, governor)
            _govTunables.value = tunables
        }
    }

    fun applyTunable(tunable: GovTunable, newValue: String) {
        viewModelScope.launch {
            CpuUtils.writeTunable(tunable.path, newValue)
            // Update UI list langsung biar kerasa responsif
             _govTunables.value = _govTunables.value.map {
                if (it.path == tunable.path) it.copy(value = newValue) else it
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
        stopJob()
    }
}
