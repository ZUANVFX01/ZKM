package com.zuan.kernelmanager.ui.battery

import android.content.Context
import android.content.SharedPreferences

class BatteryPreference private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "battery_prefs"
        private const val KEY_MANUAL_CAPACITY = "manual_design_capacity"
        private const val KEY_CUTOFF_LIMIT = "cutoff_limit"
        // Key Baru
        private const val KEY_MONITOR_ENABLED = "monitor_enabled"

        @Volatile
        private var instance: BatteryPreference? = null

        fun getInstance(context: Context): BatteryPreference {
            return instance ?: synchronized(this) {
                instance ?: BatteryPreference(context.applicationContext).also { instance = it }
            }
        }
    }

    fun getManualDesignCapacity(): Int = prefs.getInt(KEY_MANUAL_CAPACITY, 0)

    fun setManualDesignCapacity(capacity: Int) {
        prefs.edit().putInt(KEY_MANUAL_CAPACITY, capacity).apply()
    }

    fun getCutoffLimit(): Float = prefs.getFloat(KEY_CUTOFF_LIMIT, 80f)

    fun setCutoffLimit(limit: Float) {
        prefs.edit().putFloat(KEY_CUTOFF_LIMIT, limit).apply()
    }
    
    // --- MONITOR PREFS ---
    fun isMonitorEnabled(): Boolean = prefs.getBoolean(KEY_MONITOR_ENABLED, false)
    
    fun setMonitorEnabled(enable: Boolean) {
        prefs.edit().putBoolean(KEY_MONITOR_ENABLED, enable).apply()
    }
}
