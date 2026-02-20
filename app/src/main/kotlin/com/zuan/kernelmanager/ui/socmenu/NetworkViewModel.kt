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

class NetworkViewModel : ViewModel() {

    data class NetworkState(
        val tcpCongestion: String = "N/A", val availableTcp: List<String> = emptyList(),
        val syncookies: String = "0", val hasSyncookies: Boolean = false,
        val reuse: String = "0", val hasReuse: Boolean = false,
        val fastOpen: String = "0", val hasFastOpen: Boolean = false,
        val sack: String = "0", val hasSack: Boolean = false,
        val ecn: String = "0", val hasEcn: Boolean = false,
        val maxSynBacklog: String = "0", val hasMaxSynBacklog: Boolean = false,
        val printk: String = "N/A"
    )

    private val _net = MutableStateFlow(NetworkState())
    val net: StateFlow<NetworkState> = _net

    init { refreshData() }

    fun refreshData() {
        viewModelScope.launch(Dispatchers.IO) {
            _net.value = NetworkState(
                tcpCongestion = NetworkUtils.getTcpCongestion(),
                availableTcp = NetworkUtils.getAvailableTcpCongestion(),
                syncookies = Utils.readFile(NetworkUtils.TCP_SYNCOOKIES).trim(),
                hasSyncookies = Utils.testFile(NetworkUtils.TCP_SYNCOOKIES),
                reuse = Utils.readFile(NetworkUtils.TCP_REUSE).trim(),
                hasReuse = Utils.testFile(NetworkUtils.TCP_REUSE),
                fastOpen = Utils.readFile(NetworkUtils.TCP_FASTOPEN).trim(),
                hasFastOpen = Utils.testFile(NetworkUtils.TCP_FASTOPEN),
                sack = Utils.readFile(NetworkUtils.TCP_SACK).trim(),
                hasSack = Utils.testFile(NetworkUtils.TCP_SACK),
                ecn = Utils.readFile(NetworkUtils.TCP_ECN).trim(),
                hasEcn = Utils.testFile(NetworkUtils.TCP_ECN),
                maxSynBacklog = Utils.readFile(NetworkUtils.TCP_MAX_SYN_BACKLOG).trim(),
                hasMaxSynBacklog = Utils.testFile(NetworkUtils.TCP_MAX_SYN_BACKLOG),
                printk = Utils.readFile(NetworkUtils.PRINTK)
            )
        }
    }

    fun updateValue(path: String, value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Utils.writeFile(path, value)
            refreshData()
        }
    }
}
