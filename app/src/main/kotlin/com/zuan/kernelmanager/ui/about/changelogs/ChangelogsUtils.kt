/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.about.changelogs

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RemoteReleaseInfo(
    val version: String,
    val versionCode: Int,
    val changelog: String,
    val downloadUrl: String,
    val publishedAt: String,
    val isNewer: Boolean,
    val tagName: String,
    val source: String = "GitHub" // GitHub atau Telegram
)

data class ChangelogEntry(
    val version: String,
    val versionCode: Int,
    val date: String,
    val changes: List<ChangeItem>,
    val isBeta: Boolean = false,
    val isImportant: Boolean = false
)

data class ChangeItem(
    val type: ChangeType,
    val description: String
)

enum class ChangeType {
    NEW,
    IMPROVEMENT,
    FIX,
    REMOVED,
    SECURITY,
    PERFORMANCE
}

object ChangelogsUtils {
    
    private const val GITHUB_API_URL = "https://api.github.com/repos/ZUANVFX01/ZKM/releases/latest"
    private const val TELEGRAM_URL = "https://r.jina.ai/http://t.me/s/zuanvfxproject3" // Via jina.ai reader
    private val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    
    suspend fun fetchLatestRelease(currentVersionCode: Int): Result<RemoteReleaseInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(GITHUB_API_URL).openConnection()
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                connection.setRequestProperty("User-Agent", "ZuanKernelManager")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                
                val tagName = json.getString("tag_name")
                val version = tagName.removePrefix("v")
                val publishedAt = json.getString("published_at")
                val body = json.getString("body")
                val htmlUrl = json.getString("html_url")
                
                val versionCode = parseVersionCode(version)
                
                val date = try {
                    val parsedDate = apiDateFormat.parse(publishedAt)
                    dateFormat.format(parsedDate ?: Date())
                } catch (e: Exception) {
                    publishedAt.substring(0, 10)
                }
                
