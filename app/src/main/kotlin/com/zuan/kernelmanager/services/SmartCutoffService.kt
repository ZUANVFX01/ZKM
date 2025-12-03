package com.zuan.kernelmanager.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.utils.BatteryUtils

class SmartCutoffService : Service() {

    companion object {
        const val CHANNEL_ID = "SmartCutoffChannel"
        const val ACTION_STOP_SERVICE = "STOP_SERVICE"
        const val ACTION_UPDATE_LIMIT = "UPDATE_LIMIT" // Action Baru
        const val EXTRA_LIMIT = "limit_threshold"
        
        const val HYSTERESIS_GAP = 3 
    }

    private var limitThreshold: Int = 80
    private var isCutOffActive = false 
    private var isChargerPlugged = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val plugged = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                
                isChargerPlugged = plugged != 0
                handleBatteryLogic(level, status)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        when (intent.action) {
            ACTION_STOP_SERVICE -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_UPDATE_LIMIT -> {
                // Update limit secara realtime
                val oldLimit = limitThreshold
                limitThreshold = intent.getIntExtra(EXTRA_LIMIT, 80)
                if (oldLimit != limitThreshold) {
                    updateNotification("Limit updated to $limitThreshold%")
                    // Paksa cek ulang kondisi baterai sekarang juga
                    checkBatteryStateNow()
                }
                // Jangan return, biarkan lanjut agar service tetap foreground
            }
            else -> {
                // Start awal
                limitThreshold = intent.getIntExtra(EXTRA_LIMIT, 80)
            }
        }

        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Smart Cut-off Active")
            .setContentText("Monitoring battery limit at $limitThreshold%")
            .setSmallIcon(R.drawable.ic_battery_android_frame_shield)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

        startForeground(1, notification)
        
        // Register receiver hanya jika belum terdaftar (cek null safety jika perlu, 
        // tapi di onStartCommand aman dipanggil berkali-kali asalkan di-handle)
        try {
            unregisterReceiver(batteryReceiver) // Unregister dulu biar gak double
        } catch (e: Exception) { /* Igonore if not registered */ }
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        return START_REDELIVER_INTENT
    }

    private fun checkBatteryStateNow() {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        intent?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            handleBatteryLogic(level, status)
        }
    }

    private fun handleBatteryLogic(level: Int, status: Int) {
        if (!isChargerPlugged) {
            if (isCutOffActive) {
                BatteryUtils.setChargingEnabled(true)
                isCutOffActive = false
            }
            return
        }

        // Logic Cut-off
        if (level >= limitThreshold && !isCutOffActive) {
            BatteryUtils.setChargingEnabled(false) 
            isCutOffActive = true
            updateNotification("Charging stopped at $level%")
        } 
        // Logic Resume (Hysteresis)
        else if (level <= (limitThreshold - HYSTERESIS_GAP) && isCutOffActive) {
            BatteryUtils.setChargingEnabled(true)
            isCutOffActive = false
            updateNotification("Charging resumed at $level%")
        }
        // Logic tambahan: Jika limit diturunkan (misal dari 80 ke 70, padahal batre 75)
        // Service harus langsung cut jika belum cut
        else if (level >= limitThreshold && !isCutOffActive) {
             BatteryUtils.setChargingEnabled(false)
             isCutOffActive = true
             updateNotification("Charging stopped at $level%")
        }
    }
    
    private fun updateNotification(text: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Smart Cut-off Active")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_battery_android_frame_shield)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        BatteryUtils.setChargingEnabled(true)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Smart Cut-off Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
