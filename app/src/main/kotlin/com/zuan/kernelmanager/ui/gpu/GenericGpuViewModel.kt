/*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.ui.gpu

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zuan.kernelmanager.utils.GenericGpuUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GenericGpuViewModel(application: Application) : AndroidViewModel(application) {

    data class GenericGpuState(
        val isSupported: Boolean = false,
        val gpuPath: String = "",
        val minFreq: String = "N/A",
        val maxFreq: String = "N/A",
        val curFreq: String = "N/A",
        val gov: String = "N/A",
        val availableFreqs: List<String> = emptyList(),
        val availableGovs: List<String> = emptyList()
    )

    private val _state = MutableStateFlow(GenericGpuState())
    val state: StateFlow<GenericGpuState> = _state

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val path = GenericGpuUtils.getGpuPath()
            
            if (path == null) {
                _state.value = GenericGpuState(isSupported = false)
                return@launch
            }

            _state.value = GenericGpuState(
                isSupported = true,
                gpuPath = path,
                minFreq = GenericGpuUtils.getMinFreq(path),
                maxFreq = GenericGpuUtils.getMaxFreq(path),
                curFreq = GenericGpuUtils.getCurFreq(path),
                gov = GenericGpuUtils.getGov(path),
                availableFreqs = GenericGpuUtils.getAvailableFreqs(path),
                availableGovs = GenericGpuUtils.getAvailableGovs(path)
            )
        }
    }

    fun updateFreq(type: String, freq: String) {
        // type: "min" or "max"
        val path = _state.value.gpuPath
        if (path.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            GenericGpuUtils.setFreq(path, type, freq)
            loadData() // Refresh UI data setelah set
        }
    }

    fun updateGov(gov: String) {
        val path = _state.value.gpuPath
        if (path.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            GenericGpuUtils.setGov(path, gov)
            loadData()
        }
    }
}