                Result.success(
                    RemoteReleaseInfo(
                        version = version,
                        versionCode = versionCode,
                        changelog = body,
                        downloadUrl = htmlUrl,
                        publishedAt = date,
                        isNewer = versionCode > currentVersionCode,
                        tagName = tagName,
                        source = "GitHub"
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun fetchTelegramLatestPost(currentVersionCode: Int): Result<RemoteReleaseInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(TELEGRAM_URL).openConnection()
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10)")
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                
                val content = connection.inputStream.bufferedReader().use { it.readText() }
                
                // Parse konten dari jina.ai (format markdown/text)
                val lines = content.lines().filter { it.isNotBlank() }
                var version = ""
                var message = ""
                var date = ""
                
                // Cari baris yang mengandung versi (vX.Y.Z)
                for (i in lines.indices) {
                    val line = lines[i]
                    if (version.isEmpty() && line.contains(Regex("v\\d+\\.\\d+"))) {
                        val match = Regex("v([\\d.]+)").find(line)
                        if (match != null) {
                            version = match.groupValues[1]
                            message = line.trim()
                            // Coba ambil tanggal dari baris berikutnya atau yang sama
                            if (i + 1 < lines.size) {
                                val nextLine = lines[i + 1]
                                if (nextLine.contains(Regex("\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}")) || 
                                    nextLine.contains("202")) {
                                    date = nextLine.trim()
                                }
                            }
                            break
                        }
                    }
                }
                
                if (version.isEmpty()) {
                    throw Exception("Version not found in Telegram channel")
                }
                
                val versionCode = parseVersionCode(version)
                
                Result.success(
                    RemoteReleaseInfo(
                        version = version,
                        versionCode = versionCode,
                        changelog = message,
                        downloadUrl = "https://t.me/zuanvfxproject3",
                        publishedAt = date.ifEmpty { "Recently" },
                        isNewer = versionCode > currentVersionCode,
                        tagName = "v$version",
                        source = "Telegram"
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    private fun parseVersionCode(version: String): Int {
        return try {
            val parts = version.split(".")
            val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
            major * 100 + minor * 10 + patch
        } catch (e: Exception) {
            0
        }
    }
    
    fun getLocalChangelogs(): List<ChangelogEntry> = listOf(
        ChangelogEntry(
            version = "3.0.0",
            versionCode = 300,
            date = "18 February 2025",
            isImportant = true,
            changes = listOf(
                ChangeItem(ChangeType.NEW, "Complete UI redesign with Material3 Expressive"),
                ChangeItem(ChangeType.NEW, "Added glassmorphism effects with Haze library"),
                ChangeItem(ChangeType.NEW, "Video wallpaper support for background"),
                ChangeItem(ChangeType.NEW, "Weather effects overlay (Rain, Snow)"),
                ChangeItem(ChangeType.NEW, "Custom DPI settings per app"),
                ChangeItem(ChangeType.NEW, "Changelog auto-check from GitHub & Telegram"),
                ChangeItem(ChangeType.IMPROVEMENT, "Enhanced kernel parameter editing"),
                ChangeItem(ChangeType.IMPROVEMENT, "Better root handling with libsu"),
                ChangeItem(ChangeType.FIX, "Fixed memory leak in terminal"),
                ChangeItem(ChangeType.FIX, "Fixed navigation bar hide behavior")
            )
        ),
        ChangelogEntry(
            version = "2.5.0",
            versionCode = 250,
            date = "15 January 2025",
            changes = listOf(
                ChangeItem(ChangeType.NEW, "Added Dex2oat compiler controls"),
                ChangeItem(ChangeType.NEW, "Doze mode configuration"),
                ChangeItem(ChangeType.IMPROVEMENT, "Battery controller improvements"),
                ChangeItem(ChangeType.IMPROVEMENT, "Thermal devices management"),
                ChangeItem(ChangeType.FIX, "Fixed GPU stats on some devices"),
                ChangeItem(ChangeType.SECURITY, "Updated root permission checks")
            )
        ),
        ChangelogEntry(
            version = "2.4.0",
            versionCode = 240,
            date = "28 December 2024",
            isBeta = true,
            changes = listOf(
                ChangeItem(ChangeType.NEW, "Kernel Flasher integration"),
                ChangeItem(ChangeType.NEW, "KSU WebUI support"),
                ChangeItem(ChangeType.NEW, "Activity Launcher utility"),
                ChangeItem(ChangeType.PERFORMANCE, "Optimized app startup time"),
                ChangeItem(ChangeType.FIX, "Display settings crash fix")
            )
        ),
        ChangelogEntry(
            version = "2.0.0",
            versionCode = 200,
            date = "1 November 2024",
            isImportant = true,
            changes = listOf(
                ChangeItem(ChangeType.NEW, "Complete rewrite in Jetpack Compose"),
                ChangeItem(ChangeType.NEW, "Liquid navigation bar style"),
                ChangeItem(ChangeType.NEW, "Modern tabs navigation option"),
                ChangeItem(ChangeType.NEW, "Custom background image support"),
                ChangeItem(ChangeType.NEW, "Multi-language support"),
                ChangeItem(ChangeType.SECURITY, "Root access validation")
            )
        )
    )
    
    fun getChangeTypeColor(type: ChangeType): String = when (type) {
        ChangeType.NEW -> "#4CAF50"
        ChangeType.IMPROVEMENT -> "#2196F3"
        ChangeType.FIX -> "#FF9800"
        ChangeType.REMOVED -> "#F44336"
        ChangeType.SECURITY -> "#9C27B0"
        ChangeType.PERFORMANCE -> "#00BCD4"
    }
    
    fun getChangeTypeLabel(type: ChangeType): String = when (type) {
        ChangeType.NEW -> "NEW"
        ChangeType.IMPROVEMENT -> "IMPROVED"
        ChangeType.FIX -> "FIXED"
        ChangeType.REMOVED -> "REMOVED"
        ChangeType.SECURITY -> "SECURITY"
        ChangeType.PERFORMANCE -> "PERFORMANCE"
    }
}
