/*
 * Original code from: Rem01Gaming (origami_kernel_manager)
 * Modified and integrated by: Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.socmenu

import com.zuan.kernelmanager.utils.Utils

object NetworkUtils {
    const val TCP_CONG = "/proc/sys/net/ipv4/tcp_congestion_control"
    const val TCP_AVAIL_CONG = "/proc/sys/net/ipv4/tcp_available_congestion_control"
    
    const val TCP_SYNCOOKIES = "/proc/sys/net/ipv4/tcp_syncookies"
    const val TCP_REUSE = "/proc/sys/net/ipv4/tcp_tw_reuse"
    const val TCP_FASTOPEN = "/proc/sys/net/ipv4/tcp_fastopen"
    const val TCP_SACK = "/proc/sys/net/ipv4/tcp_sack"
    const val TCP_ECN = "/proc/sys/net/ipv4/tcp_ecn"
    const val TCP_MAX_SYN_BACKLOG = "/proc/sys/net/ipv4/tcp_max_syn_backlog"
    const val PRINTK = "/proc/sys/kernel/printk"

    fun getTcpCongestion(): String = Utils.readFile(TCP_CONG).trim()
    fun getAvailableTcpCongestion(): List<String> = Utils.readFile(TCP_AVAIL_CONG).split("\\s+".toRegex()).filter { it.isNotBlank() }
}
