/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.home.menu

import android.content.Context
import android.content.pm.ApplicationInfo
import com.topjohnwu.superuser.Shell

data class AppCompileItem(
    val packageName: String,
    val label: String,
    val icon: android.graphics.drawable.Drawable?,
    val isSystem: Boolean
)

object Dex2oatUtils {

    // Mode kompilasi Android (Standard)
    val COMPILE_MODES = listOf(
        "speed-profile" to "Speed Profile (Default)",
        "speed" to "Speed (Max Performance)",
        "everything" to "Everything (Max Size)",
        "quicker" to "Quicker (Balanced)",
        "verify" to "Verify (Fast Install)"
    )

    fun getInstalledApps(context: Context): List<AppCompileItem> {
        val pm = context.packageManager
        val apps = mutableListOf<AppCompileItem>()
        
        try {
            val installed = pm.getInstalledPackages(0)
            installed.forEach { pkg ->
                pkg.applicationInfo?.let { appInfo ->
                    val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val label = pm.getApplicationLabel(appInfo).toString()
                    val icon = pm.getApplicationIcon(appInfo)
                    apps.add(AppCompileItem(pkg.packageName, label, icon, isSystem))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return apps.sortedBy { it.label.lowercase() }
    }

    fun compileApp(packageName: String, mode: String): Boolean {
        // -m: mode, -f: force (memaksa compile ulang)
        val result = Shell.cmd("cmd package compile -m $mode -f $packageName").exec()
        return result.isSuccess
    }

    fun resetApp(packageName: String): Boolean {
        val result = Shell.cmd("cmd package compile --reset $packageName").exec()
        return result.isSuccess
    }
    
    fun compileAll(mode: String): Boolean {
        // -a: all packages, -f: force
        val result = Shell.cmd("cmd package compile -m $mode -f -a").exec()
        return result.isSuccess
    }

    // [BARU] Reset semua aplikasi ke state awal
    fun resetAll(): Boolean {
        val result = Shell.cmd("cmd package compile --reset -a").exec()
        return result.isSuccess
    }
}
