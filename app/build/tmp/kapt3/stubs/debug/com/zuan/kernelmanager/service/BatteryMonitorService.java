package com.zuan.kernelmanager.service;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000b\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u0000 -2\u00020\u0001:\u0001-B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\b\u0010\u0018\u001a\u00020\u0019H\u0016J\"\u0010\u001a\u001a\u00020\u000f2\b\u0010\u001b\u001a\u0004\u0018\u00010\u001c2\u0006\u0010\u001d\u001a\u00020\u000f2\u0006\u0010\u001e\u001a\u00020\u000fH\u0016J\u0012\u0010\u001f\u001a\u00020\u00192\b\u0010 \u001a\u0004\u0018\u00010\u001cH\u0016J\b\u0010!\u001a\u00020\u0019H\u0002J\b\u0010\"\u001a\u00020#H\u0002J\u0010\u0010$\u001a\u00020#2\u0006\u0010%\u001a\u00020\u0007H\u0002J\u0010\u0010&\u001a\u00020\'2\u0006\u0010(\u001a\u00020#H\u0002J\b\u0010)\u001a\u00020\u0019H\u0002J\b\u0010*\u001a\u00020\u0019H\u0016J\u0014\u0010+\u001a\u0004\u0018\u00010,2\b\u0010\u001b\u001a\u0004\u0018\u00010\u001cH\u0016R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0012\u001a\u00020\u0013X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0014R\u0010\u0010\u0015\u001a\u00020\u0016X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0017\u00a8\u0006."}, d2 = {"Lcom/zuan/kernelmanager/service/BatteryMonitorService;", "Landroid/app/Service;", "<init>", "()V", "handler", "Landroid/os/Handler;", "sessionStartTime", "", "startDeepSleep", "isScreenOn", "", "lastScreenStateChangeTime", "accumulatedScreenOnTime", "accumulatedScreenOffTime", "lastBatteryLevel", "", "screenOnDrain", "screenOffDrain", "updateRunnable", "Ljava/lang/Runnable;", "Ljava/lang/Runnable;", "receiver", "Landroid/content/BroadcastReceiver;", "Landroid/content/BroadcastReceiver;", "onCreate", "", "onStartCommand", "intent", "Landroid/content/Intent;", "flags", "startId", "onTaskRemoved", "rootIntent", "updateNotification", "generateStats", "", "formatDuration", "millis", "buildNotification", "Landroid/app/Notification;", "contentText", "createNotificationChannel", "onDestroy", "onBind", "Landroid/os/IBinder;", "Companion", "app_debug"})
public final class BatteryMonitorService extends android.app.Service {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CHANNEL_ID = "BatteryMonitorChannel";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CHANNEL_NAME = "Battery Monitor Info";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_STOP = "STOP_MONITOR";
    public static final int NOTIF_ID = 2;
    @org.jetbrains.annotations.NotNull()
    private final android.os.Handler handler = null;
    private long sessionStartTime = 0L;
    private long startDeepSleep = 0L;
    private boolean isScreenOn = true;
    private long lastScreenStateChangeTime = 0L;
    private long accumulatedScreenOnTime = 0L;
    private long accumulatedScreenOffTime = 0L;
    private int lastBatteryLevel = 0;
    private int screenOnDrain = 0;
    private int screenOffDrain = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.Runnable updateRunnable = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.BroadcastReceiver receiver = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.zuan.kernelmanager.service.BatteryMonitorService.Companion Companion = null;
    
    public BatteryMonitorService() {
        super();
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    @java.lang.Override()
    public int onStartCommand(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    @java.lang.Override()
    public void onTaskRemoved(@org.jetbrains.annotations.Nullable()
    android.content.Intent rootIntent) {
    }
    
    private final void updateNotification() {
    }
    
    private final java.lang.String generateStats() {
        return null;
    }
    
    private final java.lang.String formatDuration(long millis) {
        return null;
    }
    
    private final android.app.Notification buildNotification(java.lang.String contentText) {
        return null;
    }
    
    private final void createNotificationChannel() {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public android.os.IBinder onBind(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
        return null;
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003R\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/zuan/kernelmanager/service/BatteryMonitorService$Companion;", "", "<init>", "()V", "CHANNEL_ID", "", "CHANNEL_NAME", "ACTION_STOP", "NOTIF_ID", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}