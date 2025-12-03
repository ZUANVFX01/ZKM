/*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.ui.gpu

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zuan.kernelmanager.utils.AdrenoUtils
import com.zuan.kernelmanager.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdrenoViewModel(application: Application) : AndroidViewModel(application) {

    data class BusState(
        val name: String,
        val minFreq: String,
        val maxFreq: String,
        val availableFreqs: List<String>
    )

    data class AdrenoState(
        val minFreq: String = "N/A",
        val maxFreq: String = "N/A",
        val currentFreq: String = "N/A",
        val gov: String = "N/A",
        val availableFreq: List<String> = emptyList(),
        val availableGov: List<String> = emptyList(),
        val maxPwrlevel: String = "N/A",
        val minPwrlevel: String = "N/A",
        val defaultPwrlevel: String = "N/A",
        val adrenoBoost: String = "0",
        
        // Throttling Logic
        val hasGpuThrottling: Boolean = false,
        val gpuThrottling: String = "0",
        
        val hasAdrenoIdler: Boolean = false,
        val idlerActive: Boolean = false,
        val idlerIdleWait: String = "0",
        val idlerDownDiff: String = "0",
        val idlerWorkload: String = "0",

        val hasSimpleGpu: Boolean = false,
        val simpleGpuActive: Boolean = false,
        val simpleLaziness: String = "0",
        val simpleRampThreshold: String = "0",

        val hasBusDcvs: Boolean = false,
        val busComponents: List<BusState> = emptyList()
    )

    private val _state = MutableStateFlow(AdrenoState())
    val state: StateFlow<AdrenoState> = _state

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
             val hasIdler = AdrenoUtils.hasAdrenoIdler()
             val hasSimple = AdrenoUtils.hasSimpleGpu()
             val hasBus = AdrenoUtils.hasBusDcvs()
             val hasThrottling = AdrenoUtils.hasGpuThrottling()

             val baseState = AdrenoState(
                minFreq = Utils.readFile(AdrenoUtils.MIN_FREQ_GPU),
                maxFreq = Utils.readFile(AdrenoUtils.MAX_FREQ_GPU),
                currentFreq = AdrenoUtils.readFreqGPU(AdrenoUtils.CURRENT_FREQ_GPU),
                gov = Utils.readFile(AdrenoUtils.GOV_GPU),
                maxPwrlevel = Utils.readFile(AdrenoUtils.MAX_PWRLEVEL),
                minPwrlevel = Utils.readFile(AdrenoUtils.MIN_PWRLEVEL),
                defaultPwrlevel = Utils.readFile(AdrenoUtils.DEFAULT_PWRLEVEL),
                adrenoBoost = Utils.readFile(AdrenoUtils.ADRENO_BOOST),
                
                hasGpuThrottling = hasThrottling,
                gpuThrottling = if (hasThrottling) Utils.readFile(AdrenoUtils.GPU_THROTTLING) else "0",
                
                availableFreq = AdrenoUtils.readAvailableFreqGPU(),
                availableGov = Utils.readFile(AdrenoUtils.AVAILABLE_GOV_GPU).split(" "),

                hasAdrenoIdler = hasIdler,
                idlerActive = if (hasIdler) AdrenoUtils.readParam(AdrenoUtils.IDLER_ACTIVE).contains("Y") || AdrenoUtils.readParam(AdrenoUtils.IDLER_ACTIVE) == "1" else false,
                idlerIdleWait = if (hasIdler) AdrenoUtils.readParam(AdrenoUtils.IDLER_IDLEWAIT) else "0",
                idlerDownDiff = if (hasIdler) AdrenoUtils.readParam(AdrenoUtils.IDLER_DOWNDIFF) else "0",
                idlerWorkload = if (hasIdler) AdrenoUtils.readParam(AdrenoUtils.IDLER_WORKLOAD) else "0",

                hasSimpleGpu = hasSimple,
                simpleGpuActive = if (hasSimple) AdrenoUtils.readParam(AdrenoUtils.SIMPLE_GPU_ACTIVATE) == "1" else false,
                simpleLaziness = if (hasSimple) AdrenoUtils.readParam(AdrenoUtils.SIMPLE_GPU_LAZINESS) else "0",
                simpleRampThreshold = if (hasSimple) AdrenoUtils.readParam(AdrenoUtils.SIMPLE_RAMP_THRESHOLD) else "0",
                
                hasBusDcvs = hasBus
            )

            // LOAD & FILTER BUS DATA
            val busList = if (hasBus) {
                AdrenoUtils.getBusComponents().mapNotNull { busName ->
                    val freqs = AdrenoUtils.getBusAvailableFreqs(busName)
                    // FILTER: Hanya ambil bus yang punya list frekuensi valid
                    if (freqs.isNotEmpty()) {
                        BusState(
                            name = busName,
                            minFreq = AdrenoUtils.getBusMinFreq(busName),
                            maxFreq = AdrenoUtils.getBusMaxFreq(busName),
                            availableFreqs = freqs
                        )
                    } else null 
                }
            } else emptyList()

            _state.value = baseState.copy(busComponents = busList)
        }
    }

    fun updateFreq(target: String, selectedFreq: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val path = if (target == "min") AdrenoUtils.MIN_FREQ_GPU else AdrenoUtils.MAX_FREQ_GPU
            AdrenoUtils.writeFreqGPU(path, selectedFreq)
            loadData()
        }
    }

    fun updateGov(selectedGov: String) { viewModelScope.launch(Dispatchers.IO) { Utils.writeFile(AdrenoUtils.GOV_GPU, selectedGov); loadData() } }
    fun updateDefaultPwrlevel(value: String) { viewModelScope.launch(Dispatchers.IO) { Utils.writeFile(AdrenoUtils.DEFAULT_PWRLEVEL, value); loadData() } }
    fun updateAdrenoBoost(value: String) { viewModelScope.launch(Dispatchers.IO) { Utils.writeFile(AdrenoUtils.ADRENO_BOOST, value); loadData() } }
    
    // Logic: Checked = 1 (Throttling ON), Unchecked = 0 (Throttling OFF)
    fun updateGPUThrottling(isChecked: Boolean) { 
        viewModelScope.launch(Dispatchers.IO) { 
            Utils.writeFile(AdrenoUtils.GPU_THROTTLING, if (isChecked) "1" else "0")
            loadData() 
        } 
    }
    
    fun toggleIdler(enable: Boolean) { viewModelScope.launch(Dispatchers.IO) { Utils.writeFile(AdrenoUtils.IDLER_ACTIVE, if (enable) "Y" else "N"); loadData() } }
    fun updateIdlerParam(param: String, value: String) { viewModelScope.launch(Dispatchers.IO) { 
        val path = when(param) { "wait"->AdrenoUtils.IDLER_IDLEWAIT; "downdiff"->AdrenoUtils.IDLER_DOWNDIFF; "workload"->AdrenoUtils.IDLER_WORKLOAD; else->return@launch }
        Utils.writeFile(path, value); loadData() 
    }}
    fun toggleSimpleGpu(enable: Boolean) { viewModelScope.launch(Dispatchers.IO) { Utils.writeFile(AdrenoUtils.SIMPLE_GPU_ACTIVATE, if (enable) "1" else "0"); loadData() } }
    fun updateSimpleGpuParam(param: String, value: String) { viewModelScope.launch(Dispatchers.IO) {
        val path = when(param) { "laziness"->AdrenoUtils.SIMPLE_GPU_LAZINESS; "ramp"->AdrenoUtils.SIMPLE_RAMP_THRESHOLD; else->return@launch }
        Utils.writeFile(path, value); loadData()
    }}

    fun updateBusFreq(busName: String, target: String, freq: String) {
        viewModelScope.launch(Dispatchers.IO) {
            AdrenoUtils.setBusFreq(busName, target, freq)
            loadData()
        }
    }
}
