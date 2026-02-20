/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.about.opensource

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OpenSourceUiState(
    val contributors: List<OpenSourceContributor> = emptyList(),
    val libraries: List<OpenSourceLibrary> = emptyList(),
    val isLoading: Boolean = false,
    val selectedTab: OpenSourceTab = OpenSourceTab.CONTRIBUTORS
)

enum class OpenSourceTab {
    CONTRIBUTORS,
    LIBRARIES
}

class OpenSourceViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(OpenSourceUiState())
    val uiState: StateFlow<OpenSourceUiState> = _uiState.asStateFlow()
    
    init {
        loadOpenSourceData()
    }
    
    private fun loadOpenSourceData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Simulate loading for smooth UX
            kotlinx.coroutines.delay(300)
            
            _uiState.value = _uiState.value.copy(
                contributors = OpenSourceUtils.getContributors(),
                libraries = OpenSourceUtils.getLibraries(),
                isLoading = false
            )
        }
    }
    
    fun selectTab(tab: OpenSourceTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }
    
    fun refreshData() {
        loadOpenSourceData()
    }
}
