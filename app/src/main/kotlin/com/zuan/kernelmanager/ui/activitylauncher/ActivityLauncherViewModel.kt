/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.activitylauncher

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivityLauncherViewModel(application: Application) : AndroidViewModel(application) {

    // StateFlow untuk menyimpan list aplikasi (Data Cache)
    private val _allApps = MutableStateFlow<List<AppData>>(emptyList())
    val allApps = _allApps.asStateFlow()

    // Status Loading
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    // Progress Loading (0.0 - 1.0) -> Sekarang akan sangat cepat jadi mungkin tidak terlalu terlihat
    private val _loadProgress = MutableStateFlow(0f)
    val loadProgress = _loadProgress.asStateFlow()

    private var isDataLoaded = false

    init {
        loadApps()
    }

    fun loadApps(forceRefresh: Boolean = false) {
        if (isDataLoaded && !forceRefresh) return

        viewModelScope.launch(Dispatchers.IO) {
            if (forceRefresh) {
                _isLoading.value = true
                _loadProgress.value = 0f
            }

            val pm = getApplication<Application>().packageManager
            // Mengambil Installed Packages (Meta Data lebih ringan dari GET_ACTIVITIES full)
            // Tapi kita butuh count activities, jadi tetap pakai GET_ACTIVITIES tapi kita filter logikanya
            val packages = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES)
            
            val apps = ArrayList<AppData>(packages.size)
            val total = packages.size.toFloat()
            
            // Loop cepat tanpa memuat Gambar (Icon)
            for ((index, packInfo) in packages.withIndex()) {
                
                // Update progress sesekali saja biar gak spam thread
                if (index % 20 == 0) {
                     _loadProgress.value = (index / total) * 0.9f 
                }

                val appInfo = packInfo.applicationInfo ?: continue
                val activities = packInfo.activities
                
                // Filter logika
                if (!activities.isNullOrEmpty() || pm.getLaunchIntentForPackage(packInfo.packageName) != null) {
                    val vName = packInfo.versionName ?: "Unknown"
                    val vCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        packInfo.longVersionCode.toString()
                    } else {
                        @Suppress("DEPRECATION")
                        packInfo.versionCode.toString()
                    }
                    
                    val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val count = activities?.size ?: 0 

                    // Load Label (String) itu ringan, aman dilakukan di sini
                    val label = try {
                         pm.getApplicationLabel(appInfo).toString()
                    } catch (e: Exception) {
                        packInfo.packageName
                    }

                    // PENTING: Jangan load Icon (Drawable) di sini!
                    apps.add(AppData(
                        label = label,
                        packageName = packInfo.packageName,
                        versionName = vName,
                        versionCode = vCode,
                        isSystemApp = isSystem,
                        activityCount = count
                    ))
                }
            }
            
            // Sorting (Default dispatcher bagus untuk operasi CPU sorting list besar)
            withContext(Dispatchers.Default) {
                apps.sortBy { it.label.lowercase() }
            }
            
            // Finalize
            _loadProgress.value = 1.0f
            if (forceRefresh) delay(100) // Delay dikit cuma kalau refresh manual
            
            _allApps.value = apps
            isDataLoaded = true
            _isLoading.value = false
        }
    }
}
