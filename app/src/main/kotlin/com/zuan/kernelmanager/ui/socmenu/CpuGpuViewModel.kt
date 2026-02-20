/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.socmenu

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zuan.kernelmanager.ui.settings.SettingsPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CpuGpuViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsPreference = SettingsPreference.getInstance(application)

    // Data Classes
    data class CPUState(
        val name: String, val policyPath: String, val minFreq: String, val maxFreq: String,
        val currentFreq: String, val gov: String, val availableFreq: List<String>, 
        val availableGov: List<String>, val isPrime: Boolean = false,
        // List core yang tergabung di cluster ini beserta statusnya
        val associatedCores: List<CoreStatus> = emptyList()
    ) 

    data class GPUState(
        val type: CpuGpuUtils.GpuType, 
        val currentFreq: String,
        val usage: String
    ) { 
        companion object { val EMPTY = GPUState(CpuGpuUtils.GpuType.UNKNOWN, "N/A", "0") } 
    }

    // Flows
    private val _socName = MutableStateFlow("Processor")
    val socName: StateFlow<String> = _socName

    private val _clusterStates = MutableStateFlow<List<CPUState>>(emptyList())
    val clusterStates: StateFlow<List<CPUState>> = _clusterStates
    
    private val _gpuState = MutableStateFlow(GPUState.EMPTY)
    val gpuState: StateFlow<GPUState> = _gpuState

    private val _govTunables = MutableStateFlow<List<GovTunable>>(emptyList())
    val govTunables: StateFlow<List<GovTunable>> = _govTunables

    // Cpuset Flow
    private val _cpusetList = MutableStateFlow<List<CpusetData>>(emptyList())
    val cpusetList: StateFlow<List<CpusetData>> = _cpusetList

    private var job: Job? = null

    init {
        loadStaticInfo()
        startJob()
    }

    private fun loadStaticInfo() {
        viewModelScope.launch {
            _socName.value = CpuGpuUtils.getCpuInfo()
        }
    }

    fun startJob() {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            settingsPreference.pollingInterval.collect { interval ->
                while (true) {
                    loadDynamicCPUData()
                    loadGPUData()
                    loadCpusetData()
                    delay(interval)
                }
            }
        }
    }

    fun stopJob() { job?.cancel(); job = null }

    // --- Loaders ---
    private suspend fun loadDynamicCPUData() {
        val policies = CpuGpuUtils.getCpuPolicies() 
        if (policies.isEmpty()) return

        val rawData = policies.map { path ->
            val maxFreq = CpuGpuUtils.readFreq(path, "scaling_max_freq").toIntOrNull() ?: 0
            Triple(path, maxFreq, CpuGpuUtils.readAvailableFreq(path))
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
            
            // Load status per-core (Online/Offline)
            val affectedCpus = CpuGpuUtils.getAffectedCpus(path)
            val coreStatuses = affectedCpus.map { id ->
                CoreStatus(id, CpuGpuUtils.getCoreStatus(id))
            }
            
            CPUState(
                name = name, policyPath = path,
                minFreq = CpuGpuUtils.readFreq(path, "scaling_min_freq"),
                maxFreq = CpuGpuUtils.readFreq(path, "scaling_max_freq"),
                currentFreq = CpuGpuUtils.readFreq(path, "scaling_cur_freq"),
                gov = CpuGpuUtils.readGovernor(path),
                availableFreq = availFreq,
                availableGov = CpuGpuUtils.readAvailableGov(path),
                isPrime = isPrime,
                associatedCores = coreStatuses
            )
        }.sortedBy { it.policyPath }
        _clusterStates.value = newStates
    }

    private suspend fun loadGPUData() {
        val type = CpuGpuUtils.getGpuType()
        val usage = CpuGpuUtils.getGpuUsage() 
        
        if (type == CpuGpuUtils.GpuType.ADRENO) {
            val freq = CpuGpuUtils.readFreqGPU(CpuGpuUtils.CURRENT_FREQ_GPU)
            _gpuState.value = GPUState(type, freq, usage)
        } else {
            _gpuState.value = GPUState(type, "Dynamic", usage)
        }
    }

    private suspend fun loadCpusetData() {
        val data = CpuGpuUtils.getCpusetInfo()
        _cpusetList.value = data
    }

    // --- Actions ---
    
    fun updateFreq(target: String, selectedFreq: String, policyPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = if (target == "min") "scaling_min_freq" else "scaling_max_freq"
            CpuGpuUtils.writeFreq(policyPath, file, selectedFreq)
            loadDynamicCPUData() 
        }
    }

    fun updateGov(selectedGov: String, policyPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            CpuGpuUtils.writeGov(policyPath, selectedGov)
            loadDynamicCPUData()
            loadGovTunables(policyPath, selectedGov)
        }
    }

    fun loadGovTunables(policyPath: String, governor: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val tunables = CpuGpuUtils.getGovernorTunables(policyPath, governor)
            _govTunables.value = tunables
        }
    }

    fun applyTunable(tunable: GovTunable, newValue: String) {
        viewModelScope.launch(Dispatchers.IO) {
            CpuGpuUtils.writeTunable(tunable.path, newValue)
            val currentList = _govTunables.value.map { 
                if (it.path == tunable.path) it.copy(value = newValue) else it 
            }
            _govTunables.value = currentList
        }
    }

    // Toggle Core Action
    fun toggleCore(coreId: Int, currentStatus: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (coreId == 0) return@launch // Core 0 is sacred
            
            // Safety check: jika ingin mematikan, pastikan sisa core > 1
            if (currentStatus) {
                val activeCount = CpuGpuUtils.getTotalOnlineCores()
                if (activeCount <= 1) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(getApplication(), "Safety Halt: Cannot disable all cores.", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }
            }

            CpuGpuUtils.setCoreOnline(coreId, !currentStatus)
            loadDynamicCPUData()
        }
    }

    // Update Cpuset Action
    fun updateCpuset(cpusetPath: String, selectedCores: List<Int>) {
        viewModelScope.launch(Dispatchers.IO) {
            if (selectedCores.isEmpty()) {
                 withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Error: Cpuset must have at least 1 core.", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            CpuGpuUtils.applyCpuset(cpusetPath, selectedCores)
            loadCpusetData()
        }
    }
}
