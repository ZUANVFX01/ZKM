package com.zuan.kernelmanager.service;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 \u001d2\u00020\u0001:\u0001\u001dB\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\"\u0010\f\u001a\u00020\u00052\b\u0010\r\u001a\u0004\u0018\u00010\u000e2\u0006\u0010\u000f\u001a\u00020\u00052\u0006\u0010\u0010\u001a\u00020\u0005H\u0016J\b\u0010\u0011\u001a\u00020\u0012H\u0002J\u0018\u0010\u0013\u001a\u00020\u00122\u0006\u0010\u0014\u001a\u00020\u00052\u0006\u0010\u0015\u001a\u00020\u0005H\u0002J\u0010\u0010\u0016\u001a\u00020\u00122\u0006\u0010\u0017\u001a\u00020\u0018H\u0002J\b\u0010\u0019\u001a\u00020\u0012H\u0016J\u0014\u0010\u001a\u001a\u0004\u0018\u00010\u001b2\b\u0010\r\u001a\u0004\u0018\u00010\u000eH\u0016J\b\u0010\u001c\u001a\u00020\u0012H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u000b\u00a8\u0006\u001e"}, d2 = {"Lcom/zuan/kernelmanager/service/SmartCutoffService;", "Landroid/app/Service;", "<init>", "()V", "limitThreshold", "", "isCutOffActive", "", "isChargerPlugged", "batteryReceiver", "Landroid/content/BroadcastReceiver;", "Landroid/content/BroadcastReceiver;", "onStartCommand", "intent", "Landroid/content/Intent;", "flags", "startId", "checkBatteryStateNow", "", "handleBatteryLogic", "level", "status", "updateNotification", "text", "", "onDestroy", "onBind", "Landroid/os/IBinder;", "createNotificationChannel", "Companion", "app_debug"})
public final class SmartCutoffService extends android.app.Service {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CHANNEL_ID = "SmartCutoffChannel";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_STOP_SERVICE = "STOP_SERVICE";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_UPDATE_LIMIT = "UPDATE_LIMIT";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_LIMIT = "limit_threshold";
    public static final int HYSTERESIS_GAP = 3;
    private int limitThreshold = 80;
    private boolean isCutOffActive = false;
    private boolean isChargerPlugged = false;
    @org.jetbrains.annotations.NotNull()
    private final android.content.BroadcastReceiver batteryReceiver = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.zuan.kernelmanager.service.SmartCutoffService.Companion Companion = null;
    
    public SmartCutoffService() {
        super();
    }
    
    @java.lang.Override()
    public int onStartCommand(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    private final void checkBatteryStateNow() {
    }
    
    private final void handleBatteryLogic(int level, int status) {
    }
    
    private final void updateNotification(java.lang.String text) {
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
    
    private final void createNotificationChannel() {
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003R\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcom/zuan/kernelmanager/service/SmartCutoffService$Companion;", "", "<init>", "()V", "CHANNEL_ID", "", "ACTION_STOP_SERVICE", "ACTION_UPDATE_LIMIT", "EXTRA_LIMIT", "HYSTERESIS_GAP", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}