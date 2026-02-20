/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.utils

import android.content.Context
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
     * Mendapatkan daftar process Real-Time menggunakan 'top'.
     * Menggunakan strategi 'top -n 2' untuk mendapatkan delta CPU usage yang akurat.
     */
    fun getTopProcesses(context: Context, limit: Int, sortType: SortType): List<ProcessData> {
        val processMap = mutableMapOf<String, ProcessData>() // Gunakan Map untuk overwrite data lama (snapshot 1) dengan data baru (snapshot 2)
        val pm = context.packageManager

        try {
            // Command Logic:
            // top -b       : Batch mode (output teks biasa tanpa format terminal aneh)
            // -n 2         : Ambil 2 sampel. Sampel ke-1 = statis (average boot), Sampel ke-2 = real-time (delta).
            // -d 1         : Delay 1 detik antar sampel (untuk hitung % CPU).
            // -o ...       : Hanya output kolom yang kita butuhkan biar parsing gampang.
            
            // Kolom: PID(0), RES(1), %CPU(2), NAME(3)
            val command = "su -c top -b -n 2 -d 1 -o PID,RES,%CPU,NAME"
            
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val trimmed = line!!.trim()
                
                // Skip header atau baris kosong
                if (trimmed.isEmpty() || trimmed.startsWith("PID") || trimmed.startsWith("Tasks") || trimmed.startsWith("Mem")) {
                    continue
                }

                val p = trimmed.split(Regex("\\s+"))
                if (p.size >= 4) {
                    // Pastikan kolom pertama adalah angka (PID) untuk menghindari parsing error header nyasar
                    if (p[0].toIntOrNull() == null) continue

                    val pid = p[0]
                    val resRaw = p[1]
                    val cpuRaw = p[2]
                    val rawName = p[3] // Kadang nama process bisa panjang

                    // Kita format dulu datanya
                    val rssFormatted = formatRes(resRaw)
                    val cpuFormatted = "$cpuRaw%"
                    
                    // Logic Nama & Icon (sama seperti sebelumnya)
                    var appName = rawName
                    var icon: android.graphics.drawable.Drawable? = null
                    
                    // Optimization: Cek package name valid biar gak berat load icon system process
                    if (rawName.contains(".")) {
                        try {
                            val appInfo = pm.getApplicationInfo(rawName, 0)
                            appName = pm.getApplicationLabel(appInfo).toString()
                            // Icon opsional: disable jika list terasa berat saat scroll
                            icon = pm.getApplicationIcon(appInfo) 
                        } catch (e: Exception) {
                            // Ignore, use package name
                        }
                    } else {
                        // Bersihkan nama proses biner sistem (misal: /system/bin/surfaceflinger -> surfaceflinger)
                        if (rawName.contains("/")) {
                            appName = rawName.substringAfterLast("/")
                        }
                    }

                    // Masukkan ke Map. 
                    // Karena 'top -n 2' print PID yang sama 2 kali, data kedua (yang terbaru/real-time) 
                    // otomatis akan menimpa data pertama di map ini.
                    processMap[pid] = ProcessData(pid, rssFormatted, cpuFormatted, rawName, appName, icon)
                }
            }
            process.waitFor()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Convert Map ke List dan Lakukan Sorting Manual di Kotlin
        val sortedList = processMap.values.sortedWith(Comparator { o1, o2 ->
            if (sortType == SortType.CPU) {
                // Sort by CPU (Desc)
                val c1 = o1.cpu.replace("%", "").toFloatOrNull() ?: 0f
                val c2 = o2.cpu.replace("%", "").toFloatOrNull() ?: 0f
                c2.compareTo(c1)
            } else {
                // Sort by RAM (Desc) - Kita parse ulang MB/KB nya
                val r1 = parseResToKb(o1.res)
                val r2 = parseResToKb(o2.res)
                r2.compareTo(r1)
            }
        })

        return sortedList.take(limit)
    }

    private fun formatRes(rssVal: String): String {
        return try {
            // 'top' output kadang sudah ada suffix M/G/K, atau angka mentah (biasanya bytes atau KB tergantung versi Android)
            // Asumsi output top toybox modern biasanya ada suffix (12M, 440K). 
            // Jika angka saja, biasanya block 4K page atau KB. Kita asumsikan string saja agar aman.
            if (rssVal.all { it.isDigit() }) {
                // Jika cuma angka, anggap KB
                val kb = rssVal.toLong()
                if (kb > 1024) String.format("%.1f MB", kb / 1024f) else "$kb KB"
            } else {
                rssVal // Sudah ada format (28M, 1.2G)
            }
        } catch (e: Exception) {
            rssVal
        }
    }

    // Helper untuk sorting RAM
    private fun parseResToKb(resString: String): Long {
        return try {
            val upper = resString.uppercase()
            val num = upper.replace(Regex("[^0-9.]"), "").toFloatOrNull() ?: 0f
            when {
                upper.contains("G") -> (num * 1024 * 1024).toLong()
                upper.contains("M") -> (num * 1024).toLong()
                upper.contains("K") -> num.toLong()
                else -> num.toLong()
            }
        } catch (e: Exception) {
            0L
        }
    }
}
