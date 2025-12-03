/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * All Rights Reserved.
 */
 /*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.ui.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zuan.kernelmanager.service.SmartCutoffService
import com.zuan.kernelmanager.service.BatteryMonitorService
import com.zuan.kernelmanager.utils.BatteryUtils
import com.zuan.kernelmanager.utils.KernelUtils
import com.zuan.kernelmanager.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

class BatteryViewModel : ViewModel() {
    data class BatteryInfo(
        val level: String = "N/A",
        val tech: String = "N/A",
        val health: String = "N/A",
        val temp: String = "N/A",
        val voltage: String = "N/A",
        val deepSleep: String = "N/A",
        val designCapacity: String = "N/A",
        val manualDesignCapacity: Int = 0,
        val maximumCapacity: String = "N/A",
        // Realtime Stats
        val status: String = "Discharging",
        val currentNow: Int = 0, // mA
        val currentMin: Int = 0, // mA
        val currentMax: Int = 0, // mA
        val wattage: Float = 0f,  // Watt
        val cycleCount: String = "N/A", 
        val chargeType: String = "N/A",
        val calculatedHealth: String = "N/A" 
    )

    data class ChargingState(
        val hasFastCharging: Boolean = false,
        val isFastChargingChecked: Boolean = false,
        val kernelVersion: String = "N/A",
        val isBypassChargingChecked: Boolean = false,
    )

    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo

    private val _chargingState = MutableStateFlow(ChargingState())
    val chargingState: StateFlow<ChargingState> = _chargingState

    private val _thermalSconfig = MutableStateFlow("")
    val thermalSconfig: StateFlow<String> = _thermalSconfig

    private val _hasThermalSconfig = MutableStateFlow(false)
    val hasThermalSconfig: StateFlow<Boolean> = _hasThermalSconfig

    private val _uptime = MutableStateFlow("N/A")
    val uptime: StateFlow<String> = _uptime

    // --- SMART CUT-OFF STATE ---
    private val _smartCutoffEnabled = MutableStateFlow(false)
    val smartCutoffEnabled: StateFlow<Boolean> = _smartCutoffEnabled

    private val _cutoffLimit = MutableStateFlow(80f)
    val cutoffLimit: StateFlow<Float> = _cutoffLimit
    
    // --- MONITOR STATE ---
    private val _monitorEnabled = MutableStateFlow(false)
    val monitorEnabled: StateFlow<Boolean> = _monitorEnabled

    private var levelReceiver: BroadcastReceiver? = null
    private var tempReceiver: BroadcastReceiver? = null
    private var voltageReceiver: BroadcastReceiver? = null
    private var maxCapacityReceiver: BroadcastReceiver? = null

    private var job: Job? = null
    
    private var sessionMinCurrent = Int.MAX_VALUE
    private var sessionMaxCurrent = Int.MIN_VALUE

    fun initializeBatteryInfo(context: Context) {
        loadBatteryInfo(context)
        loadThermalSconfig()
        registerBatteryListeners(context)
        checkChargingFiles()
        
        loadSmartCutoffState(context)
        loadMonitorState(context)
    }

    private fun loadSmartCutoffState(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = BatteryPreference.getInstance(context)
            val savedLimit = prefs.getCutoffLimit()
            _cutoffLimit.value = savedLimit
            val isRunning = Utils.isServiceRunning(context, SmartCutoffService::class.java)
            _smartCutoffEnabled.value = isRunning
        }
    }
    
    // UPDATE: Load Monitor State dari Prefs juga
    private fun loadMonitorState(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = BatteryPreference.getInstance(context)
            val savedState = prefs.isMonitorEnabled()
            val isRunning = Utils.isServiceRunning(context, BatteryMonitorService::class.java)
            
            // Sinkronisasi: Jika prefs TRUE tapi service mati (kena kill), hidupkan lagi
            if (savedState && !isRunning) {
                toggleMonitor(context, true)
            }
            
            _monitorEnabled.value = savedState
        }
    }

    fun loadBatteryInfo(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val batteryPreference = BatteryPreference.getInstance(context)

                val level = BatteryUtils.getBatteryLevel(context)
                val tech = BatteryUtils.getBatteryTechnology(context)
                val health = BatteryUtils.getBatteryHealth(context)
                val designCapacity = BatteryUtils.getBatteryDesignCapacity()
                val manualDesignCapacity = batteryPreference.getManualDesignCapacity().toString()
                val deepSleep = BatteryUtils.getDeepSleep()
                val status = BatteryUtils.getChargingStatus(context)
                val cycleCount = BatteryUtils.getCycleCount()
                val chargeType = BatteryUtils.getChargeType()
                val calculatedHealth = BatteryUtils.getCalculatedHealth()

                _batteryInfo.value = _batteryInfo.value.copy(
                    level = level,
                    tech = tech,
                    health = health,
                    designCapacity = designCapacity,
                    manualDesignCapacity = manualDesignCapacity.toInt(),
                    deepSleep = deepSleep,
                    status = status,
                    cycleCount = cycleCount,
                    chargeType = chargeType,
                    calculatedHealth = calculatedHealth
                )
            } catch (e: Exception) {
                Log.e("BatteryVM", "Error loading battery info", e)
            }
        }
    }
    
    // ... (Fungsi setManualDesignCapacity, loadThermalSconfig, startJob, stopJob, listener, dll SAMA) ...
    // Biar gak kepanjangan saya skip bagian tengah yang tidak berubah
    // Langsung ke fungsi Toggle Monitor yang diupdate

    fun setManualDesignCapacity(context: Context, value: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val batteryPreference = BatteryPreference.getInstance(context)
            batteryPreference.setManualDesignCapacity(value)

            _batteryInfo.value = _batteryInfo.value.copy(
                manualDesignCapacity = value,
            )
        }
    }

    fun loadThermalSconfig() {
        viewModelScope.launch(Dispatchers.IO) {
            _thermalSconfig.value = Utils.readFile(BatteryUtils.THERMAL_SCONFIG)
            _hasThermalSconfig.value = Utils.testFile(BatteryUtils.THERMAL_SCONFIG)
        }
    }

    fun startJob() {
        job?.cancel()
        sessionMinCurrent = Int.MAX_VALUE
        sessionMaxCurrent = Int.MIN_VALUE

        job = viewModelScope.launch {
            while (isActive) {
                _uptime.value = BatteryUtils.getUptime()

                val currentRaw = BatteryUtils.getBatteryCurrentNow()
                val currentAbs = abs(currentRaw) 
                
                if (sessionMinCurrent == Int.MAX_VALUE || currentAbs < sessionMinCurrent) {
                    sessionMinCurrent = currentAbs
                }
                if (sessionMaxCurrent == Int.MIN_VALUE || currentAbs > sessionMaxCurrent) {
                    sessionMaxCurrent = currentAbs
                }

                val voltageStr = _batteryInfo.value.voltage.replace(Regex("[^0-9.]"), "")
                val voltageVal = voltageStr.toFloatOrNull() ?: 0f
                val voltageVolts = if (voltageVal > 100) voltageVal / 1000f else voltageVal
                val watts = voltageVolts * (currentAbs / 1000f)

                _batteryInfo.value = _batteryInfo.value.copy(
                    currentNow = currentRaw, 
                    currentMin = if(sessionMinCurrent == Int.MAX_VALUE) 0 else sessionMinCurrent,
                    currentMax = if(sessionMaxCurrent == Int.MIN_VALUE) 0 else sessionMaxCurrent,
                    wattage = watts
                )

                delay(1000L)
            }
        }
    }

    fun stopJob() {
        job?.cancel()
        job = null
    }

    fun registerBatteryListeners(context: Context) {
        viewModelScope.launch(Dispatchers.Main) {
            levelReceiver = BatteryUtils.registerBatteryLevelListener(context) { level ->
                val status = BatteryUtils.getChargingStatus(context)
                _batteryInfo.value = _batteryInfo.value.copy(level = level, status = status)
            }
            tempReceiver = BatteryUtils.registerBatteryTemperatureListener(context) { temp ->
                _batteryInfo.value = _batteryInfo.value.copy(temp = temp)
            }
            voltageReceiver = BatteryUtils.registerBatteryVoltageListener(context) { voltage ->
                _batteryInfo.value = _batteryInfo.value.copy(voltage = voltage)
            }
            maxCapacityReceiver = BatteryUtils.registerBatteryCapacityListener(context) { maxCapacity ->
                _batteryInfo.value = _batteryInfo.value.copy(maximumCapacity = maxCapacity)
            }
        }
    }

    fun unregisterBatteryListeners(context: Context) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                levelReceiver?.let { context.unregisterReceiver(it) }
                tempReceiver?.let { context.unregisterReceiver(it) }
                voltageReceiver?.let { context.unregisterReceiver(it) }
                maxCapacityReceiver?.let { context.unregisterReceiver(it) }
            } catch (e: Exception) {
                Log.e("BatteryVM", "Error unregistering receivers", e)
            }
            levelReceiver = null
            tempReceiver = null
            voltageReceiver = null
            maxCapacityReceiver = null
        }
    }

    fun checkChargingFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            val hasFastCharging = Utils.testFile(BatteryUtils.FAST_CHARGING)
            val isFastChargingChecked = Utils.readFile(BatteryUtils.FAST_CHARGING) == "1"
            val kernelVersion = KernelUtils.getKernelVersion()
            val isBypassChargingChecked = Utils.readFile(BatteryUtils.BYPASS_CHARGING) == "1"

            _chargingState.value = ChargingState(
                hasFastCharging = hasFastCharging,
                isFastChargingChecked = isFastChargingChecked,
                kernelVersion = kernelVersion,
                isBypassChargingChecked = isBypassChargingChecked,
            )
        }
    }

    fun updateCharging(filePath: String, checked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = Utils.writeFile(filePath, if (checked) "1" else "0")
            if (success) {
                when (filePath) {
                    BatteryUtils.FAST_CHARGING -> _chargingState.value = _chargingState.value.copy(isFastChargingChecked = checked)
                    BatteryUtils.BYPASS_CHARGING -> _chargingState.value = _chargingState.value.copy(isBypassChargingChecked = checked)
                }
            }
        }
    }

    fun updateThermalSconfig(value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Utils.setPermissions(644, BatteryUtils.THERMAL_SCONFIG)
            Utils.writeFile(BatteryUtils.THERMAL_SCONFIG, value)
            Utils.setPermissions(444, BatteryUtils.THERMAL_SCONFIG)
            _thermalSconfig.value = value
        }
    }

    fun toggleSmartCutoff(context: Context, enable: Boolean) {
        _smartCutoffEnabled.value = enable
        val intent = Intent(context, SmartCutoffService::class.java)
        if (enable) {
            intent.putExtra(SmartCutoffService.EXTRA_LIMIT, _cutoffLimit.value.toInt())
            context.startForegroundService(intent) 
        } else {
            intent.action = SmartCutoffService.ACTION_STOP_SERVICE
            context.startService(intent)
        }
    }

    fun setCutoffLimit(context: Context, value: Float) {
        _cutoffLimit.value = value
        viewModelScope.launch(Dispatchers.IO) {
            BatteryPreference.getInstance(context).setCutoffLimit(value)
        }
        if (_smartCutoffEnabled.value) {
            val intent = Intent(context, SmartCutoffService::class.java)
            intent.action = SmartCutoffService.ACTION_UPDATE_LIMIT
            intent.putExtra(SmartCutoffService.EXTRA_LIMIT, value.toInt())
            context.startService(intent)
        }
    }

    fun manualCutCharge() {
        BatteryUtils.setChargingEnabled(false)
    }

    fun manualResumeCharge(context: Context) {
        BatteryUtils.setChargingEnabled(true)
        if (_smartCutoffEnabled.value) {
            toggleSmartCutoff(context, false)
        }
    }
    
    // --- MONITOR FUNCTIONS (UPDATED) ---
    fun toggleMonitor(context: Context, enable: Boolean) {
        _monitorEnabled.value = enable
        
        // Simpan state ke preference
        viewModelScope.launch(Dispatchers.IO) {
            BatteryPreference.getInstance(context).setMonitorEnabled(enable)
        }

        val intent = Intent(context, BatteryMonitorService::class.java)
        if (enable) {
            context.startForegroundService(intent)
        } else {
            intent.action = BatteryMonitorService.ACTION_STOP
            context.startService(intent)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
        stopJob()
    }
}
