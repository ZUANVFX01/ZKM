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
        val gpuThrottling: String = "0"
    )

    private val _state = MutableStateFlow(AdrenoState())
    val state: StateFlow<AdrenoState> = _state

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
             _state.value = AdrenoState(
                minFreq = Utils.readFile(AdrenoUtils.MIN_FREQ_GPU),
                maxFreq = Utils.readFile(AdrenoUtils.MAX_FREQ_GPU),
                currentFreq = AdrenoUtils.readFreqGPU(AdrenoUtils.CURRENT_FREQ_GPU),
                gov = Utils.readFile(AdrenoUtils.GOV_GPU),
                maxPwrlevel = Utils.readFile(AdrenoUtils.MAX_PWRLEVEL),
                minPwrlevel = Utils.readFile(AdrenoUtils.MIN_PWRLEVEL),
                defaultPwrlevel = Utils.readFile(AdrenoUtils.DEFAULT_PWRLEVEL),
                adrenoBoost = Utils.readFile(AdrenoUtils.ADRENO_BOOST),
                gpuThrottling = Utils.readFile(AdrenoUtils.GPU_THROTTLING),
                availableFreq = AdrenoUtils.readAvailableFreqGPU(),
                availableGov = Utils.readFile(AdrenoUtils.AVAILABLE_GOV_GPU).split(" "),
            )
        }
    }

    fun updateFreq(target: String, selectedFreq: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val path = if (target == "min") AdrenoUtils.MIN_FREQ_GPU else AdrenoUtils.MAX_FREQ_GPU
            AdrenoUtils.writeFreqGPU(path, selectedFreq)
            loadData()
        }
    }

    fun updateGov(selectedGov: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Utils.writeFile(AdrenoUtils.GOV_GPU, selectedGov)
            loadData()
        }
    }
    
    fun updateDefaultPwrlevel(value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Utils.writeFile(AdrenoUtils.DEFAULT_PWRLEVEL, value)
            loadData()
        }
    }

    fun updateAdrenoBoost(value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Utils.writeFile(AdrenoUtils.ADRENO_BOOST, value)
            loadData()
        }
    }

    fun updateGPUThrottling(isChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val newValue = if (isChecked) "1" else "0"
            Utils.writeFile(AdrenoUtils.GPU_THROTTLING, newValue)
            loadData()
        }
    }
}