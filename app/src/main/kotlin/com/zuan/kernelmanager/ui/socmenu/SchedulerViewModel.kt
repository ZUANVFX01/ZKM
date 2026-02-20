/*
 * Original code from: Rem01Gaming (origami_kernel_manager)
 * Modified and integrated by: Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.socmenu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zuan.kernelmanager.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SchedulerViewModel : ViewModel() {

    // --- State Classes ---
    data class SchedState(
        val schedAutogroup: String = "0", val hasSchedAutogroup: Boolean = false,
        val childRunsFirst: String = "0", val hasChildRunsFirst: Boolean = false,
        val cstateAware: String = "0", val hasCstateAware: Boolean = false,
        val schedStats: String = "0", val hasSchedStats: Boolean = false,
        val tunableScaling: String = "0", val hasTunableScaling: Boolean = false // [NEW]
    )

    data class BoreState(val hasBore: Boolean = false, val bore: Int = 0)
    
    data class UclampState(
        val hasUclampMax: Boolean = false, val uclampMax: String = "N/A",
        val hasUclampMin: Boolean = false, val uclampMin: String = "N/A"
    )

    // Data class for dynamic items
    data class TunableItem(
        val name: String,
        val path: String,
        val value: String
    )

    // --- State Flows ---
    private val _sched = MutableStateFlow(SchedState())
    val sched: StateFlow<SchedState> = _sched

    private val _bore = MutableStateFlow(BoreState())
    val bore: StateFlow<BoreState> = _bore

    private val _uclamp = MutableStateFlow(UclampState())
    val uclamp: StateFlow<UclampState> = _uclamp

    private val _genericTunables = MutableStateFlow<List<TunableItem>>(emptyList())
    val genericTunables: StateFlow<List<TunableItem>> = _genericTunables

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Refresh Feature Toggles
            _sched.value = SchedState(
                schedAutogroup = Utils.readFile(SchedulerUtils.SCHED_AUTO_GROUP).trim(),
                hasSchedAutogroup = Utils.testFile(SchedulerUtils.SCHED_AUTO_GROUP),
                childRunsFirst = Utils.readFile(SchedulerUtils.SCHED_CHILD_RUNS_FIRST).trim(),
                hasChildRunsFirst = Utils.testFile(SchedulerUtils.SCHED_CHILD_RUNS_FIRST),
                cstateAware = Utils.readFile(SchedulerUtils.SCHED_CSTATE_AWARE).trim(),
                hasCstateAware = Utils.testFile(SchedulerUtils.SCHED_CSTATE_AWARE),
                schedStats = Utils.readFile(SchedulerUtils.SCHED_SCHEDSTATS).trim(),
                hasSchedStats = Utils.testFile(SchedulerUtils.SCHED_SCHEDSTATS),
                tunableScaling = Utils.readFile(SchedulerUtils.SCHED_TUNABLE_SCALING).trim(),
                hasTunableScaling = Utils.testFile(SchedulerUtils.SCHED_TUNABLE_SCALING)
            )

            _bore.value = BoreState(
                hasBore = Utils.testFile(SchedulerUtils.BORE),
                bore = Utils.readFile(SchedulerUtils.BORE).trim().toIntOrNull() ?: 0
            )

            _uclamp.value = UclampState(
                hasUclampMax = Utils.testFile(SchedulerUtils.SCHED_UTIL_CLAMP_MAX),
                uclampMax = Utils.readFile(SchedulerUtils.SCHED_UTIL_CLAMP_MAX).trim(),
                hasUclampMin = Utils.testFile(SchedulerUtils.SCHED_UTIL_CLAMP_MIN),
                uclampMin = Utils.readFile(SchedulerUtils.SCHED_UTIL_CLAMP_MIN).trim()
            )

            // 2. Refresh Dynamic Tunables
            val validTunables = mutableListOf<TunableItem>()
            SchedulerUtils.GENERIC_SCHED_TUNABLES.forEach { (name, path) ->
                if (Utils.testFile(path)) { 
                    val content = Utils.readFile(path).trim()
                    if (content.isNotEmpty()) {
                         validTunables.add(TunableItem(name, path, content))
                    }
                }
            }
            _genericTunables.value = validTunables
        }
    }

    fun updateValue(path: String, value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Utils.writeFile(path, value)
            refreshData()
        }
    }
}
