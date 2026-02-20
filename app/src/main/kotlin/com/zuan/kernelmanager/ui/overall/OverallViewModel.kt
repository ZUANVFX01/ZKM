/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.overall

import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zuan.kernelmanager.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.abs

// --- Data Classes ---
data class GithubRelease(
    val tag_name: String,
    val body: String,
    val assets: List<GithubAsset>
)

data class GithubAsset(
    val browser_download_url: String
)

data class DashboardState(
    // Update Info
    val updateRelease: GithubRelease? = null,

    // Device Info
    val deviceModel: String = "Loading...",
    val deviceCodename: String = "...",
    val socName: String = "Loading...",
    val kernelVersion: String = "Loading...",

    // Memory
    val memTotal: String = "0 GB",
    val memUsed: String = "0 GB",
    val memFree: String = "0 GB",
    val ramProgress: Float = 0f,
    val ramTextSimple: String = "0/0",

    val swapTotal: String = "0 GB",
    val swapUsed: String = "0 GB",
    val swapFree: String = "0 GB",
    val swapProgress: Float = 0f,
    val swapText: String = "0/0",
    val zramAlgorithm: String = "Unknown",

    // NEW: VM Parameters
    val vmParameters: VMParameters = VMParameters(),

    // NEW: Display Info
    val displayInfo: DisplayInfo = DisplayInfo(),

    // GPU
    val gpuFreq: String = "0 MHz",
    val gpuLoad: Int = 0,
    val gpuDetail: String = "Loading...",

    // Battery Old (Compatibility)
    val batCurrent: String = "0mA",
    val batVolt: String = "0mV",
    val batTemp: String = "0°C",

    // Battery NEW (Detailed)
    val batteryInfo: OverallBatteryInfo = OverallBatteryInfo(),
    val batteryHistory: List<Int> = List(20) { 0 },

    // CPU & Processes
    val processList: List<ProcessInfo> = emptyList(),
    val uptime: String = "0s",
    val awakeTime: String = "0s",
    val deepSleepTime: String = "0s",
    val cpuFreqs: List<String> = emptyList(),
    val cpuHistory: List<Float> = listOf(0.1f, 0.2f, 0.1f, 0.3f, 0.2f, 0.1f, 0.1f), 
    val currentCpuLoad: String = "0%",
    val perCoreLoads: List<Int> = emptyList(),
    
    // [NEW] Selected Core Detail (untuk Dialog)
    val selectedCoreInfo: CoreDetailInfo? = null,
    val isLoadingCoreDetail: Boolean = false
)

class OverallViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardState())
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    fun start(currentVersion: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val model = OverallUtils.getDeviceModel()
            val codename = OverallUtils.getDeviceCodename()
            val soc = OverallUtils.getSoCName()
            val kernel = OverallUtils.getKernelVersion()
            val gpuInfo = OverallUtils.getGpuDetail()
            val vmParams = OverallUtils.getVMParameters()
            val displayInfo = OverallUtils.getDisplayInfo(context)

            _uiState.update { 
                it.copy(
                    deviceModel = model, 
                    deviceCodename = codename, 
                    socName = soc, 
                    kernelVersion = kernel,
                    gpuDetail = gpuInfo,
                    vmParameters = vmParams,
                    displayInfo = displayInfo
                ) 
            }
            
            checkUpdate(currentVersion)
            startMonitoringLoop(context)
        }
    }

    // [NEW] Load details ketika core diklik
    fun loadCoreDetail(coreIndex: Int) {
        _uiState.update { it.copy(isLoadingCoreDetail = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val gov = OverallUtils.getCpuGovernor(coreIndex)
            val tunables = OverallUtils.getGovernorTunables(coreIndex, gov)
            
            val detail = CoreDetailInfo(
                coreIndex = coreIndex,
                governor = gov,
                tunables = tunables
            )
            
            _uiState.update { 
                it.copy(
                    selectedCoreInfo = detail,
                    isLoadingCoreDetail = false
                ) 
            }
        }
    }

    // [NEW] Tutup dialog
    fun dismissCoreDetail() {
        _uiState.update { it.copy(selectedCoreInfo = null) }
    }

    private suspend fun startMonitoringLoop(context: Context) {
        while (currentCoroutineContext().isActive) {
            val mem = OverallUtils.getMemoryInfo()
            val ramTotalStr = String.format("%.1f GB", mem.totalMem / 1024f / 1024f)
            val ramUsedStr = String.format("%.1f GB", mem.usedMem / 1024f / 1024f)
            val ramFreeStr = String.format("%.1f GB", mem.freeMem / 1024f / 1024f)
            val ramProgressCalc = if (mem.totalMem > 0) mem.usedMem.toFloat() / mem.totalMem.toFloat() else 0f
            val ramSimpleStr = "${(mem.usedMem/1024/1024)}GB / ${(mem.totalMem/1024/1024)}GB"

            val swapTotalStr = String.format("%.1f GB", mem.totalSwap / 1024f / 1024f)
            val swapUsedStr = String.format("%.1f GB", mem.usedSwap / 1024f / 1024f)
            val swapFreeStr = String.format("%.1f GB", mem.freeSwap / 1024f / 1024f)
            val swapProgressCalc = if (mem.totalSwap > 0) mem.usedSwap.toFloat() / mem.totalSwap.toFloat() else 0f
            val swapSimpleStr = "${(mem.usedSwap/1024/1024)}GB / ${(mem.totalSwap/1024/1024)}GB"

            val gpu = OverallUtils.getGpuLoadAndFreq()
            val batOld = OverallUtils.getBatteryInfoOld()
            val batNew = OverallUtils.getDetailedBatteryInfo()
            val processes = OverallUtils.getTopProcesses(context)
            
            val uptimeStr = formatDuration(SystemClock.elapsedRealtime())
            
            val upMillis = SystemClock.elapsedRealtime()
            val deepSleepMillis = (SystemClock.elapsedRealtime() - SystemClock.uptimeMillis())
            val awakeMillis = upMillis - deepSleepMillis
            
            val awakeStr = formatDuration(awakeMillis)
            val deepSleepStr = formatDuration(deepSleepMillis)

            val cpus = OverallUtils.getCpuFrequencies()
            val totalLoadPercent = OverallUtils.getCpuTotalLoad()
            
            val oldHistory = _uiState.value.cpuHistory.toMutableList()
            if (oldHistory.size > 20) oldHistory.removeAt(0)
            oldHistory.add(totalLoadPercent)
            val newHistory = oldHistory.toList()
            
            val newCoreLoads = OverallUtils.getPerCoreLoad()

            _uiState.update { currentState ->
                
                val currentHistory = currentState.batteryHistory.toMutableList()
                if (currentHistory.size >= 20) {
                    currentHistory.removeAt(0)
                }
                currentHistory.add(abs(batNew.currentNow))

                currentState.copy(
                    memTotal = ramTotalStr,
                    memUsed = ramUsedStr,
                    memFree = ramFreeStr,
                    ramProgress = ramProgressCalc,
                    ramTextSimple = ramSimpleStr,
                    
                    swapTotal = swapTotalStr,
                    swapUsed = swapUsedStr,
                    swapFree = swapFreeStr,
                    swapText = swapSimpleStr,
                    swapProgress = swapProgressCalc,
                    zramAlgorithm = mem.zramAlgorithm,

                    gpuFreq = gpu.first,
                    gpuLoad = gpu.second,
                    
                    batCurrent = batOld.first,
                    batVolt = batOld.second,
                    batTemp = batOld.third,
                    
                    batteryInfo = batNew, 
                    batteryHistory = currentHistory, 

                    processList = processes,
                    uptime = uptimeStr,
                    awakeTime = awakeStr,
                    deepSleepTime = deepSleepStr,
                    cpuFreqs = cpus,
                    cpuHistory = newHistory,
                    currentCpuLoad = "${(totalLoadPercent * 100).toInt()}%",
                    perCoreLoads = newCoreLoads
                )
            }
            delay(2000)
        }
    }
    
    private fun formatDuration(millis: Long): String {
        val seconds = millis / 1000
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        return String.format(Locale.US, "%02dh %02dm", h, m)
    }
    
    private fun checkUpdate(currentVer: String) {
         viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://api.github.com/repos/ZuanVfx/KernelManagerX/releases/latest")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("User-Agent", "KotlinApp")
                
                if (conn.responseCode == 200) {
                    val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(jsonStr)
                    val tagName = json.getString("tag_name")
                    val body = json.getString("body")
                    val assetsJson = json.getJSONArray("assets")
                    val assets = mutableListOf<GithubAsset>()
                    for (i in 0 until assetsJson.length()) {
                        val assetObj = assetsJson.getJSONObject(i)
                        assets.add(GithubAsset(assetObj.getString("browser_download_url")))
                    }
                    
                    if (tagName != currentVer) {
                        val release = GithubRelease(tagName, body, assets)
                         _uiState.update { it.copy(updateRelease = release) }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
