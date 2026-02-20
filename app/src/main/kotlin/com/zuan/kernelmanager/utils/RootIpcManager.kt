/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.topjohnwu.superuser.ipc.RootService
import com.zuan.kernelmanager.IMtkService
import com.zuan.kernelmanager.service.MtkRootService

object RootIpcManager {
    var ipc: IMtkService? = null
        private set

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            ipc = IMtkService.Stub.asInterface(service)
            Log.d("RootIPC", "Zuan MTK IPC Daemon Tersambung!")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            ipc = null
            Log.d("RootIPC", "Zuan MTK IPC Daemon Terputus")
        }
    }

    fun bind(context: Context) {
        if (ipc == null) {
            val intent = Intent(context, MtkRootService::class.java)
            RootService.bind(intent, connection)
        }
    }

    fun unbind() {
        if (ipc != null) {
            RootService.unbind(connection)
            ipc = null
        }
    }
}