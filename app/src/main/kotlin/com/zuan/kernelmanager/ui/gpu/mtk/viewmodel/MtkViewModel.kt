/*
 * Original code from: Rem01Gaming (origami_kernel_manager) and helloklf (vtools)
 * Modified and integrated by: Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.gpu.mtk.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zuan.kernelmanager.utils.MtkUtils
import com.zuan.kernelmanager.utils.RootIpcManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MtkViewModel(application: Application) : AndroidViewModel(application) {

    // === HELPER IPC SUPER CEPAT UNTUK VIEWMODEL ===
    private fun fastRead(path: String): String {
        return try { RootIpcManager.ipc?.readNode(path)?.trim() ?: "" } catch (e: Exception) { "" }
    }
    private fun fastWrite(path: String, value: String) {
        try { RootIpcManager.ipc?.writeNode(path, value) } catch (e: Exception) { }
    }

    data class FreqState(
        val availableFreqs: List<String> = emptyList(), 
        val freqMap: Map<String, String> = emptyMap(), 
        val currentMinIndex: String = "-1", 
        val currentMaxIndex: String = "-1", 
        val currentFreq: String = "N/A", 
        val isDvfsEnabled: Boolean = true, 
        val isLocked: Boolean = false, 
        val lockedIndex: String = "-1",
        // TAMBAHAN DEVFREQ (Fallback GKI)
        val isDevfreq: Boolean = false,
        val minFreq: String = "",
        val maxFreq: String = "",
        val governor: String = "",
        val availableGovernors: List<String> = emptyList()
    )
    
    data class DramState(val type: MtkUtils.DramType = MtkUtils.DramType.NONE, val availableFreqs: List<String> = emptyList(), val freqMap: Map<String, String> = emptyMap(), val currentStep: String = "-1", val isAvailable: Boolean = false)
    data class PpmPolicyUi(val idx: Int, val name: String, val enabled: Boolean)
    data class PpmState(val isAvailable: Boolean = false, val isEnabled: Boolean = false, val policies: List<PpmPolicyUi> = emptyList())
    data class BoostState(val isDvfsEnabled: Boolean = true, val isFpsGoEnabled: Boolean = false, val isGedGameMode: Boolean = false, val isGedBoostEnabled: Boolean = false, val isExtraBoostEnabled: Boolean = false, val isGpuBoostEnabled: Boolean = false, val isTouchBoostEnabled: Boolean = false, val isSchedBoostEnabled: Boolean = false)
    data class CpuMiscState(val isAvailable: Boolean = false, val cciMode: String = "0", val powerMode: String = "0", val schedBoost: String = "0", val eemOffsets: List<Pair<String, String>> = emptyList())
    data class ThermalState(val gpuTemp: Int = 0, val hasEara: Boolean = false, val earaEnabled: Boolean = false, val earaFakeThrottle: Boolean = false, val isThrottling: Boolean = false)

    data class MtkUiState(
        val freqState: FreqState = FreqState(), val dramState: DramState = DramState(),
        val ppmState: PpmState = PpmState(), val boostState: BoostState = BoostState(),
        val cpuMiscState: CpuMiscState = CpuMiscState(), val thermalState: ThermalState = ThermalState(),
        val isLoading: Boolean = false, val errorMessage: String? = null
    )

    private val _state = MutableStateFlow(MtkUiState())
    val state: StateFlow<MtkUiState> = _state

    init {
        loadAllData()
        startRealtimeUpdates()
    }

    fun loadAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val freqDef = async { loadFreqData() }
                val dramDef = async { loadDramData() }
                val ppmDef = async { loadPpmData() }
                val boostDef = async { loadBoostData() }
                val cpuDef = async { loadCpuMiscData() }
                val thermalDef = async { loadThermalData() }

                _state.value = MtkUiState(
                    freqState = freqDef.await(), dramState = dramDef.await(),
                    ppmState = ppmDef.await(), boostState = boostDef.await(),
                    cpuMiscState = cpuDef.await(), thermalState = thermalDef.await(),
                    isLoading = false
                )
            } catch (e: Exception) { _state.value = _state.value.copy(isLoading = false) }
        }
    }

    private fun startRealtimeUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            delay(1000)
            while (isActive) {
                try {
                    val currentFreq = MtkUtils.getCurrentGpuFreq()
                    val newThermal = loadThermalData()
                    
                    var newDram = _state.value.dramState
                    if (newDram.isAvailable && newDram.type != MtkUtils.DramType.NONE) {
                        val info = MtkUtils.getDramCurrentInfo(newDram.type)
                        newDram = newDram.copy(currentStep = info["force_step"] ?: info["current"] ?: "-1")
                    }

                    _state.value = _state.value.copy(
                        freqState = _state.value.freqState.copy(currentFreq = currentFreq),
                        thermalState = newThermal,
                        dramState = newDram
                    )
                } catch (e: Exception) { }
                delay(1500)
            }
        }
    }

    private suspend fun loadFreqData(): FreqState {
        val gpuArch = MtkUtils.getGpuArch()
        
        // CABANG 1: DEVFREQ (GKI / MODERN MTK)
        if (gpuArch == MtkUtils.GpuArch.DEVFREQ) {
            val devfreqNode = MtkUtils.getGpuDevfreqNode()
            if (devfreqNode != null) {
                val availFreqsRaw = fastRead("$devfreqNode/available_frequencies").split(" ").filter { it.isNotBlank() }
                val availGovs = fastRead("$devfreqNode/available_governors").split(" ").filter { it.isNotBlank() }
                
                // Format ke format MHz untuk UI
                val formattedFreqs = availFreqsRaw.map { 
                    val mhz = it.toLongOrNull() ?: 0L
                    if (mhz > 1000000) "${mhz / 1000000}" else "${mhz / 1000}"
                }
                
                // Map Label UI ke Value Raw
                val freqMap = formattedFreqs.zip(availFreqsRaw).toMap()

                val curMinRaw = fastRead("$devfreqNode/min_freq")
                val curMaxRaw = fastRead("$devfreqNode/max_freq")
                
                val minFormatted = (curMinRaw.toLongOrNull() ?: 0L).let { if (it > 1000000) "${it / 1000000}" else "${it / 1000}" }
                val maxFormatted = (curMaxRaw.toLongOrNull() ?: 0L).let { if (it > 1000000) "${it / 1000000}" else "${it / 1000}" }

                return FreqState(
                    isDevfreq = true,
                    availableFreqs = formattedFreqs.sortedByDescending { it.toLongOrNull() ?: 0L },
                    freqMap = freqMap,
                    minFreq = minFormatted,
                    maxFreq = maxFormatted,
                    governor = fastRead("$devfreqNode/governor"),
                    availableGovernors = availGovs,
                    currentFreq = MtkUtils.getCurrentGpuFreq(),
                    isDvfsEnabled = true, // Bypass DVFS flag
                    isLocked = false
                )
            }
        }

        // CABANG 2: LEGACY & V2 GED (MTK LAMA)
        val freqMap = MtkUtils.getMtkFreqMap()
        val lockIdx = when (gpuArch) {
            MtkUtils.GpuArch.V2_GED -> fastRead("/proc/gpufreqv2/fix_target_opp_index")
            MtkUtils.GpuArch.LEGACY_GED -> fastRead("/proc/gpufreq/gpufreq_opp_freq")
            else -> "-1"
        }
        return FreqState(
            isDevfreq = false,
            availableFreqs = freqMap.keys.toList().sortedByDescending { it.toIntOrNull() ?: 0 },
            freqMap = freqMap, 
            currentMinIndex = MtkUtils.getCurrentMinIndex(),
            currentMaxIndex = MtkUtils.getCurrentMaxIndex(), 
            currentFreq = MtkUtils.getCurrentGpuFreq(),
            isDvfsEnabled = MtkUtils.isGedDvfsEnabled(), 
            isLocked = lockIdx != "-1" && lockIdx.isNotBlank(), 
            lockedIndex = lockIdx
        )
    }

    private suspend fun loadDramData(): DramState {
        val dramType = MtkUtils.getDramType()
        if (dramType == MtkUtils.DramType.NONE) return DramState(type = dramType, isAvailable = false)
        val (freqs, map) = MtkUtils.getDramFreqs(dramType)
        val info = MtkUtils.getDramCurrentInfo(dramType)
        return DramState(type = dramType, availableFreqs = freqs, freqMap = map, currentStep = info["force_step"] ?: info["current"] ?: "-1", isAvailable = freqs.isNotEmpty())
    }

    private suspend fun loadPpmData(): PpmState {
        if (!MtkUtils.isPpmAvailable()) return PpmState(isAvailable = false)
        val policies = MtkUtils.getPpmPolicies().map { PpmPolicyUi(it.idx, it.name, it.enabled) }
        return PpmState(isAvailable = policies.isNotEmpty(), isEnabled = MtkUtils.isPpmEnabled(), policies = policies)
    }

    private suspend fun loadBoostData(): BoostState {
        return BoostState(
            isDvfsEnabled = MtkUtils.isGedDvfsEnabled(), isFpsGoEnabled = fastRead(MtkUtils.MTK_FPSGO) == "1",
            isGedGameMode = fastRead(MtkUtils.GED_GAME_MODE) == "1", isGedBoostEnabled = fastRead(MtkUtils.GED_BOOST_ENABLE) == "1",
            isExtraBoostEnabled = fastRead(MtkUtils.GED_EXTRA_BOOST) == "1", isGpuBoostEnabled = fastRead(MtkUtils.GED_GPU_BOOST) == "1",
            isTouchBoostEnabled = fastRead("/sys/module/ged/parameters/gx_force_cpu_boost") == "1", isSchedBoostEnabled = fastRead("/sys/module/ged/parameters/sched_boost") == "1"
        )
    }

    private suspend fun loadCpuMiscData(): CpuMiscState {
        val hasCci = RootIpcManager.ipc?.nodeExists(MtkUtils.CPU_CCI_MODE) == true
        return CpuMiscState(
            isAvailable = hasCci || RootIpcManager.ipc?.nodeExists(MtkUtils.EEM_DIR) == true, cciMode = MtkUtils.getCpuCciMode(), powerMode = MtkUtils.getCpuPowerMode(),
            schedBoost = fastRead(MtkUtils.SCHED_BOOST).ifEmpty { "0" }, eemOffsets = MtkUtils.getEemOffsets()
        )
    }

    private suspend fun loadThermalData(): ThermalState {
        var gpuTemp = 0
        try {
            val dirs = RootIpcManager.ipc?.listDirectories("/sys/class/thermal") ?: emptyList()
            dirs.filter { it.startsWith("thermal_zone") }.forEach { dirName ->
                val type = fastRead("/sys/class/thermal/$dirName/type")
                if (type.contains("mtktsbtsys", true) || type.contains("gpu", true) || type.contains("mt6785", true)) {
                    val temp = fastRead("/sys/class/thermal/$dirName/temp").toIntOrNull() ?: 0
                    if (temp > gpuTemp) gpuTemp = temp
                }
            }
        } catch (e: Exception) { }
        return ThermalState(
            gpuTemp = gpuTemp, hasEara = MtkUtils.hasEaraThermal(),
            earaEnabled = if(MtkUtils.hasEaraThermal()) MtkUtils.isEaraEnabled() else false, earaFakeThrottle = if(MtkUtils.hasEaraThermal()) MtkUtils.isEaraFakeThrottle() else false,
            isThrottling = gpuTemp > 75000
        )
    }

    // === GED MTK SPECIFIC ACTIONS ===
    fun lockGpuFreq(freqDisplay: String) = viewModelScope.launch(Dispatchers.IO) { _state.value.freqState.freqMap[freqDisplay]?.let { index -> MtkUtils.setGedDvfsEnabled(false); MtkUtils.lockGpuFreq(index); loadAllData() } }
    fun unlockGpuFreq() = viewModelScope.launch(Dispatchers.IO) { MtkUtils.resetGpuLock(); MtkUtils.setGedDvfsEnabled(true); loadAllData() }
    fun setMinFreq(freqDisplay: String) = viewModelScope.launch(Dispatchers.IO) { _state.value.freqState.freqMap[freqDisplay]?.let { index -> MtkUtils.setMtkMinFreq(index); loadAllData() } }
    fun setMaxFreq(freqDisplay: String) = viewModelScope.launch(Dispatchers.IO) { _state.value.freqState.freqMap[freqDisplay]?.let { index -> MtkUtils.setMtkMaxFreq(index); loadAllData() } }
    fun setDvfsEnabled(enable: Boolean) = viewModelScope.launch(Dispatchers.IO) { MtkUtils.setGedDvfsEnabled(enable); if (enable) MtkUtils.resetGpuLock(); loadAllData() }
    
    // === DEVFREQ GKI ACTIONS ===
    fun setGpuDevfreqMin(freqDisplay: String) = viewModelScope.launch(Dispatchers.IO) {
        val rawValue = _state.value.freqState.freqMap[freqDisplay] ?: return@launch
        val devfreqNode = MtkUtils.getGpuDevfreqNode()
        if (devfreqNode != null) { fastWrite("$devfreqNode/min_freq", rawValue); loadAllData() }
    }
    fun setGpuDevfreqMax(freqDisplay: String) = viewModelScope.launch(Dispatchers.IO) {
        val rawValue = _state.value.freqState.freqMap[freqDisplay] ?: return@launch
        val devfreqNode = MtkUtils.getGpuDevfreqNode()
        if (devfreqNode != null) { fastWrite("$devfreqNode/max_freq", rawValue); loadAllData() }
    }
    fun setGpuGovernor(gov: String) = viewModelScope.launch(Dispatchers.IO) {
        val devfreqNode = MtkUtils.getGpuDevfreqNode()
        if (devfreqNode != null) { fastWrite("$devfreqNode/governor", gov); loadAllData() }
    }

    // === OTHER ACTIONS ===
    fun setDramFreq(displayFreq: String) = viewModelScope.launch(Dispatchers.IO) { _state.value.dramState.freqMap[displayFreq]?.let { rawValue -> MtkUtils.setDramFreq(_state.value.dramState.type, displayFreq, rawValue); loadAllData() } }
    fun togglePpmPolicy(idx: Int, enable: Boolean) = viewModelScope.launch(Dispatchers.IO) { MtkUtils.togglePpmPolicy(idx, enable); loadAllData() }
    fun setPpmEnabled(enable: Boolean) = viewModelScope.launch(Dispatchers.IO) { MtkUtils.setPpmEnabled(enable); loadAllData() }
    fun toggleFeature(feature: String, enable: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        when(feature) {
            "dvfs" -> MtkUtils.setGedDvfsEnabled(enable)
            "fpsgo" -> MtkUtils.setMtkFeature(MtkUtils.MTK_FPSGO, enable)
            "ged_game" -> MtkUtils.setMtkFeature(MtkUtils.GED_GAME_MODE, enable)
            "ged_boost" -> MtkUtils.setMtkFeature(MtkUtils.GED_BOOST_ENABLE, enable)
            "extra_boost" -> MtkUtils.setMtkFeature(MtkUtils.GED_EXTRA_BOOST, enable)
            "gpu_boost" -> MtkUtils.setMtkFeature(MtkUtils.GED_GPU_BOOST, enable)
            "eara" -> MtkUtils.setEaraEnabled(enable)
            "eara_fake" -> MtkUtils.setEaraFakeThrottle(enable)
            "touch_boost" -> fastWrite("/sys/module/ged/parameters/gx_force_cpu_boost", if(enable) "1" else "0")
            "sched_boost" -> fastWrite("/sys/module/ged/parameters/sched_boost", if(enable) "1" else "0")
        }
        loadAllData()
    }
    fun setCciMode(mode: String) = viewModelScope.launch(Dispatchers.IO) { MtkUtils.setCpuCciMode(mode); loadAllData() }
    fun setPowerMode(mode: String) = viewModelScope.launch(Dispatchers.IO) { MtkUtils.setCpuPowerMode(mode); loadAllData() }
    fun setEemOffset(detName: String, offset: String) = viewModelScope.launch(Dispatchers.IO) { MtkUtils.setEemOffset(detName, offset); loadAllData() }
}