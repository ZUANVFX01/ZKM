/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val version: String,
    val changelog: String,
    val downloadUrl: String,
    val fileName: String
)

object UpdateChecker {
    private const val REPO_OWNER = "ZUANVFX01"
    private const val REPO_NAME = "ZKM"
    private const val API_URL = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/latest"

    suspend fun checkForUpdate(context: Context): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                connectTimeout = 10000
                readTimeout = 10000
            }

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                
                val tagName = json.getString("tag_name") // e.g., "v1.0.0"
                val latestVersion = tagName.removePrefix("v").removePrefix("V")
                
                val currentVersion = context.packageManager.getPackageInfo(
                    context.packageName, 0
                ).versionName ?: return@withContext null

                // Cek jika versi berbeda (GitHub lebih diutamakan untuk mencegah mod)
                if (latestVersion != currentVersion) {
                    val changelog = json.optString("body", "No changelogs provided.")
                    val assets = json.optJSONArray("assets")
                    
                    var apkUrl = ""
                    var apkName = "update.apk"
                    
                    if (assets != null && assets.length() > 0) {
                        val asset = assets.getJSONObject(0)
                        apkUrl = asset.getString("browser_download_url")
                        apkName = asset.getString("name")
                    }
                    
                    // Fallback ke html_url jika tidak ada asset langsung
                    if (apkUrl.isEmpty()) {
                        apkUrl = json.getString("html_url")
                    }

                    UpdateInfo(
                        version = latestVersion,
                        changelog = changelog,
                        downloadUrl = apkUrl,
                        fileName = apkName
                    )
                } else {
                    null // Versi sama, tidak perlu update
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun downloadUpdate(context: Context, updateInfo: UpdateInfo): Long {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        
        // Buat request ke DownloadManager
        val request = DownloadManager.Request(Uri.parse(updateInfo.downloadUrl)).apply {
            setTitle("Updating ZKM to v${updateInfo.version}")
            setDescription("Downloading...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "ZKM_v${updateInfo.version}.apk")
            setMimeType("application/vnd.android.package-archive")
            allowScanningByMediaScanner()
        }
        
        return downloadManager.enqueue(request)
    }

    fun registerInstallReceiver(context: Context, downloadId: Long) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: return
                if (id == downloadId) {
                    context?.let { installApk(it, downloadId) }
                    context?.unregisterReceiver(this)
                }
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    private fun installApk(context: Context, downloadId: Long) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = downloadManager.getUriForDownloadedFile(downloadId) ?: return
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }
}
