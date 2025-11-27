/*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.ui.gpu

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zuan.kernelmanager.utils.MtkUtils
import com.zuan.kernelmanager.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MtkViewModel(application: Application) : AndroidViewModel(application) {

    data class MtkState(
        val currentFreq: String = "Dynamic",
        val availableFreq: List<String> = emptyList(),
        val mtkFreqMap: Map<String, String> = emptyMap(),
        val mtkFixedIndex: String = "-1",
        val mtkMaxIndex: String = "-1",
        val isFpsGoEnabled: Boolean = false,
        val isGedKpiEnabled: Boolean = false,
        val isPerfmgrEnabled: Boolean = false
    )

    private val _state = MutableStateFlow(MtkState())
    val state: StateFlow<MtkState> = _state

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val freqMap = MtkUtils.getMtkFreqMap()
            val currentFixedIdx = Utils.readFile(MtkUtils.MTK_FIXED_INDEX).trim()
            val currentMaxIdx = Utils.readFile(MtkUtils.MTK_MAX_FREQ_BOUND).trim()
            val perfmgrPath = MtkUtils.getMtkPerfmgrPath()
            
            val displayFreq = if (currentFixedIdx == "-1" || currentFixedIdx.isEmpty()) "Dynamic" else {
                freqMap.entries.find { it.value.toIntOrNull() == currentFixedIdx.toIntOrNull() }?.key ?: "Unknown"
            }

            _state.value = MtkState(
                currentFreq = displayFreq,
                availableFreq = freqMap.keys.sortedBy { it.toIntOrNull() ?: 0 },
                mtkFreqMap = freqMap,
                mtkFixedIndex = currentFixedIdx,
                mtkMaxIndex = currentMaxIdx,
                isFpsGoEnabled = Utils.readFile(MtkUtils.MTK_FPSGO) == "1",
                isGedKpiEnabled = Utils.readFile(MtkUtils.MTK_GED_KPI) == "1",
                isPerfmgrEnabled = if (perfmgrPath != null) Utils.readFile(perfmgrPath) == "1" else false
            )
        }
    }

    fun updateFixedFreq(selectedFreq: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val index = _state.value.mtkFreqMap[selectedFreq] ?: return@launch
            MtkUtils.setMtkFixedFreq(index)
            loadData()
        }
    }

    fun resetFixedFreq() {
        viewModelScope.launch(Dispatchers.IO) {
            MtkUtils.setMtkFixedFreq("-1")
            loadData()
        }
    }

    fun updateMaxFreq(selectedFreq: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val index = _state.value.mtkFreqMap[selectedFreq] ?: return@launch
            MtkUtils.setMtkMaxFreq(index)
            loadData()
        }
    }

    fun toggleFeature(feature: String, enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            when(feature) {
                "fpsgo" -> MtkUtils.setMtkFeature(MtkUtils.MTK_FPSGO, enable)
                "ged_kpi" -> MtkUtils.setMtkFeature(MtkUtils.MTK_GED_KPI, enable)
                "perfmgr" -> MtkUtils.setMtkFeature(MtkUtils.getMtkPerfmgrPath(), enable)
            }
            loadData()
        }
    }
}