/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * All Rights Reserved.
 */
 /*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.SystemClock
import android.util.Log
import com.zuan.kernelmanager.ui.battery.BatteryPreference
import com.topjohnwu.superuser.Shell
import kotlin.math.roundToInt
import kotlin.math.abs

object BatteryUtils {

    const val FAST_CHARGING = "/sys/kernel/fast_charge/force_fast_charge"
    const val BYPASS_CHARGING = "/sys/class/power_supply/battery/input_suspend"
    const val BATTERY_DESIGN_CAPACITY = "/sys/class/power_supply/battery/charge_full_design"
    const val BATTERY_MAXIMUM_CAPACITY = "/sys/class/power_supply/battery/charge_full"
    const val BATTERY_TECHNOLOGY = "/sys/class/power_supply/battery/technology"
    
    // NEW PATHS (Updated)
    const val BATTERY_CURRENT_NOW = "/sys/class/power_supply/battery/current_now"
    const val BATTERY_CURRENT_AVG = "/sys/class/power_supply/battery/current_avg" 
    const val BMS_CURRENT_NOW = "/sys/class/power_supply/bms/current_now" 
    
    const val BATTERY_STATUS = "/sys/class/power_supply/battery/status"
    const val BATTERY_CYCLE_COUNT = "/sys/class/power_supply/battery/cycle_count"
    const val BATTERY_CHARGE_TYPE = "/sys/class/power_supply/battery/charge_type"

    const val THERMAL_SCONFIG = "/sys/class/thermal/thermal_message/sconfig"

    const val TAG = "BatteryUtils"

    // --- SMART CUT-OFF SCRIPTS ---
    private const val SCRIPT_DISABLE_CHARGING = """
        echo "1" > /sys/class/power_supply/battery/batt_slate_mode
        echo "1" > /sys/class/power_supply/battery/battery_input_suspend
        echo "1" > /sys/class/power_supply/battery/bd_trickle_cnt
        echo "0" > /sys/class/power_supply/battery/device/Charging_Enable
        echo "0" > /sys/class/power_supply/battery/charging_enabled
        echo "1" > /sys/class/power_supply/battery/op_disable_charge
        echo "1" > /sys/class/power_supply/battery/store_mode
        echo "1" > /sys/class/power_supply/battery/test_mode
        echo "1" > /sys/class/power_supply/battery/battery_ext/smart_charging_interruption
        echo "0" > /sys/class/power_supply/battery/siop_level
        echo "0" > /sys/class/power_supply/battery/battery_charging_enabled
        echo "0" > /sys/class/power_supply/battery/mmi_charging_enable
        echo "1" > /sys/class/power_supply/battery/stop_charging_enable
        echo "0" > /sys/class/hw_power/charger/charge_data/enable_charger
        echo "1" > /sys/class/qcom-battery/input_suspend
        echo "1" > /sys/devices/platform/charger/tran_aichg_disable_charger
        echo "1" > /sys/devices/platform/charger/bypass_charger
        echo "0" > /sys/devices/platform/huawei_charger/enable_charger
        echo "1" > /sys/devices/platform/lge-unified-nodes/charging_completed
        echo "0" > /sys/devices/platform/lge-unified-nodes/charging_enable
        echo "1" > /sys/devices/platform/mt-battery/disable_charger
        echo "1" > /sys/devices/platform/soc/soc:google,charger/charge_disable
        echo "1" > /sys/kernel/debug/google_charger/chg_suspend
        echo "1" > /sys/kernel/debug/google_charger/input_suspend
        echo "on" > /sys/kernel/nubia_charge/charger_bypass
        echo "0 1" > /proc/mtk_battery_cmd/current_cmd
    """

    private const val SCRIPT_ENABLE_CHARGING = """
        echo "0" > /sys/class/power_supply/battery/batt_slate_mode
        echo "0" > /sys/class/power_supply/battery/battery_input_suspend
        echo "0" > /sys/class/power_supply/battery/bd_trickle_cnt
        echo "1" > /sys/class/power_supply/battery/device/Charging_Enable
        echo "1" > /sys/class/power_supply/battery/charging_enabled
        echo "0" > /sys/class/power_supply/battery/op_disable_charge
        echo "0" > /sys/class/power_supply/battery/store_mode
        echo "2" > /sys/class/power_supply/battery/test_mode
        echo "0" > /sys/class/power_supply/battery/battery_ext/smart_charging_interruption
        echo "100" > /sys/class/power_supply/battery/siop_level
        echo "1" > /sys/class/power_supply/battery/battery_charging_enabled
        echo "1" > /sys/class/power_supply/battery/mmi_charging_enable
        echo "0" > /sys/class/power_supply/battery/stop_charging_enable
        echo "1" > /sys/class/hw_power/charger/charge_data/enable_charger
        echo "0" > /sys/class/qcom-battery/input_suspend
        echo "0" > /sys/devices/platform/charger/tran_aichg_disable_charger
        echo "0" > /sys/devices/platform/charger/bypass_charger
        echo "1" > /sys/devices/platform/huawei_charger/enable_charger
        echo "0" > /sys/devices/platform/lge-unified-nodes/charging_completed
        echo "1" > /sys/devices/platform/lge-unified-nodes/charging_enable
        echo "0" > /sys/devices/platform/mt-battery/disable_charger
        echo "0" > /sys/devices/platform/soc/soc:google,charger/charge_disable
        echo "0" > /sys/kernel/debug/google_charger/chg_suspend
        echo "0" > /sys/kernel/debug/google_charger/input_suspend
        echo "off" > /sys/kernel/nubia_charge/charger_bypass
        echo "0 0" > /proc/mtk_battery_cmd/current_cmd
    """

    fun setChargingEnabled(enable: Boolean) {
        val script = if (enable) SCRIPT_ENABLE_CHARGING else SCRIPT_DISABLE_CHARGING
        Thread {
            Shell.cmd(script).exec()
        }.start()
    }

    private fun Context.getBatteryIntent(): Intent? = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

    fun getBatteryTechnology(context: Context): String = runCatching {
        val result = Shell.cmd("cat $BATTERY_TECHNOLOGY").exec()
        if (result.isSuccess && result.out.isNotEmpty()) {
            result.out.firstOrNull()?.trim() ?: "N/A"
        } else {
            context.getBatteryIntent()?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "N/A"
        }
    }.getOrElse {
        Log.e(TAG, "Error reading battery technology", it)
        context.getBatteryIntent()?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "N/A"
    }

    fun getBatteryHealth(context: Context): String {
        return when (context.getBatteryIntent()?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "N/A"
        }
    }

    fun getBatteryLevel(context: Context): String {
        val level = context.getBatteryIntent()?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        return if (level != -1) "$level%" else "N/A"
    }

    fun getBatteryDesignCapacity(): String = runCatching {
        val result = Shell.cmd("cat $BATTERY_DESIGN_CAPACITY").exec()
        if (result.isSuccess && result.out.isNotEmpty()) {
            val mAh = result.out.firstOrNull()?.trim()?.toIntOrNull()?.div(1000) ?: 0
            return "$mAh mAh"
        } else {
            "N/A"
        }
    }.getOrElse { "N/A" }

    // --- CALCULATED HEALTH ---
    fun getCalculatedHealth(): String {
        val fullPaths = listOf("/sys/class/power_supply/battery/charge_full", "/sys/class/power_supply/battery/charge_counter")
        val designPaths = listOf("/sys/class/power_supply/battery/charge_full_design", "/sys/class/power_supply/battery/batt_full_capacity")

        var fullCap = 0L
        var designCap = 0L

        for (path in fullPaths) {
            val res = Shell.cmd("cat $path").exec().out.firstOrNull()?.trim()?.toLongOrNull()
            if (res != null && res > 1000) {
                fullCap = res
                break
            }
        }

        for (path in designPaths) {
            val res = Shell.cmd("cat $path").exec().out.firstOrNull()?.trim()?.toLongOrNull()
            if (res != null && res > 1000) {
                designCap = res
                break
            }
        }

        if (fullCap <= 0 || designCap <= 0) return "N/A"

        if (fullCap > 10000) fullCap /= 1000
        if (designCap > 10000) designCap /= 1000

        val percentage = (fullCap.toDouble() * 100 / designCap.toDouble())
        val formattedPercent = String.format("%.1f", percentage)

        return "$formattedPercent% (${fullCap}mAh from ${designCap}mAh)"
    }

    fun getBatteryMaximumCapacity(context: Context): String = runCatching {
        val maxCapacityResult = Shell.cmd("cat $BATTERY_MAXIMUM_CAPACITY").exec()
        if (!maxCapacityResult.isSuccess || maxCapacityResult.out.isEmpty()) {
            return "N/A"
        }

        val maxCapacity = maxCapacityResult.out.firstOrNull()?.trim()?.toIntOrNull() ?: 0
        if (maxCapacity <= 0) return "N/A"

        var designCapacity = 0
        val designCapacityResult = Shell.cmd("cat $BATTERY_DESIGN_CAPACITY").exec()
        if (designCapacityResult.isSuccess && designCapacityResult.out.isNotEmpty()) {
            designCapacity = designCapacityResult.out.firstOrNull()?.trim()?.toIntOrNull() ?: 0
        }

        if (designCapacity > 0) {
             val percentage = (maxCapacity / designCapacity.toDouble() * 100).roundToInt()
            "${maxCapacity / 1000} mAh ($percentage%)"
        } else {
            "${maxCapacity / 1000} mAh (N/A%)"
        }
    }.getOrElse { "N/A" }


    fun getBatteryCurrentNow(): Int {
        val paths = listOf(BATTERY_CURRENT_NOW, BATTERY_CURRENT_AVG, BMS_CURRENT_NOW)
        for (path in paths) {
            val result = runCatching {
                Shell.cmd("cat $path").exec().out.firstOrNull()?.trim()?.toIntOrNull()
            }.getOrNull()
            if (result != null && result != 0) {
                 return result / 1000
            }
        }
        return 0
    }

    fun getChargingStatus(context: Context): String {
        val kernelStatus = runCatching {
            Shell.cmd("cat $BATTERY_STATUS").exec().out.firstOrNull()?.trim()
        }.getOrNull()

        if (!kernelStatus.isNullOrEmpty()) return kernelStatus

        val status = context.getBatteryIntent()?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }
    }
    
    fun getCycleCount(): String = runCatching {
        Shell.cmd("cat $BATTERY_CYCLE_COUNT").exec().out.firstOrNull()?.trim() ?: "N/A"
    }.getOrElse { "N/A" }
    
    fun getChargeType(): String = runCatching {
        Shell.cmd("cat $BATTERY_CHARGE_TYPE").exec().out.firstOrNull()?.trim()?.replaceFirstChar { it.uppercase() } ?: "N/A"
    }.getOrElse { "N/A" }

    fun getUptime(): String {
        val uptimeMillis = SystemClock.elapsedRealtime()
        val seconds = (uptimeMillis / 1000) % 60
        val minutes = (uptimeMillis / (1000 * 60)) % 60
        val hours = (uptimeMillis / (1000 * 60 * 60)) % 24
        val days = (uptimeMillis / (1000 * 60 * 60 * 24))
        return buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0 || days > 0) append("${hours}h ")
            if (minutes > 0 || hours > 0 || days > 0) append("${minutes}m ")
            append("${seconds}s")
        }.trim()
    }

    fun getDeepSleep(): String {
        val deepSleepMillis = SystemClock.elapsedRealtime() - SystemClock.uptimeMillis()
        val seconds = (deepSleepMillis / 1000) % 60
        val minutes = (deepSleepMillis / (1000 * 60)) % 60
        val hours = (deepSleepMillis / (1000 * 60 * 60)) % 24
        val days = deepSleepMillis / (1000 * 60 * 60 * 24)
        val percentage = if (SystemClock.elapsedRealtime() > 0) {
            (deepSleepMillis * 100 / SystemClock.elapsedRealtime()).toInt()
        } else { 0 }
        return buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0 || days > 0) append("${hours}h ")
            if (minutes > 0 || hours > 0 || days > 0) append("${minutes}m ")
            append("${seconds}s")
            append(" ($percentage%)")
        }.trim()
    }
    
    fun getBatteryLevelRaw(context: Context): Int {
        return context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
    }

    fun getTempSimple(context: Context): String {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        return "%.1f°C".format(temp / 10.0)
    }

    private fun registerBatteryListener(context: Context, onReceive: (Intent) -> Unit): BroadcastReceiver {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let(onReceive)
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return receiver
    }

    fun registerBatteryLevelListener(context: Context, callback: (String) -> Unit): BroadcastReceiver =
        registerBatteryListener(context) { intent ->
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            callback(if (level != -1) "$level%" else "N/A")
        }

    fun registerBatteryTemperatureListener(context: Context, callback: (String) -> Unit): BroadcastReceiver =
        registerBatteryListener(context) { intent ->
            val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
            callback(if (temp != -1) "%.1f °C".format(temp / 10.0) else "N/A")
        }

    fun registerBatteryVoltageListener(context: Context, callback: (String) -> Unit): BroadcastReceiver =
        registerBatteryListener(context) { intent ->
            val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
            callback(if (voltage != -1) "%.3f V".format(voltage / 1000.0) else "N/A")
        }

    fun registerBatteryCapacityListener(context: Context, callback: (String) -> Unit): BroadcastReceiver =
        registerBatteryListener(context) {
            callback(getBatteryMaximumCapacity(context))
        }
}
