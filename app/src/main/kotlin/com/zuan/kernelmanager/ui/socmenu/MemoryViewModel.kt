/*
 * Original code from: Rem01Gaming (origami_kernel_manager)
 * Modified and integrated by: Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.socmenu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zuan.kernelmanager.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MemoryViewModel : ViewModel() {

    // State untuk VM standard
    data class MemoryState(
        val swappiness: String = "0", val hasSwappiness: Boolean = false,
        val vfsCachePressure: String = "0", val hasVfsCachePressure: Boolean = false,
        val dirtyRatio: String = "0", val hasDirtyRatio: Boolean = false,
        val dirtyBackgroundRatio: String = "0", val hasDirtyBackgroundRatio: Boolean = false,
        val minFreeKbytes: String = "0", val hasMinFreeKbytes: Boolean = false,
        val extraFreeKbytes: String = "0", val hasExtraFreeKbytes: Boolean = false
    )

    // State untuk ZRAM
    data class ZramState(
        val sizeMb: Int = 0,
        val activeAlgo: String = "Unknown",
        val availableAlgos: List<String> = emptyList()
    )

    // State untuk I/O Device
    data class IODeviceState(
        val name: String,
        val activeScheduler: String,
        val availableSchedulers: List<String>,
        // Tunables values
        val nrRequests: String = "",
        val readAheadKb: String = "",
        val rqAffinity: String = "",
        val rotational: String = "",
        val addRandom: String = "",
        val iostats: String = ""
    )

    private val _mem = MutableStateFlow(MemoryState())
    val mem: StateFlow<MemoryState> = _mem

    private val _zram = MutableStateFlow(ZramState())
    val zram: StateFlow<ZramState> = _zram

    private val _ioDevices = MutableStateFlow<List<IODeviceState>>(emptyList())
    val ioDevices: StateFlow<List<IODeviceState>> = _ioDevices

    init { refreshAll() }

    fun refreshAll() {
        viewModelScope.launch(Dispatchers.IO) {
            refreshVmStats()
            refreshZramStats()
            refreshIOStats()
        }
    }

    private fun refreshVmStats() {
        _mem.value = MemoryState(
            swappiness = Utils.readFile(MemoryUtils.SWAPPINESS).trim(),
            hasSwappiness = Utils.testFile(MemoryUtils.SWAPPINESS),
            vfsCachePressure = Utils.readFile(MemoryUtils.VFS_CACHE_PRESSURE).trim(),
            hasVfsCachePressure = Utils.testFile(MemoryUtils.VFS_CACHE_PRESSURE),
            dirtyRatio = Utils.readFile(MemoryUtils.DIRTY_RATIO).trim(),
            hasDirtyRatio = Utils.testFile(MemoryUtils.DIRTY_RATIO),
            dirtyBackgroundRatio = Utils.readFile(MemoryUtils.DIRTY_BACKGROUND_RATIO).trim(),
            hasDirtyBackgroundRatio = Utils.testFile(MemoryUtils.DIRTY_BACKGROUND_RATIO),
            minFreeKbytes = Utils.readFile(MemoryUtils.MIN_FREE_KBYTES).trim(),
            hasMinFreeKbytes = Utils.testFile(MemoryUtils.MIN_FREE_KBYTES),
            extraFreeKbytes = Utils.readFile(MemoryUtils.EXTRA_FREE_KBYTES).trim(),
            hasExtraFreeKbytes = Utils.testFile(MemoryUtils.EXTRA_FREE_KBYTES)
        )
    }

    private fun refreshZramStats() {
        val sizeBytes = MemoryUtils.getZramSizeBytes()
        val sizeMb = (sizeBytes / 1048576L).toInt() // Convert Byte to MB
        val (active, algos) = MemoryUtils.getZramAlgoInfo()
        
        _zram.value = ZramState(
            sizeMb = sizeMb,
            activeAlgo = active,
            availableAlgos = algos
        )
    }

    private fun refreshIOStats() {
        val devices = MemoryUtils.getBlockDevices()
        val newList = devices.map { devName ->
            val (sched, scheds) = MemoryUtils.getIOSchedulerInfo(devName)
            IODeviceState(
                name = devName,
                activeScheduler = sched,
                availableSchedulers = scheds,
                nrRequests = MemoryUtils.getIOTunable(devName, "nr_requests"),
                readAheadKb = MemoryUtils.getIOTunable(devName, "read_ahead_kb"),
                rqAffinity = MemoryUtils.getIOTunable(devName, "rq_affinity"),
                rotational = MemoryUtils.getIOTunable(devName, "rotational"),
                addRandom = MemoryUtils.getIOTunable(devName, "add_random"),
                iostats = MemoryUtils.getIOTunable(devName, "iostats")
            )
        }
        _ioDevices.value = newList
    }

    fun updateVmValue(path: String, value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Utils.writeFile(path, value)
            refreshVmStats()
        }
    }
    
    // ZRAM Logic
    fun applyZramChanges(targetSizeMb: Int, targetAlgo: String) {
        viewModelScope.launch(Dispatchers.IO) {
            MemoryUtils.swapoff()
            MemoryUtils.resetZram()
            MemoryUtils.setZramCompAlgorithm(targetAlgo)
            val sizeBytes = targetSizeMb * 1048576L
            MemoryUtils.setZramSize(sizeBytes)
            MemoryUtils.mkswap()
            MemoryUtils.swapon()
            refreshZramStats()
        }
    }

    // I/O Logic
    fun setIOScheduler(devName: String, scheduler: String) {
        viewModelScope.launch(Dispatchers.IO) {
            MemoryUtils.setIOScheduler(devName, scheduler)
            refreshIOStats()
        }
    }

    fun setIOTunable(devName: String, key: String, value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            MemoryUtils.setIOTunable(devName, key, value)
            refreshIOStats()
        }
    }
}
