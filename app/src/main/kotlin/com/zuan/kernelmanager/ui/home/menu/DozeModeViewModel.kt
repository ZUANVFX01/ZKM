/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.home.menu

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DozeModeViewModel : ViewModel() {

    private val _allApps = MutableStateFlow<List<DozeApp>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _selectedTab = MutableStateFlow(0)
    private val _isLoading = MutableStateFlow(true)
    private val _dozeState = MutableStateFlow("Unknown")
    private val _dozeSettings = MutableStateFlow<DozeSettings?>(null)
    private val _dozeStats = MutableStateFlow<DozeStats?>(null)
    private val _isAutoRefresh = MutableStateFlow(true)
    
    // State Baru: GMS Doze
    private val _gmsDozeMode = MutableStateFlow("Default")

    val isLoading: StateFlow<Boolean> = _isLoading
    val searchQuery: StateFlow<String> = _searchQuery
    val selectedTab: StateFlow<Int> = _selectedTab
    val dozeState: StateFlow<String> = _dozeState
    val dozeSettings: StateFlow<DozeSettings?> = _dozeSettings
    val dozeStats: StateFlow<DozeStats?> = _dozeStats
    val isAutoRefresh: StateFlow<Boolean> = _isAutoRefresh
    val gmsDozeMode: StateFlow<String> = _gmsDozeMode

    // Flow Khusus Tab "Apps" (Semua Aplikasi)
    val allFilteredApps: StateFlow<List<DozeApp>> = combine(_allApps, _searchQuery) { apps, query ->
        if (query.isBlank()) {
            apps
        } else {
            apps.filter {
                it.label.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Flow Khusus Tab "Whitelist" (Hanya aplikasi yang di-Whitelist)
    val whitelistedFilteredApps: StateFlow<List<DozeApp>> = combine(_allApps, _searchQuery) { apps, query ->
        val whitelistedOnly = apps.filter { it.isWhitelisted }
        if (query.isBlank()) {
            whitelistedOnly
        } else {
            whitelistedOnly.filter {
                it.label.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (isActive) {
                if (_isAutoRefresh.value) {
                    refreshDozeState()
                }
                delay(3000)
            }
        }
    }

    fun loadDozeData(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            
            val apps = DozeModeUtils.getDozeWhitelist(context)
            val state = DozeModeUtils.getDozeState()
            val settings = DozeModeUtils.getDozeSettings()
            val stats = DozeModeUtils.getDozeStats()
            val gmsMode = DozeModeUtils.getGmsDozeMode()
            
            withContext(Dispatchers.Main) {
                _allApps.value = apps
                _dozeState.value = state
                _dozeSettings.value = settings
                _dozeStats.value = stats
                _gmsDozeMode.value = gmsMode
                _isLoading.value = false
            }
        }
    }

    fun refreshDozeState() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = DozeModeUtils.getDozeState()
            val stats = DozeModeUtils.getDozeStats()
            val gmsMode = DozeModeUtils.getGmsDozeMode()
            
            withContext(Dispatchers.Main) {
                _dozeState.value = state
                _dozeStats.value = stats
                _gmsDozeMode.value = gmsMode
            }
        }
    }
    
    fun setGmsDozeMode(mode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = DozeModeUtils.applyGmsDozeMode(mode)
            if (success) {
                withContext(Dispatchers.Main) {
                    _gmsDozeMode.value = mode
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) { _searchQuery.value = query }
    fun onTabSelected(index: Int) { _selectedTab.value = index }
    fun toggleAutoRefresh(enabled: Boolean) { _isAutoRefresh.value = enabled }

    fun toggleWhitelist(app: DozeApp) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = if (app.isWhitelisted) {
                DozeModeUtils.removeFromWhitelist(app.packageName)
            } else {
                DozeModeUtils.addToWhitelist(app.packageName)
            }
            if (success) {
                val updatedApps = _allApps.value.map {
                    if (it.packageName == app.packageName) it.copy(isWhitelisted = !app.isWhitelisted) else it
                }
                _allApps.value = updatedApps
            }
        }
    }

    fun setDozeEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (DozeModeUtils.setDozeEnabled(enabled)) {
                _dozeSettings.value = _dozeSettings.value?.copy(isEnabled = enabled)
            }
        }
    }

    fun setAggressiveDoze(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (DozeModeUtils.setAggressiveDoze(enabled)) {
                _dozeSettings.value = _dozeSettings.value?.copy(isAggressive = enabled)
            }
        }
    }

    fun forceIdle() { viewModelScope.launch(Dispatchers.IO) { DozeModeUtils.forceIdle(); refreshDozeState() } }
    fun unforceIdle() { viewModelScope.launch(Dispatchers.IO) { DozeModeUtils.unforceIdle(); refreshDozeState() } }
    fun stepIdleState() { viewModelScope.launch(Dispatchers.IO) { DozeModeUtils.stepIdleState(); refreshDozeState() } }
    fun resetStats() { viewModelScope.launch(Dispatchers.IO) { DozeModeUtils.resetDozeStats(); refreshDozeState() } }
}
