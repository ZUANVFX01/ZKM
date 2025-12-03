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

    data class DramState(
        val type: MtkUtils.DramType = MtkUtils.DramType.NONE,
        val availableFreqsDisplay: List<String> = emptyList(), // For UI List
        val freqMap: Map<String, String> = emptyMap(), // Display -> Actual Value/Index
        val currentGov: String = "N/A",
        val availableGovs: List<String> = emptyList(),
        val minFreq: String = "N/A",
        val maxFreq: String = "N/A",
        val currentIndex: String = "N/A" // For Fliper
    )

    data class MtkState(
        val currentFreq: String = "Dynamic",
        val availableFreq: List<String> = emptyList(),
        val mtkFreqMap: Map<String, String> = emptyMap(),
        val mtkFixedIndex: String = "-1",
        val mtkMaxIndex: String = "-1",
        
        // GED
        val isFpsGoEnabled: Boolean = false,
        val isGedKpiEnabled: Boolean = false,
        val isPerfmgrEnabled: Boolean = false,
        val isGedBoostEnabled: Boolean = false,
        val isGedGameMode: Boolean = false,
        val isGedGpuBoost: Boolean = false,
        
        // Origami Extra Features
        val hasCciMode: Boolean = false,
        val cciMode: String = "0", // 0=Normal, 1=Perf
        
        val hasPowerMode: Boolean = false,
        val powerMode: String = "0", // 0=Normal, 1=Low, 2=Make, 3=Perf
        
        val hasSchedBoost: Boolean = false,
        val schedBoost: String = "0", // 0=Disabled, 1=FG, 2=All
        
        val hasPpm: Boolean = false,
        val isPpmEnabled: Boolean = false,

        val powerPolicy: MtkUtils.MtkPowerPolicy = MtkUtils.MtkPowerPolicy(),
        
        // DRAM / Memory
        val dramState: DramState = DramState()
    )

    private val _state = MutableStateFlow(MtkState())
    val state: StateFlow<MtkState> = _state

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val freqMap = MtkUtils.getMtkFreqMap()
            
            // === FIX START: Handle MTK Debug Strings (Helio G99 etc) ===
            val idxPath = MtkUtils.getFixedIndexPath()
            val rawFixedIdx = Utils.readFile(idxPath)
            val rawMaxIdx = Utils.readFile(MtkUtils.MTK_MAX_FREQ_BOUND)
            
            // Bersihkan output menggunakan parser baru
            val currentFixedIdx = MtkUtils.parseMtkIndex(rawFixedIdx)
            val currentMaxIdx = MtkUtils.parseMtkIndex(rawMaxIdx)
            // === FIX END ===

            val displayFreq = if (currentFixedIdx == "-1") "Dynamic" else {
                freqMap.entries.find { it.value == currentFixedIdx }?.key ?: "Unknown ($currentFixedIdx)"
            }
            
            // === LOAD DRAM DATA ===
            val dramType = MtkUtils.getDramType()
            val dramInfo = if (dramType != MtkUtils.DramType.NONE) MtkUtils.getDramFreqs(dramType) else Pair(emptyList(), emptyMap())
            val dramCurrent = if (dramType != MtkUtils.DramType.NONE) MtkUtils.getDramCurrentInfo(dramType) else emptyMap()
            val dramGovs = if (dramType == MtkUtils.DramType.DEVFREQ) MtkUtils.getDramGovs() else emptyList()
            
            val dramState = DramState(
                type = dramType,
                availableFreqsDisplay = dramInfo.first,
                freqMap = dramInfo.second,
                currentGov = dramCurrent["gov"] ?: "N/A",
                availableGovs = dramGovs,
                minFreq = dramCurrent["min"] ?: "N/A",
                maxFreq = dramCurrent["max"] ?: "N/A",
                currentIndex = dramCurrent["idx"] ?: "N/A"
            )

            _state.value = MtkState(
                currentFreq = displayFreq,
                availableFreq = freqMap.keys.sortedByDescending { it.toIntOrNull() ?: 0 }, 
                mtkFreqMap = freqMap,
                mtkFixedIndex = currentFixedIdx,
                mtkMaxIndex = currentMaxIdx,
                
                isFpsGoEnabled = Utils.readFile(MtkUtils.MTK_FPSGO) == "1",
                isGedKpiEnabled = Utils.readFile(MtkUtils.MTK_GED_KPI) == "1",
                isPerfmgrEnabled = MtkUtils.getMtkPerfmgrPath()?.let { Utils.readFile(it) == "1" } ?: false,
                isGedBoostEnabled = Utils.readFile(MtkUtils.GED_BOOST_ENABLE) == "1",
                isGedGameMode = Utils.readFile(MtkUtils.GED_GAME_MODE) == "1",
                isGedGpuBoost = Utils.readFile(MtkUtils.GED_GPU_BOOST) == "1",
                
                hasCciMode = MtkUtils.hasCciMode(),
                cciMode = Utils.readFile(MtkUtils.MTK_CCI_MODE).trim(),
                hasPowerMode = MtkUtils.hasPowerMode(),
                powerMode = Utils.readFile(MtkUtils.MTK_POWER_MODE).trim(),
                hasSchedBoost = MtkUtils.hasSchedBoost(),
                schedBoost = Utils.readFile(MtkUtils.MTK_SCHED_BOOST).trim(),
                hasPpm = MtkUtils.hasPpm(),
                isPpmEnabled = MtkUtils.isPpmEnabled(),
                powerPolicy = MtkUtils.getPowerPolicy(),
                
                dramState = dramState
            )
        }
    }

    // ... Existing functions ...
    fun updateFixedFreq(selectedFreq: String) { viewModelScope.launch(Dispatchers.IO) { _state.value.mtkFreqMap[selectedFreq]?.let { MtkUtils.setMtkFixedFreq(it); loadData() } } }
    fun resetFixedFreq() { viewModelScope.launch(Dispatchers.IO) { MtkUtils.setMtkFixedFreq("-1"); loadData() } }
    fun updateMaxFreq(selectedFreq: String) { viewModelScope.launch(Dispatchers.IO) { _state.value.mtkFreqMap[selectedFreq]?.let { MtkUtils.setMtkMaxFreq(it); loadData() } } }
    fun toggleFeature(feature: String, enable: Boolean) { viewModelScope.launch(Dispatchers.IO) {
            when(feature) {
                "fpsgo" -> MtkUtils.setMtkFeature(MtkUtils.MTK_FPSGO, enable)
                "ged_kpi" -> MtkUtils.setMtkFeature(MtkUtils.MTK_GED_KPI, enable)
                "perfmgr" -> MtkUtils.setMtkFeature(MtkUtils.getMtkPerfmgrPath(), enable)
                "ged_boost" -> MtkUtils.setMtkFeature(MtkUtils.GED_BOOST_ENABLE, enable)
                "ged_game" -> MtkUtils.setMtkFeature(MtkUtils.GED_GAME_MODE, enable)
                "ged_gpu_boost" -> MtkUtils.setMtkFeature(MtkUtils.GED_GPU_BOOST, enable)
                "ppm" -> MtkUtils.setPpmState(enable)
            }
            loadData()
        }
    }
    fun setCciMode(mode: String) { viewModelScope.launch(Dispatchers.IO) { Utils.writeFile(MtkUtils.MTK_CCI_MODE, mode); loadData() } }
    fun setPowerMode(mode: String) { viewModelScope.launch(Dispatchers.IO) { Utils.writeFile(MtkUtils.MTK_POWER_MODE, mode); loadData() } }
    fun setSchedBoost(mode: String) { viewModelScope.launch(Dispatchers.IO) { Utils.writeFile(MtkUtils.MTK_SCHED_BOOST, mode); loadData() } }
    fun togglePowerPolicy(key: String, enable: Boolean) { viewModelScope.launch(Dispatchers.IO) { MtkUtils.setPowerPolicy(key, enable); loadData() } }

    // === DRAM FUNCTIONS ===
    fun setDramFreq(displayFreq: String, target: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val rawValue = _state.value.dramState.freqMap[displayFreq] ?: return@launch
            MtkUtils.setDramFreq(_state.value.dramState.type, target, rawValue)
            loadData()
        }
    }

    fun setDramGov(gov: String) {
        viewModelScope.launch(Dispatchers.IO) {
            MtkUtils.setDramGov(gov)
            loadData()
        }
    }
}
