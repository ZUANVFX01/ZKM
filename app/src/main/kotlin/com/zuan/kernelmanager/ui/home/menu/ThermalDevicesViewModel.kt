/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.home.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ThermalStats(
    val maxTemp: Int = 0,
    val avgTemp: Double = 0.0,
    val activeZones: Int = 0,
    val totalZones: Int = 0
)

class ThermalDevicesViewModel : ViewModel() {

    private val _allZones = MutableStateFlow<List<ThermalZone>>(emptyList())
    private val _coolingDevices = MutableStateFlow<List<ThermalCoolingDevice>>(emptyList())
    private val _selectedFilter = MutableStateFlow("All")
    private val _isLoading = MutableStateFlow(true)
    private val _isAutoRefresh = MutableStateFlow(true)
    private val _thermalPolicy = MutableStateFlow("default")
    private val _showTripPoints = MutableStateFlow<Map<Int, List<TripPoint>>>(emptyMap())

    val isLoading: StateFlow<Boolean> = _isLoading
    val isAutoRefresh: StateFlow<Boolean> = _isAutoRefresh
    val thermalPolicy: StateFlow<String> = _thermalPolicy
    val selectedFilter: StateFlow<String> = _selectedFilter
    val coolingDevices: StateFlow<List<ThermalCoolingDevice>> = _coolingDevices
    val showTripPoints: StateFlow<Map<Int, List<TripPoint>>> = _showTripPoints

    val availableFilters = listOf("All", "CPU", "GPU", "Battery", "Skin", "Charger", "System")

    // Flow untuk Tab 1: Zones (Aktif + Filtered)
    val filteredZones: StateFlow<List<ThermalZone>> = combine(_allZones, _selectedFilter) { zones, filter ->
        val activeZones = zones.filter { it.isEnabled }
        if (filter == "All") activeZones else activeZones.filter { it.type == filter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Flow untuk Tab 3: Disabled Zones
    val disabledZones: StateFlow<List<ThermalZone>> = _allZones.map { zones ->
        zones.filter { !it.isEnabled }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Update Statistik agar cerdas memilah sensor rusak/0 derajat
    val thermalStats: StateFlow<ThermalStats> = combine(_allZones) { zonesArray ->
        val zoneList = zonesArray[0]
        if (zoneList.isEmpty()) {
            ThermalStats()
        } else {
            // Hanya ambil sensor aktif yang suhunya masuk akal (> 0°C) untuk rata-rata
            val validTemps = zoneList.filter { it.isEnabled && it.temperature > 0 }.map { it.temperature }
            ThermalStats(
                maxTemp = validTemps.maxOrNull() ?: 0,
                avgTemp = if (validTemps.isNotEmpty()) validTemps.average() else 0.0,
                activeZones = zoneList.count { it.isEnabled },
                totalZones = zoneList.size
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThermalStats())

    init {
        loadThermalData()
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (isActive) {
                if (_isAutoRefresh.value) {
                    refreshData()
                }
                delay(2000)
            }
        }
    }

    fun loadThermalData() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val zones = ThermalUtils.getAllThermalZones()
            val cooling = ThermalUtils.getCoolingDevices()
            val policy = ThermalUtils.getThermalPolicy()
            
            withContext(Dispatchers.Main) {
                _allZones.value = zones
                _coolingDevices.value = cooling
                _thermalPolicy.value = policy
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch(Dispatchers.IO) {
            val zones = ThermalUtils.getAllThermalZones()
            val cooling = ThermalUtils.getCoolingDevices()
            
            withContext(Dispatchers.Main) {
                _allZones.value = zones
                _coolingDevices.value = cooling
            }
        }
    }

    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
    }

    fun toggleAutoRefresh(enabled: Boolean) {
        _isAutoRefresh.value = enabled
    }

    fun toggleThermalZone(zone: ThermalZone) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = ThermalUtils.setThermalZoneState(zone.id, !zone.isEnabled)
            if (success) refreshData()
        }
    }

    fun setCoolingState(device: ThermalCoolingDevice, state: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = ThermalUtils.setCoolingDeviceState(device.id, state)
            if (success) refreshData()
        }
    }

    fun setThermalPolicy(policy: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = ThermalUtils.setThermalPolicy(policy)
            if (success) _thermalPolicy.value = policy
        }
    }

    fun loadTripPoints(zone: ThermalZone) {
        viewModelScope.launch(Dispatchers.IO) {
            val trips = ThermalUtils.getTripPoints(zone.path)
            withContext(Dispatchers.Main) {
                _showTripPoints.value = _showTripPoints.value.toMutableMap().apply {
                    put(zone.id, trips)
                }
            }
        }
    }

    fun clearTripPoints(zoneId: Int) {
        _showTripPoints.value = _showTripPoints.value.toMutableMap().apply {
            remove(zoneId)
        }
    }
}
