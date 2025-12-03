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
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.zuan.kernelmanager.ui.MainActivity
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.utils.BatteryUtils
import kotlin.math.abs

class BatteryMonitorService : Service() {

    companion object {
        const val CHANNEL_ID = "BatteryMonitorChannel"
        const val CHANNEL_NAME = "Battery Monitor Info"
        const val ACTION_STOP = "STOP_MONITOR"
        const val NOTIF_ID = 2
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

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateNotification()
            handler.postDelayed(this, 10000) // Update tiap 10 detik
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
        
        sessionStartTime = SystemClock.elapsedRealtime()
        startDeepSleep = SystemClock.elapsedRealtime() - SystemClock.uptimeMillis()
        lastScreenStateChangeTime = SystemClock.elapsedRealtime()
        lastBatteryLevel = BatteryUtils.getBatteryLevelRaw(this)
        
        val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        isScreenOn = powerManager.isInteractive
        
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        registerReceiver(receiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        // Tampilkan notifikasi awal
        startForeground(NOTIF_ID, buildNotification(generateStats()))
        
        handler.removeCallbacks(updateRunnable)
        handler.post(updateRunnable)
        
        return START_STICKY
    }
    
    // Agar Service restart otomatis jika dibunuh paksa oleh sistem
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

    private fun updateNotification() {
        val notification = buildNotification(generateStats())
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIF_ID, notification)
    }

    private fun generateStats(): String {
        val now = SystemClock.elapsedRealtime()
        val totalSessionTime = (now - sessionStartTime).coerceAtLeast(1)

        // Time Durations
        val currentSegment = now - lastScreenStateChangeTime
        val totalSOT = if (isScreenOn) accumulatedScreenOnTime + currentSegment else accumulatedScreenOnTime
        val totalScreenOff = if (!isScreenOn) accumulatedScreenOffTime + currentSegment else accumulatedScreenOffTime

        // Deep Sleep & Awake
        val currentGlobalDeepSleep = SystemClock.elapsedRealtime() - SystemClock.uptimeMillis()
        val sessionDeepSleep = (currentGlobalDeepSleep - startDeepSleep).coerceAtLeast(0)
        val awakeTime = (totalScreenOff - sessionDeepSleep).coerceAtLeast(0)

        // Drain Rates
        val sotHours = totalSOT / 3600000f
        val soffHours = totalScreenOff / 3600000f
        val activeDrainRate = if (sotHours > 0.01) (screenOnDrain / sotHours) else 0f
        val idleDrainRate = if (soffHours > 0.01) (screenOffDrain / soffHours) else 0f

        // Percentages
        val sotPercent = (totalSOT.toFloat() / totalSessionTime.toFloat()) * 100
        val soffPercent = (totalScreenOff.toFloat() / totalSessionTime.toFloat()) * 100
        val deepSleepPercent = (sessionDeepSleep.toFloat() / totalSessionTime.toFloat()) * 100
        val awakePercent = (awakeTime.toFloat() / totalSessionTime.toFloat()) * 100

        val sb = StringBuilder()
        sb.append("Active drain: %.2f%% /hr".format(activeDrainRate))
        sb.append(" • ")
        sb.append("Idle drain: %.2f%% /hr".format(idleDrainRate))
        
        sb.append("\nScreen on: ${formatDuration(totalSOT)} (${"%.0f".format(sotPercent)}%)")
        sb.append("\nScreen off: ${formatDuration(totalScreenOff)} (${"%.0f".format(soffPercent)}%)")
        sb.append("\nDeep sleep: ${formatDuration(sessionDeepSleep)} (${"%.1f".format(deepSleepPercent)}%)")
        sb.append("\nAwake: ${formatDuration(awakeTime)} (${"%.1f".format(awakePercent)}%)")

        return sb.toString()
    }
    
    private fun formatDuration(millis: Long): String {
        val seconds = millis / 1000
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        
        return if (h > 0) String.format("%dh %dm %ds", h, m, s)
        else if (m > 0) String.format("%dm %ds", m, s)
        else String.format("%ds", s)
    }

    private fun buildNotification(contentText: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)

        val temp = BatteryUtils.getTempSimple(this)
        val level = BatteryUtils.getBatteryLevelRaw(this)
        
        val currentNow = BatteryUtils.getBatteryCurrentNow()
        val statusText = if (currentNow > 0) "Discharging" else "Charging"
        val currentAbs = abs(currentNow)

        val title = "$level% • $temp • $statusText $currentAbs mA"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("Expand for details...") 
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setSmallIcon(R.drawable.ic_battery_android_frame_full)
            .setContentIntent(pendingIntent)
            
            // --- SETTINGAN AGAR NOTIF KEKUNCI (STICKY) ---
            .setOngoing(true)            // Wajib: Menandakan proses sedang berjalan
            .setAutoCancel(false)        // Wajib: Tidak hilang saat diklik
            .setOnlyAlertOnce(true)      // Agar tidak bunyi ting-tung tiap update
            .setShowWhen(false)          // Menyembunyikan jam notifikasi (agar bersih)
            .setLocalOnly(true)
            .setPriority(NotificationCompat.PRIORITY_LOW) 
            .setCategory(NotificationCompat.CATEGORY_SERVICE) // Menandakan ini Service System
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            
        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time battery statistics"
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
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
