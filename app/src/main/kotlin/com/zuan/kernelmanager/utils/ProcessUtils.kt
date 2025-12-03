/*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.utils

import android.content.Context
import android.content.pm.PackageManager
import java.io.BufferedReader
import java.io.InputStreamReader

data class ProcessData(
    val pid: String,
    val res: String,
    val cpu: String,
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable? = null
)

enum class SortType {
    CPU, RES
}

object ProcessUtils {

    /**
     * Mendapatkan daftar process.
     * @param limit Jumlah maksimal baris.
     * @param sortType Jenis pengurutan (CPU atau RES/RAM).
     */
    fun getTopProcesses(context: Context, limit: Int, sortType: SortType): List<ProcessData> {
        val processList = mutableListOf<ProcessData>()
        val pm = context.packageManager

        try {
            // Tentukan flag sorting berdasarkan input
            // -pcpu = sort by CPU descending
            // -rss  = sort by RSS (RAM) descending
            val sortFlag = if (sortType == SortType.CPU) "-pcpu" else "-rss"

            // Command: su -c ps -A -o PID,RSS,PCPU,NAME --sort=[FLAG] | head -n [LIMIT]
            val command = "su -c ps -A -o PID,RSS,PCPU,NAME --sort=$sortFlag | head -n ${limit + 1}"
            
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            
            reader.readLine() // Skip Header
            
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val p = line!!.trim().split(Regex("\\s+"))
                if (p.size >= 4) {
                    val pid = p[0]
                    val rssRaw = p[1]
                    val rssFormatted = formatRes(rssRaw)
                    val cpu = p[2] + "%"
                    val rawName = p[3]
                    
                    var appName = rawName
                    var icon: android.graphics.drawable.Drawable? = null
                    
                    try {
                        val appInfo = pm.getApplicationInfo(rawName, 0)
                        appName = pm.getApplicationLabel(appInfo).toString()
                        icon = pm.getApplicationIcon(appInfo)
                    } catch (e: Exception) {
                        if (rawName.startsWith("[") && rawName.endsWith("]")) {
                             appName = "Kernel Process"
                        } else if (rawName.contains("/")) {
                             // Kadang proses path binary (e.g., /system/bin/surfaceflinger)
                             appName = rawName.substringAfterLast("/")
                        }
                    }

                    processList.add(
                        ProcessData(pid, rssFormatted, cpu, rawName, appName, icon)
                    )
                }
            }
            process.waitFor()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return processList.take(limit)
    }

    private fun formatRes(rssVal: String): String {
        return try {
            val kb = rssVal.replace("K", "").toLong()
            if (kb > 1024) {
                String.format("%.1f MB", kb / 1024f)
            } else {
                "$kb KB"
            }
        } catch (e: Exception) {
            rssVal
        }
    }
}