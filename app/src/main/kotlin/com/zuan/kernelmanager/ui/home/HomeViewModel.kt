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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Speed
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
    // Field baru: Build User
    val buildUser: String = "N/A", 
    // Field lama: CPU (Tetap ada karena mau ditampilkan di bawahnya)
    val cpu: String = "N/A",
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
            ExtensionItem("Terminal", "SYSTEM", Icons.Default.Code, ExtensionStatus.GOOD, NavigationRoute.Terminal.route),
            ExtensionItem("SetEdit", "SYSTEM", Icons.Default.Description, ExtensionStatus.WARNING, NavigationRoute.SetEdit.route),
            ExtensionItem("Performa", "PERFORMANCE", Icons.Default.Speed, ExtensionStatus.GOOD, NavigationRoute.Performance.route)
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

            // Ambil info Build User
            val bUser = Utils.getBuildUser()

            val info = _deviceInfo.value.copy(
                deviceName = Utils.getDeviceName(),
                deviceCodename = Utils.getDeviceCodename(),
                manufacturer = Utils.getManufacturer(),
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
                
                // Set kedua data
                buildUser = bUser, 
                cpu = SoCUtils.getCpuInfo(),
                
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
