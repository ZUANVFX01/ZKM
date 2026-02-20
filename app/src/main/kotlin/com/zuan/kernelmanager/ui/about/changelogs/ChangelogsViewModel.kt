/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.about.changelogs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zuan.kernelmanager.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChangelogsUiState(
    val changelogs: List<ChangelogEntry> = emptyList(),
    val isLoading: Boolean = false,
    val selectedFilter: ChangeFilter = ChangeFilter.ALL,
    val expandedVersions: Set<String> = emptySet(),
    val remoteRelease: RemoteReleaseInfo? = null,
    val isCheckingUpdate: Boolean = false,
    val updateError: String? = null,
    val currentVersionCode: Int = 0,
    val sourceMode: UpdateSourceMode = UpdateSourceMode.GITHUB
)

enum class ChangeFilter {
    ALL,
    NEW_FEATURES,
    FIXES,
    IMPROVEMENTS,
    BETA_ONLY
}

enum class UpdateSourceMode {
    GITHUB,
    TELEGRAM
}

class ChangelogsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow(ChangelogsUiState())
    val uiState: StateFlow<ChangelogsUiState> = _uiState.asStateFlow()
    
    init {
        val versionCode = try {
            BuildConfig.VERSION_CODE
        } catch (e: Exception) {
            300
        }
        
        _uiState.value = _uiState.value.copy(currentVersionCode = versionCode)
        loadChangelogs()
        checkForUpdates()
    }
    
    private fun loadChangelogs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            kotlinx.coroutines.delay(400)
            _uiState.value = _uiState.value.copy(
                changelogs = ChangelogsUtils.getLocalChangelogs(),
                isLoading = false
            )
        }
    }
    
    fun checkForUpdates(mode: UpdateSourceMode? = null) {
        val targetMode = mode ?: _uiState.value.sourceMode
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCheckingUpdate = true, 
                updateError = null,
                sourceMode = targetMode
            )
            
            val result = when (targetMode) {
                UpdateSourceMode.GITHUB -> 
                    ChangelogsUtils.fetchLatestRelease(_uiState.value.currentVersionCode)
                UpdateSourceMode.TELEGRAM -> 
                    ChangelogsUtils.fetchTelegramLatestPost(_uiState.value.currentVersionCode)
            }
            
            result.onSuccess { release ->
                _uiState.value = _uiState.value.copy(
                    remoteRelease = release,
                    isCheckingUpdate = false
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    updateError = error.message,
                    isCheckingUpdate = false
                )
            }
        }
    }
    
    fun toggleSourceMode() {
        val newMode = if (_uiState.value.sourceMode == UpdateSourceMode.GITHUB) 
            UpdateSourceMode.TELEGRAM 
        else 
            UpdateSourceMode.GITHUB
        
        // Langsung check update dengan mode baru
        checkForUpdates(newMode)
    }
    
    fun setFilter(filter: ChangeFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }
    
    fun toggleVersionExpansion(version: String) {
        val current = _uiState.value.expandedVersions
        _uiState.value = _uiState.value.copy(
            expandedVersions = if (current.contains(version)) {
                current - version
            } else {
                current + version
            }
        )
    }
    
    fun expandAll() {
        _uiState.value = _uiState.value.copy(
            expandedVersions = _uiState.value.changelogs.map { it.version }.toSet()
        )
    }
    
    fun collapseAll() {
        _uiState.value = _uiState.value.copy(expandedVersions = emptySet())
    }
    
    fun getFilteredChangelogs(): List<ChangelogEntry> {
        val all = _uiState.value.changelogs
        return when (_uiState.value.selectedFilter) {
            ChangeFilter.ALL -> all
            ChangeFilter.NEW_FEATURES -> all.filter { entry -> 
                entry.changes.any { it.type == ChangeType.NEW }
            }
            ChangeFilter.FIXES -> all.filter { entry ->
                entry.changes.any { it.type == ChangeType.FIX }
            }
            ChangeFilter.IMPROVEMENTS -> all.filter { entry ->
                entry.changes.any { it.type == ChangeType.IMPROVEMENT }
            }
            ChangeFilter.BETA_ONLY -> all.filter { it.isBeta }
        }
    }
}
