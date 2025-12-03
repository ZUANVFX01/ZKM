package com.zuan.kernelmanager.utils;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\b\u0010\t\u001a\u00020\nH\u0002J\u0006\u0010\u000b\u001a\u00020\fJ\u000e\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010J\u0006\u0010\u0011\u001a\u00020\u000eJ\u000e\u0010\u0012\u001a\u00020\u00052\u0006\u0010\u000f\u001a\u00020\u0010R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/zuan/kernelmanager/utils/MonitorReader;", "", "<init>", "()V", "TAG", "", "lastTotal", "", "lastIdle", "ensureShell", "", "getCpuLoad", "", "getBatteryTemp", "", "context", "Landroid/content/Context;", "getPowerWatt", "getDebugInfo", "app_debug"})
public final class MonitorReader {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "MonitorReader";
    private static long lastTotal = 0L;
    private static long lastIdle = 0L;
    @org.jetbrains.annotations.NotNull()
    public static final com.zuan.kernelmanager.utils.MonitorReader INSTANCE = null;
    
    private MonitorReader() {
        super();
    }
    
    private final boolean ensureShell() {
        return false;
    }
    
    public final int getCpuLoad() {
        return 0;
    }
    
    public final float getBatteryTemp(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return 0.0F;
    }
    
    public final float getPowerWatt() {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDebugInfo(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
}