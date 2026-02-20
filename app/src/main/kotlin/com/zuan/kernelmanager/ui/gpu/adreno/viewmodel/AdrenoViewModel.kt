/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.gpu.adreno.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zuan.kernelmanager.utils.AdrenoUtils
import com.zuan.kernelmanager.utils.RootIpcManager // FIX: Import ditambahkan kembali!
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AdrenoViewModel(application: Application) : AndroidViewModel(application) {

    private fun fastRead(path: String): String = AdrenoUtils.readData(path)
    private fun fastWrite(path: String, value: String) { AdrenoUtils.writeData(path, value) }

    data class DevfreqState(val isAvailable: Boolean = false, val path: String = "", val name: String = "", val minFreq: String = "N/A", val maxFreq: String = "N/A", val governor: String = "N/A", val availableFreqs: List<String> = emptyList(), val availableGovs: List<String> = emptyList())
    data class BusState(val name: String, val minFreq: String, val maxFreq: String, val availableFreqs: List<String>)
    data class ThermalZone(val name: String, val temp: Int, val type: String)
    data class CoolingDevice(val name: String, val curState: Int, val maxState: Int)
    
    data class FreqState(val minFreq: String = "N/A", val maxFreq: String = "N/A", val currentFreq: String = "N/A", val governor: String = "N/A", val availableFreqs: List<String> = emptyList(), val availableGovs: List<String> = emptyList())
    data class PowerState(val adrenoBoost: String = "0", val hasGpuThrottling: Boolean = false, val gpuThrottling: String = "0")
    
    // Tab Bus (Gabungan DCVS, Busmon, UFSHC)
    data class BusDcvsState(val hasBusDcvs: Boolean = false, val busComponents: List<BusState> = emptyList(), val busmon: DevfreqState = DevfreqState(), val ufshc: DevfreqState = DevfreqState())
    data class ThermalState(val gpuTemp: Int = 0, val thermalZones: List<ThermalZone> = emptyList(), val coolingDevices: List<CoolingDevice> = emptyList(), val isThrottling: Boolean = false)
    
    // Tab Advanced (Sesuai request)
    data class AdvancedState(
        val hasAdrenoIdler: Boolean = false, val idlerActive: Boolean = false, val idlerIdleWait: String = "0", val idlerDownDiff: String = "0", val idlerWorkload: String = "0",
        val hasSimpleGpu: Boolean = false, val simpleGpuActive: Boolean = false, val simpleLaziness: String = "0", val simpleRampThreshold: String = "0",
        val hasKgsl3d0: Boolean = false, val forceNoNap: Boolean = false, val forceClkOn: Boolean = false, val forceBusOn: Boolean = false, val busSplit: Boolean = false, val throttling: Boolean = false,
        val defaultPwrlevel: String = "N/A", val maxPwrlevel: String = "N/A", val thermalPwrlevel: String = "N/A"
    )

    data class AdrenoUiState(
        val freqState: FreqState = FreqState(), val powerState: PowerState = PowerState(),
        val busState: BusDcvsState = BusDcvsState(), val thermalState: ThermalState = ThermalState(),
        val advancedState: AdvancedState = AdvancedState(), val isLoading: Boolean = false, val errorMessage: String? = null
    )

    private val _state = MutableStateFlow(AdrenoUiState())
    val state: StateFlow<AdrenoUiState> = _state

    init {
        loadAllData()
        startRealtimeUpdates()
    }

    fun loadAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val freqDef = async { loadFreqData() }
                val powerDef = async { loadPowerData() }
                val busDef = async { loadBusData() }
                val thermalDef = async { loadThermalData() }
                val advDef = async { loadAdvancedData() }

                _state.value = AdrenoUiState(
                    freqState = freqDef.await(), powerState = powerDef.await(),
                    busState = busDef.await(), thermalState = thermalDef.await(),
                    advancedState = advDef.await(), isLoading = false
                )
            } catch (e: Exception) { _state.value = _state.value.copy(isLoading = false) }
        }
    }

    private fun startRealtimeUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            delay(1000)
            while (isActive) {
                try {
                    val currentFreq = AdrenoUtils.readFreqGPU(AdrenoUtils.CURRENT_FREQ_GPU)
                    val newThermal = loadThermalData()
                    _state.value = _state.value.copy(
                        freqState = _state.value.freqState.copy(currentFreq = currentFreq),
                        thermalState = newThermal
                    )
                } catch (e: Exception) { }
                delay(1500)
            }
        }
    }

    private suspend fun loadFreqData(): FreqState = FreqState(
        minFreq = fastRead(AdrenoUtils.MIN_FREQ_GPU), maxFreq = fastRead(AdrenoUtils.MAX_FREQ_GPU),
        currentFreq = AdrenoUtils.readFreqGPU(AdrenoUtils.CURRENT_FREQ_GPU), governor = fastRead(AdrenoUtils.GOV_GPU),
        availableFreqs = AdrenoUtils.readAvailableFreqGPU(), availableGovs = fastRead(AdrenoUtils.AVAILABLE_GOV_GPU).split(" ")
    )

    private suspend fun loadPowerData(): PowerState {
        val hasThrottling = AdrenoUtils.hasGpuThrottling()
        return PowerState(
            adrenoBoost = fastRead(AdrenoUtils.ADRENO_BOOST),
            hasGpuThrottling = hasThrottling, gpuThrottling = if (hasThrottling) fastRead(AdrenoUtils.GPU_THROTTLING) else "0"
        )
    }

    private suspend fun fetchDevfreq(path: String, name: String): DevfreqState {
        if (!AdrenoUtils.checkExists(path)) return DevfreqState(isAvailable = false)
        return DevfreqState(
            isAvailable = true, path = path, name = name,
            minFreq = fastRead("$path/min_freq"), maxFreq = fastRead("$path/max_freq"), governor = fastRead("$path/governor"),
            availableFreqs = fastRead("$path/available_frequencies").split(" ").filter { it.isNotBlank() },
            availableGovs = fastRead("$path/available_governors").split(" ").filter { it.isNotBlank() }
        )
    }

    private suspend fun loadBusData(): BusDcvsState {
        val hasBus = AdrenoUtils.hasBusDcvs()
        val busList = if (hasBus) {
            AdrenoUtils.getBusComponents().mapNotNull { busName ->
                val freqs = AdrenoUtils.getBusAvailableFreqs(busName)
                if (freqs.isNotEmpty()) BusState(busName, AdrenoUtils.getBusMinFreq(busName), AdrenoUtils.getBusMaxFreq(busName), freqs) else null
            }
        } else emptyList()
        
        val busmon = fetchDevfreq(AdrenoUtils.BUSMON_DIR, "KGSL Busmon")
        val ufshcPath = AdrenoUtils.getUfshcPath()
        val ufshc = if (ufshcPath != null) fetchDevfreq(ufshcPath, "UFSHC (Storage Bus)") else DevfreqState()
        
        return BusDcvsState(hasBusDcvs = hasBus, busComponents = busList, busmon = busmon, ufshc = ufshc)
    }

    private suspend fun loadThermalData(): ThermalState {
        val zones = mutableListOf<ThermalZone>()
        val coolingDevs = mutableListOf<CoolingDevice>()
        try {
            // FIX: Tambahkan type inference <String> agar Kotlin tidak kebingungan
            val dirs = RootIpcManager.ipc?.listDirectories("/sys/class/thermal") ?: emptyList<String>()
            dirs.forEach { dirName ->
                if (dirName.startsWith("thermal_zone")) {
                    val type = fastRead("/sys/class/thermal/$dirName/type")
                    val temp = fastRead("/sys/class/thermal/$dirName/temp").toIntOrNull() ?: 0
                    if (type.contains("gpu", true) || type.contains("tsens", true)) zones.add(ThermalZone(dirName, temp, type))
                } else if (dirName.startsWith("cooling_device")) {
                    val type = fastRead("/sys/class/thermal/$dirName/type")
                    if (type.contains("gpu", true)) {
                        val cur = fastRead("/sys/class/thermal/$dirName/cur_state").toIntOrNull() ?: 0
                        val max = fastRead("/sys/class/thermal/$dirName/max_state").toIntOrNull() ?: 0
                        coolingDevs.add(CoolingDevice(type, cur, max))
                    }
                }
            }
        } catch (e: Exception) { }
        return ThermalState(gpuTemp = zones.firstOrNull { it.type.contains("gpu", true) }?.temp ?: 0, thermalZones = zones, coolingDevices = coolingDevs, isThrottling = zones.any { it.temp > 80000 })
    }

    private suspend fun loadAdvancedData(): AdvancedState {
        val hasIdler = AdrenoUtils.hasAdrenoIdler()
        val hasSimple = AdrenoUtils.hasSimpleGpu()
        val hasKgsl = AdrenoUtils.isAdreno()
        return AdvancedState(
            hasAdrenoIdler = hasIdler, idlerActive = fastRead(AdrenoUtils.IDLER_ACTIVE).contains("Y") || fastRead(AdrenoUtils.IDLER_ACTIVE) == "1",
            idlerIdleWait = fastRead(AdrenoUtils.IDLER_IDLEWAIT), idlerDownDiff = fastRead(AdrenoUtils.IDLER_DOWNDIFF), idlerWorkload = fastRead(AdrenoUtils.IDLER_WORKLOAD),
            hasSimpleGpu = hasSimple, simpleGpuActive = fastRead(AdrenoUtils.SIMPLE_GPU_ACTIVATE) == "1", simpleLaziness = fastRead(AdrenoUtils.SIMPLE_GPU_LAZINESS), simpleRampThreshold = fastRead(AdrenoUtils.SIMPLE_RAMP_THRESHOLD),
            hasKgsl3d0 = hasKgsl,
            forceNoNap = fastRead("${AdrenoUtils.KGSL_3D0_DIR}/force_no_nap") == "1",
            forceClkOn = fastRead("${AdrenoUtils.KGSL_3D0_DIR}/force_clk_on") == "1",
            forceBusOn = fastRead("${AdrenoUtils.KGSL_3D0_DIR}/force_bus_on") == "1",
            busSplit = fastRead("${AdrenoUtils.KGSL_3D0_DIR}/bus_split") == "1",
            throttling = fastRead("${AdrenoUtils.KGSL_3D0_DIR}/throttling") == "1",
            defaultPwrlevel = fastRead("${AdrenoUtils.KGSL_3D0_DIR}/default_pwrlevel"),
            maxPwrlevel = fastRead("${AdrenoUtils.KGSL_3D0_DIR}/max_pwrlevel"),
            thermalPwrlevel = fastRead("${AdrenoUtils.KGSL_3D0_DIR}/thermal_pwrlevel")
        )
    }

    // ACTIONS
    fun updateFreq(target: String, selectedFreq: String) = viewModelScope.launch(Dispatchers.IO) { AdrenoUtils.writeFreqGPU(if (target == "min") AdrenoUtils.MIN_FREQ_GPU else AdrenoUtils.MAX_FREQ_GPU, selectedFreq); loadAllData() }
    fun updateGov(selectedGov: String) = viewModelScope.launch(Dispatchers.IO) { fastWrite(AdrenoUtils.GOV_GPU, selectedGov); loadAllData() }
    fun updateGPUThrottling(isChecked: Boolean) = viewModelScope.launch(Dispatchers.IO) { fastWrite(AdrenoUtils.GPU_THROTTLING, if (isChecked) "1" else "0"); loadAllData() }
    fun updateAdrenoBoost(value: String) = viewModelScope.launch(Dispatchers.IO) { fastWrite(AdrenoUtils.ADRENO_BOOST, value); loadAllData() }
    fun updateBusFreq(busName: String, target: String, freq: String) = viewModelScope.launch(Dispatchers.IO) { AdrenoUtils.setBusFreq(busName, target, freq); loadAllData() }
    
    // DEVFREQ ACTIONS (UFSHC & Busmon)
    fun updateDevfreq(path: String, param: String, value: String) = viewModelScope.launch(Dispatchers.IO) { fastWrite("$path/$param", value); loadAllData() }

    // ADVANCED KGSL ACTIONS
    fun updateKgslToggle(param: String, enable: Boolean) = viewModelScope.launch(Dispatchers.IO) { fastWrite("${AdrenoUtils.KGSL_3D0_DIR}/$param", if(enable) "1" else "0"); loadAllData() }
    fun updateKgslPwrlevel(param: String, value: String) = viewModelScope.launch(Dispatchers.IO) { fastWrite("${AdrenoUtils.KGSL_3D0_DIR}/$param", value); loadAllData() }
    
    // IDLER & SIMPLE GPU
    fun toggleIdler(enable: Boolean) = viewModelScope.launch(Dispatchers.IO) { fastWrite(AdrenoUtils.IDLER_ACTIVE, if (enable) "Y" else "N"); loadAllData() }
    fun updateIdlerParam(param: String, value: String) = viewModelScope.launch(Dispatchers.IO) { fastWrite(when(param) { "wait" -> AdrenoUtils.IDLER_IDLEWAIT "downdiff" -> AdrenoUtils.IDLER_DOWNDIFF "workload" -> AdrenoUtils.IDLER_WORKLOAD else -> return@launch }, value); loadAllData() }
    fun toggleSimpleGpu(enable: Boolean) = viewModelScope.launch(Dispatchers.IO) { fastWrite(AdrenoUtils.SIMPLE_GPU_ACTIVATE, if (enable) "1" else "0"); loadAllData() }
    fun updateSimpleGpuParam(param: String, value: String) = viewModelScope.launch(Dispatchers.IO) { fastWrite(when(param) { "laziness" -> AdrenoUtils.SIMPLE_GPU_LAZINESS "ramp" -> AdrenoUtils.SIMPLE_RAMP_THRESHOLD else -> return@launch }, value); loadAllData() }
}