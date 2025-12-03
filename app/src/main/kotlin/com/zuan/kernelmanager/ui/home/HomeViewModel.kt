/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * All Rights Reserved.
 */
 /*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.ui.home

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zuan.kernelmanager.ui.navigation.NavigationRoute
import com.zuan.kernelmanager.utils.KernelUtils
import com.zuan.kernelmanager.utils.SoCUtils
import com.zuan.kernelmanager.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ExtensionItem(
    val name: String,
    val type: String,
    val imageVector: ImageVector,
    val status: ExtensionStatus,
    val route: String
)

enum class ExtensionStatus { GOOD, WARNING }

data class DeviceInfo(
    val deviceName: String = "N/A",
    val deviceCodename: String = "N/A",
    val manufacturer: String = "N/A",
    val ramInfo: String = "N/A",
    val ramTotalText: String = "N/A",
    val ramAvailableText: String = "N/A",
    val ramUsageProgress: Float = 0f,
    val zram: String = "N/A",
    
    // --- UPDATED: Menambahkan Build Tags ---
    val buildTags: String = "N/A",
    // ---------------------------------------

    val buildUserLabel: String = "CPU", 
    val cpuModel: String = "N/A",
    
    val gpuModel: String = "N/A",
    val gpuVendor: String = "N/A",
    val gpuGlVersion: String = "N/A",
    val androidVersionNumber: String = "N/A",
    val androidVersionTrend: String = "",
    val sdkVersion: String = "N/A",
    val hasWireGuard: Boolean = false,
    val wireGuard: String = "N/A",
    val kernelVersion: String = "N/A",
    val fullKernelVersion: String = "N/A",
    val selinuxStatus: String = "N/A"
)

class HomeViewModel : ViewModel() {

    private val _deviceInfo = MutableStateFlow(DeviceInfo())
    val deviceInfo: StateFlow<DeviceInfo> = _deviceInfo.asStateFlow()

    private val _extensionList = MutableStateFlow<List<ExtensionItem>>(emptyList())
    val extensionList: StateFlow<List<ExtensionItem>> = _extensionList.asStateFlow()

    init {
        loadExtensions()
    }

    private fun loadExtensions() {
        _extensionList.value = listOf(
            ExtensionItem("FastShell", "SYSTEM", Icons.Default.Code, ExtensionStatus.GOOD, NavigationRoute.FastShell.route),
            ExtensionItem("SetEdit", "SYSTEM", Icons.Default.Description, ExtensionStatus.WARNING, NavigationRoute.SetEdit.route),
            ExtensionItem("FpsManager", "PERFORMANCE", Icons.Default.BarChart, ExtensionStatus.GOOD, NavigationRoute.FpsManager.route),
            ExtensionItem("ProcessManager", "MONITORING", Icons.Default.Assessment, ExtensionStatus.GOOD, NavigationRoute.ProcessManager.route)
        )
    }

    fun loadDeviceInfo(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val (vendor, renderer, version) = SoCUtils.getGpuInfo()
            
            val rawAndroidVer = Utils.getAndroidVersion()
            val verNumber = rawAndroidVer.split(" ").firstOrNull() ?: rawAndroidVer
            val verTrend = rawAndroidVer.split(" ").getOrNull(1) ?: ""

            val currentSelinux = Utils.getSelinuxStatus()
            val ramStats = Utils.getRamStatus(context)

            val dynamicLabel = Utils.getBuildUser()
            val cpuName = SoCUtils.getCpuInfo()
            
            // --- LOAD TAGS ---
            val tags = Utils.getBuildTags()

            val info = _deviceInfo.value.copy(
                deviceName = Utils.getDeviceName(),
                deviceCodename = Utils.getDeviceCodename(),
                manufacturer = Utils.getManufacturer(),
                
                // --- ASSIGN TAGS ---
                buildTags = tags,
                // -------------------
                
                ramInfo = Utils.getTotalRam(context),
                ramTotalText = ramStats.totalString,
                ramAvailableText = ramStats.availString,
                ramUsageProgress = ramStats.usagePercent,
                zram = KernelUtils.getZramSize(),
                gpuVendor = vendor,
                gpuModel = renderer,
                gpuGlVersion = version,
                androidVersionNumber = verNumber,
                androidVersionTrend = verTrend,
                sdkVersion = Utils.getSdkVersion(),
                
                buildUserLabel = dynamicLabel,
                cpuModel = cpuName,
                
                hasWireGuard = Utils.testFile(KernelUtils.WIREGUARD_VERSION),
                wireGuard = KernelUtils.getWireGuardVersion(),
                kernelVersion = KernelUtils.getKernelVersion(),
                fullKernelVersion = KernelUtils.getFullKernelVersion(),
                selinuxStatus = currentSelinux
            )
            _deviceInfo.value = info
        }
    }

    fun startRamMonitor(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val ramStats = Utils.getRamStatus(context)
                _deviceInfo.value = _deviceInfo.value.copy(
                    ramAvailableText = ramStats.availString,
                    ramUsageProgress = ramStats.usagePercent
                )
                delay(2000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}
