/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.utils

import android.app.ActivityManager
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.format.Formatter
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.topjohnwu.superuser.Shell
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

object Utils {
    // --- DEVICE INFO UTILS ---
    fun getDeviceName(): String = Build.MODEL
    fun getDeviceCodename(): String = Build.DEVICE
    fun getAndroidVersion(): String = Build.VERSION.RELEASE
    fun getSdkVersion(): String = Build.VERSION.SDK_INT.toString()
    fun getManufacturer(): String = Build.MANUFACTURER
    fun getBuildTags(): String = Build.TAGS

    // --- KERNEL VERSION (Tanpa Shell) ---
    fun getKernelVersion(): String {
        return try {
            val reader = BufferedReader(FileReader("/proc/version"))
            val line = reader.readLine()
            reader.close()
            if (line.isNotEmpty()) {
                val temp = line.split(" (")[0] 
                temp.replace("Linux version ", "") 
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            System.getProperty("os.version") ?: "Unknown"
        }
    }

    // --- SYSTEM PROPERTY ---
    fun getSystemProperty(key: String): String = runCatching {
        Shell.cmd("getprop $key").exec()
            .takeIf { it.isSuccess }?.out?.firstOrNull()?.trim()
    }.getOrNull().orEmpty()

    // --- FILE OPERATIONS (YANG TADI HILANG) ---
    fun getTemp(filePath: String): String {
        val value = readFile(filePath).trim()
        return value.toFloatOrNull()?.div(1000)?.let { "%.1f".format(it) } ?: "N/A"
    }

    fun testFile(filePath: String): Boolean = File(filePath).exists() ||
        Shell.cmd("test -f $filePath && echo true || echo false").exec()
            .takeIf { it.isSuccess }?.out?.firstOrNull() == "true"

    fun setPermissions(permission: Int, filePath: String) {
        Shell.cmd("chmod $permission $filePath").exec()
    }

    fun readFile(filePath: String): String =
        Shell.cmd("cat $filePath").exec().takeIf { it.isSuccess }?.out?.joinToString("\n")?.trim().orEmpty()

    fun writeFile(filePath: String, value: String): Boolean = runCatching {
        Shell.cmd("echo \"$value\" > $filePath").exec().isSuccess
    }.getOrElse {
        Log.e("writeFile", "Error writing to $filePath: ${it.message}", it)
        false
    }

    // --- APP INFO ---
    fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.longVersionCode})"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    fun getAppVersionName(context: Context): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
        } catch (e: Exception) {
            "0.0.0"
        }
    }

    // --- RAM & MEMORY UTILS ---
    fun getTotalRam(context: Context): String {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val totalMemVars = memInfo.totalMem.toDouble()
        return String.format("%.1f GB", totalMemVars / (1024 * 1024 * 1024))
    }

    fun getSelinuxStatus(): String {
        val result = Shell.cmd("getenforce").exec()
        return result.out.firstOrNull()?.trim() ?: "Unknown"
    }

    fun getBuildUser(): String {
        val cmd = "getprop ro.build.fingerprint 2>/dev/null | tr '/' '\\n' 2>/dev/null | grep user 2>/dev/null | cut -f1 -d: 2>/dev/null"
        val result = Shell.cmd(cmd).exec()
        val output = result.out.firstOrNull()?.trim()
        return if (!output.isNullOrEmpty()) output else "CPU"
    }

    // --- RAM STATUS ---
    data class RamStatus(
        val totalMem: Long,
        val availMem: Long,
        val usedMem: Long,
        val usagePercent: Float,
        val availString: String,
        val totalString: String
    )

    fun getRamStatus(context: Context): RamStatus {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)

        val total = memInfo.totalMem
        val avail = memInfo.availMem
        val used = total - avail
        val percent = if (total > 0) used.toFloat() / total.toFloat() else 0f

        return RamStatus(
            totalMem = total,
            availMem = avail,
            usedMem = used,
            usagePercent = percent,
            availString = Formatter.formatShortFileSize(context, avail),
            totalString = Formatter.formatShortFileSize(context, total)
        )
    }

    // --- SERVICE CHECKER UTILS ---
    @Suppress("DEPRECATION") 
    fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager?.getRunningServices(Int.MAX_VALUE) ?: emptyList()) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    // --- UPDATE & INSTALLER UTILS ---
    fun downloadAndInstallApk(context: Context, url: String, fileName: String) {
        try {
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            if (file.exists()) file.delete()

            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Update ZKM")
                .setDescription("Downloading latest version...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setMimeType("application/vnd.android.package-archive")

            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = manager.enqueue(request)

            Toast.makeText(context, "Start downloading updates...", Toast.LENGTH_SHORT).show()

            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(ctxt: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        installApk(ctxt, fileName)
                        try { ctxt.unregisterReceiver(this) } catch (e: Exception) {}
                    }
                }
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to start download: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun installApk(context: Context, fileName: String) {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
        
        if (file.exists()) {
            try {
                val authority = "${context.packageName}.provider" 
                val uri = FileProvider.getUriForFile(context, authority, file)

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to open the installer: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Update file not found!", Toast.LENGTH_SHORT).show()
        }
    }
}
