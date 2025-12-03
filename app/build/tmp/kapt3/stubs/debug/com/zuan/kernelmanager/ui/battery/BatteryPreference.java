package com.zuan.kernelmanager.ui.battery;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0004\u0018\u0000 \u00152\u00020\u0001:\u0001\u0015B\u0011\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u0006\u0010\b\u001a\u00020\tJ\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\tJ\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u000f\u001a\u00020\u000b2\u0006\u0010\u0010\u001a\u00020\u000eJ\u0006\u0010\u0011\u001a\u00020\u0012J\u000e\u0010\u0013\u001a\u00020\u000b2\u0006\u0010\u0014\u001a\u00020\u0012R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lcom/zuan/kernelmanager/ui/battery/BatteryPreference;", "", "context", "Landroid/content/Context;", "<init>", "(Landroid/content/Context;)V", "prefs", "Landroid/content/SharedPreferences;", "getManualDesignCapacity", "", "setManualDesignCapacity", "", "capacity", "getCutoffLimit", "", "setCutoffLimit", "limit", "isMonitorEnabled", "", "setMonitorEnabled", "enable", "Companion", "app_debug"})
public final class BatteryPreference {
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS_NAME = "battery_prefs";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_MANUAL_CAPACITY = "manual_design_capacity";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_CUTOFF_LIMIT = "cutoff_limit";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_MONITOR_ENABLED = "monitor_enabled";
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.zuan.kernelmanager.ui.battery.BatteryPreference instance;
    @org.jetbrains.annotations.NotNull()
    public static final com.zuan.kernelmanager.ui.battery.BatteryPreference.Companion Companion = null;
    
    private BatteryPreference(android.content.Context context) {
        super();
    }
    
    public final int getManualDesignCapacity() {
        return 0;
    }
    
    public final void setManualDesignCapacity(int capacity) {
    }
    
    public final float getCutoffLimit() {
        return 0.0F;
    }
    
    public final void setCutoffLimit(float limit) {
    }
    
    public final boolean isMonitorEnabled() {
        return false;
    }
    
    public final void setMonitorEnabled(boolean enable) {
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u000b\u001a\u00020\n2\u0006\u0010\f\u001a\u00020\rR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/zuan/kernelmanager/ui/battery/BatteryPreference$Companion;", "", "<init>", "()V", "PREFS_NAME", "", "KEY_MANUAL_CAPACITY", "KEY_CUTOFF_LIMIT", "KEY_MONITOR_ENABLED", "instance", "Lcom/zuan/kernelmanager/ui/battery/BatteryPreference;", "getInstance", "context", "Landroid/content/Context;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.zuan.kernelmanager.ui.battery.BatteryPreference getInstance(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
    }
}