/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import com.topjohnwu.superuser.Shell
import com.zuan.kernelmanager.ui.MainActivity
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.socmenu.BatteryUtils
import com.zuan.kernelmanager.utils.MonitorReader
import kotlin.math.abs

class BatteryMonitorService : Service() {

    companion object {
        const val CHANNEL_ID = "BatteryMonitorChannel"
        const val CHANNEL_NAME = "Battery Monitor Info"
        const val ACTION_STOP = "STOP_MONITOR"
        const val NOTIF_ID = 2
        private const val TAG = "BatteryMonitorService"
    }

    private val handler = Handler(Looper.getMainLooper())
    
    // --- Session Variables ---
    private var sessionStartTime = 0L
    private var startDeepSleep = 0L
    
    // Screen Tracking
    private var isScreenOn = true
    private var lastScreenStateChangeTime = 0L
    private var accumulatedScreenOnTime = 0L
    private var accumulatedScreenOffTime = 0L
    
    // Drain Tracking
    private var lastBatteryLevel = 0
    private var screenOnDrain = 0
    private var screenOffDrain = 0
    
    // Cache String
    private var cachedBigText: String = "Collecting data..."
    private var cachedSummary: String = "Initializing..."
    private var cachedTitle: String = "Battery Monitor"
    
    private var isShellReady = false

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateNotification()
            handler.postDelayed(this, 3000) 
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val now = SystemClock.elapsedRealtime()
            
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> {
                    accumulatedScreenOffTime += (now - lastScreenStateChangeTime)
                    lastScreenStateChangeTime = now
                    isScreenOn = true
                    updateNotification()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    accumulatedScreenOnTime += (now - lastScreenStateChangeTime)
                    lastScreenStateChangeTime = now
                    isScreenOn = false
                    updateNotification()
                }
                Intent.ACTION_BATTERY_CHANGED -> {
                    val currentLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    if (lastBatteryLevel != -1 && currentLevel < lastBatteryLevel) {
                        val drop = lastBatteryLevel - currentLevel
                        if (isScreenOn) {
                            screenOnDrain += drop
                        } else {
                            screenOffDrain += drop
                        }
                    }
                    lastBatteryLevel = currentLevel
                    updateNotification()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        // Reset Stats saat Service dibuat
        sessionStartTime = SystemClock.elapsedRealtime()
        startDeepSleep = SystemClock.elapsedRealtime() - SystemClock.uptimeMillis()
        lastScreenStateChangeTime = SystemClock.elapsedRealtime()
        
        // Ambil status awal
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        lastBatteryLevel = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 50
        
        val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        isScreenOn = powerManager.isInteractive
        
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        registerReceiver(receiver, filter)
        
        // Init Shell Background
        Thread {
            try {
                if (Shell.getShell().isRoot) {
                    isShellReady = true
                    MonitorReader.initializeShell()
                }
            } catch (e: Exception) { isShellReady = false }
        }.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        prepareStats()
        
        // Handling Android 14 Foreground Service
        if (Build.VERSION.SDK_INT >= 34) {
            try {
                startForeground(NOTIF_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } catch (e: Exception) {
                 startForeground(NOTIF_ID, buildNotification())
            }
        } else {
            startForeground(NOTIF_ID, buildNotification())
        }
        
        handler.removeCallbacks(updateRunnable)
        handler.postDelayed(updateRunnable, 2000L)
        
        return START_STICKY
    }

    private fun updateNotification() {
        prepareStats()
        val notification = buildNotification()
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIF_ID, notification)
    }

