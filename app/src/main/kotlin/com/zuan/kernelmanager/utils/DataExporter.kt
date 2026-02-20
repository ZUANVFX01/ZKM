/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.zuan.kernelmanager.data.FpsDataPoint
import com.zuan.kernelmanager.data.FpsSession
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DataExporter {

    fun exportSessionToCsv(context: Context, session: FpsSession, points: List<FpsDataPoint>) {
        try {
            // 1. Siapkan Folder
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val zkmDir = File(downloadsDir, "ZKM_Reports")
            if (!zkmDir.exists()) {
                zkmDir.mkdirs()
            }

            // 2. Buat Nama File
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val cleanPkgName = session.packageName.replace(".", "_")
            val fileName = "FPS_${cleanPkgName}_$timestamp.csv"
            val file = File(zkmDir, fileName)

            // 3. Susun Isi CSV (Pakai Locale.US biar desimal jadi TITIK, bukan KOMA)
            val csvBuilder = StringBuilder()
            
            // --- HEADER LAPORAN ---
            csvBuilder.append("=== ZKM FPS REPORT ===\n")
            csvBuilder.append("App Name,${session.packageName}\n")
            csvBuilder.append("Date,${SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.US).format(Date(session.startTime))}\n")
            csvBuilder.append("Duration,${session.durationSeconds} sec\n")
            // Format angka summary biar rapi (1 angka di belakang koma)
            csvBuilder.append("Avg FPS,${String.format(Locale.US, "%.1f", session.avgFps)}\n")
            csvBuilder.append("Avg Temp,${String.format(Locale.US, "%.1f", session.avgTemp)} C\n")
            csvBuilder.append("Avg Power,${String.format(Locale.US, "%.1f", session.avgWatt)} W\n")
            csvBuilder.append("\n") // Baris kosong pemisah

            // --- TABEL DATA (KOLOM) ---
            // Header Kolom
            csvBuilder.append("Time (s),FPS,Temp (C),CPU Load (%),Power (W),RAM Used (MB)\n")

            // Isi Data (Baris per Baris)
            points.forEach { point ->
                // Format angka: Locale.US (penting!) dan %.1f (1 desimal)
                val timeSec = String.format(Locale.US, "%.1f", point.timestamp / 1000f)
                val fps = String.format(Locale.US, "%.1f", point.fps)
                val temp = String.format(Locale.US, "%.1f", point.temp)
                val watt = String.format(Locale.US, "%.1f", point.watt)
                
                // Masukkan ke baris CSV
                csvBuilder.append("$timeSec,$fps,$temp,${point.cpuLoad},$watt,${point.ramUsageMb}\n")
            }

            // 4. Simpan & Share
            file.writeText(csvBuilder.toString())
            Toast.makeText(context, "Saved to: Downloads/ZKM_Reports/$fileName", Toast.LENGTH_LONG).show()

            val authority = "${context.packageName}.provider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "ZKM Report: ${session.packageName}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share FPS Data via...")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
