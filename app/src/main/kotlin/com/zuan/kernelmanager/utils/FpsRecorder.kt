/*
 * Original code from: helloklf (vtools)
 * Modified and integrated by: Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.utils

import android.content.Context
import android.util.Log
import com.zuan.kernelmanager.data.FpsDataPoint
import com.zuan.kernelmanager.data.FpsDatabase
import com.zuan.kernelmanager.data.FpsSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Collections
import kotlin.math.pow
import kotlin.math.sqrt

object FpsRecorder {
    private const val TAG = "FpsRecorder"
    
    var isRecording = false
        private set
    
    var targetPackage: String = ""
        private set
        
    var isPaused = false
        private set

    private var startTime = 0L
    private val recordedPoints = Collections.synchronizedList(mutableListOf<FpsDataPoint>())
    
    fun startRecording(initialPkg: String) {
        if (isRecording) return
        
        // Simpan apa adanya dulu (bisa jadi kosong atau "Unknown")
        targetPackage = initialPkg
        
        isRecording = true
        isPaused = false
        startTime = System.currentTimeMillis()
        recordedPoints.clear()
        Log.d(TAG, "REC START: $targetPackage")
    }

    fun tick(
        context: Context,
        currentPkg: String, 
        fps: Float,
        cpu: Int,
        watt: Float,
        temp: Float,
        ramMb: Int
    ) {
        if (!isRecording) return

        // --- FITUR BARU: LATE BINDING ---
        // Jika saat start targetPackage masih kosong/unknown, dan sekarang kita nemu package valid,
        // UPDATE targetPackage-nya sekarang!
        if ((targetPackage.isEmpty() || targetPackage == "Unknown App") && currentPkg.isNotEmpty()) {
            targetPackage = currentPkg
            Log.d(TAG, "Target Package UPDATED to: $targetPackage")
        }

        // --- AUTO PAUSE LOGIC ---
        // Pause hanya jika kita SUDAH punya target valid, dan currentPkg beda
        val isValidTarget = targetPackage.isNotEmpty() && targetPackage != "Unknown App"
        val isDifferentApp = currentPkg.isNotEmpty() && currentPkg != targetPackage
        
        if (isValidTarget && isDifferentApp) {
            if (!isPaused) isPaused = true
            return 
        } else {
            if (isPaused) isPaused = false
        }

        // Simpan Data
        try {
            val relativeTime = System.currentTimeMillis() - startTime
            recordedPoints.add(
                FpsDataPoint(
                    sessionId = 0, 
                    timestamp = relativeTime,
                    fps = fps,
                    cpuLoad = cpu,
                    watt = watt,
                    temp = temp,
                    ramUsageMb = ramMb
                )
            )
        } catch (e: Exception) {}
    }

    fun stopRecording(context: Context) {
        if (!isRecording) return
        isRecording = false
        val endTime = System.currentTimeMillis()
        
        if (recordedPoints.isEmpty()) return

        // Jika sampai akhir masih Unknown, ya sudah nasib.
        val finalPackageName = if (targetPackage.isEmpty()) "Unknown App" else targetPackage
        val pointsToSave = ArrayList(recordedPoints)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val durationSec = ((endTime - startTime) / 1000).coerceAtLeast(1)
                
                val avgFps = pointsToSave.map { it.fps }.average().toFloat()
                val minFps = pointsToSave.minOf { it.fps }
                val maxFps = pointsToSave.maxOf { it.fps }
                
                // Variance
                val varianceSum = pointsToSave.sumOf { (it.fps - avgFps).pow(2).toDouble() }
                val stdDev = sqrt(varianceSum / pointsToSave.size).toFloat()
                
                // Smoothness (>80% of Avg)
                val stableFrames = pointsToSave.count { it.fps >= (avgFps * 0.8) }
                val smoothness = (stableFrames.toFloat() / pointsToSave.size.toFloat()) * 100f
                
                val avgTemp = pointsToSave.map { it.temp }.average().toFloat()
                val maxTemp = pointsToSave.maxOf { it.temp }
                
                val avgWatt = pointsToSave.map { it.watt }.average().toFloat()
                val maxWatt = pointsToSave.maxOf { it.watt }
                
                val avgRam = pointsToSave.map { it.ramUsageMb }.average().toInt()
                val maxRam = pointsToSave.maxOf { it.ramUsageMb }

                val session = FpsSession(
                    packageName = finalPackageName,
                    startTime = startTime,
                    endTime = endTime,
                    durationSeconds = durationSec,
                    avgFps = avgFps, minFps = minFps, maxFps = maxFps,
                    variance = stdDev, smoothness = smoothness,
                    avgTemp = avgTemp, maxTemp = maxTemp,
                    avgWatt = avgWatt, maxWatt = maxWatt,
                    avgRam = avgRam, maxRam = maxRam
                )

                val db = FpsDatabase.getDatabase(context)
                val sessionId = db.fpsDao().insertSession(session)
                val finalPoints = pointsToSave.map { it.copy(sessionId = sessionId) }
                db.fpsDao().insertDataPoints(finalPoints)
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
