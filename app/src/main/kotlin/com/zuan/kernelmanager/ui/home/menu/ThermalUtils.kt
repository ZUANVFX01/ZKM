/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.home.menu

import com.topjohnwu.superuser.Shell
import java.io.File
import kotlin.math.abs

data class ThermalZone(
    val id: Int,
    val name: String,
    val type: String,
    val temperature: Int,
    val path: String,
    val isEnabled: Boolean
)

data class ThermalCoolingDevice(
    val id: Int,
    val name: String,
    val type: String,
    val curState: Int,
    val maxState: Int,
    val path: String
)

data class TripPoint(
    val id: Int,
    val temperature: Int,
    val type: String
)

object ThermalUtils {

    private const val THERMAL_PATH = "/sys/class/thermal"
    private const val COOLING_PATH = "/sys/class/thermal/cooling_device"

    // Helper untuk memfilter suhu absurd dari sensor dummy (-273000°C dll)
    private fun normalizeTemperature(rawTemp: Int): Int {
        var temp = if (abs(rawTemp) > 1000) rawTemp / 1000 else rawTemp
        // Jika suhu di bawah -40C (contoh -273 Absolute Zero) atau di atas 200C, itu sensor mati/dummy
        if (temp < -40 || temp > 200) {
            temp = 0
        }
        return temp
    }

    fun getAllThermalZones(): List<ThermalZone> {
        val zones = mutableListOf<ThermalZone>()
        
        try {
            val thermalDir = File(THERMAL_PATH)
            if (!thermalDir.exists()) return emptyList()
            
            val zoneDirs = thermalDir.listFiles { file ->
                file.isDirectory && file.name.startsWith("thermal_zone")
            }?.sortedBy { 
                it.name.replace("thermal_zone", "").toIntOrNull() ?: 0 
            } ?: emptyList()
            
            zoneDirs.forEach { zoneDir ->
                val id = zoneDir.name.replace("thermal_zone", "").toIntOrNull() ?: 0
                val name = readThermalFile(zoneDir, "type") ?: "Unknown"
                val type = categorizeThermalType(name)
                
                val rawTemp = readThermalFile(zoneDir, "temp")?.toIntOrNull() ?: 0
                val temp = normalizeTemperature(rawTemp)
                
                val enabled = readThermalFile(zoneDir, "mode")?.trim() != "disabled"
                
                zones.add(ThermalZone(
                    id = id,
                    name = getDisplayName(name),
                    type = type,
                    temperature = temp,
                    path = zoneDir.absolutePath,
                    isEnabled = enabled
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return zones
    }

    fun getCoolingDevices(): List<ThermalCoolingDevice> {
        val devices = mutableListOf<ThermalCoolingDevice>()
        
        try {
            val coolingDir = File(COOLING_PATH)
            if (!coolingDir.exists()) return emptyList()
            
            val deviceDirs = coolingDir.listFiles { file ->
                file.isDirectory && file.name.startsWith("cooling_device")
            }?.sortedBy {
                it.name.replace("cooling_device", "").toIntOrNull() ?: 0
            } ?: emptyList()
            
            deviceDirs.forEach { deviceDir ->
                val id = deviceDir.name.replace("cooling_device", "").toIntOrNull() ?: 0
                val name = readThermalFile(deviceDir, "type") ?: "Unknown"
                val curState = readThermalFile(deviceDir, "cur_state")?.toIntOrNull() ?: 0
                val maxState = readThermalFile(deviceDir, "max_state")?.toIntOrNull() ?: 0
                
                devices.add(ThermalCoolingDevice(
                    id = id,
                    name = name,
                    type = categorizeCoolingType(name),
                    curState = curState,
                    maxState = maxState,
                    path = deviceDir.absolutePath
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return devices
    }

    fun setThermalZoneState(zoneId: Int, enabled: Boolean): Boolean {
        val mode = if (enabled) "enabled" else "disabled"
        val result = Shell.cmd("echo $mode > /sys/class/thermal/thermal_zone$zoneId/mode").exec()
        return result.isSuccess
    }

    fun setCoolingDeviceState(deviceId: Int, state: Int): Boolean {
        val result = Shell.cmd("echo $state > /sys/class/thermal/cooling_device$deviceId/cur_state").exec()
        return result.isSuccess
    }

    fun getThermalPolicy(): String {
        val result = Shell.cmd("cat /sys/class/thermal/thermal_policy 2>/dev/null || echo 'default'").exec()
        return if (result.isSuccess) result.out.joinToString("").trim() else "default"
    }

    fun setThermalPolicy(policy: String): Boolean {
        val result = Shell.cmd("echo $policy > /sys/class/thermal/thermal_policy 2>/dev/null").exec()
        return result.isSuccess
    }

    fun getTripPoints(zonePath: String): List<TripPoint> {
        val trips = mutableListOf<TripPoint>()
        val zoneDir = File(zonePath)
        
        for (i in 0..9) {
            val rawTemp = readThermalFile(zoneDir, "trip_point_${i}_temp")?.toIntOrNull() ?: break
            val temp = normalizeTemperature(rawTemp)
            val type = readThermalFile(zoneDir, "trip_point_${i}_type") ?: "Unknown"
            trips.add(TripPoint(i, temp, type))
        }
        
        return trips
    }

    private fun readThermalFile(dir: File, filename: String): String? {
        return try {
            val file = File(dir, filename)
            if (file.exists() && file.canRead()) {
                file.readText().trim()
            } else {
                val result = Shell.cmd("cat ${file.absolutePath} 2>/dev/null").exec()
                if (result.isSuccess && result.out.isNotEmpty()) result.out[0].trim() else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun categorizeThermalType(name: String): String {
        return when {
            name.contains("cpu", ignoreCase = true) || name.contains("cluster", ignoreCase = true) -> "CPU"
            name.contains("gpu", ignoreCase = true) || name.contains("graphics", ignoreCase = true) -> "GPU"
            name.contains("battery", ignoreCase = true) || name.contains("batt", ignoreCase = true) -> "Battery"
            name.contains("charger", ignoreCase = true) || name.contains("usb", ignoreCase = true) -> "Charger"
            name.contains("skin", ignoreCase = true) || name.contains("surface", ignoreCase = true) -> "Skin"
            name.contains("modem", ignoreCase = true) || name.contains("radio", ignoreCase = true) -> "Modem"
            name.contains("wifi", ignoreCase = true) -> "WiFi"
            name.contains("cam", ignoreCase = true) || name.contains("camera", ignoreCase = true) -> "Camera"
            name.contains("flash", ignoreCase = true) || name.contains("led", ignoreCase = true) -> "Flash"
            name.contains("pa", ignoreCase = true) || name.contains("amplifier", ignoreCase = true) -> "PA"
            else -> "System"
        }
    }

    private fun categorizeCoolingType(name: String): String {
        return when {
            name.contains("cpu", ignoreCase = true) -> "CPU"
            name.contains("gpu", ignoreCase = true) -> "GPU"
            name.contains("fan", ignoreCase = true) || name.contains("cooler", ignoreCase = true) -> "Fan"
            name.contains("backlight", ignoreCase = true) || name.contains("brightness", ignoreCase = true) -> "Display"
            name.contains("thermal", ignoreCase = true) -> "Thermal"
            else -> "Other"
        }
    }

    private fun getDisplayName(name: String): String {
        return name.replace("_", " ")
            .replace("tsens", "Temperature Sensor")
            .replace("tz", "Zone")
            .capitalizeWords()
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
