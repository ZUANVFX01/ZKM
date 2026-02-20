/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.service

import android.content.Intent
import android.os.IBinder
import com.topjohnwu.superuser.ipc.RootService
import com.zuan.kernelmanager.IMtkService
import java.io.File

class MtkRootService : RootService() {
    override fun onBind(intent: Intent): IBinder {
        return object : IMtkService.Stub() {
            
            override fun readNode(path: String): String {
                return try {
                    val file = File(path)
                    if (file.exists()) file.readText().trim() else ""
                } catch (e: Exception) {
                    ""
                }
            }

            override fun writeNode(path: String, value: String): Boolean {
                return try {
                    val file = File(path)
                    file.writeText(value) // Eksekusi murni sebagai Root (uid 0)
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            override fun nodeExists(path: String): Boolean {
                return File(path).exists()
            }
            
            override fun listDirectories(path: String): List<String> {
                return try {
                    val file = File(path)
                    if (file.isDirectory) {
                        file.listFiles()?.map { it.name } ?: emptyList()
                    } else emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
    }
}