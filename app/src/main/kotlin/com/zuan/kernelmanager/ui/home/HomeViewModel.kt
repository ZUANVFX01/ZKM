/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.home

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.navigation.NavigationRoute
import com.zuan.kernelmanager.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// --- DATA CLASSES ---

data class ExtensionItem(
    @StringRes val nameRes: Int,
    val type: String,
    @StringRes val descriptionRes: Int,
    val icon: ImageVector, 
    val route: String
)

data class GithubRelease(
    val tag_name: String,
    val body: String,
    val assets: List<GithubAsset>
)

data class GithubAsset(val browser_download_url: String)

data class DeviceInfo(
    val deviceName: String = "Xiaomi",
    val manufacturer: String = "Xiaomi",
    val kernelVersion: String = "Unknown Kernel",
    val androidVersion: String = "12"
)

class HomeViewModel : ViewModel() {

    private val _deviceInfo = MutableStateFlow(DeviceInfo())
    val deviceInfo: StateFlow<DeviceInfo> = _deviceInfo.asStateFlow()

    private val _allExtensions = MutableStateFlow<List<ExtensionItem>>(emptyList())
    
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val filteredExtensions = combine(_allExtensions, _selectedCategory) { list, category ->
        if (category == "All") list else list.filter { it.type.equals(category, ignoreCase = true) }
    }

    var updateRelease by mutableStateOf<GithubRelease?>(null)
    private val repoOwner = "ZUANVFX01"
    private val repoName = "ZKM"

    init {
        loadExtensions()
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    private fun loadExtensions() {
        _allExtensions.value = listOf(
            // --- SYSTEM ---
            ExtensionItem(
                nameRes = R.string.ext_terminal,
                type = "System",
                descriptionRes = R.string.desc_terminal,
                icon = Icons.Rounded.Terminal,
                route = NavigationRoute.Terminal.route
            ),
            ExtensionItem(
                nameRes = R.string.ext_setedit,
                type = "System",
                descriptionRes = R.string.desc_setedit,
                icon = Icons.Rounded.EditNote,
                route = NavigationRoute.SetEdit.route
            ),
            ExtensionItem(
                nameRes = R.string.ext_flasher,
                type = "System",
                descriptionRes = R.string.desc_flasher,
                icon = Icons.Rounded.SystemUpdate,
                route = NavigationRoute.KernelFlasher.route
            ),
            ExtensionItem(
                nameRes = R.string.ext_launcher,
                type = "System",
                descriptionRes = R.string.desc_launcher,
                icon = Icons.Rounded.RocketLaunch,
                route = NavigationRoute.ActivityLauncher.route
            ),
            
            // Menu DoD
            ExtensionItem(
                nameRes = R.string.ext_dod,
                type = "System",
                descriptionRes = R.string.desc_dod,
                icon = Icons.Rounded.AppBlocking,
                route = NavigationRoute.DebloatFreeze.route
            ),

            // Menu App Compiler (Dex2oat)
            ExtensionItem(
                nameRes = R.string.ext_compiler,
                type = "System",
                descriptionRes = R.string.desc_compiler,
                icon = Icons.Rounded.Build,
                route = NavigationRoute.Dex2oat.route
            ),

            // Menu Thermal Devices
            ExtensionItem(
                nameRes = R.string.ext_thermal,
                type = "System",
                descriptionRes = R.string.desc_thermal,
                icon = Icons.Rounded.Thermostat,
                route = NavigationRoute.ThermalDevices.route
            ),

            // Menu Display
            ExtensionItem(
                nameRes = R.string.ext_display,
                type = "System",
                descriptionRes = R.string.desc_display,
                icon = Icons.Rounded.DisplaySettings,
                route = NavigationRoute.Display.route
            ),

            // Menu Battery Controller
            ExtensionItem(
                nameRes = R.string.ext_battery,
                type = "System",
                descriptionRes = R.string.desc_battery,
                icon = Icons.Rounded.BatteryChargingFull,
                route = NavigationRoute.BatteryController.route
            ),

            // Menu Doze Mode
            ExtensionItem(
                nameRes = R.string.ext_dozemode,
                type = "System",
                descriptionRes = R.string.desc_dozemode,
                icon = Icons.Rounded.Bedtime,
                route = NavigationRoute.DozeMode.route
            ),

            // --- MONITOR ---
            ExtensionItem(
                nameRes = R.string.ext_logs,
                type = "Monitor",
                descriptionRes = R.string.desc_logs,
                icon = Icons.Rounded.Description, 
                route = NavigationRoute.LogsView.route
            ),
            ExtensionItem(
                nameRes = R.string.ext_fps,
                type = "Monitor",
                descriptionRes = R.string.desc_fps,
                icon = Icons.Rounded.Speed,
                route = NavigationRoute.FpsManager.route
            ),
            ExtensionItem(
                nameRes = R.string.ext_process,
                type = "Monitor",
                descriptionRes = R.string.desc_process,
                icon = Icons.Rounded.Memory,
                route = NavigationRoute.ProcessManager.route
            ),

            // --- ROOT ---
            ExtensionItem(
                nameRes = R.string.ext_ksuweb,
                type = "Root",
                descriptionRes = R.string.desc_ksuweb,
                icon = Icons.Rounded.Public,
                route = NavigationRoute.KsuWebUI.route
            )
        )
    }

    fun loadDeviceInfo(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val info = DeviceInfo(
                deviceName = Utils.getDeviceName(),
                manufacturer = Utils.getManufacturer(),
                kernelVersion = Utils.getKernelVersion(),
                androidVersion = Utils.getAndroidVersion()
            )
            _deviceInfo.value = info
            
            val currentVersion = Utils.getAppVersionName(context)
            checkForUpdate(currentVersion)
        }
    }

    fun checkForUpdate(currentVersionName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://api.github.com/repos/$repoOwner/$repoName/releases/latest")
                val connection = url.openConnection() as HttpURLConnection
                if (connection.responseCode == 200) {
                    val json = JSONObject(connection.inputStream.bufferedReader().readText())
                    val tagName = json.getString("tag_name")
                    val body = json.getString("body")
                    val assets = json.getJSONArray("assets")
                    val downloadUrl = if (assets.length() > 0) assets.getJSONObject(0).getString("browser_download_url") else ""

                    if (tagName != currentVersionName && downloadUrl.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            updateRelease = GithubRelease(tagName, body, listOf(GithubAsset(downloadUrl)))
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}
