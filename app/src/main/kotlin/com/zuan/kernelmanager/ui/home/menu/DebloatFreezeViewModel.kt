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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DebloatFreezeViewModel : ViewModel() {

    private val _allApps = MutableStateFlow<List<AppUiModel>>(emptyList())
    
    private val _searchQuery = MutableStateFlow("")
    // 0=All, 1=User, 2=System, 3=Frozen
    private val _selectedTab = MutableStateFlow(0) 
    private val _isLoading = MutableStateFlow(true)
    
    private val _totalAppsCount = MutableStateFlow(0)
    private val _systemAppsCount = MutableStateFlow(0)
    private val _userAppsCount = MutableStateFlow(0)
    private val _frozenAppsCount = MutableStateFlow(0) // [BARU]

    val isLoading: StateFlow<Boolean> = _isLoading
    val searchQuery: StateFlow<String> = _searchQuery
    val selectedTab: StateFlow<Int> = _selectedTab
    
    val totalAppsCount: StateFlow<Int> = _totalAppsCount
    val systemAppsCount: StateFlow<Int> = _systemAppsCount
    val userAppsCount: StateFlow<Int> = _userAppsCount
    val frozenAppsCount: StateFlow<Int> = _frozenAppsCount

    val filteredApps: StateFlow<List<AppUiModel>> = combine(_allApps, _searchQuery, _selectedTab) { apps, query, tab ->
        val tabFiltered = when (tab) {
            1 -> apps.filter { !it.isSystem } 
            2 -> apps.filter { it.isSystem }
            3 -> apps.filter { !it.isEnabled } // [BARU] Tab Frozen
            else -> apps 
        }

        if (query.isBlank()) {
            tabFiltered
        } else {
            tabFiltered.filter { 
                it.label.contains(query, ignoreCase = true) || 
                it.packageName.contains(query, ignoreCase = true) 
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadApps(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val apps = DebloatFreezeUtils.getInstalledApps(context)
            
            val sysCount = apps.count { it.isSystem }
            val frozenCount = apps.count { !it.isEnabled }
            val allCount = apps.size
            
            withContext(Dispatchers.Main) {
                _allApps.value = apps
                _totalAppsCount.value = allCount
                _systemAppsCount.value = sysCount
                _userAppsCount.value = allCount - sysCount
                _frozenAppsCount.value = frozenCount
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
    }

    fun toggleFreeze(context: Context, app: AppUiModel) {
        viewModelScope.launch(Dispatchers.IO) {
            val newState = !app.isEnabled
            val success = DebloatFreezeUtils.toggleAppState(app.packageName, newState)
            if (success) {
                loadApps(context)
            }
        }
    }

    fun debloatApp(context: Context, app: AppUiModel) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = DebloatFreezeUtils.debloatApp(app.packageName)
            if (success) {
                loadApps(context)
            }
        }
    }
    
    // [BARU] Wrapper untuk buka settings
    fun openAppSettings(context: Context, packageName: String) {
        DebloatFreezeUtils.openAppSystemSettings(context, packageName)
    }
}
