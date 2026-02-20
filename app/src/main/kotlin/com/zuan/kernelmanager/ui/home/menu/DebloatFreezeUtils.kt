/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.home.menu

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import com.topjohnwu.superuser.Shell

data class AppUiModel(
    val label: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val targetSdk: Int,
    val isSystem: Boolean,
    val isEnabled: Boolean,
    val iconDrawable: android.graphics.drawable.Drawable 
)

object DebloatFreezeUtils {

    fun getInstalledApps(context: Context): List<AppUiModel> {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_META_DATA)
        
        return packages.mapNotNull { pkg ->
            try {
                val appInfo = pkg.applicationInfo ?: return@mapNotNull null
                
                val label = pm.getApplicationLabel(appInfo).toString()
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isEnabled = appInfo.enabled

                AppUiModel(
                    label = label,
                    packageName = pkg.packageName,
                    versionName = pkg.versionName ?: "Unknown",
                    versionCode = if (android.os.Build.VERSION.SDK_INT >= 28) pkg.longVersionCode else pkg.versionCode.toLong(),
                    targetSdk = appInfo.targetSdkVersion,
                    isSystem = isSystem,
                    isEnabled = isEnabled,
                    iconDrawable = pm.getApplicationIcon(appInfo)
                )
            } catch (e: Exception) {
                null 
            }
        }.sortedBy { it.label.lowercase() }
    }

    fun toggleAppState(packageName: String, enable: Boolean): Boolean {
        val command = if (enable) "pm enable $packageName" else "pm disable-user --user 0 $packageName"
        val result = Shell.cmd(command).exec()
        return result.isSuccess
    }

    fun debloatApp(packageName: String): Boolean {
        val result = Shell.cmd("pm uninstall --user 0 $packageName").exec()
        return result.isSuccess
    }

    // [BARU] Buka App Info Android
    fun openAppSystemSettings(context: Context, packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
