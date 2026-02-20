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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class Dex2oatViewModel : ViewModel() {

    private val _appList = MutableStateFlow<List<AppCompileItem>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(true)
    
    // String ini akan muncul di UI memberi tahu user aplikasi apa yang sedang diproses
    private val _processingStatus = MutableStateFlow<String?>(null) 
    
    private val _selectedMode = MutableStateFlow("speed-profile")

    val isLoading: StateFlow<Boolean> = _isLoading
    val processingStatus: StateFlow<String?> = _processingStatus
    val selectedMode: StateFlow<String> = _selectedMode
    val searchQuery: StateFlow<String> = _searchQuery
    
    val filteredApps = combine(_appList, _searchQuery) { apps, query ->
        if (query.isBlank()) apps else apps.filter { 
            it.label.contains(query, true) || it.packageName.contains(query, true) 
        }
    }

    fun loadApps(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val apps = Dex2oatUtils.getInstalledApps(context)
            _appList.value = apps
            _isLoading.value = false
        }
    }

    fun onSearch(query: String) {
        _searchQuery.value = query
    }

    fun onModeSelect(mode: String) {
        _selectedMode.value = mode
    }

    fun compileApp(app: AppCompileItem) {
        viewModelScope.launch(Dispatchers.IO) {
            _processingStatus.value = "Compiling ${app.label}..."
            Dex2oatUtils.compileApp(app.packageName, _selectedMode.value)
            _processingStatus.value = null
        }
    }

    fun resetApp(app: AppCompileItem) {
        viewModelScope.launch(Dispatchers.IO) {
            _processingStatus.value = "Resetting ${app.label}..."
            Dex2oatUtils.resetApp(app.packageName)
            _processingStatus.value = null
        }
    }
    
    fun compileAll() {
        viewModelScope.launch(Dispatchers.IO) {
            _processingStatus.value = "Compiling ALL APPS (This may take a while)..."
            Dex2oatUtils.compileAll(_selectedMode.value)
            _processingStatus.value = null
        }
    }

    // [BARU] Compile hanya System Apps
    fun compileSystemApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val systemApps = _appList.value.filter { it.isSystem }
            val total = systemApps.size
            
            systemApps.forEachIndexed { index, app ->
                _processingStatus.value = "System Apps (${index + 1}/$total): ${app.label}"
                Dex2oatUtils.compileApp(app.packageName, _selectedMode.value)
            }
            _processingStatus.value = null
        }
    }

    // [BARU] Compile hanya User/Third-Party Apps
    fun compileUserApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val userApps = _appList.value.filter { !it.isSystem }
            val total = userApps.size
            
            userApps.forEachIndexed { index, app ->
                _processingStatus.value = "User Apps (${index + 1}/$total): ${app.label}"
                Dex2oatUtils.compileApp(app.packageName, _selectedMode.value)
            }
            _processingStatus.value = null
        }
    }

    // [BARU] Reset Semua
    fun resetAllApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _processingStatus.value = "Resetting ALL apps..."
            Dex2oatUtils.resetAll()
            _processingStatus.value = null
        }
    }
}