    private fun prepareStats() {
        try {
            val now = SystemClock.elapsedRealtime()
            val totalSessionTime = (now - sessionStartTime).coerceAtLeast(1)

            val currentSegment = now - lastScreenStateChangeTime
            val totalSOT = if (isScreenOn) accumulatedScreenOnTime + currentSegment else accumulatedScreenOnTime
            val totalScreenOff = if (!isScreenOn) accumulatedScreenOffTime + currentSegment else accumulatedScreenOffTime

            // --- HITUNG WAKTU ---
            val currentGlobalDeepSleep = SystemClock.elapsedRealtime() - SystemClock.uptimeMillis()
            val sessionDeepSleep = (currentGlobalDeepSleep - startDeepSleep).coerceAtLeast(0)
            val awakeTime = (totalScreenOff - sessionDeepSleep).coerceAtLeast(0)

            // --- HITUNG DRAIN RATE (Safe Division) ---
            val sotHours = totalSOT / 3600000f
            val soffHours = totalScreenOff / 3600000f
            
            // Cegah error Infinity/NaN dengan cek > 0.01 jam
            val activeDrainRate = if (sotHours > 0.01f) (screenOnDrain / sotHours) else 0f
            val idleDrainRate = if (soffHours > 0.01f) (screenOffDrain / soffHours) else 0f

            // --- AMBIL DATA BATERAI LIVE ---
            val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
            val tempRaw = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            val temp = tempRaw / 10.0
            val voltageRaw = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0 // mV
            val voltage = voltageRaw / 1000f // Convert ke V
            
            val healthInt = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, 0) ?: 0
            val health = when(healthInt) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                else -> "Normal"
            }

            // Current (mA)
            val currentNow = BatteryUtils.getBatteryCurrentNow() // Menggunakan Shell/File reader
            val symbol = if (currentNow > 0) "+" else ""
            
            // --- FORMAT TAMPILAN (TARGET IMAGE STYLE) ---
            
            // Baris 1: Battery: 88% | 35.0°C | +350 mA 
            val line1 = "Battery: $level% | $temp°C | $symbol$currentNow mA"
            
            // Baris 2: Voltage: 4.02V | Health: Good
            val line2 = "Voltage: %.2fV | Health: $health".format(voltage)
            
            // Baris 3: Screen On: ... | Screen Off: ...
            val line3 = "Screen On: ${formatDuration(totalSOT)} | Screen Off: ${formatDuration(totalScreenOff)}"
            
            // Baris 4: Deep Sleep: ... | Awake: ...
            val line4 = "Deep Sleep: ${formatDuration(sessionDeepSleep)} | Awake: ${formatDuration(awakeTime)}"
            
            // Baris 5: Active: ... | Idle: ...
            val line5 = "Active: %.2f%%/h | Idle: %.2f%%/h".format(activeDrainRate, idleDrainRate)

            // Set ke variabel Cache
            cachedTitle = "Battery Monitor" // Judul Notif (Nama App)
            cachedSummary = "$line1" // Tampil saat notifikasi dilipat (collapsed)
            
            // Gabungkan untuk BigText (Expanded)
            val sb = StringBuilder()
            sb.append(line1)
            sb.append("\n")
            sb.append(line2)
            sb.append("\n")
            sb.append(line3)
            sb.append("\n")
            sb.append(line4)
            sb.append("\n")
            sb.append(line5)
            
            cachedBigText = sb.toString()

        } catch (e: Exception) {
            Log.e(TAG, "Error calculating stats: ${e.message}")
            cachedSummary = "Calculating..."
            cachedBigText = "Collecting battery data...\nPlease wait."
        }
    }
    
    private fun formatDuration(millis: Long): String {
        val seconds = millis / 1000
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        
        return if (h > 0) String.format("%dh %02dm", h, m)
        else if (m > 0) String.format("%02dm %02ds", m, s)
        else String.format("%02ds", s)
    }

    private fun buildNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(cachedTitle) // Judul utama
            .setContentText(cachedSummary) // Teks pendek saat collapsed
            .setStyle(NotificationCompat.BigTextStyle().bigText(cachedBigText)) // Teks panjang saat expanded
            .setSmallIcon(R.drawable.ic_battery_android_frame_full)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows detailed battery statistics"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
        try { unregisterReceiver(receiver) } catch (e: Exception) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        val restartServicePendingIntent = PendingIntent.getService(
            applicationContext, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmService = applicationContext.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        alarmService.set(
            android.app.AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        )
        super.onTaskRemoved(rootIntent)
    }
}
