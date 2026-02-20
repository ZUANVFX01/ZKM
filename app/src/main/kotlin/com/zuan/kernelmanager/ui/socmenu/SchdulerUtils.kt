/*
 * Original code from: Rem01Gaming (origami_kernel_manager)
 * Modified and integrated by: Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.socmenu

object SchedulerUtils {
    // Base Paths
    const val PROC_KERNEL = "/proc/sys/kernel"
    
    // Existing Features (BORE, EAS, etc)
    const val BORE = "/proc/sys/kernel/sched_bore"
    const val SCHED_AUTO_GROUP = "/proc/sys/kernel/sched_autogroup_enabled"
    const val SCHED_CHILD_RUNS_FIRST = "/proc/sys/kernel/sched_child_runs_first"
    const val SCHED_CSTATE_AWARE = "/proc/sys/kernel/sched_cstate_aware"
    const val SCHED_SCHEDSTATS = "/proc/sys/kernel/sched_schedstats"
    const val SCHED_TUNABLE_SCALING = "/proc/sys/kernel/sched_tunable_scaling" // [NEW] Added Constant
    
    const val SCHED_UTIL_CLAMP_MAX = "/proc/sys/kernel/sched_util_clamp_max"
    const val SCHED_UTIL_CLAMP_MIN = "/proc/sys/kernel/sched_util_clamp_min"

    // New Dynamic Tunables Map
    // [NOTE] Tunable Scaling dihapus dari sini karena sudah punya tombol sendiri
    val GENERIC_SCHED_TUNABLES = mapOf(
        "Deadline Period Max (us)" to "$PROC_KERNEL/sched_deadline_period_max_us",
        "Deadline Period Min (us)" to "$PROC_KERNEL/sched_deadline_period_min_us",
        "Energy Aware" to "$PROC_KERNEL/sched_energy_aware",
        "Latency (ns)" to "$PROC_KERNEL/sched_latency_ns",
        "Migration Cost (ns)" to "$PROC_KERNEL/sched_migration_cost_ns",
        "Min Granularity (ns)" to "$PROC_KERNEL/sched_min_granularity_ns",
        "Nr Migrate" to "$PROC_KERNEL/sched_nr_migrate",
        "PELT Multiplier" to "$PROC_KERNEL/sched_pelt_multiplier",
        "RR Timeslice (ms)" to "$PROC_KERNEL/sched_rr_timeslice_ms",
        "RT Period (us)" to "$PROC_KERNEL/sched_rt_period_us",
        "RT Runtime (us)" to "$PROC_KERNEL/sched_rt_runtime_us",
        "UClamp Min RT Default" to "$PROC_KERNEL/sched_util_clamp_min_rt_default",
        "Wakeup Granularity (ns)" to "$PROC_KERNEL/sched_wakeup_granularity_ns"
    )
}
