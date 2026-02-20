/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.utils

import android.content.Context
import android.util.Log
import com.topjohnwu.superuser.Shell

object ShellExecutor {

    private const val TAG = "ZKM_Shell"
    
    // Status cache
    var isRootAvailable = false
        private set

    fun init(context: Context) {
        // Cek Root saat inisialisasi
        checkRootAccess()
    }

    fun checkRootAccess(): Boolean {
        try {
            // LibSU akan otomatis meminta izin root jika belum ada
            isRootAvailable = Shell.getShell().isRoot
        } catch (e: Exception) {
            Log.e(TAG, "Root check failed: ${e.message}")
            isRootAvailable = false
        }
        return isRootAvailable
    }

    fun executeWithResult(command: String): String {
        if (!isRootAvailable) {
            // Coba init ulang siapa tahu user baru grant root
            if (!checkRootAccess()) return "" 
        }

        return try {
            val result = Shell.cmd(command).exec()
            if (result.isSuccess) {
                result.out.joinToString("\n").trim()
            } else {
                Log.e(TAG, "Command failed: $command")
                ""
            }
        } catch(e: Exception){ 
            Log.e(TAG, "Exec error: ${e.message}")
            "" 
        }
    }
}
